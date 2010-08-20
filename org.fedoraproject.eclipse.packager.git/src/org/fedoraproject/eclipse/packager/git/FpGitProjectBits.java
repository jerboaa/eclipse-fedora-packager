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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.egit.core.RepositoryCache;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
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
	
	// String regexp pattern used for branch mapping this should basically be the
	// same pattern as fedpkg uses. ATM this pattern is:
	// BRANCHFILTER = 'f\d\d\/master|master|el\d\/master|olpc\d\/master'
	private final Pattern BRANCH_PATTERN = Pattern
			.compile("(?:origin/)?(f)(\\d\\d?)/master|(?:origin/)?(master)|(?:origin/)?(el)(\\d)/master|(?:origin/)?(olpc)(\\d)/master" //$NON-NLS-1$
			);
	
	/**
	 * See {@link IFpProjectBits#getBranchName(String)}
	 */
	@Override
	public String getBranchName(String branchName) {
		if (!isInitialized()) {
			return null;
		}
		return this.branches.get(branchName);
	}

	/**
	 * Parse current branch from active local branch.
	 * 
	 * See {@link IFpProjectBits#getCurrentBranchName()}
	 */
	@Override
	public String getCurrentBranchName() {
		if (!isInitialized()) {
			return null;
		}
		RepositoryCache repoCache = org.eclipse.egit.core.Activator
				.getDefault().getRepositoryCache();
		Repository repo = null;
		String currentBranch = null;
		try {
			repo = repoCache.lookupRepository(new File(this.project
					.getProject().getLocation().toOSString()
					+ "/.git")); //$NON-NLS-1$
			// make sure it's a named branch
			if (!isNamedBranch(repo.getFullBranch())) {
				return null; // unknown branch!
			}
			// get the current head target
			currentBranch = repo.getBranch();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return mapBranchName(currentBranch);
	}

	/**
	 * See {@link IFpProjectBits#getScmUrl()}
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
					+ "/.git")); //$NON-NLS-1$
			Map<String, Ref> remotes = repo.getRefDatabase().getRefs(
					Constants.R_REMOTES);
			Set<String> keyset = remotes.keySet();
			String branch;
			for (String key : keyset) {
				// use shortenRefName() to get rid of refs/*/ prefix
				branch = repo.shortenRefName(remotes.get(key).getName());
				branch = mapBranchName(branch); // do the branch name mapping
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
	 * See {@link IFpProjectBits#initialize(IResource)}
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

	/**
	 * Determine distribution qualifier. This is VCS specific because
	 * branch determination is VCS specific.
	 * 
	 * See {@link IFpProjectBits#getDist()}
	 */
	@Override
	public String getDist() {
		/* fedpkg is (was?) doing this:
		 * # Still requires a 'branch' file in each branch
        self.branch = self._findbranch()
        if self.branch.startswith('F-'):
            self.distval = self.branch.split('-')[1]
            self.distvar = 'fedora'
            self.dist = '.fc%s' % self.distval
            self.target = 'dist-f%s-updates-candidate' % self.distval
            self.mockconfig = 'fedora-%s-%s' % (self.distval, self.localarch)
        elif self.branch.startswith('EL-'):
            self.distval = self.branch.split('-')[1]
            self.distvar = 'epel'
            self.dist = '.el%s' % self.distval
            self.target = 'dist-%sE-epel-testing-candidate' % self.distval
            self.mockconfig = 'epel-%s-%s' % (self.distval, self.localarch)
        elif self.branch.startswith('OLPC-'):
            self.distval = self.branch.split('-')[1]
            self.distvar = 'olpc'
            self.dist = '.olpc%s' % self.distval
            self.target = 'dist-olpc%s' % self.distval
        # Need to do something about no branch here
        elif self.branch == 'devel':
            self.distval = '14' # this is hardset for now, which is bad
            self.distvar = 'fedora'
            self.dist = '.fc%s' % self.distval
            self.target = 'dist-f%s' % self.distval # will be dist-rawhide
		 */
		String currBranch = getCurrentBranchName();
		if (currBranch.startsWith("F-") || currBranch.startsWith("EL-") || currBranch.startsWith("OLPC-")) {
			String version = currBranch.split("-")[1];
			return ".fc" + version;
		} else if (currBranch.equals("devel")) { //$NON-NLS-1$
			String nextversion = "14"; // TODO: Look at remote branches and calculate something more precise.
			return ".fc" + nextversion;
		}
		return null;
	}

	/**
	 * Maps branch names to the internal format used by all IFpProjectBits
	 * implementations. For example <code>mapBranchName("f8")</code> would
	 * return <code>"F-8"</code> and <code>mapBranchName("master")</code> would
	 * return <code>"devel"</code>.
	 * 
	 * @param from
	 *            The original raw branch name with "refs/something"
	 *            prefixes omitted.
	 * @return The mapped branch name.
	 */
	private String mapBranchName(String from) {
		String prefix, version;
		Matcher branchMatcher = BRANCH_PATTERN.matcher(from);
		if (!branchMatcher.matches()) {
			// This should never happen. Maybe something wrong with the regular
			// expression?
			System.err.println("no match!");
			return null;
		}
		for (int i = 1; i < branchMatcher.groupCount(); i++) {
			prefix = branchMatcher.group(i); // null if group didn't match at all
			version = branchMatcher.group(i+1);
			if (version == null && prefix != null && prefix.equals(Constants.MASTER)) {
				// matched master
				return "devel"; //$NON-NLS-1$
			} else if (version != null && prefix != null) {
				// F, EPEL, OLPC matches
				return prefix.toUpperCase() + "-" + version; //$NON-NLS-1$
			}
		}
		// something's fishy
		return null;
	}
	
	/**
	 * Returns true if given branch name is NOT an ObjectId in string format.
	 * I.e. if branchName has been created by doing repo.getBranch(), it would
	 * return SHA1 Strings for remote branches. We don't want that.
	 * 
	 * @param branchName
	 * @return
	 */
	private boolean isNamedBranch(String branchName) {
		if (branchName.startsWith(Constants.R_HEADS)
				|| branchName.startsWith(Constants.R_TAGS)
				|| branchName.startsWith(Constants.R_REMOTES)) {
			return true;
		}
		return false;
	}

	@Override
	public IStatus updateVCS(FedoraProjectRoot projectRoot,
			IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return Status.OK_STATUS;
	}

}
