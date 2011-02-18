package org.fedoraproject.eclipse.packager.api.errors;

/**
 * Thrown if a file with a specific MD5 is still missing from the
 * lookaside cache.
 */
public class FileMissingFromLookasideCacheException extends
		FedoraPackagerAPIException {

	private static final long serialVersionUID = -1682359355021803771L;

	FileMissingFromLookasideCacheException(String message, Throwable cause) {
		super(message, cause);
	}

	FileMissingFromLookasideCacheException(String message) {
		super(message);
	}
}
