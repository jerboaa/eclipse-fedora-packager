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
		super.setUp();
		handler = new LocalBuildHandler();
		handler.setDebug(true);
		handler.setResource(branch);
		Shell aShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		handler.setShell(aShell);
		handler.execute(null);
		handler.waitForJob();
	}

	public void testBuildFolder() throws Exception {
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
	}
}
