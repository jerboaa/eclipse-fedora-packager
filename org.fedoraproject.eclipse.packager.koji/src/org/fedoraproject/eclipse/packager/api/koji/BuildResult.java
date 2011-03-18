package org.fedoraproject.eclipse.packager.api.koji;

import org.fedoraproject.eclipse.packager.api.ICommandResult;

/**
 * Result of a Fedora packager clone operation.
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
