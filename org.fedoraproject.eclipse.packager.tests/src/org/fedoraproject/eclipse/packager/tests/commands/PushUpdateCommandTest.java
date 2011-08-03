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
/**
 * 
 */
package org.fedoraproject.eclipse.packager.tests.commands;

import static org.junit.Assert.*;

import java.net.URL;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.fedoraproject.eclipse.packager.IProjectRoot;
import org.fedoraproject.eclipse.packager.api.FedoraPackager;
import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
import org.fedoraproject.eclipse.packager.bodhi.api.BodhiClient;
import org.fedoraproject.eclipse.packager.bodhi.api.IBodhiClient;
import org.fedoraproject.eclipse.packager.bodhi.api.PushUpdateCommand;
import org.fedoraproject.eclipse.packager.bodhi.api.PushUpdateCommand.RequestType;
import org.fedoraproject.eclipse.packager.bodhi.api.PushUpdateCommand.UpdateType;
import org.fedoraproject.eclipse.packager.bodhi.api.PushUpdateResult;
import org.fedoraproject.eclipse.packager.tests.utils.git.GitTestProject;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link PushUpdateCommand}. For this test too succeed, you'll need to have
 * the following Java system properties set:
 * <ul>
 * <li>{@code org.fedoraproject.eclipse.packager.tests.bodhi.testInstanceURL}
 * URL to a local bodhi instance where updates pushing can be tested</li>
 * </ul>
 */
public class PushUpdateCommandTest {

	private static final String BODHI_TEST_INSTANCE_URL_PROP = "org.fedoraproject.eclipse.packager.tests.bodhi.testInstanceURL"; //$NON-NLS-1$
	private static final String BODHI_ADMIN_USERNAME = "guest"; //$NON-NLS-1$
	private static final String BODHI_ADMIN_PASSWORD = "guest"; //$NON-NLS-1$
	private static final String PACKAGE_UPDATE_NVR = "ed-1.5-3.fc15"; //$NON-NLS-1$
	
	// project under test
	private GitTestProject testProject;
	// main interface class
	private FedoraPackager packager;
	// Fedora packager root
	private IProjectRoot fpRoot;
	
	/**
	 * Clone a test project to be used for testing.
	 * 
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.testProject = new GitTestProject("eclipse-fedorapackager");
		this.fpRoot = FedoraPackagerUtils.getProjectRoot((this.testProject
				.getProject()));
		this.packager = new FedoraPackager(fpRoot);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		this.testProject.dispose();
	}

	/**
	 * Test method for 
	 * {@link PushUpdateCommand#checkConfiguration()}.
	 */
	@Test
	public void testCheckConfiguration() throws Exception {
		PushUpdateCommand pushUpdateCommand = (PushUpdateCommand) packager
				.getCommandInstance(PushUpdateCommand.ID);
		try {
			pushUpdateCommand.call(new NullProgressMonitor());
			fail("Should have thrown an exception. Command is not properly configured.");
		} catch (CommandMisconfiguredException e) {
			// pass
		}
	}
	
	/**
	 * Basic test for {@link PushUpdateCommand}.
	 * 
	 * @throws Exception
	 */
	@Test
	public void canPushUpdate() throws Exception {
		PushUpdateCommand pushUpdateCommand = (PushUpdateCommand) packager
				.getCommandInstance(PushUpdateCommand.ID);
		String bodhiTestInstanceURL = System.getProperty(BODHI_TEST_INSTANCE_URL_PROP);
		if (bodhiTestInstanceURL == null) {
			fail(BODHI_TEST_INSTANCE_URL_PROP  + " not set");
		}
		URL bodhiServerURL = new URL(bodhiTestInstanceURL);
		IBodhiClient client = new BodhiClient(bodhiServerURL);
		String[] builds = { PACKAGE_UPDATE_NVR };
		// setup the command and call it
		PushUpdateResult result = pushUpdateCommand.client(client).bugs(PushUpdateCommand.NO_BUGS)
				.usernamePassword(BODHI_ADMIN_USERNAME, BODHI_ADMIN_PASSWORD)
				.comment("Test update. Please disregard!").release("F15").requestType(RequestType.TESTING)
				.updateType(UpdateType.ENHANCEMENT).builds(builds).call(new NullProgressMonitor());
		assertNotNull(result.getUpdateResponse());
		assertTrue(result.wasSuccessful());
		assertEquals("ed", result.getUpdateResponse().getUpdates()[0].getBuilds()[0].getPkg().getName());
		assertEquals(PACKAGE_UPDATE_NVR, result.getUpdateName());
	}

}
