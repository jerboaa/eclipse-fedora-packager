package org.fedoraproject.eclipse.packager.api.errors;

/**
 * Thrown if Eclipse Fedora Packager detected a problem with the current
 * Fedora project root (i.e. either .spec file or sources file is missing).
 */
public class InvalidProjectRootException extends FedoraPackagerAPIException {

	private static final long serialVersionUID = -6133727213256708483L;
	
	/**
	 * @param message
	 */
	public InvalidProjectRootException(String message) {
		super(message);
	}

}
