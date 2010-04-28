package org.fedoraproject.eclipse.packager.tests;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.fedoraproject.eclipse.packager.rpm.SRPMBuildHandler;

public class SRPMTest extends AbstractTest {
	@Override
	protected void setUp() throws Exception {
		System.out.println("SETUP1 START");
		super.setUp();
		System.out.println("SETUP1 END");
		
		System.out.println("SETUP2 START");
		handler = new SRPMBuildHandler();
		handler.setDebug(true);
		handler.setResource(branch);
		Shell aShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		handler.setShell(aShell);
		handler.execute(null);
		handler.waitForJob();

		System.out.println("SETUP2 END");
	}

	@Override
	protected void tearDown() throws Exception {
		System.out.println("TEARDOWN START");
		super.tearDown();
		System.out.println("TEARDOWN END");
	}

	public void testSRPM() throws Exception {
		System.out.println("TESTSRPM START");
		branch.refreshLocal(IResource.DEPTH_INFINITE, null);
		IFile srpm = (IFile) branch.findMember("ed-0.8-1.fc8.src.rpm");
		assertNotNull(srpm);
		assertTrue(srpm.getLocation().toFile().length() > 0);
		System.out.println("TESTSRPM END");
	}
}
