package org.fedoraproject.eclipse.packager.koji;

/**
 * 
 * Exception raised if some Koji hub-client initialization fails.
 *
 */
public class KojiHubClientInitException extends Exception {

	/**
	 * @param e
	 */
	public KojiHubClientInitException(final Exception e) {
		super(e);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
}
