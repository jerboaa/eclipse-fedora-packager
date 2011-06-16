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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
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
public class BodhiUpdateSWTBotTest {
 
	private static SWTWorkbenchBot	bot;
    private GitTestProject efpProject;
	
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
		// Make sure we have the Package Explorer view open and shown
		PackageExplorer.openView();
	}

	@Before
	public void setUp() throws Exception {
		// Import eclipse-fedorapackager
		efpProject = new GitTestProject("eclipse-fedorapackager");
		IResource efpSpec = efpProject.getProject().findMember(new Path("eclipse-fedorapackager.spec"));
		assertNotNull(efpSpec);
	}
 
	/**
	 * Basic functional test for Bodhi updates.
	 * 
	 * @throws Exception
	 */
	@Test
	public void canPushBuildToKoji() throws Exception {
		
		// get tree of Package Explorer view
		SWTBotTree packagerTree = PackageExplorer.getTree();
		
		// Select spec file
		final SWTBotTreeItem efpItem = PackageExplorer.getProjectItem(
				packagerTree, "eclipse-fedorapackager");
		efpItem.expand();
		efpItem.select("eclipse-fedorapackager.spec");
		
		// Create update (this should only use stubs)
		clickOnCreateBodhiUpdate(packagerTree);
		
		// Assert success. I.e. look for the update popup message
		bot.waitUntil(Conditions.shellIsActive(org.fedoraproject.eclipse.
				packager.bodhi.BodhiText.BodhiUpdateInfoDialog_updateResponseTitle));
		SWTBotShell updateMsgWindow = bot.shell(org.fedoraproject.eclipse.
				packager.bodhi.BodhiText.BodhiUpdateInfoDialog_updateResponseTitle);
		assertNotNull(updateMsgWindow);
		updateMsgWindow.close();
	}
	
	@After
	public void tearDown() throws Exception {
		this.efpProject.dispose();
	}
	
	/**
	 * Context menu click helper. Click on "Push Build to Koji".
	 * 
	 * @param Tree of Package Explorer view.
	 * @throws Exception
	 */
	private void clickOnCreateBodhiUpdate(SWTBotTree packagerTree) throws Exception {
		String menuItem = "Create New Bodhi Update";
		ContextMenuHelper.clickContextMenu(packagerTree,
				"Fedora Packager",	menuItem);
	}
 
}