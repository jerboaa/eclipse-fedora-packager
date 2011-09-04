/*******************************************************************************
 * Copyright (c) 2010-2011 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.fedoraproject.eclipse.packager.tests.utils.git;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.egit.core.op.ConnectProviderOperation;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.fedoraproject.eclipse.packager.PackagerPlugin;
import org.fedoraproject.eclipse.packager.git.FedoraPackagerGitCloneOperation;
import org.fedoraproject.eclipse.packager.git.GitUtils;

/**
 * Fixture for Git based projects.
 */
public class GitTestProject {
	private IProject project;
	private Git git;

	public GitTestProject(final String packageName) throws InterruptedException {
		Job cloneProjectJob = new Job(packageName) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				FedoraPackagerGitCloneOperation cloneOp = new FedoraPackagerGitCloneOperation();
				try {
					cloneOp.setCloneURI(
							GitUtils.getFullGitURL(
									GitUtils.getAnonymousGitBaseUrl(), packageName))
							.setPackageName(packageName);
				} catch (URISyntaxException e1) {
					// ignore
				}
				try {
					git = cloneOp.run(monitor);
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (CoreException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return Status.OK_STATUS;
			}
		};
		cloneProjectJob.schedule();
		// wait for it to finish
		cloneProjectJob.join();

		project = ResourcesPlugin.getWorkspace().getRoot()
		.getProject(packageName);
		try {
			project.create(null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			project.open(null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			project.setPersistentProperty(PackagerPlugin.PROJECT_PROP,
			"true" /* unused value */);
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ConnectProviderOperation connect = new ConnectProviderOperation(
				project);
		try {
			connect.execute(null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void dispose() throws Exception {
		project.delete(true, true, null);
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
	}

	/**
	 * Get underlying IProject
	 *
	 * @return
	 */
	public IProject getProject() {
		return this.project;
	}

	/**
	 * @return the gitRepo
	 */
	public Git getGitRepo() {
		return this.git;
	}

	/**
	 * Checkouts branch
	 *
	 * @param refName
	 *            full name of branch
	 * @throws CoreException
	 * @throws InvalidRefNameException
	 * @throws RefNotFoundException
	 * @throws RefAlreadyExistsException
	 * @throws JGitInternalException
	 */
	public void checkoutBranch(String branchName) throws JGitInternalException, RefAlreadyExistsException, RefNotFoundException, InvalidRefNameException, CoreException {
		boolean branchExists = false;
			ListBranchCommand lsBranchCmd = this.git.branchList();
			for (Ref branch: lsBranchCmd.call()) {
				if (Repository.shortenRefName(branch.getName()).equals(branchName)) {
					branchExists = true;
					break; // short circuit
				}
			}
			if (!branchExists) {
				System.err.println("Branch: '" + branchName + "' does not exist!");
				return;
			}
		CheckoutCommand checkoutCmd = this.git.checkout();
		checkoutCmd.setName(Constants.R_HEADS + branchName);
		checkoutCmd.call();
		// refresh after checkout
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
	}
}
