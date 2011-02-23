package org.fedoraproject.eclipse.packager.api.errors;

/**
 * Thrown if a file with a specific MD5 is already available in
 * the lookaside cache.
 */
public class FileAvailableInLookasideCacheException extends
		FedoraPackagerAPIException {

	private static final long serialVersionUID = -1682359355021803771L;

	FileAvailableInLookasideCacheException(String message, Throwable cause) {
		super(message, cause);
	}

	FileAvailableInLookasideCacheException(String message) {
		super(message);
	}
}
