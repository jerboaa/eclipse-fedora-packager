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
package org.fedoraproject.eclipse.packager.git;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IResource;
import org.eclipse.egit.core.RepositoryCache;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.fedoraproject.eclipse.packager.IFpProjectBits;

/**
 * Git specific project bits (branches management and such).
 * Implementation of
 * org.fedoraproject.eclipse.packager.vcsContribution
 * extension point.
 * 
 * @author Red Hat Inc.
 *
 */
public class FpGitProjectBits implements IFpProjectBits {
	
	private IResource project; // The underlying project
	private HashMap<String, String> branches; // All branches
	private boolean initialized = false; // keep track if instance is initialized
	
	/**
	 * See {@link IFpProjectBits#getBranchName(String)
	 */
	@Override
	public String getBranchName(String branchName) {
		if (!isInitialized()) {
			return null;
		}
		return this.branches.get(branchName);
	}

	/**
	 * See {@link IFpProjectBits#getCurrentBranchName()
	 */
	@Override
	public String getCurrentBranchName() {
		if (!isInitialized()) {
			return null;
		}
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * See {@link IFpProjectBits#getScmUrl(IResource)
	 */
	@Override
	public String getScmUrl() {
		if (!isInitialized()) {
			return null;
		}
		// TODO Auto-generated method stub
		return "dummy output";
	}
	
	/**
	 * Parse available branch names from Git remote branches.
	 * 
	 * @return
	 */
	private HashMap<String, String> getBranches() {
		// We get the Repository from RepositoryCache
		RepositoryCache repoCache = org.eclipse.egit.core.Activator
				.getDefault().getRepositoryCache();
		Repository repo = null;
		HashMap<String, String> branches = new HashMap<String, String>();
		try {
			repo = repoCache.lookupRepository(new File(this.project
					.getProject().getLocation().toOSString()
					+ "/.git"));
			Map<String, Ref> remotes = repo.getRefDatabase().getRefs(
					Constants.R_REMOTES);
			Set<String> keyset = remotes.keySet();
			String branch, prefix, version;
			StringTokenizer tokenizer;
			for (String key : keyset) {
				branch = remotes.get(key).getName().substring(Constants.R_REMOTES.length());
				tokenizer = new StringTokenizer(branch, "/");  //$NON-NLS-1$
				tokenizer.nextToken(); // ignore "origin"
				branch = tokenizer.nextToken();
				prefix = branch.substring(0, 1);
				version = branch.substring(1);
				if (prefix.equals("f")) {
					branch = "F-" + version; //$NON-NLS-1$
				}
				// Not sure if we want to map this that way?
				if (branch.equals(Constants.MASTER)) {
					branches.put("devel", "devel");
				}
				branches.put(branch, branch);
			}
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
		return branches;
	}

	/**
	 * Do instance specific initialization.
	 * 
	 * See {@link IFpProjectBits#initialize(IResource)
	 */
	@Override
	public void initialize(IResource resource) {
		this.project = resource.getProject();
		this.branches = getBranches();
		this.initialized = true;
	}
	
	/**
	 * Determine if instance has been properly initialized
	 */
	private boolean isInitialized() {
		return this.initialized;
	}

	@Override
	public String getDist() {
		// TODO Auto-generated method stub
		return null;
	}

}
