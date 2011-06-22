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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
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
public class KojiBuildSWTBotTest {
 
	private static SWTWorkbenchBot	bot;
    private GitTestProject efpProject;
    
    // The build test requires ~/.fedora.cert
	private File fedoraCert = new File(System.getProperty("user.home") + 
		IPath.SEPARATOR + ".fedora.cert");
	private File tmpExistingFedoraCert; // temporary reference to already existing ~/.fedora.cert
	private boolean fedoraCertExisted = false;
	
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
		// Put KojiBuildHandler into testing mode
//		KojiBuildHandler.inTestingMode = true;
	}
 
	/**
	 * Basic functional test for Koji build tests. This test
	 * uses KojiHubClientStub. This assumes a valid ~/.fedora.cert_tests is
	 * present or system property "eclipseFedoraPackagerTestsCertificate"
	 * is set to the path to a valid .fedora.cert. The latter takes
	 * precedence. This test also requires ~/.fedora-upload-ca.cert and
	 * ~/.fedora-server-ca.cert.  It's up to you to get them somewhere
	 * (fedora-packager-setup, maybe).
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked" })
	@Test
	public void canPushBuildToKoji() throws Exception {
		
		// Set up .fedora.cert, return may be null
		tmpExistingFedoraCert = setupFedoraCert();
		
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
		SWTBot buildDialogBot = buildMsgWindow.bot();
		// Get widget with expected build message
		Widget buildMessageWidget = buildDialogBot.widget(
				WidgetMatcherFactory.allOf(
				WidgetMatcherFactory.withText(
						NLS.bind(org.fedoraproject.eclipse.packager.koji.KojiText.
								KojiMessageDialog_buildNumberMsg, "1337"))));
		assertNotNull(buildMessageWidget);
		buildMsgWindow.close();
	}
	
	@After
	public void tearDown() throws Exception {
		this.efpProject.dispose();
		// clean up some temp .fedora.cert
		if (tmpExistingFedoraCert != null) {
			reestablishFedoraCert();
		}
		// remove ~/.fedora.cert if it didn't exist
		if (!fedoraCertExisted && fedoraCert.exists()) {
			fedoraCert.delete();
		}
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
	
	/**
	 * We need a valid .fedora.cert for doing a successful upload.
	 * This will first look for a system property called
	 * "eclipseFedoraPackagerTestsCertificate" and if set uses the
	 * file pointed to by it as ~/.fedora.cert. If this property
	 * is not set, ~/.fedora.cert_tests will be used instead. If
	 * nothing succeeds, fail.
	 * 
	 * @return A File handle to a copy of an already existing
	 * 			~/.fedora.cert or null if there wasn't any.
	 */
	private File setupFedoraCert() {
		// move away potentially existing ~/.fedora.cert
		File oldFedoraCertMovedAway = null;
		if (fedoraCert.exists()) {
			fedoraCertExisted = true;
			try {
				oldFedoraCertMovedAway = File.createTempFile(".fedora", ".cert");
				FileInputStream fsIn = new FileInputStream(fedoraCert);
				FileOutputStream fsOut = new FileOutputStream(oldFedoraCertMovedAway);
				int buf;
				// copy stuff
				while ( (buf = fsIn.read()) != -1 ) {
					fsOut.write(buf);
				}
				fsIn.close();
				fsOut.close();
			} catch (IOException e) {
				fail("Unable to setup test: (~/.fedora.cert)!");
			}
		}
		// Use template cert to copy to ~/.fedora.cert
		String certTemplatePath = System.getProperty("eclipseFedoraPackagerTestsCertificate");
		if (certTemplatePath == null) {
			// try ~/.fedora.cert_tests
			File fedoraCertTests = new File(System.getProperty("user.home") +
					IPath.SEPARATOR + ".fedora.cert_tests");
			if (fedoraCertTests.exists()) {
				certTemplatePath = fedoraCertTests.getAbsolutePath();
			} else {
				// can't continue - fail
				fail("System property \"eclipseFedoraPackagerTestsCertificate\" " +
						"needs to be configured or ~/.fedora.cert_tests be present" +
						" in order for this test to work.");
			}
		}
		// certTemplatePath must not be null at this point
		assertNotNull(certTemplatePath);
		
		// Copy things over
		File fedoraCertTests = new File(certTemplatePath);
		try {
			FileInputStream fsIn = new FileInputStream(fedoraCertTests);
			FileOutputStream fsOut = new FileOutputStream(fedoraCert);
			int buf;
			// copy stuff
			while ( (buf = fsIn.read()) != -1 ) {
				fsOut.write(buf);
			}
			fsIn.close();
			fsOut.close();
		} catch (IOException e) {
			fail("Unable to setup test: (~/.fedora.cert)!");
		}
		
		// if there was a ~/.fedora.cert return a File handle to it,
		// null otherwise
		if (fedoraCertExisted) {
			return oldFedoraCertMovedAway;
		} else {
			return null;
		}
	}
	
	/**
	 * Reestablish moved away ~/.fedora.cert
	 * 
	 * @param oldFedoraCertMovedAway The File handle to a copy of ~/.fedora.cert
	 * 								 before any tests were run.
	 */
	private void reestablishFedoraCert() {
		// Do this only if old file still exists
		if (tmpExistingFedoraCert.exists()) {
			try {
				FileInputStream fsIn = new FileInputStream(tmpExistingFedoraCert);
				FileOutputStream fsOut = new FileOutputStream(fedoraCert);
				int buf;
				// copy stuff
				while ( (buf = fsIn.read()) != -1 ) {
					fsOut.write(buf);
				}
				fsIn.close();
				fsOut.close();
				// remove temorary file
				tmpExistingFedoraCert.delete();				
			} catch (IOException e) {
				fail("copying back ~/.fedora.cert");
			}
		}
	}
 
}