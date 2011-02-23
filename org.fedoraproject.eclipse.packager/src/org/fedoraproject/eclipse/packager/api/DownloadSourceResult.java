package org.fedoraproject.eclipse.packager.api;

/**
 * Represents the result of a {@code DownloadSourceCommand}
 */
public class DownloadSourceResult implements ICommandResult {
	
	private boolean successful = true;

	/**
	 * @param successful the successful to set
	 */
	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	/**
	 * See {@link ICommandResult#wasSuccessful()}.
	 */
	@Override
	public boolean wasSuccessful() {
		return successful;
	}
}
