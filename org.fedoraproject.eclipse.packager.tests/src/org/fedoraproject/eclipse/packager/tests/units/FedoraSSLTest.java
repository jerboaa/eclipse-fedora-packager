/**
 * 
 */
package org.fedoraproject.eclipse.packager.tests.units;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;

import javax.net.ssl.SSLContext;

import org.apache.commons.ssl.KeyMaterial;
import org.fedoraproject.eclipse.packager.FedoraSSL;
import org.junit.Before;
import org.junit.Test;

/**
 * Test Fedora SSL connection setup
 */
public class FedoraSSLTest {

	private FedoraSSL fedoraSSL;
	private final String certFile = System.getProperty("user.home")
			+ File.separatorChar + ".fedora.cert";
	private final String uploadCertFile = System.getProperty("user.home")
			+ File.separatorChar + ".fedora-upload-ca.cert";
	private final String serverCertFile = System.getProperty("user.home")
			+ File.separatorChar + ".fedora-server-ca.cert";
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		File fedoraCert = new File(certFile);
		File fedoraUploadCert = new File(uploadCertFile);
		File fedoraServerCert = new File(serverCertFile);
		if ( !(fedoraCert.exists() && fedoraUploadCert.exists() && fedoraServerCert.exists()) ) {
			fail("Files " + certFile + ", " + uploadCertFile + ", "
					+ serverCertFile
					+ " need to exist for this test to work!");
		}
		this.fedoraSSL = new FedoraSSL(fedoraCert, fedoraUploadCert, fedoraServerCert);
	}

	/**
	 * Test for properly set up Fedora authentication enabled SSL context.
	 */
	@Test
	public void canGetInitializedSSLContext() throws Exception {
		SSLContext ctxt = this.fedoraSSL.getInitializedSSLContext();
		assertNotNull(ctxt);
	}
	
	/**
	 * Test key material retrieval.
	 */
	@Test
	public void canGetKeyMaterial() throws Exception {
		// Get key material for fedora.cert
		KeyMaterial keymat = this.fedoraSSL.getFedoraCertKeyMaterial();
		assertNotNull(keymat);
		assertNotNull(keymat.getKeyStore());
	}

}
