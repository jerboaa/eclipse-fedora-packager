package org.fedoraproject.eclipse.packager.api.errors;


/**
 * Thrown if Eclipse Fedora Packager detected checksum errors in downloaded
 * sources.
 */
public class InvalidCheckSumException extends FedoraPackagerAPIException {
	
	private static final long serialVersionUID = -4530245999844216895L;
	/**
	 * @param message
	 * @param cause
	 */
	public InvalidCheckSumException(String message, Throwable cause) {
		super(message, cause);
	}
	/**
	 * @param message
	 */
	public InvalidCheckSumException(String message) {
		super(message);
	}
}
