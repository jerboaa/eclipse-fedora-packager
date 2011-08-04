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
package org.fedoraproject.eclipse.packager.rpm.api;

import java.util.ArrayList;
import java.util.HashMap;

import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;

/**
 * Class for performing SCM-integrated Mock builds.
 *
 */
public class SCMMockBuildCommand extends MockBuildCommand {
	

	

	

	

	

	/**
	 * Enumeration of supported repository types.
	 *
	 */
	public static enum RepoType{
		/**
		 * Git project. 
		 */
		GIT,
		/**
		 * CVS project.
		 */
		CVS,
		/**
		 * SVN project.
		 */
		SVN;
	}
	/**
	 *  The unique ID of this command.
	 */
	public static final String ID = "SCMMockBuildCommand"; //$NON-NLS-1$
	protected static final String SCM_ENABLE_OPTION = "--scm-enable"; //$NON-NLS-1$
	protected static final String SCM_VARIABLE_OPTION = "--scm-option"; //$NON-NLS-1$
	private static final String PACKAGE = "package"; //$NON-NLS-1$
	private static final String BRANCH = "branch"; //$NON-NLS-1$
	private static final String SPEC = "spec"; //$NON-NLS-1$
	private static final String EXT_SRC_DIR = "ext_src_dir"; //$NON-NLS-1$
	private static final String WRITE_TAR = "write_tar"; //$NON-NLS-1$
	protected HashMap<String, String> variableHash = new HashMap<String, String>();
	protected String repoLocation = null;
	protected RepoType repo = null;
	
	/**
	 * @param repoType The type of repository which contains the package.
	 * @return This command.
	 * @throws IllegalArgumentException if {@code null} is passed
	 */
	public SCMMockBuildCommand useRepoType(RepoType repoType) throws IllegalArgumentException{
		if (repoType == null){
			throw new IllegalArgumentException();
		}
		repo = repoType; 
		return this;
	}
	
	/**
	 * @param repoLocation The location of the repository folder (i.e. the parent folder).
	 * @return This command.
	 * @throws IllegalArgumentException if {@code null} is passed
	 */
	public SCMMockBuildCommand useRepoPath(String repoLocation){
		if (repoLocation == null){
			throw new IllegalArgumentException();
		}
		this.repoLocation = repoLocation;
		return this;
	}
	
	/**
	 * @param packageName The name of the package being built.
	 * @return This command.
	 * @throws IllegalArgumentException if {@code null} is passed
	 */
	public SCMMockBuildCommand usePackage(String packageName){
		if (packageName == null){
			throw new IllegalArgumentException();
		}
		variableHash.put(PACKAGE, packageName);
		return this;
	}
	
	/**
	 * @param branchName The branch of the repository that should be built.
	 * @return This command.
	 * @throws IllegalArgumentException if {@code null} is passed
	 */
	public SCMMockBuildCommand useBranch(String branchName){
		if (branchName == null){
			throw new IllegalArgumentException();
		}
		variableHash.put(BRANCH, branchName); 
		return this;
	}
	
	/**
	 * @param specfile The name of the specfile to be used.
	 * @return This command.
	 * @throws IllegalArgumentException if {@code null} is passed
	 */
	public SCMMockBuildCommand useSpec(String specfile){
		if (specfile == null){
			throw new IllegalArgumentException();
		}
		variableHash.put(SPEC, specfile); 
		return this;
	}
	
	/**
	 * @param directory The directory containing the source file listed in the specfile.
	 * @return This command.
	 * @throws IllegalArgumentException if {@code null} is passed
	 */
	public SCMMockBuildCommand useDownloadedSourceDirectory(String directory){
		if (directory == null){
			throw new IllegalArgumentException();
		}
		variableHash.put(EXT_SRC_DIR, directory); 
		return this;
	}
	
	/**
	 * @param write The true/false value of whether to write a tarball.
	 * @return This command.
	 */
	public SCMMockBuildCommand writeTarball(boolean write){
		if (!write){ 
			variableHash.put(WRITE_TAR, "False"); //$NON-NLS-1$ 
		} else {
			variableHash.put(WRITE_TAR, "True"); //$NON-NLS-1$ 
		}
		return this;
	}
	/**
	 * Set the command used in the chroot to retrieve the local repository.
	 */
	protected void setGetter(){
		String pack = variableHash.get(PACKAGE);
		//use SCM mock environment variable if package not assigned locally
		if (pack == null){
			pack = "SCM_PKG";  //$NON-NLS-1$
		}
		String branch = variableHash.get(BRANCH);
		//use SCM mock environment variable if package not assigned locally
		if (branch == null){
			pack = "SCM_BRN"; //$NON-NLS-1$
		}
		switch (repo){
			case GIT: variableHash.put("git_get", "git clone -b " + branch + " file://" + repoLocation + "/" + pack + " " + pack); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			case CVS: variableHash.put("cvs_get", "cp -rf " + repoLocation + "/" + pack + "/" + branch + " " + pack); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			case SVN: variableHash.put("svn_get", "svn co file://" + repoLocation + "/" + pack + "/" + branch + " " + pack); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			//if repo unset locally, set for all possible defaults 
			default: variableHash.put("git_get", "git clone -b " + branch + " file://" + repoLocation + "/" + pack + " " + pack); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			 	variableHash.put("cvs_get", "cp -rf " + repoLocation + "/" + pack + "/" + branch + " " + pack); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			 	variableHash.put("svn_get", "svn co file://" + repoLocation + "/" + pack + "/" + branch + " " + pack); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		}
	}
	
	@Override
	protected String[] buildMockCLICommand(){
		if (repoLocation != null){ 
			setGetter();
		}
		ArrayList<String> flags = new ArrayList<String>();
		String resDirOpt = MOCK_RESULT_DIR_OPTION;
		resDirOpt += "="; //$NON-NLS-1$
		resDirOpt += this.resultDir;
		
		flags.add(MOCK_BINARY);
		flags.add(MOCK_CHROOT_CONFIG_OPTION);
		flags.add(this.mockConfig);
		flags.add(MOCK_NO_CLEANUP_AFTER_OPTION);
		flags.add(resDirOpt);
		flags.add(SCM_ENABLE_OPTION);
		if (repo != null){
			flags.add(SCM_VARIABLE_OPTION);
			flags.add("method=" + repo.toString().toLowerCase()); //$NON-NLS-1$
		}
		for (String key : variableHash.keySet()){
			flags.add(SCM_VARIABLE_OPTION);
			flags.add(key + "=" + variableHash.get(key)); //$NON-NLS-1$ 
		}
		variableHash = new HashMap<String, String>();
		return flags.toArray(new String[]{});
	}
	
	@Override
	protected void checkConfiguration() throws CommandMisconfiguredException {
		//no required parameters
	}
	
	
}
