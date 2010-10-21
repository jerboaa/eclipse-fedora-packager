package org.fedoraproject.eclipse.packager.swtbottests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.fedoraproject.eclipse.packager.swtbottests.utils.ContextMenuHelper;
import org.fedoraproject.eclipse.packager.tests.git.utils.GitTestProject;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
 
@RunWith(SWTBotJunit4ClassRunner.class)
public class NewSourcesSWTBotTest {
 
	private static SWTWorkbenchBot	bot;
	private GitTestProject efpProject;
 
	@BeforeClass
	public static void beforeClass() throws Exception {
		bot = new SWTWorkbenchBot();
		bot.viewByTitle("Welcome").close();
	}
	
	@Before
	public void setUp() throws Exception {
		// Import eclipse-fedorapackager
		efpProject = new GitTestProject("eclipse-fedorapackager");
		IResource efpSpec = efpProject.getProject().findMember(new Path("eclipse-fedorapackager.spec"));
		assertNotNull(efpSpec);
	}
 
	/**
	 * Upload Sources test (Add to existing sources) with empty
	 * source file.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings({ "static-access", "unchecked" })
	@Test
	public void cannotUploadEmptySourceFileAddToSourcesHandler() throws Exception {
		// Create empty source file and try to upload
		IResource emptySourceFile;
		emptySourceFile = createNewFile("REMOVE_ME.txt", null);
		assertNotNull(emptySourceFile);
		
		openPackageExplorerView();
		
		// Select empty file
		UIThreadRunnable
        .syncExec(new VoidResult() {
            public void run() {
            	bot.tree().expandNode("eclipse-fedorapackager").select("REMOVE_ME.txt");
        }});
		
		// Click on file and try to upload
		clickOnAddNewSources();
		// Wait for error to pop up
		bot.waitUntil(Conditions.shellIsActive("Fedora Packager"));
		SWTBotShell efpErrorWindow = bot.shell("Fedora Packager");
		assertNotNull(efpErrorWindow);
		SWTBot errorDialogBot = efpErrorWindow.bot();
		// Get widget with expected error message
		Widget errorMessageWidget = errorDialogBot.widget(
				WidgetMatcherFactory.allOf(
				WidgetMatcherFactory.withText(
						NLS.bind(org.fedoraproject.eclipse.packager.Messages.
								uploadHandler_invalidFile, "REMOVE_ME.txt"))));
		assertNotNull(errorMessageWidget);
		efpErrorWindow.close();
	}
	
	/**
	 * Upload Sources test (Add to existing sources) with empty
	 * source file.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings({ "static-access", "unchecked" })
	@Test
	public void cannotUploadEmptySourceFileReplaceSourcesHandler() throws Exception {
		// Create empty source file and try to upload
		IResource emptySourceFile;
		emptySourceFile = createNewFile("REMOVE_ME.txt", null);
		assertNotNull(emptySourceFile);
		
		openPackageExplorerView();
		
		// Try again, but use "Replace existing sources"
		UIThreadRunnable
	    .syncExec(new VoidResult() {
	        public void run() {
	        	bot.tree().expandNode("eclipse-fedorapackager").select("REMOVE_ME.txt");
	    }});
		
		// Click on file and try to upload
		clickOnReplaceExistingSources();
		// Wait for error to pop up
		bot.waitUntil(Conditions.shellIsActive("Fedora Packager"));
		SWTBotShell efpErrorWindow = bot.shell("Fedora Packager");
		assertNotNull(efpErrorWindow);
		SWTBot errorDialogBot = efpErrorWindow.bot();
		// Get widget with expected error message
		Widget errorMessageWidget = errorDialogBot.widget(
				WidgetMatcherFactory.allOf(
				WidgetMatcherFactory.withText(
						NLS.bind(org.fedoraproject.eclipse.packager.Messages.
								uploadHandler_invalidFile, "REMOVE_ME.txt"))));
		assertNotNull(errorMessageWidget);
		efpErrorWindow.close();
	}
 
	@After
	public void tearDown() throws Exception {
		this.efpProject.dispose();
	}
	
	/**
	 * Context menu click helper. Click on "Add to existing sources".
	 * 
	 * @throws Exception
	 */
	private void clickOnAddNewSources() throws Exception {
		// Assumes Packages Explorer view active and bot.tree()
		// points to its tree.
		String subMenu = "Upload This File";
		String menuItem = "Add to existing sources";
		ContextMenuHelper.clickContextMenu(bot.tree(), "Fedora Packager",
				subMenu, menuItem);
	}
	
	/**
	 * Context menu click helper. Click on "Replace existing sources".
	 * 
	 * @throws Exception
	 */
	private void clickOnReplaceExistingSources() throws Exception {
		// Assumes Packages Explorer view active and bot.tree()
		// points to its tree.
		String subMenu = "Upload This File";
		String menuItem = "Replace existing sources";
		ContextMenuHelper.clickContextMenu(bot.tree(), "Fedora Packager",
				subMenu, menuItem);
	}
	
	/**
	 * Create a file with given name in project as created in setUp().
	 * 
	 * @param name Name of newly created file.
	 * @param contents Null or integer contents of file.
	 * @return A reference to the newly created file.
	 * @throws IOException
	 * @throws CoreException
	 */
	private IResource createNewFile(String name, Integer contents) throws IOException, CoreException {
		IProject project = efpProject.getProject();
		File newSource = new File(project.getLocation().toOSString() +
				IPath.SEPARATOR + name);
		newSource.createNewFile();
		if (contents != null) {
			FileWriter out = new FileWriter(newSource);
			out.write(contents);
			out.close();
		}
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		IResource result = project.findMember(name);
		if (result == null) {
			throw new IOException("Could not create file: '" + name +
					"' for test.");
		}
		return result;
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
		packageExplorer.show();
	}
 
}