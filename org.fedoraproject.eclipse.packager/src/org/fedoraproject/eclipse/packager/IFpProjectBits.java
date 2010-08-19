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
package org.fedoraproject.eclipse.packager;

import org.eclipse.core.resources.IResource;

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
	 * Returns the branch name specified by branchName.
	 * 
	 * @param branchName Branch name for which to get the actual name for.
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
	 * Get the dist for the given branch.
	 * 
	 * @return String The dist as used by koji.
	 */
	public String getDist();
	
	/**
	 * Initialize IFpProjectBits instance
	 * 
	 * @param resource The underlying project.
	 */
	public void initialize(IResource resource);
	
}
