package org.fedoraproject.eclipse.packager.api.errors;

/**
 * Thrown if the plug-in failed to instantiate an extension point provided class.
 *
 */
public class FedoraPackagerExtensionPointException extends FedoraPackagerAPIException {

	private static final long serialVersionUID = 1317581835065207671L;

	/**
	 * @param message
	 * @param cause
	 */
	public FedoraPackagerExtensionPointException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * @param message
	 */
	public FedoraPackagerExtensionPointException(String message) {
		super(message);
	}
}
