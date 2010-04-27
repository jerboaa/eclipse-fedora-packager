package org.fedoraproject.eclipse.packager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
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
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.client.Tag;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.client.listeners.TagListener;
import org.eclipse.team.internal.ccvs.core.resources.CVSEntryLineTag;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.EditorPart;

@SuppressWarnings("restriction")
public abstract class CommonHandler extends AbstractHandler {
	protected boolean debug = false;
	protected IResource resource;
	protected IResource specfile;
	protected Shell shell;
	protected HashMap<String, HashMap<String, String>> branches;
	protected Job job;
	protected ExecutionEvent event;
	protected Process proc = null;

	public IResource getSpecfile() {
		return specfile;
	}

	public void setSpecfile(IResource specfile) {
		this.specfile = specfile;
	}

	public IResource getResource() {
		return resource;
	}

	public Shell getShell() {
		return shell;
	}

	public Job getJob() {
		return job;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	private HashMap<String, HashMap<String, String>> getBranches() {
		HashMap<String, HashMap<String, String>> ret = new HashMap<String, HashMap<String, String>>();

		String[] branches = { "RHL-7:rhl7:.rhl7:rhl:7",
				"RHL-8:rhl8:.rhl8:rhl:8", "RHL-9:rhl9:.rhl9:rhl:9",
				"OLPC-2:dist-olpc2:.olpc2:olpc:2",
				"OLPC-3:dist-olpc3:.olpc3:olpc:3",
				"OLPC-4:dist-olpc4:.olpc4:olpc:4",
				"EL-4:dist-4E-epel-testing-candidate:.el4:epel:4",
				"EL-5:dist-5E-epel-testing-candidate:.el5:epel:5", "FC-1:fc1:.fc1:fedora:1",
				"FC-2:fc2:.fc2:fedora:2", "FC-3:fc3:.fc3:fedora:3",
				"FC-4:fc4:.fc4:fedora:4", "FC-5:fc5:.fc5:fedora:5",
				"FC-6:fc6:.fc6:fedora:6",
				"F-7:dist-fc7-updates-candidate:.fc7:fedora:7",
				"F-8:dist-f8-updates-candidate:.fc8:fedora:8",
				"F-9:dist-f9-updates-candidate:.fc9:fedora:9",
				"F-10:dist-f10-updates-candidate:.fc10:fedora:10",
				"F-11:dist-f11-updates-candidate:.fc11:fedora:11",
				"F-12:dist-f12-updates-candidate:.fc12:fedora:12",
				"devel:dist-f13:.fc13:fedora:13" };

		for (String branch : branches) {
			HashMap<String, String> temp = new HashMap<String, String>();
			StringTokenizer st = new StringTokenizer(branch, ":");
			String target = st.nextToken();
			temp.put("target", st.nextToken());
			temp.put("dist", st.nextToken());
			temp.put("distvar", st.nextToken());
			temp.put("distval", st.nextToken());
			ret.put(target, temp);
		}

		return ret;
	}

	public Object execute(ExecutionEvent e) throws ExecutionException {
		this.event = e;
		job = new Job ("Fedora Packager") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask(getTaskName(), IProgressMonitor.UNKNOWN);
				
				// in debug mode, these will be set programmatically
				if (!debug) {
					shell = HandlerUtil.getActiveShell(event);
					Display.getDefault().syncExec(new Runnable() {
						@Override
						public void run() {
							resource = getResource(event);
						}						
					});					
				}

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
			
			@Override
			protected void canceling() {
				if (proc != null) {
					proc.destroy();
				}
			}
		};
		
		job.setUser(true);
		job.schedule();
		return null;
	}

	protected abstract String getTaskName();

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
			result = ext != null && ext.equals("spec");
		}
		return result;
	}

	public abstract IStatus doExecute(ExecutionEvent event, IProgressMonitor monitor)
	throws ExecutionException;

	protected String getBranchName(String branch) throws CoreException {
		// check for early-branched
		if (branch.equals("devel")) {
			return getDevelBranch();
		}
		return branch;
	}

	// checks to see if branch is early-branched
	private String getDevelBranch() throws CoreException {
		int highestVersion = 0;
		for (String branch : branches.keySet()) {
			if (branch.startsWith("F-")) {
				int version = Integer.parseInt(branch.substring(2));
				highestVersion = Math.max(version, highestVersion);
			}
		}
		String newestBranch = "F-" + String.valueOf(highestVersion);
		String secondNewestBranch = "F-" + String.valueOf(highestVersion - 1);

		return containsSpec(secondNewestBranch) ? newestBranch : "devel";
	}

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

	protected IResource getResource(ExecutionEvent event) {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part == null) {
			return null;
		}
		if (part instanceof EditorPart) {
			IEditorInput input = ((EditorPart) part).getEditorInput();
			if (input instanceof IFileEditorInput) {
				return ((IFileEditorInput) input).getFile();
			} else {
				return null;
			}
		}
		IWorkbenchSite site = part.getSite();
		if (site == null) {
			return null;
		}
		ISelectionProvider provider = site.getSelectionProvider();
		if (provider == null) {
			return null;
		}
		ISelection selection = provider.getSelection();
		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection)
			.getFirstElement();
			if (element instanceof IResource) {
				return (IResource) element;
			} else if (element instanceof IAdaptable) {
				IAdaptable adaptable = (IAdaptable) element;
				Object adapted = adaptable.getAdapter(IResource.class);
				return (IResource) adapted;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	//	protected Specfile parseSpec() {
	//		// Read spec file
	//		Specfile spec = null;
	//		SpecfileParser parser = new SpecfileParser();
	//		String buf = "";
	//
	//		try {
	//			BufferedReader br = new BufferedReader(new InputStreamReader(
	//					((IFile) resource).getContents()));
	//			String line = br.readLine();
	//
	//			int count = 0;
	//			while (line != null) {
	//				buf += line + '\n';
	//				line = br.readLine();
	//				count++;
	//			}
	//			spec = parser.parse(buf);
	//		} catch (CoreException e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		} catch (IOException e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		}
	//
	//		return spec;
	//	}

	protected String makeTagName() throws CoreException {
		String name = rpmQuery(specfile, "NAME").replaceAll("^[0-9]+", "");
		String version = rpmQuery(specfile, "VERSION");
		String release = rpmQuery(specfile, "RELEASE");
		return (name + "-" + version + "-" + release).replaceAll("\\.", "_");
	}

	protected IStatus createCVSTag(String tagName, boolean forceTag, IProgressMonitor monitor) {
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

			String args[] = new String[] { "." };

			LocalOption[] opts;
			if (forceTag) {
				opts = new LocalOption[] { Tag.FORCE_REASSIGNMENT };
			}
			else {
				opts = new LocalOption[0];
			}

			// cvs tag "tagname" "project"
			IStatus status = Command.TAG.execute(session,
					Command.NO_GLOBAL_OPTIONS, opts, tag, args, listener,
					monitor);

			session.close();
			if (!status.isOK()) {
				MultiStatus temp = new MultiStatus(status.getPlugin(), status.getCode(), status.getMessage(), status.getException());
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

	protected String rpmQuery(IResource specfile, String format) throws CoreException {
		IResource parent = specfile.getParent();
		String dir = parent.getLocation().toString();
		String defines = getRPMDefines(dir);

		HashMap<String, String> branch = branches.get(parent.getName());
		String distDefines = getDistDefines(branch);

		String cmd = "rpm " + defines + " " + distDefines + " -q --qf \"%{"
		+ format + "}\\n\" --specfile "
		+ specfile.getLocation().toString();

		String result = null;

		ShellScript script = new ShellScript(cmd, 0);
		result = script.execNoLog();

		return result.substring(0, result.indexOf('\n'));
	}

	protected String getDistDefines(HashMap<String, String> branch) {
		// substitution for rhel
		String distvar = branch.get("distvar").equals("epel") ? "rhel" : branch
				.get("distvar");
		return "--define \"dist " + branch.get("dist") + "\" " + "--define \""
		+ distvar + " " + branch.get("distval") + "\"";
	}

	protected String getRPMDefines(String dir) {
		return "--define \"_sourcedir " + dir + "\" " + "--define \"_specdir "
		+ dir + "\" " + "--define \"_builddir " + dir + "\" "
		+ "--define \"_srcrpmdir " + dir + "\" "
		+ "--define \"_rpmdir " + dir + "\"";
	}

	protected boolean isTagged() throws CoreException {
		String branchName = specfile.getParent().getName();
		IProject proj = specfile.getProject();

		// get CVSProvider
		CVSTeamProvider provider = (CVSTeamProvider) RepositoryProvider
		.getProvider(proj, CVSProviderPlugin.getTypeId());
		// get CVSROOT
		CVSWorkspaceRoot cvsRoot = provider.getCVSWorkspaceRoot();

		ICVSFolder folder = cvsRoot.getLocalRoot().getFolder(branchName);

		CVSEntryLineTag tag = folder.getFolderSyncInfo().getTag();
		if (tag == null) {
			throw new CVSException(folder.getName() + " is not tagged");
		}

		return tag.getName().equals(makeTagName());
	}

	public String getClog() throws IOException {
		File file = specfile.getLocation().toFile();
		SpecfileParser parser = new SpecfileParser();
		Specfile spec = null;
		String buf = "";

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
			SpecfileSection clogSection = null;
			for (SpecfileSection section : spec.getSections()) {
				if (section.getName().equals("changelog")) {
					clogSection = section;
				}
			}

			String clog = buf.substring(clogSection.getLineStartPosition());
			String[] lines = clog.split("\n");

			// get the first clog entry
			String recentClog = "";

			// ignore the header
			int i = 1;
			while (!lines[i].equals("")) {
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

	protected IStatus handleError(final String message, Throwable exception, final boolean isError, boolean showInDialog) {
		// do not ask for user interaction while in debug mode
		if (showInDialog && !debug) {
			if (Display.getCurrent() == null) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						if (isError) {
							MessageDialog.openError(shell, "Fedora Packager", message);
						}
						else {
							MessageDialog.openInformation(shell, "Fedora Packager", message);
						}						
					}					
				});
			}
			else {
				if (isError) {
					MessageDialog.openError(shell, "Fedora Packager", message);
				}
				else {
					MessageDialog.openInformation(shell, "Fedora Packager", message);
				}	
			}
		}
		return new Status(isError ? IStatus.ERROR : IStatus.OK, PackagerPlugin.PLUGIN_ID, message, exception);
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
			okPressed = MessageDialog.openQuestion(shell, "Fedora Packager", "Branch is already tagged with " + tagName + ".\nAttempt to overwrite?");
		}
		else {
			YesNoRunnable op = new YesNoRunnable("Branch is already tagged with " + tagName + ".\nAttempt to overwrite?");
			Display.getDefault().syncExec(op);
			okPressed = op.isOkPressed();
		}
		return okPressed;
	}

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

	public final class YesNoRunnable implements Runnable {
		private final String question;
		private boolean okPressed;

		public YesNoRunnable(String question) {
			this.question = question;
		}

		@Override
		public void run() {
			okPressed = MessageDialog.openQuestion(shell, "Fedora Packager", question);
		}

		public boolean isOkPressed() {
			return okPressed;
		}
	}
}