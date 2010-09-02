package org.fedoraproject.eclipse.packager.preferences;

/**
 * Constant definitions for plug-in preferences
 */
public final class PreferencesConstants {

	/***************************************************
	 * Prefences keys
	 **************************************************/

	/**
	 * Preference key for the look-aside download URL
	 */
	public static final String PREF_LOOKASIDE_DOWNLOAD_URL = "lookasideDownloadURL"; //$NON-NLS-1$
	
	/**
	 * URL of the Hub/XMLRPC interface of the build system
	 */
	public static final String PREF_LOOKASIDE_UPLOAD_URL = "lookasideUploadURL"; //$NON-NLS-1$
	
	/***************************************************
	 * Preferences default values
	 **************************************************/
	
	/**
	 * Default URL of the build system's Web interface
	 */
	public static final String DEFAULT_LOOKASIDE_DOWNLOAD_URL = 
		"http://pkgs.fedoraproject.org/repo/pkgs"; //$NON-NLS-1$
	
	/**
	 * Default URL of the build system's Hub/XMLRPC interface
	 */
	public static final String DEFAULT_LOOKASIDE_UPLOAD_URL = 
		"https://pkgs.fedoraproject.org/repo/pkgs/upload.cgi"; //$NON-NLS-1$
}
