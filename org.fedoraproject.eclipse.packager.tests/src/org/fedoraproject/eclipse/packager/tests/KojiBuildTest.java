package org.fedoraproject.eclipse.packager.tests;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.fedoraproject.eclipse.packager.koji.KojiBuildHandler;

public class KojiBuildTest extends AbstractTest {
	private KojiHubClientStub koji;

	@Override
	protected void setUp() throws Exception {
		System.out.println("SETUP1 START");
		super.setUp();
		System.out.println("SETUP1 END");
		
		System.out.println("SETUP2 START");
		handler = new KojiBuildHandler();
		handler.setDebug(true);
		koji = new KojiHubClientStub();
		((KojiBuildHandler) handler).setKoji(koji);
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

	public void testBuild() throws Exception {
		System.out.println("TESTBUILD START");
		assertEquals("1337", handler.waitForJob().getMessage());
		System.out.println("TESTBUILD END");
	}
	
	public void testSCMURL() throws Exception {
		System.out.println("TESTSCMURL START");
		assertEquals("cvs://cvs.fedoraproject.org/cvs/pkgs?rpms/ed/F-10#ed-1_1-1_fc10", koji.scmURL);
		System.out.println("TESTSCMURL END");
	}
	
	public void testTarget() throws Exception {
		System.out.println("TESTTARGET START");
		assertEquals("dist-f10-updates-candidate", koji.target);
		System.out.println("TESTTARGET END");
	}
}
