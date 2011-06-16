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

import java.util.HashMap;

import org.fedoraproject.eclipse.packager.koji.api.IKojiHubClient;
import org.fedoraproject.eclipse.packager.koji.api.KojiBuildInfo;
import org.fedoraproject.eclipse.packager.koji.api.KojiSSLHubClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for the Koji hub client class. Note that for a successful run of
 * these tests, one has to have valid Fedora certificates in ~/
 * 
 */
public class KojiSSLHubClientTest {
	
	/**
	 * Name-version-release of some successful build.
	 */
	private static final String EFP_NVR = "eclipse-fedorapackager-0.1.12-1.fc15";
	/**
	 * Some known to be working scm-URL
	 */
	private static final String EFP_SCM_URL = "git://pkgs.fedoraproject.org/eclipse-fedorapackager.git?#302d36c1427a0d8578d0a1d88b4c9337a4407dde";

	private IKojiHubClient kojiClient;
	
	@Before
	public void setUp() throws Exception {
		// a bare SSL Koji client
		kojiClient = new KojiSSLHubClient("https://koji.fedoraproject.org/kojihub");
	}

	@After
	public void tearDown() throws Exception {
		kojiClient.logout();
	}
	
	/**
	 * Log on to Koji using SSL authentication.
	 * This test requires proper certificates to be set up.
	 * 
	 * @throws Exception
	 */
	@Test
	public void canLoginUsingSSLCertificate() throws Exception {
		// Logging in to koji should return session data
		HashMap<?, ?> sessionData = kojiClient.login();
		assertNotNull(sessionData);
		assertNotNull(sessionData.get("session-id"));
		assertTrue(sessionData.get("session-id") instanceof Integer);
		assertNotNull(sessionData.get("session-key"));
	}
	
	/**
	 * Get build info test.
	 * 
	 * @throws Exception
	 */
	@Test
	public void canGetBuildInfo() throws Exception {
		// First log in
		HashMap<?, ?> sessionData = kojiClient.login();
		assertNotNull(sessionData);
		// get build info for eclipse-fedorapackager-0.1.12-1.fc15
		KojiBuildInfo info = kojiClient.getBuild(EFP_NVR);
		assertNotNull(info);
		assertTrue(info.getRelease().equals("1.fc15"));
		assertTrue(info.getPackageName().equals("eclipse-fedorapackager"));
		assertTrue(info.getVersion().equals("0.1.12"));
		assertTrue(info.isComplete());
	}
	
	/**
	 * Push scratch build test.
	 * 
	 * @throws Exception
	 */
	@Test
	public void canPushScratchBuild() throws Exception {
		// Log in first
		HashMap<?, ?> sessionData = kojiClient.login();
		assertNotNull(sessionData);
		// get build info for eclipse-fedorapackager-0.1.13-fc15
		boolean isScratchBuild = true;
		int taskId = kojiClient.build("dist-rawhide", EFP_SCM_URL, EFP_NVR, isScratchBuild);
		System.out.println("Pushed task ID: " + taskId);
		assertNotNull(taskId);
	}

}
