package org.fedoraproject.eclipse.packager.tests.units;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;

import org.fedoraproject.eclipse.packager.koji.KojiHubClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for the Koji hub client class
 *
 */
public class KojiHubClientTest {

	private KojiHubClient kojiClient;
	
	@Before
	public void setUp() throws Exception {
		// a bare SSL Koji client
		kojiClient = new KojiHubClient();
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
		// Use SSL enabled server for SSL login
		String sslKojiHostURL = "https://koji.fedoraproject.org/kojihub";
		kojiClient.setHubUrl(sslKojiHostURL);
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
