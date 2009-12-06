package org.fedoraproject.eclipse.packager.tests;


import org.eclipse.core.resources.IContainer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.fedoraproject.eclipse.packager.rpm.PrepHandler;
import org.fedoraproject.eclipse.packager.rpm.RPMHandler;

public class PrepTest extends AbstractTest {
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		handler = new PrepHandler();
		handler.setDebug(true);
		handler.setResource(branch);
		Shell aShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		handler.setShell(aShell);
		handler.execute(null);		
		handler.waitForJob();
	}
	
	public void testExitStatus() throws Exception {	
		assertEquals(0, ((RPMHandler) handler).getExitStatus());
	}
	
	public void testSourceFolder() throws Exception {
		IContainer folder = (IContainer) branch.findMember("ed-0.8");
		assertNotNull(folder);
		assertTrue(folder.members().length > 0);
	}
}
