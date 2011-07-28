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
package org.fedoraproject.eclipse.packager.tests.units;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.fedoraproject.eclipse.packager.bodhi.api.BodhiClient;
import org.fedoraproject.eclipse.packager.bodhi.api.BodhiLoginResponse;
import org.fedoraproject.eclipse.packager.bodhi.api.BodhiUpdateResponse;
import org.fedoraproject.eclipse.packager.bodhi.api.errors.BodhiClientLoginException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the bodhi client. For this test too succeed, you'll need to have
 * the following Java system properties set:
 * <ul>
 * <li>{@code org.fedoraproject.eclipse.packager.tests.bodhi.fasUsername} a
 * valid FAS username to test logins</li>
 * <li>{@code org.fedoraproject.eclipse.packager.tests.bodhi.fasPassword}
 * corresponding password to above login</li>
 * <li>{@code org.fedoraproject.eclipse.packager.tests.bodhi.testInstanceURL}
 * URL to a local bodhi instance where updates pushing can be tested</li>
 * </ul>
 */
public class BodhiClientTest {

	private BodhiClient client;
	private URL bodhiUrl;
	
	// URL should end with '/'
	private static final String BODHI_STAGING = "https://admin.stg.fedoraproject.org/updates/"; //$NON-NLS-1$
	private static final String BODHI_ADMIN_USERNAME = "guest"; //$NON-NLS-1$
	private static final String BODHI_ADMIN_PASSWORD = "guest"; //$NON-NLS-1$
	private static final String PACKAGE_UPDATE_NVR = "ed-1.5-3.fc15"; //$NON-NLS-1$
	private static final String FAS_USERNAME_PROP = "org.fedoraproject.eclipse.packager.tests.bodhi.fasUsername"; //$NON-NLS-1$
	private static final String FAS_PASSWORD_PROP = "org.fedoraproject.eclipse.packager.tests.bodhi.fasPassword"; //$NON-NLS-1$
	private static final String BODHI_TEST_INSTANCE_URL_PROP = "org.fedoraproject.eclipse.packager.tests.bodhi.testInstanceURL"; //$NON-NLS-1$
	
	@Before
	public void setUp() throws Exception {
		try {
			bodhiUrl = new URL(BODHI_STAGING);
		} catch (MalformedURLException e) {
			// ignore
		}
	}
	
	@After
	public void tearDown() {
		try {
			client.logout();
		} catch (Exception e) {
			// don't care
		}
	}

	@Test
	public void testLogin() throws Exception {
		// require fas username
		String fasUsername = System.getProperty(FAS_USERNAME_PROP);
		if (fasUsername == null) {
			fail(FAS_USERNAME_PROP + " not set");
		}
		// require password
		String fasPassword = System.getProperty(FAS_PASSWORD_PROP);
		if (fasPassword == null) {
			fail(FAS_PASSWORD_PROP + " not set");
		}
		// use bodhi staging
		client = new BodhiClient(this.bodhiUrl);
		try {
			client.login("invalid-user", "invalid-password");
			fail("Should have thrown a login exception");
		} catch (BodhiClientLoginException e) {
			assertTrue(e.isInvalidCredentials());
		}
		client.shutDownConnection();
		client = new BodhiClient(this.bodhiUrl);
		BodhiLoginResponse resp = null;
		try {
			resp = client.login(fasUsername, fasPassword);
			return;
		} catch (Exception e) {
			fail("Should not have thrown any exception!");
		}
		assertNotNull(resp);
		assertEquals(fasUsername, resp.getUser().getUsername());
		assertNotNull(resp.getCsrfToken());
	}

	@Test
	public void testLogout() throws Exception {
		// require fas username
		String fasUsername = System.getProperty(FAS_USERNAME_PROP);
		if (fasUsername == null) {
			fail(FAS_USERNAME_PROP  + " not set");
		}
		// require password
		String fasPassword = System.getProperty(FAS_PASSWORD_PROP);
		if (fasPassword == null) {
			fail(FAS_PASSWORD_PROP  + " not set");
		}
		client = new BodhiClient(this.bodhiUrl);
		client.login(fasUsername, fasPassword);
		client.logout();
	}

	/**
	 * Test updates pushing. This will only work if the build in question has
	 * been built in the Koji instance which is connected to the Bodhi test
	 * instance.
	 * 
	 * @throws Exception
	 */
	@Test
	public void canCreateNewUpdate() throws Exception {
		String bodhiTestInstanceURL = System.getProperty(BODHI_TEST_INSTANCE_URL_PROP);
		if (bodhiTestInstanceURL == null) {
			fail(BODHI_TEST_INSTANCE_URL_PROP  + " not set");
		}
		URL bodhiTestUrl = new URL(bodhiTestInstanceURL);
		client = new BodhiClient(bodhiTestUrl);
		BodhiLoginResponse resp = client.login(BODHI_ADMIN_USERNAME, BODHI_ADMIN_PASSWORD);
		// sanity check
		assertNotNull(BODHI_ADMIN_PASSWORD, resp.getUser().getPassword());
		// push the update
		String[] builds = { PACKAGE_UPDATE_NVR };
		BodhiUpdateResponse updateResponse = client.createNewUpdate(builds,
				"F15", "enhancement", "testing", "",
				"This is a test. Please disregard", "", false, true, 3, -3, true);
		assertEquals("Update successfully created", updateResponse.getFlashMsg());
		assertEquals("ed", updateResponse.getUpdates()[0].getBuilds()[0].getPkg().getName());
	}

}
