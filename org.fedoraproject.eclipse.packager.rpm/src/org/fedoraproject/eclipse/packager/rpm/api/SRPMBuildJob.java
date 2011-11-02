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
package org.fedoraproject.eclipse.packager.rpm.api;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.fedoraproject.eclipse.packager.BranchConfigInstance;
import org.fedoraproject.eclipse.packager.FedoraPackagerLogger;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.IProjectRoot;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
import org.fedoraproject.eclipse.packager.rpm.RPMPlugin;
import org.fedoraproject.eclipse.packager.rpm.RpmText;
import org.fedoraproject.eclipse.packager.rpm.api.RpmBuildCommand.BuildType;
import org.fedoraproject.eclipse.packager.rpm.api.errors.RpmBuildCommandException;
import org.fedoraproject.eclipse.packager.utils.FedoraHandlerUtils;

/**
 * A job for SRPM builds.
 *
 */
public class SRPMBuildJob extends Job {
	
	private RpmBuildCommand srpmBuild;
	private IProjectRoot fedoraProjectRoot;
	private FedoraPackagerLogger logger;
	private RpmBuildResult srpmBuildResult;
	private BranchConfigInstance bci;
	
	/**
	 * @param jobName
	 * @param rpmBuild
	 * @param fedoraProjectRoot
	 * @param bci 
	 */
	public SRPMBuildJob(String jobName, RpmBuildCommand rpmBuild,
			IProjectRoot fedoraProjectRoot, BranchConfigInstance bci) {
		super(jobName);
		this.fedoraProjectRoot = fedoraProjectRoot;
		this.logger = FedoraPackagerLogger.getInstance();
		this.srpmBuild = rpmBuild;
		this.bci = bci;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask(NLS.bind(RpmText.MockBuildHandler_creatingSrpm,
				fedoraProjectRoot.getPackageName(),
				fedoraProjectRoot.getPackageName()), IProgressMonitor.UNKNOWN);
		try {
			// build fresh SRPM
			List<String> nodeps = new ArrayList<String>(1);
			nodeps.add(RpmBuildCommand.NO_DEPS);
			// want SRPM build
			srpmBuild.buildType(BuildType.SOURCE).flags(nodeps);
			// set branch config
			srpmBuild.branchConfig(bci);
			logger.logDebug(NLS.bind(RpmText.MockBuildHandler_creatingSrpm,
					fedoraProjectRoot.getPackageName()));
			try {
				logger.logDebug(NLS.bind(FedoraPackagerText.callingCommand,
						RpmBuildCommand.class.getName()));
				srpmBuildResult = srpmBuild.call(monitor);
				fedoraProjectRoot.getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
			} catch (CommandMisconfiguredException e) {
				// This shouldn't happen, but report error anyway
				logger.logError(e.getMessage(), e);
				return FedoraHandlerUtils.errorStatus(RPMPlugin.PLUGIN_ID,
						e.getMessage(), e);
			} catch (CommandListenerException e) {
				// There are no command listeners registered, so shouldn't
				// happen. Do something reasonable anyway.
				logger.logError(e.getMessage(), e);
				return FedoraHandlerUtils.errorStatus(RPMPlugin.PLUGIN_ID,
						e.getMessage(), e);
			} catch (RpmBuildCommandException e) {
				logger.logError(e.getMessage(), e.getCause());
				return FedoraHandlerUtils.errorStatus(RPMPlugin.PLUGIN_ID,
						e.getMessage(), e.getCause());
			} catch (CoreException e) {
				logger.logError(e.getMessage(), e);
				return FedoraHandlerUtils.errorStatus(RPMPlugin.PLUGIN_ID,
						e.getMessage(), e);
			}
		} finally {
			monitor.done();
		}
		return Status.OK_STATUS;
	}
	
	/**
	 * 
	 * @return The result of the SRPM build or {@code null} if the build failed.
	 */
	public RpmBuildResult getSRPMBuildResult() {
		return this.srpmBuildResult;
	}

}
