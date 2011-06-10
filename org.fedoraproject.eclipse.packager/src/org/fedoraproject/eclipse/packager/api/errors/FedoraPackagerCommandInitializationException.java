package org.fedoraproject.eclipse.packager.api.errors;

/**
 * Exception thrown if packager command initialization failed.
 */
public class FedoraPackagerCommandInitializationException extends
		FedoraPackagerExtensionPointException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8053342144830979684L;
	
	/**
	 * @param message
	 * @param cause
	 */
	public FedoraPackagerCommandInitializationException(String message, Throwable cause) {
		super(message, cause);
	}
}
