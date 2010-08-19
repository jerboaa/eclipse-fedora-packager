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
package org.fedoraproject.eclipse.packager.test_utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.egit.core.op.BranchOperation;
import org.eclipse.egit.core.op.CloneOperation;
import org.eclipse.egit.core.op.ConnectProviderOperation;
import org.eclipse.egit.core.RepositoryCache;
import org.eclipse.egit.core.RepositoryUtil;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.URIish;

public class GitTestProject {	
	private IProject project;
	private Repository gitRepo;
	
	public GitTestProject(String packageName) throws CoreException, URISyntaxException, InvocationTargetException, InterruptedException {
		final URIish uri = new URIish(getGitURL(packageName));
		final CloneOperation clone = new CloneOperation(uri, true,
				new ArrayList<Ref>(), new File(ResourcesPlugin
						.getWorkspace().getRoot().getLocation().toFile(),
						packageName), Constants.R_HEADS + Constants.MASTER,
				"origin");
		clone.run(null); // clone project
		// Add cloned repository to the list of Git repositories so that it
		// shows up in the Git repositories view.
		final RepositoryUtil config = org.eclipse.egit.core.Activator.getDefault().getRepositoryUtil();
		config.addConfiguredRepository(clone.getGitDir());
		this.project = ResourcesPlugin.getWorkspace().getRoot()
				.getProject(packageName);
		this.project.create(null);
		this.project.open(null);
		ConnectProviderOperation connect = new ConnectProviderOperation(
				this.project);
		connect.execute(null);
		this.project.refreshLocal(IResource.DEPTH_INFINITE, null);
		// find repo we've just created and set gitRepo
		RepositoryCache repoCache = org.eclipse.egit.core.Activator
				.getDefault().getRepositoryCache();
		try {
			this.gitRepo = repoCache.lookupRepository(new File(this.project
					.getProject().getLocation().toOSString()
					+ "/.git"));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		// Do the local branches limbo
		createLocalBranches(new NullProgressMonitor());
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
	public Repository getGitRepo() {
		return this.gitRepo;
	}
	
	/**
	 * Checkouts branch
	 *
	 * @param refName
	 *            full name of branch
	 * @throws CoreException
	 */
	public void checkoutBranch(String refName) throws CoreException {
		try {
			if (this.gitRepo.getRefDatabase().getRef(refName) == null) {
				System.err.println("Reference: '" + refName + "' does not exist!");
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		new BranchOperation(this.gitRepo, refName).execute(null);
	}

	/**
	 * Get Git URL for given packageName. 
	 * 
	 * @param packageName
	 * @return
	 */
	private String getGitURL(String packageName) {
		return "git://pkgs.fedoraproject.org/" + packageName + ".git";
	}
	
	/**
	 * Create local branches based on remotes. Don't do checkouts.
	 * 
	 * @param monitor
	 * @throws CoreException
	 */
	private void createLocalBranches(IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Creating local branches",
				IProgressMonitor.UNKNOWN);

		try {
			Map<String, Ref> remotes = this.gitRepo.getRefDatabase()
					.getRefs(Constants.R_REMOTES);
			Set<String> keyset = remotes.keySet();
			String branch;
			for (String key : keyset) {
				// use shortenRefName() to get rid of refs/*/ prefix
				Ref origRef = remotes.get(key);
				branch = this.gitRepo.shortenRefName(origRef
						.getName());
				// omit "origin
				branch = branch.substring("origin".length()); //$NON-NLS-1$
				// create local branches
				String newRefName = Constants.R_HEADS + branch;

				RefUpdate updateRef = this.gitRepo.updateRef(newRefName);
				ObjectId startAt = new RevWalk(this.gitRepo).parseCommit(this.gitRepo
							.resolve(origRef.getName()));
				updateRef.setNewObjectId(startAt);
				updateRef.setRefLogMessage(
						"branch: Created from " + origRef.getName(), false); //$NON-NLS-1$
				updateRef.update();
			}
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}
}
