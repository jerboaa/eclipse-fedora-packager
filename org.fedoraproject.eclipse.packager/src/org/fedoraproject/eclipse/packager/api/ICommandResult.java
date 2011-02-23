package org.fedoraproject.eclipse.packager.api;

/**
 * Common interfaces for FedoraPackagerCommand results.
 *
 */
public interface ICommandResult {

	/**
	 * Method to determine if a command was overall successful.
	 * 
	 * @return The overall success status. 
	 */
	public boolean wasSuccessful();
}
