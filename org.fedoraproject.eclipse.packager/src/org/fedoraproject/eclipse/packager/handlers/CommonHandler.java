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

import java.text.MessageFormat;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileSection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.PackagerPlugin;

/**
 * Common handler functionality.
 */
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

	/**
	 * Utility method which allows a handler to wait until a job is
	 * finished. The private job member variable can be set via
	 * setJob().
	 * 
	 * @return The status of the wait operation.
	 */
	public IStatus waitForJob() {
		while (job.getState() != Job.NONE) {
			try {
				job.join();
			} catch (InterruptedException e) {
			}
		}
		return job.getResult();
	}
	
	/**
	 * Set CommonHandler's job member. This might be useful in conjunction
	 * with {@link CommonHandler#waitForJob()}.
	 * 
	 * @param j
	 */
	public void setJob(Job j) {
		this.job = j;
	}

	protected boolean promptForceTag(final String tagName) {
		boolean okPressed;
		if (Display.getCurrent() != null) {
			okPressed = MessageDialog.openQuestion(shell, FedoraPackagerText.get().commonHandler_fedoraPackagerName,
			MessageFormat.format(FedoraPackagerText.get().commonHandler_branchAlreadyTaggedMessage, tagName));
		} else {
			YesNoRunnable op = new YesNoRunnable(
					MessageFormat.format(FedoraPackagerText.get().commonHandler_branchAlreadyTaggedMessage, tagName));
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
			okPressed = MessageDialog.openQuestion(shell, FedoraPackagerText.get().commonHandler_fedoraPackagerName, //$NON-NLS-1$
					question);
		}

		public boolean isOkPressed() {
			return okPressed;
		}
	}
}