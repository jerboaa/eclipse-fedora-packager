package org.fedoraproject.eclipse.packager.cvs;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.fedoraproject.eclipse.packager.CommonHandler;

public class TagHandler extends CommonHandler {

	@Override
	public IStatus doExecute(ExecutionEvent event, IProgressMonitor monitor) throws ExecutionException {
		IStatus result = doTag(monitor);

		return result;
	}

	@Override
	protected String getTaskName() {
		return "Tagging Branch";
	}
}
