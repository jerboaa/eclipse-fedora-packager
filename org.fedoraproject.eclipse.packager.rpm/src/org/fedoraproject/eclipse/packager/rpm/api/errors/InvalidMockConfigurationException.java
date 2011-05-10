package org.fedoraproject.eclipse.packager.rpm.api.errors;

import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerAPIException;

/**
 * Thrown if an invalid mock configuration was specified.
 *
 */
public class InvalidMockConfigurationException extends FedoraPackagerAPIException {

	private static final long serialVersionUID = -1807148158333426036L;
	
	/**
	 * 
	 * @param msg
	 */
	public InvalidMockConfigurationException(String msg) {
		super(msg);
	}

}
