/*******************************************************************************
 * Copyright (c) 2010 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.fedoraproject.eclipse.packager.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.rpm.core.utils.Utils;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Messages;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileParser;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileSection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.fedoraproject.eclipse.packager.PackagerPlugin;
import org.fedoraproject.eclipse.packager.SourcesFile;


public abstract class CommonHandler extends AbstractHandler {
	protected boolean debug = false;
	private IResource resource; //TODO remove when subclasses don't use it
	private IResource specfile;
	protected Shell shell;
	protected HashMap<String, HashMap<String, String>> branches;
	private Job job;
	private ExecutionEvent event;
	private SourcesFile sourcesFile;

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setResource(IResource resource) {
		this.resource = resource;
	}

	public void setShell(Shell shell) {
		this.shell = shell;
	}

	private boolean isSpec(IResource resource) {
		boolean result = false;
		if (resource instanceof IFile) {
			String ext = resource.getFileExtension();
			result = ext != null && ext.equals("spec"); //$NON-NLS-1$
		}
		return result;
	}

	protected String makeTagName() throws CoreException {
		String name = rpmQuery(specfile, "NAME").replaceAll("^[0-9]+", "");  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String version = rpmQuery(specfile, "VERSION");  //$NON-NLS-1$
		String release = rpmQuery(specfile, "RELEASE");  //$NON-NLS-1$
		return (name + "-" + version + "-" + release).replaceAll("\\.", "_");  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	protected String rpmQuery(IResource specfile, String format)
			throws CoreException {
		IResource parent = specfile.getParent();
		String dir = parent.getLocation().toString();
		List<String> defines = FedoraHandlerUtils.getRPMDefines(dir);

		List<String> distDefines = getDistDefines(branches, parent.getName());

		String result = null;
		defines.add(0, "rpm"); //$NON-NLS-1$
		defines.addAll(distDefines);
		defines.add("-q"); //$NON-NLS-1$
		defines.add("--qf"); //$NON-NLS-1$
		defines.add("%{" + format + "}\\n");  //$NON-NLS-1$//$NON-NLS-2$
		defines.add("--specfile"); //$NON-NLS-1$
		defines.add(specfile.getLocation().toString());

		try {
			result = Utils.runCommandToString(defines.toArray(new String[0]));
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					PackagerPlugin.PLUGIN_ID, e.getMessage(), e));
		}

		return result.substring(0, result.indexOf('\n'));
	}

	protected List<String> getDistDefines(HashMap<String, HashMap<String,String>> branches, String parentName) {
		// substitution for rhel
		ArrayList<String> distDefines = new ArrayList<String>();
		if (branches != null) {
			HashMap<String, String> branch = branches.get(parentName);
			String distvar = branch.get("distvar").equals("epel") ? "rhel"  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
					: branch.get("distvar"); //$NON-NLS-1$
			distDefines.add("--define"); //$NON-NLS-1$
			distDefines.add("dist " + branch.get("dist"));  //$NON-NLS-1$//$NON-NLS-2$
			distDefines.add("--define"); //$NON-NLS-1$
			distDefines.add(distvar + branch.get("dist")); //$NON-NLS-1$
		}
		return distDefines;
	}

	public String getClog() throws IOException {
		File file = specfile.getLocation().toFile();
		SpecfileParser parser = new SpecfileParser();
		Specfile spec = null;
		String buf = ""; //$NON-NLS-1$

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			String line = br.readLine();

			int count = 0;
			while (line != null) {
				buf += line + '\n';
				line = br.readLine();
				count++;
			}
			spec = parser.parse(buf);

			// get the changelog section
			SpecfileSection clogSection = spec.getSection("changelog"); //$NON-NLS-1$

			String clog = buf.substring(clogSection.getLineStartPosition());
			String[] lines = clog.split("\n"); //$NON-NLS-1$

			// get the first clog entry
			String recentClog = ""; //$NON-NLS-1$

			// ignore the header
			int i = 1;
			while (!lines[i].equals("")) { //$NON-NLS-1$
				recentClog += lines[i] + '\n';
				i++;
			}

			return recentClog.trim();
		} finally {
			br.close();
		}
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
	
	protected SourcesFile getSourcesFile() {
		IFile sourcesIFile = specfile.getParent()
				.getFile(new Path("./sources")); //$NON-NLS-1$
		try {
			sourcesIFile.refreshLocal(1, new NullProgressMonitor());
		} catch (CoreException e) {
			// Show what has gone wrong
			handleError(e);
		}
		return new SourcesFile(sourcesIFile);
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