/**
 * 
 */
package org.fedoraproject.eclipse.packager.tests.units;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.io.File;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.ssl.KeyMaterial;
import org.fedoraproject.eclipse.packager.FedoraSSL;
import org.junit.Before;
import org.junit.Test;

/**
 * Test Fedora SSL connection setup
 */
public class FedoraSSLTest {

	private FedoraSSL sslConnection;
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
		this.sslConnection = new FedoraSSL(fedoraCert, fedoraUploadCert, fedoraServerCert);
	}

	/**
	 * {@link FedoraSSL#initSSLConnection()} sets up defaults for SSL connections. As a result
	 * new HttpsUrlConnection instances must have those defaults set. This is what this test is testing
	 * for. In particular it tests for the default SSLSocketFactory.
	 */
	@Test
	public void isDefaultSSLSocketFactorySet() throws Exception {
		SSLSocketFactory preSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
		assertNotNull(preSocketFactory);
		assertSame(preSocketFactory, HttpsURLConnection.getDefaultSSLSocketFactory());
		// This should set a different default SSLSocketFactory
		this.sslConnection.initSSLConnection();
		SSLSocketFactory postSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
		assertNotNull(postSocketFactory);
		assertNotSame(preSocketFactory, postSocketFactory);
	}
	
	/**
	 * {@link FedoraSSL#initSSLConnection()} sets up defaults for SSL connections. As a result
	 * new HttpsUrlConnection instances must have those defaults set. This is what this test is testing
	 * for. In particular it tests for the default HostNameVerifier.
	 */
	@Test
	public void isDefaultHostNameVerifierSet() throws Exception {
		HostnameVerifier preHostNameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
		assertNotNull(preHostNameVerifier);
		assertSame(preHostNameVerifier, HttpsURLConnection.getDefaultHostnameVerifier());
		// This should set a different default SSLSocketFactory
		this.sslConnection.initSSLConnection();
		HostnameVerifier postHostNameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
		assertNotNull(postHostNameVerifier);
		assertNotSame(preHostNameVerifier, postHostNameVerifier);
	}
	
	/**
	 * Test key material retrieval.
	 */
	@Test
	public void canGetKeyMaterial() throws Exception {
		// Get key material for fedora.cert
		KeyMaterial keymat = this.sslConnection.getFedoraCertKeyMaterial();
		assertNotNull(keymat);
		assertNotNull(keymat.getKeyStore());
	}

}
