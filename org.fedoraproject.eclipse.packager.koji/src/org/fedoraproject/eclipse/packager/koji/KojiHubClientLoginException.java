package org.fedoraproject.eclipse.packager.koji;

/**
 * @author Redhat Inc.
 *
 */
public class KojiHubClientLoginException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * @param msg
	 */
	public KojiHubClientLoginException(String msg) {
		super(msg);
	}
	
	
	/**
	 * @param e
	 */
	public KojiHubClientLoginException(Exception e) {
		super(e);
	}

}
