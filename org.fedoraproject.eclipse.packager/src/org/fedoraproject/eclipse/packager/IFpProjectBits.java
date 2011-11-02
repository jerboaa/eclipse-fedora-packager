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
package org.fedoraproject.eclipse.packager;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;

/**
 * Interface for VCS specific bits of an FpProject. Implementations should
 * handle branch related things and other VCS specific parts.
 * 
 * @author Red Hat Inc.
 * 
 */
public interface IFpProjectBits {

	/**
	 * Get the current branch name.
	 * 
	 * @return The current branch name.
	 */
	public String getCurrentBranchName();

	/**
	 * Get the version of the current branch name used for repository management
	 * 
	 * @return The raw version of the current branch name.
	 */
	public String getRawCurrentBranchName();

	/**
	 * Returns the branch name specified by branchName.
	 * 
	 * @param branchName
	 *            Branch name for which to get the actual name for.
	 * @return The actual branch name.
	 */
	public String getBranchName(String branchName);

	/**
	 * Get the VCS specific URL for the given resource.
	 * 
	 * @return String representation of URL.
	 */
	public String getScmUrl();

	/**
	 * Get the BranchConfigurationInstance for the current branch.
	 * 
	 * @return BranchConfigurationInstance for the current branch.
	 */
	public BranchConfigInstance getBranchConfig();

	/**
	 * Updates the given project from the remote
	 * 
	 * @param projectRoot
	 *            The project root to update.
	 * @param monitor
	 *            The monitor to show progress.
	 * @return The status of the operation.
	 */
	public IStatus updateVCS(IProjectRoot projectRoot, IProgressMonitor monitor);

	/**
	 * Ignores the given resource.
	 * 
	 * @param resourceToIgnore
	 *            The resource to ignore.
	 * @return The status of the operation.
	 */
	public IStatus ignoreResource(IResource resourceToIgnore);

	/**
	 * Initialize IFpProjectBits instance
	 * 
	 * @param fedoraProjectRoot
	 *            The underlying fedoraprojectRoot object.
	 */
	public void initialize(IProjectRoot fedoraProjectRoot);

	/**
	 * Tag a revision of the VCS.
	 * 
	 * @param projectRoot
	 * @param monitor
	 * @param bci
	 * @return The status of the tagging operation.
	 */
	public IStatus tagVcs(IProjectRoot projectRoot, IProgressMonitor monitor,
			BranchConfigInstance bci);

	/**
	 * Determine if tag exists in VCS.
	 * 
	 * @param fedoraProjectRoot
	 * @param tag
	 * @param bci
	 * @return True if tag exists, false otherwise.
	 */
	public boolean isVcsTagged(IProjectRoot fedoraProjectRoot, String tag,
			BranchConfigInstance bci);

	/**
	 * Utility method to check whether the given VCS needs to tag.
	 * 
	 * @return True if tag needs to be created, false otherwise.
	 */
	public boolean needsTag();

	/**
	 * Returns the scm url in format suitable for koji.
	 * 
	 * @param fedoraProjectRoot
	 *            The fedora project root.
	 * @param bci
	 *            The current branch configuration.
	 * @return The scm url as expected by koji.
	 */
	public String getScmUrlForKoji(IProjectRoot fedoraProjectRoot,
			BranchConfigInstance bci);

	/**
	 * Checks whether there are local changes.
	 * 
	 * @param fedoraProjectRoot
	 *            The project root to check for local changes.
	 * 
	 * @return Whether there are local changes or not.
	 * @throws CommandListenerException
	 */
	public boolean hasLocalChanges(IProjectRoot fedoraProjectRoot)
			throws CommandListenerException;

	/**
	 * Stage changes to an array of files.
	 * 
	 * @param files
	 *            The files to add the changes to the repository.
	 * @throws CommandListenerException
	 */
	public void stageChanges(String[] files) throws CommandListenerException;
}
