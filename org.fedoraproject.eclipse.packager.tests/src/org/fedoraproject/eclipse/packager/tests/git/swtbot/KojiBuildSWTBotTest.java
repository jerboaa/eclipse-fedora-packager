package org.fedoraproject.eclipse.packager.tests.git.swtbot;

import static org.junit.Assert.*;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.fedoraproject.eclipse.packager.koji.KojiBuildHandler;
import org.fedoraproject.eclipse.packager.tests.git.utils.GitTestProject;
import org.fedoraproject.eclipse.packager.tests.utils.swtbot.ContextMenuHelper;
import org.fedoraproject.eclipse.packager.tests.utils.swtbot.PackageExplorer;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
 
@RunWith(SWTBotJunit4ClassRunner.class)
public class KojiBuildSWTBotTest {
 
	private static SWTWorkbenchBot	bot;
    private GitTestProject efpProject;
	
	@BeforeClass
	public static void beforeClass() throws Exception {
		bot = new SWTWorkbenchBot();
		bot.viewByTitle("Welcome").close();
		// Make sure we have the Package Explorer view open and shown
		PackageExplorer.openView();
	}

	@Before
	public void setUp() throws Exception {
		// Import eclipse-fedorapackager
		efpProject = new GitTestProject("eclipse-fedorapackager");
		IResource efpSpec = efpProject.getProject().findMember(new Path("eclipse-fedorapackager.spec"));
		assertNotNull(efpSpec);
		// Put KojiBuildHandler into testing mode
		KojiBuildHandler.inTestingMode = true;
	}
 
	/**
	 * Basic functional test for Koji build tests. This test
	 * uses KojiHubClientStub.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings({ "static-access", "unchecked" })
	@Test
	public void canPushBuildToKoji() throws Exception {
		
		// get tree of Package Explorer view
		SWTBotTree packagerTree = PackageExplorer.getTree();
		
		// Select spec file
		final SWTBotTreeItem efpItem = PackageExplorer.getProjectItem(
				packagerTree, "eclipse-fedorapackager");
		efpItem.expand();
		efpItem.select("eclipse-fedorapackager.spec");
		
		// Push build (this should use the stub)
		clickOnPushBuildToKoji(packagerTree);
		
		// Assert success. I.e. look for the task popup message
		// extend SWTBot conditions timeout
		SWTBotPreferences.TIMEOUT = 10000;
		bot.waitUntil(Conditions.shellIsActive("Koji Build"));
		SWTBotShell buildMsgWindow = bot.shell("Koji Build");
		assertNotNull(buildMsgWindow);
		// reset SWTBot timeout to default value
		SWTBotPreferences.TIMEOUT = 5000;
		SWTBot errorDialogBot = buildMsgWindow.bot();
		// Get widget with expected build message
		Widget buildMessageWidget = errorDialogBot.widget(
				WidgetMatcherFactory.allOf(
				WidgetMatcherFactory.withText(
						NLS.bind(org.fedoraproject.eclipse.packager.koji.Messages.
								kojiMessageDialog_buildNumberMsg, "1337"))));
		assertNotNull(buildMessageWidget);
		buildMsgWindow.close();
	}
	
	/**
	 * Context menu click helper. Click on "Push Build to Koji".
	 * 
	 * @param Tree of Package Explorer view.
	 * @throws Exception
	 */
	private void clickOnPushBuildToKoji(SWTBotTree packagerTree) throws Exception {
		String menuItem = "Push Build to Koji";
		ContextMenuHelper.clickContextMenu(packagerTree,
				"Fedora Packager",	menuItem);
	}
 
}