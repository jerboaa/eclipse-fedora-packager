package org.fedoraproject.eclipse.packager.bodhi.api.errors;

import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerAPIException;

/**
 * Thrown if bodhi client initialization failed.
 *
 */
public class BodhiClientInitException extends FedoraPackagerAPIException {

	private static final long serialVersionUID = -5959602555911802342L;
	
	/**
	 * @param msg
	 * @param cause
	 */
	public BodhiClientInitException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
