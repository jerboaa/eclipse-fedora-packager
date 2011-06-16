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
package org.fedoraproject.eclipse.packager;

import java.io.File;

/**
 * Factory for FedoraSSL
 */
public class FedoraSSLFactory {

	/**
	 * Create a new FedoraSSL instance using the default location of
	 * certificates.
	 * 
	 * @return The new instance.
	 */
	public static FedoraSSL getInstance() {
		FedoraSSL newInstance = new FedoraSSL(
				new File(FedoraSSL.DEFAULT_CERT_FILE),
				new File(FedoraSSL.DEFAULT_UPLOAD_CA_CERT),
				new File(FedoraSSL.DEFAULT_SERVER_CA_CERT));
		return newInstance;
	}
	
	/**
	 * Instantiate a Fedora SSL object given the certificate files.
	 * 
	 * @param fedoraCert
	 * @param fedoraUploadCert
	 * @param fedoraServerCert
	 * @return A FedoraSSL instance.
	 */
	public static FedoraSSL getInstance(File fedoraCert, File fedoraUploadCert,
			File fedoraServerCert) {
		return new FedoraSSL(fedoraCert, fedoraUploadCert, fedoraServerCert);
	}
}
