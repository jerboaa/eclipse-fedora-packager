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

import static org.junit.Assert.assertNotNull;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.fedoraproject.eclipse.packager.tests.utils.git.GitTestProject;
import org.fedoraproject.eclipse.packager.ui.tests.utils.ContextMenuHelper;
import org.fedoraproject.eclipse.packager.ui.tests.utils.PackageExplorer;
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
		// Import ed and switch to F13 branch
		edProject = new GitTestProject("ed");
		edProject.checkoutBranch("f13");
		IResource edSpec = edProject.getProject().findMember(new Path("ed.spec"));
		assertNotNull(edSpec);
	}
 
	/**
	 * Basic functional test of Download Sources handler.
	 * 
	 * @throws Exception
	 */
	@Test
	public void canDownloadSources() throws Exception {
		// Get Package Explorer tree
		SWTBotTree packagerTree = PackageExplorer.getTree();
		
		// Select spec-file
		final SWTBotTreeItem edItem = PackageExplorer.getProjectItem(
				packagerTree, "ed");
		edItem.expand();
		edItem.select("ed.spec");

		// Trigger context menu "Fedora Packager" => "Download Sources"
		clickOnDownloadSources(packagerTree);
		
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
	 * Context menu click helper. Simulates click of 
	 * "Fedora Packager" => "Download Sources"
	 * 
	 * @param Tree of Package Explorer view.
	 * @throws Exception
	 */
	private void clickOnDownloadSources(SWTBotTree packagerTree) throws Exception {
		String menuString = "Download Sources";
		ContextMenuHelper.clickContextMenu(packagerTree, "Fedora Packager",
				menuString);
	}
 
}