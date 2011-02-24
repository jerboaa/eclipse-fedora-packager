package org.fedoraproject.eclipse.packager.api.errors;

/**
 * Common exception superclass for command listeners.
 *
 */
public class CommandListenerException extends FedoraPackagerAPIException {

	private static final long serialVersionUID = -8660306046206103772L;

	/**
	 * Wrap a listener exception in a CommandListenerException. 
	 * 
	 * @param cause
	 */
	public CommandListenerException(Throwable cause) {
		super("unused", cause); //$NON-NLS-1$
	}
}
