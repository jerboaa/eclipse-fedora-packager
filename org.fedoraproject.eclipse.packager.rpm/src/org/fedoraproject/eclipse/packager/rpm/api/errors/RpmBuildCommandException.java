package org.fedoraproject.eclipse.packager.rpm.api.errors;

import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerAPIException;

/**
 * Thrown when something caused an RPM build command to fail.
 *
 */
public class RpmBuildCommandException extends FedoraPackagerAPIException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1189026188243127676L;

	/**
	 * 
	 * @param msg A message indicating what went wrong.
	 * @param cause The actual cause.
	 */
	public RpmBuildCommandException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
