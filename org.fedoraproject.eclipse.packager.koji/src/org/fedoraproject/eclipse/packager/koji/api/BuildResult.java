package org.fedoraproject.eclipse.packager.koji.api;

import org.fedoraproject.eclipse.packager.api.ICommandResult;

/**
 * Result of a koji build as triggered by KojiBuildCommand.
 */
public class BuildResult implements ICommandResult {

	/*
	 * (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.api.ICommandResult#wasSuccessful()
	 */
	@Override
	public boolean wasSuccessful() {
		return false;
	}

}
