package org.fedoraproject.eclipse.packager.api.errors;


/**
 * Thrown if sources are already present and up-to-date when a {@code DownloadSourcesCommand}
 * is triggered.
 *
 */
public class SourcesUpToDateException extends FedoraPackagerAPIException {
	
	private static final long serialVersionUID = -4530245999844216895L;
	/**
	 * @param message
	 * @param cause
	 */
	public SourcesUpToDateException(String message, Throwable cause) {
		super(message, cause);
	}
	/**
	 * @param message
	 */
	public SourcesUpToDateException(String message) {
		super(message);
	}
}
