package org.fedoraproject.eclipse.packager.cvs;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.fedoraproject.eclipse.packager.rpm.RPMHandler;

public class DownloadHandler extends RPMHandler {
	@Override
	public IStatus doExecute(ExecutionEvent event, IProgressMonitor monitor) throws ExecutionException {
		return retrieveSources(event, monitor);
	}

	@Override
	protected String getTaskName() {
		return Messages.getString("DownloadHandler.0"); //$NON-NLS-1$
	}
}
