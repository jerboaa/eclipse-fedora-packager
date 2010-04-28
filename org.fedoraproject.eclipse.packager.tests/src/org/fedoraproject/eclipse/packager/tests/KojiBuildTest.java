package org.fedoraproject.eclipse.packager.tests;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.fedoraproject.eclipse.packager.koji.KojiBuildHandler;

public class KojiBuildTest extends AbstractTest {
	private KojiHubClientStub koji;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		handler = new KojiBuildHandler();
		handler.setDebug(true);
		koji = new KojiHubClientStub();
		((KojiBuildHandler) handler).setKoji(koji);
		handler.setResource(branch);
		Shell aShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		handler.setShell(aShell);
		handler.execute(null);
		handler.waitForJob();
	}

	public void testBuild() throws Exception {
		assertEquals("1337", handler.waitForJob().getMessage());
	}
	
	public void testSCMURL() throws Exception {
		assertEquals("cvs://cvs.fedoraproject.org/cvs/pkgs?rpms/ed/F-10#ed-1_1-1_fc10", koji.scmURL);
	}
	
	public void testTarget() throws Exception {
		assertEquals("dist-f10-updates-candidate", koji.target);
	}
}
