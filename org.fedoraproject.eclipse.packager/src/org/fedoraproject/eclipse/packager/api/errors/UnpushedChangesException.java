package org.fedoraproject.eclipse.packager.api.errors;


/**
 * Thrown if a build has been attempted to be pushed to Koji, but there have
 * been unpushed changes on the current branch of the local repository.
 */
public class UnpushedChangesException extends FedoraPackagerAPIException {

	private static final long serialVersionUID = 8776718118316661590L;

	/**
	 * Default constructor.
	 * 
	 * @param msg
	 */
	public UnpushedChangesException(String msg) {
		super(msg);
	}
}
