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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.fedoraproject.eclipse.packager.git.FpGitProjectBits;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;
import org.fedoraproject.eclipse.packager.tests.utils.git.GitTestCase;
import org.fedoraproject.eclipse.packager.tests.utils.git.GitTestProject;

public class GitFpBitsTest extends GitTestCase {

	public void testGetCurrentBranchName() throws Exception {
		FpGitProjectBits projectBits = (FpGitProjectBits) FedoraPackagerUtils
				.getVcsHandler(getFedoraprojectRoot());
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
	}

	public void testGetBranchName() {
		// this should do initialization
		FpGitProjectBits projectBits = (FpGitProjectBits) FedoraPackagerUtils
				.getVcsHandler(getFedoraprojectRoot());
		assertNotNull(projectBits);
		assertNotNull(projectBits.getBranchName("F-7")); // should be there
		assertNotNull(projectBits.getBranchName("devel")); // master mapped to
															// devel
	}

	public void testGetDistVal() throws Exception {
		FpGitProjectBits projectBits = (FpGitProjectBits) FedoraPackagerUtils
				.getVcsHandler(getFedoraprojectRoot());
		assertNotNull(projectBits);
		// make sure we meet pre-condition (we should be on master)
		assertEquals("devel", projectBits.getCurrentBranchName());
		// ATM this will change with the next Fedora release, so expect this to
		// fail
		assertEquals(projectBits.getBranchConfig().getDistVal(), "17");
		GitTestProject testProject = getProject();
		// switch to remote branch f13
		testProject.checkoutBranch("f13");
		assertEquals(projectBits.getBranchConfig().getDistVal(), "13");
	}

	public void testNonExactNamedBranches() throws JGitInternalException,
			RefAlreadyExistsException, RefNotFoundException,
			InvalidRefNameException, CoreException {
		FpGitProjectBits projectBits = (FpGitProjectBits) FedoraPackagerUtils
				.getVcsHandler(getFedoraprojectRoot());
		assertNotNull(projectBits);
		GitTestProject testProject = getProject();
		// create branch name containing f16
		testProject.getGitRepo().branchCreate()
				.setName("fedora_betaf16_testbranch").call();
		// check out created branch
		testProject.checkoutBranch("fedora_betaf16_testbranch");
		assertEquals(projectBits.getBranchConfig().getEquivalentBranch(), "F-16");
	}
	
	public void testElRhelNonExactBranches() throws JGitInternalException,
			RefAlreadyExistsException, RefNotFoundException,
			InvalidRefNameException, CoreException {
		FpGitProjectBits projectBits = (FpGitProjectBits) FedoraPackagerUtils
				.getVcsHandler(getFedoraprojectRoot());
		assertNotNull(projectBits);
		GitTestProject testProject = getProject();
		// create branch name containing el6
		testProject.getGitRepo().branchCreate()
				.setName("something_test_el6_testbranch").call();
		// check out created branch
		testProject.checkoutBranch("something_test_el6_testbranch");
		assertEquals(projectBits.getBranchConfig().getEquivalentBranch(),
				"EL-6");
		// create branch name containing rhel-6.2
		testProject.getGitRepo().branchCreate()
				.setName("my_cool_feature-hell-rhel-6.2_testbranch").call();
		// check out created branch
		testProject.checkoutBranch("my_cool_feature-hell-rhel-6.2_testbranch");
		assertEquals(projectBits.getBranchConfig().getEquivalentBranch(),
				"RHEL-6.2");
		// create branch name containing rhel-6
		testProject.getGitRepo().branchCreate()
				.setName("my_cool_feature-hell-rhel-6_testbranch").call();
		// check out created branch
		testProject.checkoutBranch("my_cool_feature-hell-rhel-6_testbranch");
		assertEquals(projectBits.getBranchConfig().getEquivalentBranch(),
				"RHEL-6");
	}
}
