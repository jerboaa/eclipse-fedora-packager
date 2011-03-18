package org.fedoraproject.eclipse.packager.api.errors;

import org.fedoraproject.eclipse.packager.api.FedoraPackager;

/**
 * Thrown if an Eclipse Fedora Packager command was not found in the registry.
 * @see FedoraPackager 
 *
 */
public class FedoraPackagerCommandNotFoundException extends
		FedoraPackagerAPIException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -596745668287575312L;

	/**
	 * @param message
	 */
	public FedoraPackagerCommandNotFoundException(String message) {
		super(message);
	}
}
