/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.fedoraproject.eclipse.packager.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Messages;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileSection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.PackagerPlugin;


public abstract class CommonHandler extends AbstractHandler {
	protected boolean debug = false;
	protected Shell shell;
	private Job job;

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setShell(Shell shell) {
		this.shell = shell;
	}

	public String getClog(FedoraProjectRoot projectRoot) {
		Specfile spec = projectRoot.getSpecfileModel();
		// get the changelog section
		SpecfileSection clogSection = spec.getSection("changelog"); //$NON-NLS-1$

		String clog = clogSection.getContents();
		String[] lines = clog.split("\n"); //$NON-NLS-1$

		// get the first clog entry
		String recentClog = ""; //$NON-NLS-1$

		// ignore the header
		int i = 1;
		if (lines.length > 1) {
			while (!lines[i].equals("")) { //$NON-NLS-1$
				recentClog += lines[i] + '\n';
				i++;
			}
		}

		return recentClog.trim();
	}

	protected IStatus error(String message) {
		return new Status(IStatus.ERROR, PackagerPlugin.PLUGIN_ID, message);
	}

	protected IStatus handleError(final String message, Throwable exception,
			final boolean isError, boolean showInDialog) {
		// do not ask for user interaction while in debug mode
		if (showInDialog && !debug) {
			if (Display.getCurrent() == null) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						if (isError) {
							MessageDialog.openError(shell, Messages.getString("FedoraPackager.name"), //$NON-NLS-1$
									message);
						} else {
							MessageDialog.openInformation(shell,
									Messages.getString("FedoraPackager.name"), message); //$NON-NLS-1$
						}
					}
				});
			} else {
				if (isError) {
					MessageDialog.openError(shell, Messages.getString("FedoraPackager.name"), message); //$NON-NLS-1$
				} else {
					MessageDialog.openInformation(shell, Messages.getString("FedoraPackager.name"),//$NON-NLS-1$
							message);
				}
			}
		}
		return new Status(isError ? IStatus.ERROR : IStatus.OK,
				PackagerPlugin.PLUGIN_ID, message, exception);
	}

	protected IStatus handleError(String message) {
		return handleError(message, null, true, false);
	}

	protected IStatus handleError(String message, boolean showInDialog) {
		return handleError(message, null, true, showInDialog);
	}

	protected IStatus handleOK(String message, boolean showInDialog) {
		return handleError(message, null, false, showInDialog);
	}

	protected IStatus handleError(Exception e) {
		return handleError(e.getMessage(), e, true, false);
	}

	protected IStatus handleError(Exception e, boolean showInDialog) {
		return handleError(e.getMessage(), e, true, showInDialog);
	}

	public IStatus waitForJob() {
		while (job.getState() != Job.NONE) {
			try {
				job.join();
			} catch (InterruptedException e) {
			}
		}
		return job.getResult();
	}

	protected boolean promptForceTag(final String tagName) {
		boolean okPressed;
		if (Display.getCurrent() != null) {
			okPressed = MessageDialog.openQuestion(shell, Messages.getString("FedoraPackager.name"), //$NON-NLS-1$
					"Branch is already tagged with " + tagName
							+ ".\nAttempt to overwrite?");
		} else {
			YesNoRunnable op = new YesNoRunnable(
					"Branch is already tagged with " + tagName
							+ ".\nAttempt to overwrite?");
			Display.getDefault().syncExec(op);
			okPressed = op.isOkPressed();
		}
		return okPressed;
	}
	
	public final class YesNoRunnable implements Runnable {
		private final String question;
		private boolean okPressed;

		public YesNoRunnable(String question) {
			this.question = question;
		}

		@Override
		public void run() {
			okPressed = MessageDialog.openQuestion(shell,  Messages.getString("FedoraPackager.name"), //$NON-NLS-1$
					question);
		}

		public boolean isOkPressed() {
			return okPressed;
		}
	}
}