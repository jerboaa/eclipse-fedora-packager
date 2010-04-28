package org.fedoraproject.eclipse.packager.tests;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.fedoraproject.eclipse.packager.rpm.SRPMBuildHandler;

public class SRPMTest extends AbstractTest {
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		handler = new SRPMBuildHandler();
		handler.setDebug(true);
		handler.setResource(branch);
		Shell aShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		handler.setShell(aShell);
		handler.execute(null);
		handler.waitForJob();
	}

	public void testSRPM() throws Exception {
		branch.refreshLocal(IResource.DEPTH_INFINITE, null);
		IFile srpm = (IFile) branch.findMember("ed-1.1-1.fc10.src.rpm");
		assertNotNull(srpm);
		assertTrue(srpm.getLocation().toFile().length() > 0);
	}
}
