package org.fedoraproject.eclipse.packager.rpm;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

public class SRPMBuildHandler extends RPMHandler implements IHandler {

	@Override
	public IStatus doExecute(ExecutionEvent event, IProgressMonitor monitor) throws ExecutionException {
		return makeSRPM(event, monitor);
	}

	@Override
	protected String getTaskName() {
		return Messages.getString("SRPMBuildHandler.0"); //$NON-NLS-1$
	}
}
