package org.fedoraproject.eclipse.packager.bodhi.api;

import org.eclipse.core.runtime.IProgressMonitor;
import org.fedoraproject.eclipse.packager.api.FedoraPackagerCommand;
import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerAPIException;

/**
 * Command for pushing an update to Bodhi
 *
 */
public class PushUpdateCommand extends FedoraPackagerCommand<PushUpdateResult> {

	@Override
	protected void checkConfiguration() throws CommandMisconfiguredException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public PushUpdateResult call(IProgressMonitor monitor) throws FedoraPackagerAPIException {
		// TODO Auto-generated method stub
		return null;
	}

}
