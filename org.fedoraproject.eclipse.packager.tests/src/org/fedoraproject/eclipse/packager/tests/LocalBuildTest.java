package org.fedoraproject.eclipse.packager.tests;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.fedoraproject.eclipse.packager.rpm.LocalBuildHandler;

public class LocalBuildTest extends AbstractTest {
	String[] arches = {"noarch", "i386", "i586", "i686", "x86_64", "ia64", "s390", "s390x", "ppc", "ppc64", "pseries", "ppc64pseries", "iseries", "ppc64iseries", "athlon", "alpha", "alphaev6", "sparc", "sparc64", "sparcv9", "sparcv9v", "sparc64v", "i164", "mac", "sh", "mips", "geode"};
	@Override
	protected void setUp() throws Exception {
		System.out.println("SETUP1 START");
		super.setUp();
		System.out.println("SETUP1 END");
		
		System.out.println("SETUP2 START");
		handler = new LocalBuildHandler();
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

	public void testBuildFolder() throws Exception {
		System.out.println("TESTBUILDFOLDER START");
		branch.refreshLocal(IResource.DEPTH_INFINITE, null);
		IFolder buildFolder = null;
		for (String arch : arches) {
			buildFolder = (IFolder) branch.findMember(arch);
			if (buildFolder != null) {
				break;
			}
		}
		assertNotNull(buildFolder);
		assertTrue(buildFolder.members().length > 0);
		System.out.println("TESTBUILDFOLDER END");
	}
}
