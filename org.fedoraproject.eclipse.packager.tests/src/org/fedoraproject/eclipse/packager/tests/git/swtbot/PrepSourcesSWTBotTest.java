package org.fedoraproject.eclipse.packager.tests.git.swtbot;

import static org.junit.Assert.*;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
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
public class PrepSourcesSWTBotTest {
 
	private static SWTWorkbenchBot	bot;
	private GitTestProject edProject;
 
	@BeforeClass
	public static void beforeClass() throws Exception {
		bot = new SWTWorkbenchBot();
		bot.viewByTitle("Welcome").close();
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
	 * Prepare sources test.
	 * 
	 * @throws Exception
	 */
	@Test
	public void canPrepareSourcesForLocalBuild() throws Exception {
		
		SWTBotTree packagerTree = PackageExplorer.getTree();
		
		// Select spec file
		final SWTBotTreeItem edItem = PackageExplorer.getProjectItem(
				packagerTree, "ed");
		edItem.expand();
		edItem.select("ed.spec");
		
		// Click prepare sources context menu item
		clickOnPrepareSources(packagerTree);
		// Wait for upload process to start
		bot.waitUntil(Conditions.shellIsActive(org.fedoraproject.
				eclipse.packager.rpm.Messages.prepHandler_jobName));
		SWTBotShell efpJobWindow = bot.shell(org.fedoraproject.
				eclipse.packager.rpm.Messages.prepHandler_jobName);
		assertNotNull(efpJobWindow);
		// Wait for upload process to finish
		bot.waitUntil(Conditions.shellCloses(efpJobWindow));
		
		// Assert success
		IFolder edFolder = (IFolder)edProject.getProject().findMember(new Path("ed-1.1"));
		// Should exist and should not be empty
		assertNotNull(edFolder);
		assertTrue("Expected expanded ed sources folder to exist",
				edFolder.exists());
		assertTrue("Expected expanded ed sources folder to contain files",
				edFolder.members().length > 0);
	}
 
	@After
	public void tearDown() throws Exception {
		this.edProject.dispose();
	}
	
	/**
	 * Context menu click helper. Click on "Prepare Sources for Build".
	 * 
	 * @param Tree of Package Explorer view.
	 * @throws Exception
	 */
	private void clickOnPrepareSources(SWTBotTree packagerTree) throws Exception {
		String menuItem = "Prepare Sources for Build";
		ContextMenuHelper.clickContextMenu(packagerTree, "Fedora Packager",
				menuItem);
	}
 
}