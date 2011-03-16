package org.fedoraproject.eclipse.packager.api.errors;

/**
 * Thrown if Eclipse Fedora Packager detected errors while updating the VCS
 * ignore file.
 */
public class VCSIgnoreFileUpdateException extends FedoraPackagerAPIException {
	
	private static final long serialVersionUID = 960289323631735201L;
	
	/**
	 * @param message
	 * @param cause
	 */
	public VCSIgnoreFileUpdateException(String message, Throwable cause) {
		super(message, cause);
	}
	/**
	 * @param message
	 */
	public VCSIgnoreFileUpdateException(String message) {
		super(message);
	}
}
