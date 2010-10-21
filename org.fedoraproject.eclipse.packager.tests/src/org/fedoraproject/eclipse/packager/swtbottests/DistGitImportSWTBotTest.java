package org.fedoraproject.eclipse.packager.swtbottests;

import static org.junit.Assert.assertNotNull;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
 
@RunWith(SWTBotJunit4ClassRunner.class)
public class DistGitImportSWTBotTest {
 
	private static SWTWorkbenchBot	bot;
	private IProject edProject;
 
	@BeforeClass
	public static void beforeClass() throws Exception {
		bot = new SWTWorkbenchBot();
		bot.viewByTitle("Welcome").close();
	}
 
	/**
	 * Basic functional test of import wizard. All cases should succeed.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	@Test
	public void canImportNewFedoraDistGitProject() throws Exception {
		bot.menu("File").menu("Import...").click();
 
		SWTBotShell shell = bot.shell("Import");
		shell.activate();
		bot.tree().expandNode("Git").select("Projects from Fedora Git");
		bot.button("Next >").click();
		
		// Import ed
		SWTBotShell importDialog = bot.shell("Import from Fedora Git");
		bot.textWithLabel("Package name:").setText("ed");
 
		bot.button("Finish").click();
		// Wait for import operation to finish
		bot.waitUntil(Conditions.shellCloses(importDialog));
		// Find newly created project
		IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
		edProject = (IProject)wsRoot.findMember(new Path("ed"));
		assertNotNull(edProject);
		IResource edSpecFile = edProject.findMember(new Path("ed.spec"));
		assertNotNull(edSpecFile);
		// Delete project again
		this.edProject.delete(true, new NullProgressMonitor());
	}
	
	/**
	 * Try to import non-existent project. I don't know why the error
	 * dialog does not show up during this test run... :-(
	 * 
	 * @throws Exception
	 */
	/*@SuppressWarnings("static-access")
	@Test
	public void testShowsProperErrorMessageForNotExistentDistGitProject() throws Exception {
		bot = new SWTWorkbenchBot();
		bot.menu("File").menu("Import...").click();
 
		SWTBotShell shell = bot.shell("Import");
		shell.activate();
		bot.tree().expandNode("Git").select("Projects from Fedora Git");
		bot.button("Next >").click();
		
		// Import something non-existent
		bot.textWithLabel("Package name:").setText("not__there_repo");
		bot.button("Finish").click();
		// Get widget with expected error message
		final Widget test = bot.widget(
				WidgetMatcherFactory.allOf(
				WidgetMatcherFactory.withRegex(".*" +
						NLS.bind(org.fedoraproject.eclipse.packager.git.Messages.
								fedoraCheckoutWizard_repositoryNotFound, "not__there_repo") +
								".*" )));
		// Wait for problem shell to appear
		assertNotNull(test);
		// FIXME: Assert that proper error message is shown
	}*/
 
}