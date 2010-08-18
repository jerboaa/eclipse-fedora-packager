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

import java.util.HashMap;

import org.eclipse.core.resources.IResource;
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
