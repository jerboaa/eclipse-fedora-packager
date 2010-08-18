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

import org.eclipse.core.resources.IResource;
import org.fedoraproject.eclipse.packager.IFpProjectBits;

/**
 * Git specific project bits (branches management and such).
 * 
 * @author Red Hat Inc.
 *
 */
public class FpGitProjectBits implements IFpProjectBits {

	public FpGitProjectBits() {
		//TODO Implement this!
	}
	
	@Override
	public String getBranchName(String branchName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCurrentBranchName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getScmUrl(IResource resource) {
		// TODO Auto-generated method stub
		return "dummy output";
	}

}
