package org.fedoraproject.eclipse.packager.api.errors;

/**
 * Thrown if Eclipse Fedora Packager detected errors while updating the
 * {@code sources} file.
 */
public class SourcesFileUpdateException extends FedoraPackagerAPIException {
	
	private static final long serialVersionUID = -2705064305397582135L;
	
	/**
	 * @param message
	 * @param cause
	 */
	public SourcesFileUpdateException(String message, Throwable cause) {
		super(message, cause);
	}
	/**
	 * @param message
	 */
	public SourcesFileUpdateException(String message) {
		super(message);
	}
}
