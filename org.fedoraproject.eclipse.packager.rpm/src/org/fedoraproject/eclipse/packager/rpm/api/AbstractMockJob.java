/*******************************************************************************
 * Copyright (c) 2011 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.fedoraproject.eclipse.packager.rpm.api;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.fedoraproject.eclipse.packager.BranchConfigInstance;
import org.fedoraproject.eclipse.packager.FedoraPackagerLogger;
import org.fedoraproject.eclipse.packager.IProjectRoot;
import org.fedoraproject.eclipse.packager.rpm.RpmText;
import org.fedoraproject.eclipse.packager.utils.FedoraHandlerUtils;

/**
 * Superclass for mock jobs, which share the same
 * job listener.
 *
 */
public abstract class AbstractMockJob extends Job {

	protected MockBuildResult result;
	protected Shell shell;
	protected IProjectRoot fpr;
	protected BranchConfigInstance bci;
	
	/**
	 * @param name
	 * @param shell 
	 * @param fedoraProjectRoot 
	 * @param bci 
	 */
	public AbstractMockJob(String name, Shell shell, IProjectRoot fedoraProjectRoot, BranchConfigInstance bci) {
		super(name);
		this.shell = shell;
		this.fpr = fedoraProjectRoot;
		this.bci = bci;
	}
	
	/**
	 * 
	 * @return A job listener for the {@code done} event.
	 */
	protected IJobChangeListener getMockJobFinishedJobListener() {
		IJobChangeListener listener = new JobChangeAdapter() {

			// We are only interested in the done event
			@Override
			public void done(IJobChangeEvent event) {
				FedoraPackagerLogger logger = FedoraPackagerLogger
						.getInstance();
				IStatus jobStatus = event.getResult();
				if (jobStatus.getSeverity() == IStatus.CANCEL) {
					// cancelled log this in any case
					logger.logInfo(RpmText.AbstractMockJob_mockCancelledMsg);
					FedoraHandlerUtils.showInformationDialog(shell, fpr
							.getProductStrings().getProductName(),
							RpmText.AbstractMockJob_mockCancelledMsg);
					return;
				}
				if (result.wasSuccessful()) {
					// TODO: Make this a link to the directory
					String msg = NLS.bind(
							RpmText.AbstractMockJob_mockSucceededMsg,
							result.getResultDirectoryPath());
					logger.logDebug(msg);
					FedoraHandlerUtils.showInformationDialog(shell, fpr
							.getProductStrings().getProductName(), msg);
				} else {
					String msg = NLS.bind(
							RpmText.AbstractMockJob_mockFailedMsg,
							result.getResultDirectoryPath());
					logger.logDebug(msg);
					FedoraHandlerUtils.showInformationDialog(shell, fpr
							.getProductStrings().getProductName(), msg);
				}
			}
		};
		return listener;
	}
}
