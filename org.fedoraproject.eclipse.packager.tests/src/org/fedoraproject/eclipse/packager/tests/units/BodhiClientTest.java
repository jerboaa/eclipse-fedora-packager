package org.fedoraproject.eclipse.packager.tests.units;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.fedoraproject.eclipse.packager.bodhi.api.BodhiClient;
import org.fedoraproject.eclipse.packager.bodhi.api.BodhiLoginResponse;
import org.fedoraproject.eclipse.packager.bodhi.api.errors.BodhiClientLoginException;
import org.junit.Before;
import org.junit.Test;

public class BodhiClientTest {

	private BodhiClient client;
	private URL bodhiUrl;
	
	// URL should end with '/'
	private static final String BODHI_STAGING = "https://admin.stg.fedoraproject.org/updates/";
	
	@Before
	public void setUp() throws Exception {
		try {
			bodhiUrl = new URL(BODHI_STAGING);
		} catch (MalformedURLException e) {
			// ignore
		}
	}

	@Test
	public void testLogin() throws Exception {
		// unfortunately there is no testing server to use
		client = new BodhiClient(this.bodhiUrl);
		try {
			client.login("invalid-user", "invalid-password");
			fail("Should have thrown a login exception");
		} catch (BodhiClientLoginException e) {
			assertTrue(e.isInvalidCredentials());
		}
		client.shutDownConnection();
		client = new BodhiClient(this.bodhiUrl);
		BodhiLoginResponse resp = client.login("jerboaa", "xxxx");
		assertEquals("jerboaa", resp.getUser().getUsername());
	}

	@Test
	public void testLogout() throws Exception {
		client = new BodhiClient(this.bodhiUrl);
		client.login("jerboaa", "xxxx");
		client.logout();
	}

	@Test
	public void canCreateNewUpdate() {
		fail("Not yet implemented");
	}

}
