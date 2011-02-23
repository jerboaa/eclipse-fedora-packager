package org.fedoraproject.eclipse.packager.api.errors;


/**
 * Thrown if Eclipse Fedora Packager failed to download a source file as 
 * listed in the sources file.
 */
public class DownloadFailedException extends FedoraPackagerAPIException {
	
	private static final long serialVersionUID = -4530245999844216895L;
	/**
	 * @param message
	 * @param cause
	 */
	public DownloadFailedException(String message, Throwable cause) {
		super(message, cause);
	}
	/**
	 * @param message
	 */
	public DownloadFailedException(String message) {
		super(message);
	}
}
