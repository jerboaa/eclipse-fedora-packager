/**
 * 
 */
package org.fedoraproject.eclipse.packager.rpm.api;

import org.eclipse.core.runtime.IProgressMonitor;
import org.fedoraproject.eclipse.packager.api.FedoraPackagerCommand;
import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerAPIException;

/**
 * Command for building binary RPMs.
 *
 */
public class RpmBuildCommand extends FedoraPackagerCommand<RpmBuildResult> {

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
	public RpmBuildResult call(IProgressMonitor monitor)
			throws FedoraPackagerAPIException {
		// TODO Auto-generated method stub
		return null;
	}

}
