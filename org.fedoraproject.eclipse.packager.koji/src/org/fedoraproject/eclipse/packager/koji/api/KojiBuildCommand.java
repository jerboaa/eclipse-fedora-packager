package org.fedoraproject.eclipse.packager.koji.api;

import org.eclipse.core.runtime.IProgressMonitor;
import org.fedoraproject.eclipse.packager.api.FedoraPackagerCommand;
import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerAPIException;

/**
 * Fedora Packager clone command.
 */
public class KojiBuildCommand extends FedoraPackagerCommand<BuildResult> {

	/**
	 *  The unique ID of this command.
	 */
	public static final String ID = "KojiBuildCommand"; //$NON-NLS-1$
	
	@Override
	protected void checkConfiguration() throws CommandMisconfiguredException {
		
	}

	@Override
	public BuildResult call(IProgressMonitor monitor) throws FedoraPackagerAPIException {
		System.out.println(this.getClass().getName() + " called.");
		return null;
	}

}
