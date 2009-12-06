package org.fedoraproject.eclipse.packager.tests;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.fedoraproject.eclipse.packager.bodhi.BodhiNewHandler;

public class BodhiNewTest extends AbstractTest {
	private BodhiClientStub bodhi;

	@Override
	protected void setUp() throws Exception {
		System.out.println("SETUP1 START");
		super.setUp();
		System.out.println("SETUP1 END");
		
		System.out.println("SETUP2 START");
		handler = new BodhiNewHandler();
		bodhi = new BodhiClientStub();
		handler.setDebug(true);
		((BodhiNewHandler) handler).setBodhi(bodhi);
		((BodhiNewHandler) handler).setDialog(new BodhiNewDialogStub((BodhiNewHandler) handler));
		((BodhiNewHandler) handler).setAuthDialog(new UserValidationDialogStub());
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

	public void testUpdate() throws Exception {
		System.out.println("TESTBUILD START");
		IStatus result = handler.waitForJob();
		String tgflash = result.getMessage();
		String update = result.getChildren()[0].getMessage();
		assertEquals("1337", tgflash);
		assertEquals("7331", update);
		System.out.println("TESTBUILD END");
	}
	
	public void testAuth() throws Exception {
		System.out.println("TESTSCMURL START");
		assertEquals("user", bodhi.username);
		assertEquals("pass", bodhi.password);
		System.out.println("TESTSCMURL END");
	}
	
	public void testArgs() throws Exception {
		String clog = "* Wed Aug 22 2007 Karsten Hopp <karsten@redhat.com> 0.8-1\n"
			+ "- update to 0.8";
		System.out.println("TESTTARGET START");
		assertEquals("ed-0.8-1.fc8", bodhi.buildName);
		assertEquals("F8", bodhi.release);
		assertEquals("", bodhi.bugs);
		assertEquals("bugfix", bodhi.type);
		assertEquals("testing", bodhi.request);
		assertEquals(clog, bodhi.notes);
		System.out.println("TESTTARGET END");
	}
}
