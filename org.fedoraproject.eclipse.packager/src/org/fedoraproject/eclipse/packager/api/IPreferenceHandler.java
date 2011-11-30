package org.fedoraproject.eclipse.packager.api;

/**
 * Handlers requiring a preference, should implement this interface. 
 *
 */
public interface IPreferenceHandler {

	/**
	 * Gets the preference required for proper functioning of the handler.
	 * 
	 * @return The value of the preference. 
	 */
	public String getPreference();
}
