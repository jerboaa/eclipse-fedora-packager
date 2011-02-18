package org.fedoraproject.eclipse.packager.api.errors;

/**
 * Superclass of all exceptions thrown by the API classes in
 * {@code org.fedoraproject.eclipse.packager.api}
 *
 */
public class FedoraPackagerAPIException extends Exception {

	private static final long serialVersionUID = -3988956265787343112L;

	FedoraPackagerAPIException(String message, Throwable cause) {
		super(message, cause);
	}

	FedoraPackagerAPIException(String message) {
		super(message);
	}
}
