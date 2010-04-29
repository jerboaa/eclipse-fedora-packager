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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

public class SRPMBuildHandler extends RPMHandler {

	@Override
	public IStatus doExecute(ExecutionEvent event, IProgressMonitor monitor) throws ExecutionException {
		return makeSRPM(event, monitor);
	}

	@Override
	protected String getTaskName() {
		return Messages.getString("SRPMBuildHandler.0"); //$NON-NLS-1$
	}
}
