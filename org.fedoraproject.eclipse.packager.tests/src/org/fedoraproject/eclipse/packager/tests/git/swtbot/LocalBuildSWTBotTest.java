package org.fedoraproject.eclipse.packager.tests.git.swtbot;

import static org.junit.Assert.*;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.fedoraproject.eclipse.packager.tests.git.utils.GitTestProject;
import org.fedoraproject.eclipse.packager.tests.utils.swtbot.ContextMenuHelper;
import org.fedoraproject.eclipse.packager.tests.utils.swtbot.PackageExplorer;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
 
@RunWith(SWTBotJunit4ClassRunner.class)
public class LocalBuildSWTBotTest {
 
	private final String[] ARCHS = {"noarch", "i386", "i586", "i686", "x86_64",
			"ia64", "s390", "s390x", "ppc", "ppc64", "pseries", "ppc64pseries",
			"iseries", "ppc64iseries", "athlon", "alpha", "alphaev6", "sparc",
			"sparc64", "sparcv9", "sparcv9v", "sparc64v", "i164", "mac", "sh",
			"mips", "geode"};
	private static SWTWorkbenchBot	bot;
	private GitTestProject edProject;
 
	@BeforeClass
	public static void beforeClass() throws Exception {
		bot = new SWTWorkbenchBot();
		try {
			bot.viewByTitle("Welcome").close();
		} catch (WidgetNotFoundException e) {
			// ignore
		}
		PackageExplorer.openView();
	}
	
	@Before
	public void setUp() throws Exception {
		// Import ed
		edProject = new GitTestProject("ed");
		// use F13 branch of ed
		edProject.checkoutBranch(Constants.R_HEADS + "f13/master");
		IResource edSpec = edProject.getProject().findMember(new Path("ed.spec"));
		assertNotNull(edSpec);
	}
 
	/**
	 * Build for local architecture test.
	 * 
	 * @throws Exception
	 */
	@Test
	public void canBuildForLocalArchitecture() throws Exception {
		
		// get tree of Package Explorer view
		SWTBotTree packagerTree = PackageExplorer.getTree();
		
		// Select spec file
		final SWTBotTreeItem edItem = PackageExplorer.getProjectItem(
				packagerTree, "ed");
		edItem.expand();
		edItem.select("ed.spec");
		
		// Click local build context menu item
		clickOnBuildForLocalArchitecture(packagerTree);
		// Wait for upload process to start
		bot.waitUntil(Conditions.shellIsActive(org.fedoraproject.
				eclipse.packager.rpm.Messages.localBuildHandler_jobName));
		SWTBotShell efpJobWindow = bot.shell(org.fedoraproject.
				eclipse.packager.rpm.Messages.localBuildHandler_jobName);
		assertNotNull(efpJobWindow);
		// Wait for upload process to finish, extend timeout
		SWTBotPreferences.TIMEOUT = 5 * 5000;
		bot.waitUntil(Conditions.shellCloses(efpJobWindow));
		SWTBotPreferences.TIMEOUT = 5000;
		
		// Assert success
		IFolder buildFolder = null;
		for (String arch : ARCHS) {
			buildFolder = (IFolder) edProject.getProject().findMember(arch);
			if (buildFolder != null) {
				break;
			}
		}
		assertNotNull(buildFolder);
		assertTrue("Expected ed rpm to be created in " + buildFolder.getName(),
				buildFolder.members().length > 0);
	}
 
	@After
	public void tearDown() throws Exception {
		this.edProject.dispose();
	}
	
	/**
	 * Context menu click helper. Click on "Build for Local Architecture".
	 * 
	 * @param Tree of Package Explorer view.
	 * @throws Exception
	 */
	private void clickOnBuildForLocalArchitecture(SWTBotTree packagerTree) throws Exception {
		String menuItem = "Build for Local Architecture";
		ContextMenuHelper.clickContextMenu(packagerTree, "Fedora Packager",
				menuItem);
	}
 
}