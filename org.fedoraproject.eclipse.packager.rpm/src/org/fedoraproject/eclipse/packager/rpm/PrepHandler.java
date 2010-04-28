package org.fedoraproject.eclipse.packager.rpm;

import java.util.ArrayList;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;

public class PrepHandler extends RPMHandler {

	@Override
	public IStatus doExecute(ExecutionEvent event, IProgressMonitor monitor) throws ExecutionException {
		IStatus result = retrieveSources(event, monitor);
		if (result.isOK()) {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			ArrayList<String> flags = new ArrayList<String>();
			flags.add("--nodeps");
			flags.add("-bp");
			result = rpmBuild(flags, null, monitor); //$NON-NLS-1$
		}	
		
		return result;
	}

	@Override
	protected String getTaskName() {
		return Messages.getString("PrepHandler.1"); //$NON-NLS-1$
	}

}
