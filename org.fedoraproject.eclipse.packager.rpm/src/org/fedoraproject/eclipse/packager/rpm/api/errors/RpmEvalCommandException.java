package org.fedoraproject.eclipse.packager.rpm.api.errors;

import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerAPIException;
import org.fedoraproject.eclipse.packager.rpm.RpmText;
import org.fedoraproject.eclipse.packager.rpm.api.RpmEvalCommand;

/**
 * Thrown if {@link RpmEvalCommand} failed.
 *
 */
public class RpmEvalCommandException extends FedoraPackagerAPIException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2967309558080827035L;

	/**
	 * @param cause
	 */
	public RpmEvalCommandException(Throwable cause) {
		super(RpmText.RpmEvalCommandException_msg, cause);
	}
	
}
