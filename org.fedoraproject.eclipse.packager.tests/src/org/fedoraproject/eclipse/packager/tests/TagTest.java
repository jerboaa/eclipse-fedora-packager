package org.fedoraproject.eclipse.packager.tests;


import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.fedoraproject.eclipse.packager.cvs.TagHandler;

public class TagTest extends AbstractTest {
	private IStatus result;
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		handler = new TagHandler();
		handler.setDebug(true);
		handler.setResource(branch);
		Shell aShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		handler.setShell(aShell);
		handler.execute(null);

		result = handler.waitForJob();
	}
	
	public void testNeedWriteAccess() throws Exception {
		String errMsg = "\"tag\" requires write access to the repository";
		assertTrue(result.getChildren()[0].getMessage().contains(errMsg));
	}
	
}
