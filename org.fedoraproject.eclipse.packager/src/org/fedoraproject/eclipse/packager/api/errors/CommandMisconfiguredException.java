package org.fedoraproject.eclipse.packager.api.errors;

import org.fedoraproject.eclipse.packager.api.FedoraPackagerCommand;

/**
 * Thrown if a FedoraPackager command was not properly configured prior calling
 * {@link FedoraPackagerCommand#call(org.eclipse.core.runtime.IProgressMonitor)}.
 */
public class CommandMisconfiguredException extends FedoraPackagerAPIException {
	
	private static final long serialVersionUID = 2429930146036876634L;
	/**
	 * @param message
	 * @param cause
	 */
	public CommandMisconfiguredException(String message, Throwable cause) {
		super(message, cause);
	}
	/**
	 * @param message
	 */
	public CommandMisconfiguredException(String message) {
		super(message);
	}
}
