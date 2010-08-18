package org.fedoraproject.eclipse.packager.tests.git;

import org.eclipse.core.resources.IProject;
import org.fedoraproject.eclipse.packager.git.FpGitProjectBits;
import org.fedoraproject.eclipse.packager.handlers.FedoraHandlerUtils;

public class GitFpBitsTest extends GitTestCase {

	public void testGetCurrentBranchName() {
		fail("Not yet implemented");
	}

	public void testGetScmUrl() {
		fail("Not yet implemented");
	}

	public void testInitialize() {
		IProject project = getiProject();
		// this should do initialization
		FpGitProjectBits projectBits = (FpGitProjectBits)FedoraHandlerUtils.getVcsHandler(project);
		assertNotNull(projectBits);
	}

}
