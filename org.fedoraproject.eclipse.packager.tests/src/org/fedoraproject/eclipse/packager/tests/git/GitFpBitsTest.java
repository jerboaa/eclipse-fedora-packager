package org.fedoraproject.eclipse.packager.tests.git;

import org.eclipse.core.resources.IProject;
import org.eclipse.jgit.lib.Constants;
import org.fedoraproject.eclipse.packager.git.FpGitProjectBits;
import org.fedoraproject.eclipse.packager.handlers.FedoraHandlerUtils;
import org.fedoraproject.eclipse.packager.test_utils.GitTestProject;

public class GitFpBitsTest extends GitTestCase {

	public void testGetCurrentBranchName() throws Exception {
		IProject project = getiProject();
		FpGitProjectBits projectBits = (FpGitProjectBits)FedoraHandlerUtils.getVcsHandler(project);
		assertNotNull(projectBits);
		// make sure we meet pre-condition (we should be on master)
		assertEquals("devel", projectBits.getCurrentBranchName());
		GitTestProject testProject = getProject();
		// switch to remote branch f13
		testProject.checkoutBranch(Constants.R_HEADS + "f13/master");
		assertEquals("F-13", projectBits.getCurrentBranchName());
	}
	
	public void testGetBranchName() {
		IProject project = getiProject();
		// this should do initialization
		FpGitProjectBits projectBits = (FpGitProjectBits)FedoraHandlerUtils.getVcsHandler(project);
		assertNotNull(projectBits);
		assertNotNull(projectBits.getBranchName("F-7")); // should be there
		assertNotNull(projectBits.getBranchName("devel")); // master mapped to devel
	}

	public void testGetScmUrl() {
		fail("Not yet implemented");
	}

}
