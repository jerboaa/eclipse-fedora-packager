package org.fedoraproject.eclipse.packager.swtbottests;

import static org.junit.Assert.*;

import org.eclipse.core.resources.IFile;
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
public class CreateSRPMSWTBotTest {
	
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
		// use F10 branch of ed
		edProject.checkoutBranch(Constants.R_HEADS + "f10/master");
		IResource edSpec = edProject.getProject().findMember(new Path("ed.spec"));
		assertNotNull(edSpec);
	}
 
	/**
	 * Create SRPM test.
	 * 
	 * @throws Exception
	 */
	@Test
	public void canCreateSRPM() throws Exception {
		
		openPackageExplorerView();
		
		// Select spec file
		final SWTBotTreeItem edItem = bot.tree().expandNode("ed");
		bot.waitUntil(Conditions.widgetIsEnabled(edItem));
    	edItem.select("ed.spec");
		
		// Click local build context menu item
		clickOnCreateSRPM();
		// Wait for upload process to start
		bot.waitUntil(Conditions.shellIsActive(org.fedoraproject.
				eclipse.packager.rpm.Messages.srpmHandler_jobName));
		SWTBotShell efpJobWindow = bot.shell(org.fedoraproject.
				eclipse.packager.rpm.Messages.srpmHandler_jobName);
		assertNotNull(efpJobWindow);
		// Wait for upload process to finish
		bot.waitUntil(Conditions.shellCloses(efpJobWindow));
		
		// Assert success
		IFile srpm = (IFile) edProject.getProject().findMember("ed-1.1-1.fc10.src.rpm");
		assertNotNull(srpm);
		assertTrue("Expected ed's SRPM to be a non-empty file.",
				srpm.getLocation().toFile().length() > 0);
	}
 
	@After
	public void tearDown() throws Exception {
		this.edProject.dispose();
	}
	
	/**
	 * Context menu click helper. Click on "Create SRPM".
	 * 
	 * @throws Exception
	 */
	private void clickOnCreateSRPM() throws Exception {
		// Assumes Packages Explorer view active and bot.tree()
		// points to its tree.
		String menuItem = "Create SRPM";
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