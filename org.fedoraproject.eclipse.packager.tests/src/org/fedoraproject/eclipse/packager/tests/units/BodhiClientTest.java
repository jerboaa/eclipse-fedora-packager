package org.fedoraproject.eclipse.packager.tests.units;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.fedoraproject.eclipse.packager.bodhi.api.BodhiClient;
import org.fedoraproject.eclipse.packager.bodhi.api.IBodhiClient;
import org.junit.Before;
import org.junit.Test;

public class BodhiClientTest {

	private IBodhiClient client;
	
	@Before
	public void setUp() throws Exception {
		URL defaultBodhiURL = null;
		try {
			defaultBodhiURL = new URL(BodhiClient.BODHI_URL);
		} catch (MalformedURLException e) {
			// ignore
		}
		// unfortunately there is no testing server to use
		client = new BodhiClient(defaultBodhiURL);
	}

	@Test
	public void testLogin() throws Exception {
		client.login("jerboaa", "testing");
	}

	@Test
	public void testLogout() {
		fail("Not yet implemented");
	}

	@Test
	public void canCreateNewUpdate() {
		fail("Not yet implemented");
	}

}
