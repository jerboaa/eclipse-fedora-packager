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
package org.fedoraproject.eclipse.packager.rpm;

import java.util.ArrayList;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;

public class LocalBuildHandler extends RPMHandler {

	@Override
	public IStatus doExecute(ExecutionEvent event, IProgressMonitor monitor) throws ExecutionException {
		IStatus result = retrieveSources(monitor);
		if (result.isOK()) {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			try {
				// search for noarch directive, otherwise use local arch
				final String arch = rpmQuery(specfile, "ARCH"); //$NON-NLS-1$
				
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				// perform rpmbuild
				ArrayList<String> flags = new ArrayList<String>();
				flags.add("--target");
				flags.add(arch);
				flags.add("-ba");
				result = rpmBuild(flags, //$NON-NLS-1$ //$NON-NLS-2$
						monitor);

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
