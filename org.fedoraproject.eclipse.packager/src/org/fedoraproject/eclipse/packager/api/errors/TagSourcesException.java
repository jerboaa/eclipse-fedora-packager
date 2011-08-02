package org.fedoraproject.eclipse.packager.api.errors;


/**
 * Thrown if CVS tagging of sources failed.
 *
 */
public class TagSourcesException extends FedoraPackagerAPIException {

	private static final long serialVersionUID = -7183217099259208591L;

	/**
	 * @param msg
	 */
	public TagSourcesException(String msg) {
		super(msg);
	}
	
	/**
	 * @param msg
	 * @param cause
	 */
	public TagSourcesException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
