package org.fedoraproject.eclipse.packager.git;

/**
 * Utility class for Git constants.
 *
 */
public final class GitConstants {
	
	/**
	 * Protocol prefix for anonymous Git.
	 */
	public static final String ANONYMOUS_PROTOCOL = "git://"; //$NON-NLS-1$
	/**
	 * Protocol prefix for authenticated Git clone operations.
	 */
	public static final String AUTHENTICATED_PROTOCOL = "ssh://"; //$NON-NLS-1$
	/**
	 * Token which separates username from Git URL
	 */
	public static final String USERNAME_SEPARATOR = "@"; //$NON-NLS-1$
	
}
