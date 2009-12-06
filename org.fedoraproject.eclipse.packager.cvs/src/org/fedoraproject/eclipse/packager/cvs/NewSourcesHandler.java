package org.fedoraproject.eclipse.packager.cvs;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;

public class NewSourcesHandler extends UploadHandler implements IHandler {
	@Override
	public IStatus doExecute(ExecutionEvent event, IProgressMonitor monitor) throws ExecutionException {
		monitor.subTask("Examining resources");
		try {
			existing = getSources();
		} catch (IOException e) {
			e.printStackTrace();
			return handleError(e);
		}

		// get the sources and .cvsignore files
		final File sources;
		final File cvsignore;
		try {
			sources = getFileFor("sources");
			cvsignore = getFileFor(".cvsignore");
		} catch (IOException e) {
			e.printStackTrace();
			return handleError(e);
		}

		// don't add empty files
		final File toAdd = resource.getLocation().toFile();
		if (toAdd.length() == 0) {
			return handleOK(resource.getName() + " is empty", true);
		}
		
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		final String filename = resource.getName();
		// use our Fedora client certificate to start SSL connection
		IStatus result = performUpload(toAdd, filename, monitor);

		if (result.isOK()) {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			monitor.subTask("Updating 'sources' and '.cvsignore'");
			result = updateFiles(sources, cvsignore, toAdd,
					filename, monitor);

		}
		
		return result;
	}

	@Override
	protected IStatus updateCVSIgnore(File cvsignore, File toAdd) {
		return updateCVSIgnore(cvsignore, toAdd, true /* overwrite */);
	}

	@Override
	protected IStatus updateSources(File sources, File toAdd) {
		return updateSources(sources, toAdd, true /* overwrite */);
	}

}
