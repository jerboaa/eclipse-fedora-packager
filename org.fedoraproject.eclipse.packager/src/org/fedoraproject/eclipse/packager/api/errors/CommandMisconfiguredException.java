package org.fedoraproject.eclipse.packager.api.errors;


/**
 * Thrown if a FedoraPackager command was not properly configured prior calling
 * {@code FedoraPackager#call()}.
 */
public class CommandMisconfiguredException extends FedoraPackagerAPIException {
	
	private static final long serialVersionUID = 2429930146036876634L;
	/**
	 * @param message
	 * @param cause
	 */
	public CommandMisconfiguredException(String message, Throwable cause) {
		super(message, cause);
	}
	/**
	 * @param message
	 */
	public CommandMisconfiguredException(String message) {
		super(message);
	}
}
