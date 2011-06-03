package org.fedoraproject.eclipse.packager.cvs;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFolder;
import org.eclipse.team.internal.ccvs.core.resources.RemoteModule;
import org.eclipse.team.internal.ccvs.ui.operations.CheckoutSingleProjectOperation;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

/**
 * Operation for checking out a module from CVS
 *
 */
@SuppressWarnings("restriction")
public class CVSCheckoutOperation {

	
	private String moduleName;
	private IProject project;
	private String scmURL;
	
	/**
	 * Set the module name, which should get checked out.
	 * 
	 * @param moduleName
	 */
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
	
	/**
	 * Set the base URL to checkout from.
	 * 
	 * @param scmURL
	 */
	public void setScmURL(String scmURL) {
		this.scmURL = scmURL;
	}
	
	/**
	 * Run the checkout.
	 * @return The checked out project.
	 * 
	 * @throws Exception
	 */
	public IProject run() throws Exception {
		// make sure module name is properly set
		if (moduleName == null) {
			throw new IllegalStateException();
		}
		ICVSRepositoryLocation repo = CVSRepositoryLocation.fromString(getScmURL());
		ICVSRemoteFolder remoteFolder = repo.getRemoteFolder("rpms", null);
		RemoteModule remoteModule = new RemoteModule(moduleName, (RemoteFolder) remoteFolder, repo, moduleName,new LocalOption[]{}, new CVSTag(), true);
		
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		project = root.getProject(moduleName);
		
		IProgressService progress = PlatformUI.getWorkbench().getProgressService();
		IRunnableWithProgress op = new CheckoutSingleProjectOperation(null, remoteModule, project, null, false);
		progress.busyCursorWhile(op);
		
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		return project;
	}
	
	/**
	 * @return the scmURL
	 */
	protected String getScmURL() {
		// return the default if not set explicitly
		if (this.scmURL == null) {
			return CVSUtils.getDefaultCVSBaseUrl();
		}
		return scmURL;
	}
}
