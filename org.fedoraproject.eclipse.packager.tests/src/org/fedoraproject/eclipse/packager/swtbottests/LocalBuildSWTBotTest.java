package org.fedoraproject.eclipse.packager.swtbottests;

import static org.junit.Assert.*;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.fedoraproject.eclipse.packager.swtbottests.utils.ContextMenuHelper;
import org.fedoraproject.eclipse.packager.tests.git.utils.GitTestProject;
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
		bot.viewByTitle("Welcome").close();
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
		
		openPackageExplorerView();
		
		// Select spec file
		final SWTBotTreeItem edItem = bot.tree().expandNode("ed");
		bot.waitUntil(Conditions.widgetIsEnabled(edItem));
    	edItem.select("ed.spec");
		
		// Click local build context menu item
		clickOnBuildForLocalArchitecture();
		// Wait for upload process to start
		bot.waitUntil(Conditions.shellIsActive(org.fedoraproject.
				eclipse.packager.rpm.Messages.localBuildHandler_jobName));
		SWTBotShell efpJobWindow = bot.shell(org.fedoraproject.
				eclipse.packager.rpm.Messages.localBuildHandler_jobName);
		assertNotNull(efpJobWindow);
		// Wait for upload process to finish
		bot.waitUntil(Conditions.shellCloses(efpJobWindow));
		
		// Assert success
		IFolder buildFolder = null;
		for (String arch : ARCHS) {
			buildFolder = (IFolder) edProject.getProject().findMember(arch);
			if (buildFolder != null) {
				break;
			}
		}
		assertNotNull(buildFolder);
		assertTrue("Expected ed rpm to be created in " + buildFolder.getName(), buildFolder.members().length > 0);
	}
 
	@After
	public void tearDown() throws Exception {
		this.edProject.dispose();
	}
	
	/**
	 * Context menu click helper. Click on "Build for Local Architecture".
	 * 
	 * @throws Exception
	 */
	private void clickOnBuildForLocalArchitecture() throws Exception {
		// Assumes Packages Explorer view active and bot.tree()
		// points to its tree.
		String menuItem = "Build for Local Architecture";
		ContextMenuHelper.clickContextMenu(bot.tree(), "Fedora Packager",
				menuItem);
	}
	
	/**
	 * Opens Window => Show View => Other... => Java => Package Explorer
	 * view.
	 */
	private void openPackageExplorerView() throws Exception {
		// Open Package Explorer view
		bot.menu("Window").menu("Show View").menu("Other...").click();
		SWTBotShell shell = bot.shell("Show View");
		shell.activate();
		bot.tree().expandNode("Java").select("Package Explorer");
		bot.button("OK").click();
		// Make sure view is active
		SWTBotView packageExplorer = bot.activeView();
		assertEquals("Package Explorer", packageExplorer.getTitle());
		assertTrue(packageExplorer.isActive());
		packageExplorer.setFocus();
		packageExplorer.show();
	}
 
}