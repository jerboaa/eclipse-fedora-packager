package org.fedoraproject.eclipse.packager.koji.api;

/**
 * Exceptions thrown by koji clients.
 */
public class KojiHubClientException extends Exception {

	private static final long serialVersionUID = -3622365505538797782L;

	/**
	 * Default constructor
	 */
	public KojiHubClientException() {
		// empty
	}
	
	/**
	 * @param cause
	 */
	public KojiHubClientException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * @param msg
	 */
	public KojiHubClientException(String msg) {
		super(msg);
	}
}
