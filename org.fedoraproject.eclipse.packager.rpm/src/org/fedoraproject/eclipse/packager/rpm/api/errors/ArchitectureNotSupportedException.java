package org.fedoraproject.eclipse.packager.rpm.api.errors;

import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerAPIException;
import org.fedoraproject.eclipse.packager.rpm.api.MockBuildCommand;

/**
 * Thrown if an invalid build architecture was specified.
 * 
 * @see MockBuildCommand
 *
 */
public class ArchitectureNotSupportedException extends
		FedoraPackagerAPIException {

	private static final long serialVersionUID = -1506508744645204460L;

	/**
	 * @param msg
	 */
	public ArchitectureNotSupportedException(String msg) {
		super(msg);
	}
}
