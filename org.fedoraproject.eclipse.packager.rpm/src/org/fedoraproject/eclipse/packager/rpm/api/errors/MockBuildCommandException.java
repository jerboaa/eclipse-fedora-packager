package org.fedoraproject.eclipse.packager.rpm.api.errors;

import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerAPIException;

/**
 * Thrown if some error during a mock build occurred.
 *
 */
public class MockBuildCommandException extends FedoraPackagerAPIException {

	private static final long serialVersionUID = 8892676010157287621L;

	/**
	 * @param msg
	 * @param cause
	 */
	public MockBuildCommandException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	/**
	 * @param msg
	 */
	public MockBuildCommandException(String msg) {
		super(msg);
	}
}
