package org.fedoraproject.eclipse.packager.swtbottests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.fedoraproject.eclipse.packager.swtbottests.utils.ContextMenuHelper;
import org.fedoraproject.eclipse.packager.tests.git.utils.GitTestProject;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
 
@RunWith(SWTBotJunit4ClassRunner.class)
public class DownloadSourcesSWTBotTest {
 
	private static SWTWorkbenchBot	bot;
	private GitTestProject edProject;
 
	@BeforeClass
	public static void beforeClass() throws Exception {
		bot = new SWTWorkbenchBot();
		bot.viewByTitle("Welcome").close();
	}
	
	@Before
	public void setUp() throws Exception {
		// Import ed and switch to F13 branch
		edProject = new GitTestProject("ed");
		edProject.checkoutBranch(Constants.R_HEADS + "f13/master");
		IResource edSpec = edProject.getProject().findMember(new Path("ed.spec"));
		assertNotNull(edSpec);
	}
 
	/**
	 * Basic functional test of Download Sources handler.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	@Test
	public void canDownloadSources() throws Exception {
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
		
		// Select spec-file (run in UI thread, to make sure selection works
		// properly)
		UIThreadRunnable
        .syncExec(new VoidResult() {
            public void run() {
            	bot.tree().expandNode("ed").select("ed.spec");
        }});
		
		// Trigger context menu "Fedora Packager" => "Download Sources"
		clickOnDownloadSources();
		
		// Wait for download dialog to finish, but first wait until it opens
		bot.waitUntil(Conditions.shellIsActive("Fedora Packager"));
		SWTBotShell fedoraPackagerJobWindow = bot.shell("Fedora Packager");
		bot.waitUntil(Conditions.shellCloses(fedoraPackagerJobWindow));
		
		// Assert that sources have been downloaded
		IProject ed = edProject.getProject();
		IResource edSourceTarBall = ed.findMember(new Path("ed-1.1.tar.bz2"));
		assertNotNull(edSourceTarBall);
	}
 
	@After
	public void tearDown() throws Exception {
		this.edProject.dispose();
	}
	
	/**
	 * Context menu click helper.
	 * 
	 * @throws Exception
	 */
	private void clickOnDownloadSources() throws Exception {
		// Assumes Packages Explorer view active and bot.tree()
		// points to its tree.
		String menuString = "Download Sources";
		ContextMenuHelper.clickContextMenu(bot.tree(), "Fedora Packager",
				menuString);
	}
 
}