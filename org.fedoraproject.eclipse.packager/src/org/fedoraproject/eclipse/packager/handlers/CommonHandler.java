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
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
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

import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.client.Tag;
import org.eclipse.team.internal.ccvs.core.client.listeners.TagListener;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.IFpProjectBits;
import org.fedoraproject.eclipse.packager.PackagerPlugin;
import org.fedoraproject.eclipse.packager.SourcesFile;


public abstract class CommonHandler extends AbstractHandler {
	protected boolean debug = false;
	private IResource specfile;
	protected Shell shell;
	private Job job;

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setShell(Shell shell) {
		this.shell = shell;
	}

	protected String makeTagName(FedoraProjectRoot projectRoot) throws CoreException {
		String name = rpmQuery(projectRoot, "NAME").replaceAll("^[0-9]+", "");  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String version = rpmQuery(projectRoot, "VERSION");  //$NON-NLS-1$
		String release = rpmQuery(projectRoot, "RELEASE");  //$NON-NLS-1$
		return (name + "-" + version + "-" + release).replaceAll("\\.", "_");  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	protected IStatus createCVSTag(String tagName, boolean forceTag,
			IProgressMonitor monitor) {
		IStatus result;
		IProject proj = specfile.getProject();
		CVSTag tag = new CVSTag(tagName, CVSTag.VERSION);

		// get CVSProvider
		CVSTeamProvider provider = (CVSTeamProvider) RepositoryProvider
				.getProvider(proj, CVSProviderPlugin.getTypeId());
		// get Repository Location
		ICVSRepositoryLocation location;
		try {
			location = provider.getRemoteLocation();

			// get CVSROOT
			CVSWorkspaceRoot cvsRoot = provider.getCVSWorkspaceRoot();

			ICVSFolder folder = cvsRoot.getLocalRoot().getFolder(
					specfile.getParent().getName());

			// Make new CVS Session
			Session session = new Session(location, folder, true);
			session.open(monitor, true);

			TagListener listener = new TagListener();

			String args[] = new String[] { "." }; //$NON-NLS-1$

			LocalOption[] opts;
			if (forceTag) {
				opts = new LocalOption[] { Tag.FORCE_REASSIGNMENT };
			} else {
				opts = new LocalOption[0];
			}

			// cvs tag "tagname" "project"
			IStatus status = Command.TAG.execute(session,
					Command.NO_GLOBAL_OPTIONS, opts, tag, args, listener,
					monitor);

			session.close();
			if (!status.isOK()) {
				MultiStatus temp = new MultiStatus(status.getPlugin(), status
						.getCode(), status.getMessage(), status.getException());
				for (IStatus error : session.getErrors()) {
					temp.add(error);
				}
				result = temp;
			} else {
				result = status;
			}
		} catch (CVSException e) {
			result = handleError(e);
		}

		return result;
	}

	protected String rpmQuery(FedoraProjectRoot projectRoot, String format)
			throws CoreException {
		IResource parent = projectRoot.getSpecFile().getParent();
		String dir = parent.getLocation().toString();
		List<String> defines = FedoraHandlerUtils.getRPMDefines(dir);
		IFpProjectBits projectBits = FedoraHandlerUtils.getVcsHandler(projectRoot.getSpecFile());
		List<String> distDefines = getDistDefines(projectBits, parent.getName());

		String result = null;
		defines.add(0, "rpm"); //$NON-NLS-1$
		defines.addAll(distDefines);
		defines.add("-q"); //$NON-NLS-1$
		defines.add("--qf"); //$NON-NLS-1$
		defines.add("%{" + format + "}\\n");  //$NON-NLS-1$//$NON-NLS-2$
		defines.add("--specfile"); //$NON-NLS-1$
		defines.add(projectRoot.getSpecFile().getLocation().toString());

		try {
			result = Utils.runCommandToString(defines.toArray(new String[0]));
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					PackagerPlugin.PLUGIN_ID, e.getMessage(), e));
		}

		return result.substring(0, result.indexOf('\n'));
	}

	protected List<String> getDistDefines(IFpProjectBits projectBits, String parentName) {
		// substitution for rhel
		ArrayList<String> distDefines = new ArrayList<String>();
		String distvar = projectBits.getDistVariable().equals("epel") ? "rhel" //$NON-NLS-1$//$NON-NLS-2$ 
				: projectBits.getDistVariable(); 
		distDefines.add("--define"); //$NON-NLS-1$
		distDefines.add("dist " + projectBits.getDist()); //$NON-NLS-1$
		distDefines.add("--define"); //$NON-NLS-1$
		distDefines.add(distvar + projectBits.getDist()); 
		return distDefines;
	}
	

	protected boolean isTagged(FedoraProjectRoot projectRoot, String tagName) throws CoreException {
		String branchName = specfile.getParent().getName();
		IProject proj = specfile.getProject();

		// get CVSProvider
		CVSTeamProvider provider = (CVSTeamProvider) RepositoryProvider
		.getProvider(proj, CVSProviderPlugin.getTypeId());
		// get CVSROOT
		CVSWorkspaceRoot cvsRoot = provider.getCVSWorkspaceRoot();

		ICVSFolder folder = cvsRoot.getLocalRoot().getFolder(branchName);
		CVSTag tag = new CVSTag(tagName, CVSTag.VERSION);
		ICVSRemoteResource remoteResource = CVSWorkspaceRoot.getRemoteResourceFor(specfile).forTag(tag);
		if (remoteResource == null) {
			throw new CVSException(folder.getName() + " is not tagged");
		}

		return tag.getName().equals(makeTagName(projectRoot));
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

	// FIXME: This breaks git
	protected IStatus doTag(FedoraProjectRoot projectRoot, IProgressMonitor monitor) {
		monitor.subTask("Generating Tag Name from Specfile");
		final String tagName;
		try {
			tagName = makeTagName(projectRoot);
		} catch (CoreException e) {
			e.printStackTrace();
			return handleError(e);
		}

		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		monitor.subTask("Tagging as " + tagName);
		IStatus result = createCVSTag(tagName, false, monitor);
		String errExists = "Tag " + tagName + " has been already created";
		if (!result.isOK()) {
			boolean tagExists = false;
			if (result.getMessage().contains(errExists)) {
				tagExists = true;
			}
			if (result.isMultiStatus()) {
				for (IStatus error : result.getChildren()) {
					if (error.getMessage().contains(errExists)) {
						tagExists = true;
					}
				}
			}
			if (tagExists) {
				// prompt to force tag
				if (promptForceTag(tagName)) {
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
					result = createCVSTag(tagName, true, monitor);
				}
			}
		}
		return result;
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