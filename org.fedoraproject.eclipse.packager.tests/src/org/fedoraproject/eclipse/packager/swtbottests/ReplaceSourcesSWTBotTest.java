package org.fedoraproject.eclipse.packager.swtbottests;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
public class ReplaceSourcesSWTBotTest {
 
	private static SWTWorkbenchBot	bot;
	private GitTestProject efpProject;
	// Filenames used for this test
	private final String EMPTY_FILE_NAME_VALID = "REMOVE_ME.tar";
	private final String NON_EMPTY_FILE_NAME_INVALID = "REMOVE_ME.exe";
	private final String VALID_SOURCE_FILENAME_NON_EMPTY = "REMOVE_ME.tar.bz2";
 
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
	 * Upload Sources test (Replace existing sources) with empty
	 * source file.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings({ "static-access", "unchecked" })
	@Test
	public void cannotUploadEmptySourceFileReplaceSourcesHandler() throws Exception {
		// Create empty source file and try to upload
		IResource emptySourceFile;
		emptySourceFile = createNewFile(EMPTY_FILE_NAME_VALID, null);
		assertNotNull(emptySourceFile);
		
		openPackageExplorerView();
		
		// Select source file
		final SWTBotTreeItem efpItem = bot.tree().expandNode("eclipse-fedorapackager");
		bot.waitUntil(Conditions.widgetIsEnabled(efpItem));
    	efpItem.select(EMPTY_FILE_NAME_VALID);

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
								uploadHandler_invalidFile, EMPTY_FILE_NAME_VALID))));
		assertNotNull(errorMessageWidget);
		efpErrorWindow.close();
	}
	
	/**
	 * Upload Sources test (Replace existing sources) with non-empty
	 * source file which has an invalid extension.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings({ "static-access", "unchecked" })
	@Test
	public void cannotUploadNonEmptySourceFileWithInvalidExtensionReplaceSourcesHandler() throws Exception {
		// Create non-empty, invalid source file in project
		IResource invalidSourceFile;
		invalidSourceFile = createNewFile(NON_EMPTY_FILE_NAME_INVALID, 0x900);
		assertNotNull(invalidSourceFile);
		
		openPackageExplorerView();
		
		// Select source file
		final SWTBotTreeItem efpItem = bot.tree().expandNode("eclipse-fedorapackager");
		bot.waitUntil(Conditions.widgetIsEnabled(efpItem));
    	efpItem.select(NON_EMPTY_FILE_NAME_INVALID);
		
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
								uploadHandler_invalidFile, NON_EMPTY_FILE_NAME_INVALID))));
		assertNotNull(errorMessageWidget);
		efpErrorWindow.close();
	}
	
	/**
	 * Upload Sources test (Replace existing sources) with valid source
	 * file. This assumes a valid ~/.fedora.cert is present.
	 * 
	 * @throws Exception
	 */
	@Test
	public void canUploadValidSourceFileReplaceSourcesHandler() throws Exception {
		// Create valid source file in project
		IResource validSourceFile;
		validSourceFile = createNewFile(VALID_SOURCE_FILENAME_NON_EMPTY, 0x90);
		assertNotNull(validSourceFile);
		
		// Set up expectations
		final String newSourcesContent = "fcd3dfe8777d16d64235bc7ae6bdcb8a  " + VALID_SOURCE_FILENAME_NON_EMPTY + "\n";
		final String newLineGitIgnore = VALID_SOURCE_FILENAME_NON_EMPTY + "\n";
		final String gitIgnoreBefore = readGitIgnore();
		
		openPackageExplorerView();
		
		// Select source file to be uploaded
		final SWTBotTreeItem efpItem = bot.tree().expandNode("eclipse-fedorapackager");
		bot.waitUntil(Conditions.widgetIsEnabled(efpItem));
    	efpItem.select(VALID_SOURCE_FILENAME_NON_EMPTY);
		
		// Click on file and try to upload
		clickOnReplaceExistingSources();
		// Wait for upload process to start
		bot.waitUntil(Conditions.shellIsActive(org.fedoraproject.
				eclipse.packager.Messages.newSourcesHandler_jobName));
		SWTBotShell efpUploadWindow = bot.shell(org.fedoraproject.
				eclipse.packager.Messages.newSourcesHandler_jobName);
		assertNotNull(efpUploadWindow);
		// Wait for upload process to finish
		bot.waitUntil(Conditions.shellCloses(efpUploadWindow));
		
		// Assert success
		final String sourcesFileAfter = readSourcesFile();
		final String gitIgnoreAfter = readGitIgnore();
		assertEquals(
				newSourcesContent,
				sourcesFileAfter
		);
		assertEquals(
				(gitIgnoreBefore + newLineGitIgnore),
				gitIgnoreAfter
		);
		// Make sure sources file is still around
		IResource newSource = efpProject.getProject().findMember(new Path(VALID_SOURCE_FILENAME_NON_EMPTY));
		assertNotNull(newSource);
	}
 
	@After
	public void tearDown() throws Exception {
		this.efpProject.dispose();
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
		packageExplorer.setFocus();
		packageExplorer.show();
	}
	
	/**
	 * Convenience for readFile("sources").
	 * 
	 * @return Contents of sources file.
	 */
	private String readSourcesFile() {
		return readFile("sources");
	}
	
	/**
	 * Convenience method for readfile(".gitignore").
	 * 
	 * @return Contents of .gitignore file.
	 */
	private String readGitIgnore() {
		return readFile(".gitignore");
	}
	
	/**
	 * Read contents of a text file and return its contents
	 * as a String.
	 * 
	 * @param name The name of the file to read from.
	 * @return The contents of that file.
	 */
	private String readFile(String name) {
		StringBuffer result = new StringBuffer();
		File file = efpProject.getProject().findMember(name).getLocation().toFile();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			do {
				line = br.readLine();
				if (line != null) {
					result.append(line + "\n");
				}
			} while (line != null);
			br.close();
		} catch (IOException e) {
			fail("Could not read from file " + name);
		}
		return result.toString();
	}
 
}