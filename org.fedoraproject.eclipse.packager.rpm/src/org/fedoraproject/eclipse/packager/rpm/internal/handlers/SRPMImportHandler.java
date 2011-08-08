/*******************************************************************************
 * Copyright (c) 2010-2011 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.fedoraproject.eclipse.packager.rpm.internal.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Shell;
import org.fedoraproject.eclipse.packager.api.FedoraPackagerAbstractHandler;
import org.fedoraproject.eclipse.packager.api.FileDialogRunable;
import org.fedoraproject.eclipse.packager.rpm.RpmText;
import org.fedoraproject.eclipse.packager.rpm.api.SRPMImportJob;
import org.fedoraproject.eclipse.packager.utils.FedoraHandlerUtils;

/**
 * Import handler for SRPMImportCommand
 * 
 */
public class SRPMImportHandler extends FedoraPackagerAbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = getShell(event);
		IResource eventResource = FedoraHandlerUtils.getResource(event);
		FileDialogRunable fdr = new FileDialogRunable("*.src.rpm", //$NON-NLS-1$
				RpmText.SRPMImportHandler_FileDialogTitle);
		shell.getDisplay().syncExec(fdr);
		String srpm = fdr.getFile();
		if (srpm == null) {
			return null; // handlers must return null
		}
		Job job;
		if (eventResource instanceof IContainer) {
			job = new SRPMImportJob(
					RpmText.SRPMImportHandler_ImportingFromSRPM, shell,
					((IContainer) eventResource), srpm);
		} else {
			job = new SRPMImportJob(
					RpmText.SRPMImportHandler_ImportingFromSRPM, shell,
					eventResource.getParent(), srpm);
		}
		job.setUser(true);
		job.schedule();
		return null;
	}

}
