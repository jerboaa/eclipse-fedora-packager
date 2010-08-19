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
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
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
import org.fedoraproject.eclipse.packager.PackagerPlugin;
import org.fedoraproject.eclipse.packager.SourcesFile;

@SuppressWarnings("restriction")
public abstract class CommonHandler extends AbstractHandler {
	protected boolean debug = false;
	protected IResource resource;
	protected IResource specfile;
	protected Shell shell;
	protected HashMap<String, HashMap<String, String>> branches;
	protected Job job;
	protected ExecutionEvent event;
	protected SourcesFile sourcesFile;

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	// FIXME: This will break with Git
	//private abstract HashMap<String, HashMap<String, String>> getBranches();
	private HashMap<String, HashMap<String, String>> getBranches() {
		HashMap<String, HashMap<String, String>> ret = new HashMap<String, HashMap<String, String>>();

		IFile branchesFile = resource.getProject().getFolder("common").getFile(  //$NON-NLS-1$
				"branches"); //$NON-NLS-1$
		InputStream is;
		try {
			is = branchesFile.getContents();
			BufferedReader bufReader = new BufferedReader(
					new InputStreamReader(is));
			List<String> branches = new ArrayList<String>();
			String line;
			while ((line = bufReader.readLine()) != null) {
				branches.add(line);
			}

			for (String branch : branches) {
				HashMap<String, String> temp = new HashMap<String, String>();
				StringTokenizer st = new StringTokenizer(branch, ":");	//$NON-NLS-1$
				String target = st.nextToken();
				temp.put("target", st.nextToken()); 	//$NON-NLS-1$
				temp.put("dist", st.nextToken());		//$NON-NLS-1$
				temp.put("distvar", st.nextToken());	//$NON-NLS-1$
				temp.put("distval", st.nextToken());	//$NON-NLS-1$
				ret.put(target, temp);
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ret;
	}

	@Override
	public Object execute(final ExecutionEvent e) throws ExecutionException {
		this.event = e;
		job = new Job(Messages.getString("FedoraPackager.jobName")) { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask(getTaskName(), IProgressMonitor.UNKNOWN);

				branches = getBranches();
				if (branches == null) {
					return handleError("Couldn't retrieve branch information");
				}

				IContainer branch = null;
				// branch folder selected
				if (resource instanceof IContainer
						&& branches.containsKey(resource.getName())) {
					branch = (IContainer) resource;
				} else if (resource.getParent() != null
						&& branches.containsKey(resource.getParent().getName())) {
					branch = resource.getParent();
				}
				// ensure resource selected is either a branch folder or a child
				// resource of a branch folder
				if (branch == null) {
					return handleError("Must be in a branch directory");
				}

				if (isSpec(resource)) {
					specfile = resource;
				} else {
					try {
						for (IResource member : branch.members()) {
							if (isSpec(member)) {
								specfile = member;
							}
						}
					} catch (CoreException e) {
						e.printStackTrace();
						return handleError(e);
					}
				}

				// if we haven't obtained a specfile, die
				if (specfile == null) {
					return handleError("Could not locate a specfile within "
							+ resource.getParent().getName());
				}

				IStatus result;
				try {
					result = doExecute(event, monitor);
				} catch (ExecutionException e) {
					e.printStackTrace();
					result = handleError(e);
				}
				monitor.done();
				return result;
			}

		};
		job.setUser(true);
		job.schedule();
		return null;
	}

	protected final String getTaskName(){
		return ""; //TODO remove 
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

	public final IStatus doExecute(ExecutionEvent event,
			IProgressMonitor monitor) throws ExecutionException{
		return Status.OK_STATUS;
	}

	// FIXME: This will break Git
	protected String getBranchName(String branch) throws CoreException {
		// check for early-branched
		if (branch.equals("devel")) { //$NON-NLS-1$
			return getDevelBranch();
		}
		return branch;
	}

	// checks to see if branch is early-branched
	private String getDevelBranch() throws CoreException {
		int highestVersion = 0;
		for (String branch : branches.keySet()) {
			if (branch.startsWith("F-")) { //$NON-NLS-1$
				int version = Integer.parseInt(branch.substring(2));
				highestVersion = Math.max(version, highestVersion);
			}
		}
		String newestBranch = "F-" + String.valueOf(highestVersion); //$NON-NLS-1$
		String secondNewestBranch = "F-" + String.valueOf(highestVersion - 1); //$NON-NLS-1$

		return containsSpec(secondNewestBranch) ? newestBranch : "devel"; //$NON-NLS-1$
	}

	// FIXME: This will break Git
	protected boolean containsSpec(String branch) throws CoreException {
		IProject proj = specfile.getProject();
		// get CVSProvider
		CVSTeamProvider provider = (CVSTeamProvider) RepositoryProvider
				.getProvider(proj, CVSProviderPlugin.getTypeId());

		// get CVSROOT
		CVSWorkspaceRoot cvsRoot = provider.getCVSWorkspaceRoot();

		ICVSFolder folder = cvsRoot.getLocalRoot().getFolder(branch);

		// search "branch" for a spec file
		return folder.getFile(specfile.getName()) != null;
	}

	protected String makeTagName() throws CoreException {
		String name = rpmQuery(specfile, "NAME").replaceAll("^[0-9]+", "");  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String version = rpmQuery(specfile, "VERSION");  //$NON-NLS-1$
		String release = rpmQuery(specfile, "RELEASE");  //$NON-NLS-1$
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

	

	protected boolean isTagged(String tagName) throws CoreException {
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

		return tag.getName().equals(makeTagName());
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
	protected IStatus doTag(IProgressMonitor monitor) {
		monitor.subTask("Generating Tag Name from Specfile");
		final String tagName;
		try {
			tagName = makeTagName();
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