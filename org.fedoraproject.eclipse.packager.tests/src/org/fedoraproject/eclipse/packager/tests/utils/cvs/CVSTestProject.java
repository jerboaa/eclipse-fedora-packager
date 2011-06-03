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
package org.fedoraproject.eclipse.packager.tests.utils.cvs;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
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

@SuppressWarnings("restriction")
public class CVSTestProject {
	
	private String scmURL;
	// In fact the folder representing the current branch
	private IResource currentBranch;
	private IProject project;

	/**
	 * Check out a module. Switches to the devel branch.
	 * 
	 * @param name
	 * @throws Exception
	 */
	public void checkoutModule(String name) throws Exception {
		ICVSRepositoryLocation repo = CVSRepositoryLocation.fromString(getScmURL());
		ICVSRemoteFolder remoteFolder = repo.getRemoteFolder("rpms", null);
		RemoteModule remoteModule = new RemoteModule(name, (RemoteFolder) remoteFolder, repo, name,new LocalOption[]{}, new CVSTag(), true);
		
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		project = root.getProject(name);
		
		IProgressService progress = PlatformUI.getWorkbench().getProgressService();
		IRunnableWithProgress op = new CheckoutSingleProjectOperation(null, remoteModule, project, null, false);
		progress.busyCursorWhile(op);
		
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		
		// switch to the devel branch
		currentBranch = project.findMember(new Path("devel"));
	}
	
	/**
	 * Delete everything.
	 * 
	 * @throws Exception
	 */
	public void dispose() throws Exception {
		project.delete(true, true, null);
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
	}

	/**
	 * @return the currentBranch
	 */
	public IResource getCurrentBranch() {
		return currentBranch;
	}
	
	/**
	 *
	 * @return the underlying project
	 */
	public IProject getProject() {
		return project;
	}
	
	/**
	 * @return the scmURL
	 */
	public String getScmURL() {
		// return the default if not set explicitly
		if (this.scmURL == null) {
			return ":pserver;username=anonymous;hostname=cvs.fedoraproject.org:/cvs/pkgs";
		}
		return scmURL;
	}
	
	/**
	 * @param scmURL the scmURL to set
	 */
	public void setScmURL(String scmURL) {
		this.scmURL = scmURL;
	}
	
	/**
	 * Switch to branch {@code name}
	 * 
	 * @param name
	 */
	public IResource checkoutBranch(String name) throws BranchNotFoundException {
		IResource candidate = this.project.findMember(new Path(name));
		if (candidate == null) {
			throw new BranchNotFoundException(name);
		}
		// set the new branch
		this.currentBranch = candidate;
		return candidate;
	}
	
	/**
	 * Get the mapping file for build target-mapping.
	 * 
	 * @return
	 */
	public IResource getBuildMapFile() {
		return this.project.findMember(new Path("common" + IPath.SEPARATOR + "build.map"));
	}
}
