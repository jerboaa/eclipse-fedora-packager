package org.fedoraproject.eclipse.packager.tests.units;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;

import org.fedoraproject.eclipse.packager.koji.api.IKojiHubClient;
import org.fedoraproject.eclipse.packager.koji.api.KojiSSLHubClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for the Koji hub client class. Note that for a successful run of
 * this test, one has to have valid Fedora certificates in ~/
 * 
 */
public class KojiHubClientTest {

	private IKojiHubClient kojiClient;
	
	@Before
	public void setUp() throws Exception {
		// a bare SSL Koji client
		kojiClient = new KojiSSLHubClient("https://koji.fedoraproject.org/kojihub");
	}

	@After
	public void tearDown() throws Exception {
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
		assertNotNull(new Integer((Integer)sessionData.get("session-id")));
		assertNotNull(sessionData.get("session-key"));
		/*System.out.println("Session id (SSL): " + sessionData.get("session-id"));
		System.out.println("Session key (SSL): " + sessionData.get("session-key"));*/
	}

}
