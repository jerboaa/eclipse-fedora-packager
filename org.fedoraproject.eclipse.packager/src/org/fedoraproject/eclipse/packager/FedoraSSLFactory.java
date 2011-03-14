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
