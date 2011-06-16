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
package org.fedoraproject.eclipse.packager.tests;

import org.fedoraproject.eclipse.packager.git.FpGitProjectBits;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;
import org.fedoraproject.eclipse.packager.tests.utils.git.GitTestCase;
import org.fedoraproject.eclipse.packager.tests.utils.git.GitTestProject;

public class GitFpBitsTest extends GitTestCase {

	public void testGetCurrentBranchName() throws Exception {
		FpGitProjectBits projectBits = (FpGitProjectBits)FedoraPackagerUtils.getVcsHandler(getFedoraprojectRoot());
		assertNotNull(projectBits);
		// make sure we meet pre-condition (we should be on master)
		assertEquals("devel", projectBits.getCurrentBranchName());
		GitTestProject testProject = getProject();
		// switch to branch f13
		testProject.checkoutBranch("f13");
		assertEquals("F-13", projectBits.getCurrentBranchName());
		// switch to branch fc6
		testProject.checkoutBranch("fc6");
		assertEquals("FC-6", projectBits.getCurrentBranchName());
		// switch to f14 branch, which uses old naming scheme
		testProject.checkoutBranch("f14/master");
		assertEquals("F-14", projectBits.getCurrentBranchName());
	}
	
	public void testGetBranchName() {
		// this should do initialization
		FpGitProjectBits projectBits = (FpGitProjectBits)FedoraPackagerUtils.getVcsHandler(getFedoraprojectRoot());
		assertNotNull(projectBits);
		assertNotNull(projectBits.getBranchName("F-7")); // should be there
		assertNotNull(projectBits.getBranchName("devel")); // master mapped to devel
	}
	
	public void testGetDistVal() throws Exception {
		FpGitProjectBits projectBits = (FpGitProjectBits)FedoraPackagerUtils.getVcsHandler(getFedoraprojectRoot());
		assertNotNull(projectBits);
		// make sure we meet pre-condition (we should be on master)
		assertEquals("devel", projectBits.getCurrentBranchName());
		// ATM this will change with the next Fedora release, so expect this to fail
		assertEquals(projectBits.getDistVal(), "16");
		GitTestProject testProject = getProject();
		// switch to remote branch f13
		testProject.checkoutBranch("f13");
		assertEquals(projectBits.getDistVal(), "13");
	}

}
