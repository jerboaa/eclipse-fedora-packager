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
package org.fedoraproject.eclipse.packager.cvs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.fedoraproject.eclipse.packager.IFpProjectBits;

/**
 * CVS specific FpProject bits.
 * 
 * @author Red Hat Inc.
 * 
 */
@SuppressWarnings("restriction")
public class FpCVSProjectBits implements IFpProjectBits {

	private IResource project; // The underlying project
	private HashMap<String, HashMap<String, String>> branches; // All branches

	/**
	 * Create CVS specific project bits from IResource
	 */
	public FpCVSProjectBits(IResource project) {
		this.branches = getBranches(project);
	}
	
	/**
	 * Create CVS specific project bits from IResource
	 */
	public FpCVSProjectBits() {
	}

	/**
	 * See {@link IFpProjectBits#getCurrentBranchName()}
	 */
	@Override
	public String getCurrentBranchName() {
		return null;
	}
	
	public String getCurrentBranchName(IResource resource) {
		return resource.getParent().getName();
	}

	/**
	 * See {@link IFpProjectBits#getBranchName(String)}
	 */
	@Override
	public String getBranchName(String branchName) {
		// check for early-branched
		if (branchName.equals("devel")) { //$NON-NLS-1$
			try {
				return getDevelBranch();
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return branchName;
	}

	/**
	 * Parse branches from "common/branches" file.
	 * 
	 * @param resource
	 * @return A map of branch names.
	 */
	private HashMap<String, HashMap<String, String>> getBranches(
			IResource resource) {
		HashMap<String, HashMap<String, String>> ret = new HashMap<String, HashMap<String, String>>();

		IFile branchesFile = resource.getProject().getFolder("common").getFile( //$NON-NLS-1$
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
				StringTokenizer st = new StringTokenizer(branch, ":"); //$NON-NLS-1$
				String target = st.nextToken();
				temp.put("target", st.nextToken()); //$NON-NLS-1$
				temp.put("dist", st.nextToken()); //$NON-NLS-1$
				temp.put("distvar", st.nextToken()); //$NON-NLS-1$
				temp.put("distval", st.nextToken()); //$NON-NLS-1$
				ret.put(target, temp);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ret;
	}

	/**
	 * Checks if branch has been early-branched.
	 * 
	 * @return The actual name of the development branch.
	 * @throws CoreException
	 */
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

	/**
	 * Check if branch contains spec file.
	 * 
	 * @param branch
	 * @return True if given branch contains the spec file in CVS.
	 * @throws CoreException
	 */
	private boolean containsSpec(String branch) throws CoreException {
		// get CVSProvider
		CVSTeamProvider provider = (CVSTeamProvider) RepositoryProvider
				.getProvider(this.project.getProject(),
						CVSProviderPlugin.getTypeId());

		// get CVSROOT
		CVSWorkspaceRoot cvsRoot = provider.getCVSWorkspaceRoot();

		ICVSFolder folder = cvsRoot.getLocalRoot().getFolder(branch);

		// search "branch" for a spec file
		// FIXME: Make this less hard-coded!
		return folder.getFile(this.project.getProject().getName() + ".spec") != null;
	}

	@Override
	public String getScmUrl(IResource resource) {
		String ret = null;
		// get the project for this resource
		IProject proj = resource.getProject();

		if (CVSTeamProvider.isSharedWithCVS(proj)) {
			// get CVSProvider
			CVSTeamProvider provider = (CVSTeamProvider) RepositoryProvider
					.getProvider(proj, CVSProviderPlugin.getTypeId());
			// get Repository Location
			try {
				ICVSRepositoryLocation location = provider.getRemoteLocation();

				// get CVSROOT
				CVSWorkspaceRoot cvsRoot = provider.getCVSWorkspaceRoot();

				ICVSFolder folder = cvsRoot.getLocalRoot();
				FolderSyncInfo syncInfo = folder.getFolderSyncInfo();

				String module = syncInfo.getRepository();

				ret = "cvs://" + location.getHost() + location.getRootDirectory() //$NON-NLS-1$
						+ "?" + module + "/" + getCurrentBranchName(resource); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (CVSException cvsException) {
				cvsException.printStackTrace();
			}
		}

		return ret;
	}

}
