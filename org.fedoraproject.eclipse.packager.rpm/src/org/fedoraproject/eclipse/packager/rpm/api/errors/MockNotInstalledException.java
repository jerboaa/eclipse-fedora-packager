package org.fedoraproject.eclipse.packager.rpm.api.errors;

import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerAPIException;
import org.fedoraproject.eclipse.packager.rpm.RpmText;
import org.fedoraproject.eclipse.packager.rpm.api.MockBuildCommand;

/**
 * Thrown if {@link MockBuildCommand} determined that the mock program is not
 * installed. 
 *
 */
public class MockNotInstalledException extends FedoraPackagerAPIException {

	private static final long serialVersionUID = 5103090592551146652L;

	/**
	 * default constructor
	 */
	public MockNotInstalledException() {
		super(RpmText.MockNotInstalledException_msg);
	}
}
