package org.fedoraproject.eclipse.packager.rpm;

import java.io.File;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;

public class LocalBuildHandler extends RPMHandler {

	@Override
	public IStatus doExecute(ExecutionEvent event, IProgressMonitor monitor) throws ExecutionException {
		IStatus result = retrieveSources(event, monitor);
		if (result.isOK()) {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			try {
				// search for noarch directive, otherwise use local arch
				final String arch = rpmQuery(specfile, "ARCH"); //$NON-NLS-1$
				final String log;

				log = specfile.getParent().getLocation().toOSString()
				+ Path.SEPARATOR + ".build-" //$NON-NLS-1$
				+ rpmQuery(specfile, "VERSION") + "-" //$NON-NLS-1$ //$NON-NLS-2$
				+ rpmQuery(specfile, "RELEASE") + ".log"; //$NON-NLS-1$ //$NON-NLS-2$

				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				// perform rpmbuild
				result = rpmBuild("--target " + arch + " -ba", //$NON-NLS-1$ //$NON-NLS-2$
						new File(log), monitor);

			} catch (CoreException e) {
				e.printStackTrace();
				result = handleError(e);
			}
		}

		return result;
	}

	@Override
	protected String getTaskName() {
		return Messages.getString("LocalBuildHandler.8"); //$NON-NLS-1$
	}
}
