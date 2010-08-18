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
	private HashMap<String, HashMap<String, String>> branches; // All branches
	
	/**
	 * See {@link IFpProjectBits#getBranchName(String)
	 */
	@Override
	public String getBranchName(String branchName) {
		if (!isInitialized()) {
			return null;
		}
		// TODO Auto-generated method stub
		return null;
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
	
	private Object getBranches() {
		RepositoryCache repoCache = org.eclipse.egit.core.Activator.getDefault()
		.getRepositoryCache();
		Repository repo = null;
		try {
			repo = repoCache.lookupRepository(new File(this.project.getProject().getName()));
			Map<String, Ref> remotes = repo.getRefDatabase().getRefs(Constants.R_REMOTES);
			Set<String> keyset = remotes.keySet();
			for (String key: keyset) {
				System.out.println("Key: "+ key + " value: " + remotes.get(key).getName());
			}
		} catch (IOException ioexception) {
			ioexception.printStackTrace();
		}
		return null;
	}

	/**
	 * Do instance specific initialization.
	 * 
	 * See {@link IFpProjectBits#initialize(IResource)
	 */
	@Override
	public void initialize(IResource resource) {
		this.project = resource.getProject();		
	}
	
	/**
	 * Determine if instance has been properly initialized
	 */
	private boolean isInitialized() {
		if (this.project != null && this.branches != null) {
			return true;
		} else {
			return false;
		}
	}

}
