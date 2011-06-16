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
package org.fedoraproject.eclipse.packager.tests;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;

import javax.net.ssl.SSLContext;

import org.apache.commons.ssl.KeyMaterial;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.fedoraproject.eclipse.packager.FedoraSSL;
import org.fedoraproject.eclipse.packager.FedoraSSLFactory;
import org.junit.Before;
import org.junit.Test;

/**
 * Test Fedora SSL connection setup
 */
public class FedoraSSLTest {

	private FedoraSSL expiredFedoraSSL;
	private FedoraSSL anonymousFedoraSSL;
	private static final String CERT_FILE = "resources/fedora-ssl/fedora-example-invalid.cert";
	private static final String UPLOAD_CERT_FILE = "resources/fedora-ssl/fedora-upload-ca.cert";
	private static final String SERVER_CERT_FILE = "resources/fedora-ssl/fedora-server-ca.cert";
	
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		String fedCertName = FileLocator.toFileURL(
				FileLocator.find(TestsPlugin.getDefault().getBundle(),
						new Path(CERT_FILE), null)).getFile();
		String fedUploadCertName = FileLocator.toFileURL(
				FileLocator.find(TestsPlugin.getDefault().getBundle(),
						new Path(UPLOAD_CERT_FILE), null)).getFile();
		String fedServerCertName = FileLocator.toFileURL(
				FileLocator.find(TestsPlugin.getDefault().getBundle(),
						new Path(SERVER_CERT_FILE), null)).getFile();
		File fedoraCert = new File(fedCertName);
		File fedoraUploadCert = new File(fedUploadCertName);
		File fedoraServerCert = new File(fedServerCertName);
		this.expiredFedoraSSL = FedoraSSLFactory.getInstance(fedoraCert,
				fedoraUploadCert, fedoraServerCert);
		this.anonymousFedoraSSL = FedoraSSLFactory.getInstance(new File("/tmp/i_do_not_exist.cert"),
				fedoraUploadCert, fedoraServerCert);
	}

	/**
	 * Test for properly set up Fedora authentication enabled SSL context.
	 */
	@Test
	public void canGetInitializedSSLContext() throws Exception {
		SSLContext ctxt = this.expiredFedoraSSL.getInitializedSSLContext();
		assertNotNull(ctxt);
	}
	
	/**
	 * FileNotFoundException should be thrown if a certificate is missing
	 * and {@link FedoraSSL#getInitializedSSLContext()} or {@link FedoraSSL#getFedoraCertKeyMaterial()} is called.
	 * 
	 */
	@Test
	public void throwsFileNotFoundExceptionIfCertsMissing() throws Exception {
		try {
			anonymousFedoraSSL.getInitializedSSLContext();
			fail("should have thrown FileNotFoundException");
		} catch (FileNotFoundException e) {
			// pass
		}
		try {
			anonymousFedoraSSL.getFedoraCertKeyMaterial();
			fail("should have thrown FileNotFoundException");
		} catch (FileNotFoundException e) {
			// pass
		}
	}
	
	/**
	 * Test key material retrieval.
	 */
	@Test
	public void canGetKeyMaterial() throws Exception {
		// Get key material for fedora.cert
		KeyMaterial keymat = this.expiredFedoraSSL.getFedoraCertKeyMaterial();
		assertNotNull(keymat);
		assertNotNull(keymat.getKeyStore());
	}
	
	@Test
	public void canGetUsernameFromCertificate() throws Exception {
		String username = this.expiredFedoraSSL.getUsernameFromCert();
		assertNotNull(username);
		assertEquals("jerboaa", username);
		username = this.anonymousFedoraSSL.getUsernameFromCert();
		assertNotNull(username);
		assertEquals(FedoraSSL.UNKNOWN_USER, username);
	}
	
	/**
	 * Test for the validity checker. This test requires a valid ~/.fedora.cert
	 *  
	 * @throws Exception
	 */
	@Test
	public void canDetermineCertificateValidity() throws Exception {
		FedoraSSL validFedoraSSL = FedoraSSLFactory.getInstance();
		// should be valid
		assertTrue(validFedoraSSL.isFedoraCertValid());
		// should not be valid
		assertFalse(expiredFedoraSSL.isFedoraCertValid());
	}

}
