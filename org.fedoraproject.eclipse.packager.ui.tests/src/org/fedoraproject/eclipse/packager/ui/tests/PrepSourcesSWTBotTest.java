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

import static org.junit.Assert.*;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
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
public class PrepSourcesSWTBotTest {
 
	private static SWTWorkbenchBot	bot;
	private GitTestProject edProject;
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
		fpRoot = FedoraPackagerUtils.getProjectRoot(edProject.getProject());
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
		bot.waitUntil(Conditions.shellIsActive(fpRoot.getProductStrings().getProductName()));
		SWTBotShell efpJobWindow = bot.shell(fpRoot.getProductStrings().getProductName());
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