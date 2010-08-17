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

/**
 * Interface for VCS specific bits of an FpProject. Implementations should
 * handle branch related things and other VCS specific parts.
 * 
 * @author Red Hat Inc.
 *
 */
public interface IFpProjectBits {
	
	/**
	 * Returns the current branch name.
	 */
	public String getCurrentBranchName();
	
	/**
	 * Returns the branch name specified by branchName.
	 */
	public String getBranchName(String branchName);
	
	
}
