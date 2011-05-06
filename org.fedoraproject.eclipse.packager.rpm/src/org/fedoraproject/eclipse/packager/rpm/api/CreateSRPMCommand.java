package org.fedoraproject.eclipse.packager.rpm.api;

import org.eclipse.core.runtime.IProgressMonitor;
import org.fedoraproject.eclipse.packager.api.FedoraPackagerCommand;
import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerAPIException;

/**
 * Fedora packager command for creating source RPMs.
 *
 */
public class CreateSRPMCommand extends FedoraPackagerCommand<CreateSRPMResult> {

	/* (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.api.FedoraPackagerCommand#checkConfiguration()
	 */
	@Override
	protected void checkConfiguration() throws CommandMisconfiguredException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.api.FedoraPackagerCommand#call(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public CreateSRPMResult call(IProgressMonitor monitor)
			throws FedoraPackagerAPIException {
		// TODO Auto-generated method stub
		return null;
	}

}
