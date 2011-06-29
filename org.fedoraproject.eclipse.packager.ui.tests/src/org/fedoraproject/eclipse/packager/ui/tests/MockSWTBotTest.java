/*******************************************************************************
 * Copyright (c) 2010-2011 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.fedoraproject.eclipse.packager.ui.tests;

import static org.eclipse.swtbot.swt.finder.SWTBotAssert.assertTextContains;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotStyledText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.fedoraproject.eclipse.packager.IProjectRoot;
import org.fedoraproject.eclipse.packager.tests.utils.git.GitTestProject;
import org.fedoraproject.eclipse.packager.ui.tests.utils.ContextMenuHelper;
import org.fedoraproject.eclipse.packager.ui.tests.utils.PackageExplorer;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
 
@RunWith(SWTBotJunit4ClassRunner.class)
public class MockSWTBotTest {
	
	private static SWTWorkbenchBot	bot;
	private GitTestProject edProject;
	private Map<String, Boolean> expectedResources = new HashMap<String, Boolean>();
	private IProjectRoot fpRoot;
 
	@BeforeClass
	public static void beforeClass() throws Exception {
		bot = new SWTWorkbenchBot();
		try {
			bot.viewByTitle("Welcome").close();
			// hide Subclipse Usage stats popup if present/installed
			bot.shell("Subclipse Usage").activate();
			bot.button("Cancel").click();
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
		edProject.checkoutBranch("f13");
		IResource edSpec = edProject.getProject().findMember(new Path("ed.spec"));
		assertNotNull(edSpec);
		// set up expectations for resources produced
		expectedResources.put("root.log", false);
		expectedResources.put("state.log", false);
		expectedResources.put("build.log", false);
		expectedResources.put("RPM", false);
		fpRoot = FedoraPackagerUtils.getProjectRoot(edProject.getProject());
	}
 
	/**
	 * Test mock builds on local architecture. This assumes mock to be installed
	 * and PAM configured (look in /etc/pam.d/mock) so that the test-executing
	 * user is not required to enter the root password.
	 * 
	 * @throws Exception
	 */
	@Test
	public void canMockBuildOnLocalArchitecture() throws Exception {
		
		// get tree of Package Explorer view
		SWTBotTree packagerTree = PackageExplorer.getTree();
		
		// Select spec file
		final SWTBotTreeItem edItem = PackageExplorer.getProjectItem(
				packagerTree, "ed");
		edItem.expand();
		edItem.select("ed.spec");
		
		// Click mock build context menu item
		clickOnMockBuild(packagerTree);
		// Wait for fedora packager job to start
		bot.waitUntil(Conditions.shellIsActive(fpRoot.getProductStrings().getProductName()));
		SWTBotShell efpJobWindow = bot.shell(fpRoot.getProductStrings().getProductName());
		assertNotNull(efpJobWindow);
		// Wait for mock build to finish, this takes a while so increase timeout
		SWTBotPreferences.TIMEOUT = 5 * 60 * 1000; // set this to 5 minutes for now
		bot.waitUntil(Conditions.shellCloses(efpJobWindow));
		// reset timeout to default (5 secs)
		SWTBotPreferences.TIMEOUT = 5000;
		
		// Assert success
		// Make sure "INFO: done" is printed on Console
		SWTBotView consoleView = bot.viewByTitle("Console");
		Widget consoleViewComposite = consoleView.getWidget();
		StyledText consoleText = bot.widget(WidgetMatcherFactory.widgetOfType(
				StyledText.class),
				consoleViewComposite);
		SWTBotStyledText styledTextConsole = new SWTBotStyledText(consoleText);
		assertTextContains("INFO: Done", styledTextConsole);
		
		// Make sure we have build.log, root.log, and state.log +
		// rpms/srpms have been created
		IProject project = this.edProject.getProject();
		IFolder edBuildFolder = null;
		// Find build folder
		for (IResource item: project.members()) {
			if (item.getName().startsWith("ed-1_1") && item.getName().endsWith("fc13")) {
				edBuildFolder = (IFolder)item;
				break;
			}
		}
		assertNotNull(edBuildFolder);
		// Search build folder for expected resources
		boolean rpmFound = false;
		for (IResource item: edBuildFolder.members()) {
			if (item.getName().equals("build.log")
					|| item.getName().equals("root.log")
					|| item.getName().equals("state.log")) {
				// erase item from expectedResources
				expectedResources.remove(item.getName());
			} else if ( !rpmFound && item.getName().endsWith(".rpm")) {
				expectedResources.remove("RPM");
				// there may be more than one rpms
				rpmFound = true;
			}
		}
		// We are good if expectedResources is empty, should have been
		// removed earlier
		assertTrue("The map of expected resources should have been empty.",
				expectedResources.isEmpty());
	}
 
	@After
	public void tearDown() throws Exception {
		this.edProject.dispose();
	}
	
	/**
	 * Context menu click helper. Click on "Local Build Using Mock".
	 * 
	 * @param Tree of Package Explorer view.
	 * @throws Exception
	 */
	private void clickOnMockBuild(SWTBotTree packagerTree) throws Exception {
		String menuItem = "Local Build Using Mock";
		ContextMenuHelper.clickContextMenu(packagerTree, "Fedora Packager",
				menuItem);
	}
 
}