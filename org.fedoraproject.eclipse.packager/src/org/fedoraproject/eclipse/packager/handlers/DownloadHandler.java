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
package org.fedoraproject.eclipse.packager.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.Messages;

/**
 * Class responsible for downloading source files
 * 
 * @author Red Hat inc.
 *
 */
public class DownloadHandler extends WGetHandler {
	
	public IStatus doExecute(FedoraProjectRoot fedoraProjectRoot, IProgressMonitor monitor) {
		return retrieveSources(fedoraProjectRoot, monitor);
	}

	@Override
	protected String getTaskName() {
		return Messages.getString("DownloadHandler.0"); //$NON-NLS-1$
	}

	@Override
	public IStatus doExecute(ExecutionEvent event, IProgressMonitor monitor)
			throws ExecutionException {
		// TODO Remove once every handler handles it execute directly
		return null;
	}

}
