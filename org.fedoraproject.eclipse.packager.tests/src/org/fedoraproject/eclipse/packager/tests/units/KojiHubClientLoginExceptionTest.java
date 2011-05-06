package org.fedoraproject.eclipse.packager.tests.units;


import static org.junit.Assert.*;

import java.security.GeneralSecurityException;

import org.junit.Before;
import org.junit.Test;
import org.fedoraproject.eclipse.packager.koji.api.errors.*;

public class KojiHubClientLoginExceptionTest {

	private KojiHubClientLoginException exceptionUnderTest;
	
	@Before
	public void setUp() throws Exception {
		exceptionUnderTest = new KojiHubClientLoginException(new GeneralSecurityException());
	}

	/**
	 * We should be able to determine if login exception was due to an expired
	 * certificate. This test requires a valid ~/.fedora.cert
	 * 
	 */
	@Test
	public void testIsCertificateExpired() {
		assertFalse(exceptionUnderTest.isCertificateExpired());
	}

}
