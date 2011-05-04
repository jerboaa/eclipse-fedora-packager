package org.fedoraproject.eclipse.packager.koji.api;

/**
 * Exceptions thrown by koji clients.
 */
public class KojiClientException extends Exception {

	private static final long serialVersionUID = -3622365505538797782L;

	/**
	 * Default constructor
	 */
	public KojiClientException() {
		// empty
	}
	
	/**
	 * @param cause
	 */
	public KojiClientException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * @param msg
	 */
	public KojiClientException(String msg) {
		super(msg);
	}
}
