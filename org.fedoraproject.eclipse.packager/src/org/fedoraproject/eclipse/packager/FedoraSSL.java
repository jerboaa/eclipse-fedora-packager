/*******************************************************************************
 * Copyright (c) 2010 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.fedoraproject.eclipse.packager;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.commons.ssl.KeyMaterial;
import org.apache.commons.ssl.TrustChain;
import org.apache.commons.ssl.TrustMaterial;

/**
 * Helper class for Fedora related SSL things.
 * Uses org.apache.commons.ssl (from not-yet-commons).
 */
public class FedoraSSL {
	
	/**
	 * Default certificate path.
	 */
	public static final String DEFAULT_CERT_FILE = System.getProperty("user.home") //$NON-NLS-1$
			+ File.separatorChar + ".fedora.cert"; //$NON-NLS-1$
	/**
	 * Default upload CA cert.
	 */
	public static final String DEFAULT_UPLOAD_CA_CERT = System.getProperty("user.home") //$NON-NLS-1$
			+ File.separatorChar + ".fedora-upload-ca.cert"; //$NON-NLS-1$
	/**
	 * Default server CA cert.
	 */
	public static final String DEFAULT_SERVER_CA_CERT = System.getProperty("user.home") //$NON-NLS-1$
			+ File.separatorChar + ".fedora-server-ca.cert"; //$NON-NLS-1$
	
	private File fedoraCert;
	private File fedoraUploadCert;
	private File fedoraServerCert;
	
	/**
	 * Create a SSLConnection object.
	 * 
	 * @param fedoraCert
	 * @param fedoraUploadCert
	 * @param fedoraServerCert
	 */
	public FedoraSSL(File fedoraCert, File fedoraUploadCert, File fedoraServerCert) {
		this.fedoraCert = fedoraCert;
		this.fedoraServerCert = fedoraServerCert;
		this.fedoraUploadCert = fedoraUploadCert;
	}
	
	/**
	 * Set up an SSLContext, and initialize it properly.
	 * 
	 * @throws GeneralSecurityException
	 * @throws IOException
	 * @return The initialized SSLConext instance.
	 */
	public SSLContext getInitializedSSLContext() throws GeneralSecurityException,
			IOException {
		TrustChain tc = getTrustChain();

		KeyMaterial kmat = getFedoraCertKeyMaterial();

		SSLContext sc = SSLContext.getInstance("SSL"); //$NON-NLS-1$

		sc.init((KeyManager[]) kmat.getKeyManagers(), (TrustManager[]) tc
				.getTrustManagers(), new java.security.SecureRandom());
		return sc;
	}

	/**
	 * Retrieve key material from fedoraCert as specified by
	 * constructor.
	 * 
	 * @return The key material.
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	public KeyMaterial getFedoraCertKeyMaterial()
			throws GeneralSecurityException, IOException {
		KeyMaterial kmat = new KeyMaterial(fedoraCert, fedoraCert,
				new char[0]);
		return kmat;
	}

	/**
	 * 
	 * @return
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	private TrustChain getTrustChain()
			throws GeneralSecurityException, IOException {
		TrustChain tc = new TrustChain();
		tc.addTrustMaterial(new TrustMaterial(fedoraUploadCert));
		tc.addTrustMaterial(new TrustMaterial(fedoraServerCert));
		return tc;
	}
}
