package org.fedoraproject.eclipse.packager.rpm.api.errors;

import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerAPIException;

/**
 * Thrown if user attempting to do a mock build is not part of the system
 * group "mock".
 *
 */
public class UserNotInMockGroupException extends FedoraPackagerAPIException {

	private static final long serialVersionUID = 7699978530785831168L;

	/**
	 * 
	 * @param msg
	 */
	public UserNotInMockGroupException(String msg) {
		super(msg);
	}
}
