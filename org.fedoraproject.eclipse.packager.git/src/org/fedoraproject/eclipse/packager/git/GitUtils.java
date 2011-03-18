package org.fedoraproject.eclipse.packager.git;


/**
 * Utility class for Fedora Git related things.
 */
public class GitUtils {

	/**
	 * @param gitBaseUrl
	 * @param packageName
	 * @return The full clone URL for the given package.
	 */
	public static String getFullGitURL(String gitBaseUrl, String packageName) {
		return gitBaseUrl + packageName + GitConstants.GIT_REPO_SUFFIX;
	}

	/**
	 * @return The anonymous base URL to clone from.
	 */
	public static String getAnonymousGitBaseUrl() {
		return GitConstants.ANONYMOUS_PROTOCOL
				+ GitPreferencesConstants.DEFAULT_CLONE_BASE_URL;
	}

	/**
	 * @param username
	 * @return The SSH base URL to clone from.
	 */
	public static String getAuthenticatedGitBaseUrl(String username) {
		return GitConstants.AUTHENTICATED_PROTOCOL + username
				+ GitConstants.USERNAME_SEPARATOR
				+ GitPreferencesConstants.DEFAULT_CLONE_BASE_URL;
	}
}
