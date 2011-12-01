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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.fedoraproject.eclipse.packager.FedoraPackagerLogger;
import org.fedoraproject.eclipse.packager.FedoraPackagerPreferencesConstants;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.PackagerPlugin;
import org.fedoraproject.eclipse.packager.api.FedoraPackagerAbstractHandler;
import org.fedoraproject.eclipse.packager.api.FileDialogRunable;
import org.fedoraproject.eclipse.packager.api.IPreferenceHandler;
import org.fedoraproject.eclipse.packager.api.UploadSourceCommand;
import org.fedoraproject.eclipse.packager.rpm.RPMPlugin;
import org.fedoraproject.eclipse.packager.rpm.RpmText;
import org.fedoraproject.eclipse.packager.rpm.api.ISRPMImportCommandSLLPolicyCallback;
import org.fedoraproject.eclipse.packager.rpm.api.SRPMImportCommand;
import org.fedoraproject.eclipse.packager.rpm.api.SRPMImportResult;
import org.fedoraproject.eclipse.packager.rpm.api.errors.SRPMImportCommandException;
import org.fedoraproject.eclipse.packager.utils.FedoraHandlerUtils;

/**
 * Import handler for SRPMImportCommand
 * 
 */
public class SRPMImportHandler extends FedoraPackagerAbstractHandler implements IPreferenceHandler, ISRPMImportCommandSLLPolicyCallback{
	private final FedoraPackagerLogger logger = FedoraPackagerLogger
			.getInstance();
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = getShell(event);
		IResource eventResource = FedoraHandlerUtils.getResource(event);
		FileDialogRunable fdr = new FileDialogRunable("*.src.rpm", //$NON-NLS-1$
				RpmText.SRPMImportHandler_FileDialogTitle);
		shell.getDisplay().syncExec(fdr);
		final String srpm = fdr.getFile();
		if (srpm == null) {
			return null; // handlers must return null
		}
		IContainer fpr;
		if (eventResource instanceof IContainer) {
			fpr = (IContainer) eventResource;
		} else {
			fpr = eventResource.getParent();
		}
		final SRPMImportHandler self = this;
		final IContainer fprContainer = fpr;
		Job job = new Job(RpmText.SRPMImportHandler_ImportingFromSRPM) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask(RpmText.SRPMImportJob_InitialSetup,
						IProgressMonitor.UNKNOWN);
				try {
					logger.logDebug(NLS.bind(FedoraPackagerText.callingCommand,
							SRPMImportCommand.class.getName()));
					SRPMImportCommand srpmImport = new SRPMImportCommand(srpm,
							fprContainer.getProject(), fprContainer,
							getPreference(), self);
					monitor.setTaskName(RpmText.SRPMImportJob_ExtractingSRPM);
					SRPMImportResult importResult;
					importResult = srpmImport.call(monitor);
					if (!importResult.wasSuccessful()) {
						return FedoraHandlerUtils.errorStatus(
								RPMPlugin.PLUGIN_ID,
								RpmText.SRPMImportJob_ExtractFailed);
					}
					return Status.OK_STATUS;
				} catch (SRPMImportCommandException e) {
					logger.logError(e.getMessage(), e);
					return FedoraHandlerUtils.errorStatus(RPMPlugin.PLUGIN_ID,
							e.getMessage(), e);
				} finally {
					monitor.done();
				}
			}
		};
		job.setUser(true);
		job.schedule();
		return null;
	}

	@Override
	public String getPreference() {
		return PackagerPlugin
				.getStringPreference(FedoraPackagerPreferencesConstants.PREF_LOOKASIDE_UPLOAD_URL);
	}

	@Override
	public void setSSLPolicy(UploadSourceCommand uploadCmd, String uploadUrl) {
		// enable SLL authentication
		uploadCmd.setFedoraSSLEnabled(true);
	}
}
