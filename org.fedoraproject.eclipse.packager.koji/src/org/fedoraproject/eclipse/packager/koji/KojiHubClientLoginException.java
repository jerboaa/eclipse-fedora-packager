package org.fedoraproject.eclipse.packager.koji;

public class KojiHubClientLoginException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public KojiHubClientLoginException(String msg) {
		super(msg);
	}
	
	public KojiHubClientLoginException(Exception e) {
		super(e);
	}

}
