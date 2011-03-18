package org.fedoraproject.eclipse.packager.tests.utils;

import org.fedoraproject.eclipse.packager.api.ICommandResult;

/**
 * Fixture for {@link FedoraPackagerCommandDummyImpl}.
 *
 */
public class DummyResult implements ICommandResult {

	private boolean overallStatus;
	
	public DummyResult(boolean status) {
		this.overallStatus = status;
	}
	
	@Override
	public boolean wasSuccessful() {
		return overallStatus;
	}

}
