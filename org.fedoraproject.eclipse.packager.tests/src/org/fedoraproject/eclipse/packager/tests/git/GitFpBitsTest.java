package org.fedoraproject.eclipse.packager.tests.git;

import org.eclipse.jgit.lib.Constants;
import org.fedoraproject.eclipse.packager.git.FpGitProjectBits;
import org.fedoraproject.eclipse.packager.handlers.FedoraHandlerUtils;
import org.fedoraproject.eclipse.packager.tests.git.utils.GitTestCase;
import org.fedoraproject.eclipse.packager.tests.git.utils.GitTestProject;

public class GitFpBitsTest extends GitTestCase {

	public void testGetCurrentBranchName() throws Exception {
		FpGitProjectBits projectBits = (FpGitProjectBits)FedoraHandlerUtils.getVcsHandler(getFedoraprojectRoot());
		assertNotNull(projectBits);
		// make sure we meet pre-condition (we should be on master)
		assertEquals("devel", projectBits.getCurrentBranchName());
		GitTestProject testProject = getProject();
		// switch to branch f13
		testProject.checkoutBranch(Constants.R_HEADS + "f13/master");
		assertEquals("F-13", projectBits.getCurrentBranchName());
		// switch to branch fc6
		testProject.checkoutBranch(Constants.R_HEADS + "fc6/master");
		assertEquals("FC-6", projectBits.getCurrentBranchName());
	}
	
	public void testGetBranchName() {
		// this should do initialization
		FpGitProjectBits projectBits = (FpGitProjectBits)FedoraHandlerUtils.getVcsHandler(getFedoraprojectRoot());
		assertNotNull(projectBits);
		assertNotNull(projectBits.getBranchName("F-7")); // should be there
		assertNotNull(projectBits.getBranchName("devel")); // master mapped to devel
	}
	
	public void testGetDistVal() throws Exception {
		FpGitProjectBits projectBits = (FpGitProjectBits)FedoraHandlerUtils.getVcsHandler(getFedoraprojectRoot());
		assertNotNull(projectBits);
		// make sure we meet pre-condition (we should be on master)
		assertEquals("devel", projectBits.getCurrentBranchName());
		// ATM this will change with the next Fedora release, so expect this to fail
		assertEquals(projectBits.getDistVal(), "15");
		GitTestProject testProject = getProject();
		// switch to remote branch f13
		testProject.checkoutBranch(Constants.R_HEADS + "f13/master");
		assertEquals(projectBits.getDistVal(), "13");
	}

}
