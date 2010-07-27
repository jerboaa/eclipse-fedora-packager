/*******************************************************************************
 * Copyright (c) 2010 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.fedoraproject.eclipse.packager.cvs;

import java.util.Set;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.fedoraproject.eclipse.packager.rpm.Messages;
import org.fedoraproject.eclipse.packager.rpm.RPMHandler;

public class DownloadHandler extends RPMHandler {
	@Override
	public IStatus doExecute(ExecutionEvent event, IProgressMonitor monitor) throws ExecutionException {
		return retrieveSources(monitor);
	}

	@Override
	protected String getTaskName() {
		return Messages.getString("DownloadHandler.0"); //$NON-NLS-1$
	}
	
	protected IStatus retrieveSources(IProgressMonitor monitor) {
		sources = getSources();

		Set<String> sourcesToGet = sources.keySet();

		// check md5sum of any local sources
		checkSources(sourcesToGet);

		if (sourcesToGet.isEmpty()) {
			return handleOK(Messages.getString("RPMHandler.3"), false); //$NON-NLS-1$
		}

		// Need to download remaining sources from repo
		IStatus status = null;
		for (final String source : sourcesToGet) {
			monitor.subTask(NLS.bind(Messages.getString("RPMHandler.4"), source)); //$NON-NLS-1$
			final String url = repo + "/" + specfile.getProject().getName() //$NON-NLS-1$
					+ "/" + source + "/" + sources.get(source) + "/" + source; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			status = download(url, source, monitor);
			if (!status.isOK()) {
				// download failed
				deleteSource(source);
				break;
			}
		}

		if (!status.isOK()) {
			return handleError(status.getMessage());
		}

		// sources downloaded successfully, check MD5
		checkSources(sourcesToGet);

		// if all checks pass we should have an empty list
		if (!sources.isEmpty()) {
			String failedSources = ""; //$NON-NLS-1$
			for (String source : sources.keySet()) {
				failedSources += source + '\n';
			}
			return handleError(Messages.getString("RPMHandler.10") //$NON-NLS-1$
					+ failedSources);
		} else {
			return Status.OK_STATUS;
		}
	}
	
	private void deleteSource(String file) {
		IContainer branch = specfile.getParent();
		IResource toDelete = branch.findMember(file);
		if (toDelete != null) {
			try {
				toDelete.delete(true, null);
			} catch (CoreException e) {
				e.printStackTrace();
				handleError(e);
			}
		}
	}
}
