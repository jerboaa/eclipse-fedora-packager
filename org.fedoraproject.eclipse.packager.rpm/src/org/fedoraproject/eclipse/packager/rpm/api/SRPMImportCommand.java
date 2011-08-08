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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.linuxtools.rpm.ui.SRPMImportOperation;
import org.eclipse.linuxtools.rpm.core.RPMProjectLayout;
import org.eclipse.osgi.util.NLS;
import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
import org.fedoraproject.eclipse.packager.rpm.RpmText;
import org.fedoraproject.eclipse.packager.rpm.api.errors.SRPMImportCommandException;

/**
 * Command for importing SRPM to a project.
 * 
 */
public class SRPMImportCommand {

	/**
	 * @param srpm
	 *            The srpm being imported.
	 * @param project
	 *            The project that is receiving the import.
	 */
	public SRPMImportCommand(String srpm, IProject project) {
		this.srpm = srpm;
		this.project = project;
	}

	/**
	 * Unique identifier of this command.
	 */
	private String srpm = null;
	private String[] uploadFiles;
	private IProject project = null;

	protected void checkConfiguration() throws CommandMisconfiguredException {
		if (srpm == null) {
			throw new CommandMisconfiguredException(
					RpmText.SRPMImportCommand_PathNotSet);
		} else if (!new File(srpm).exists()) {
			throw new CommandMisconfiguredException(NLS.bind(
					RpmText.SRPMImportCommand_SRPMNotFound, srpm));
		}
		if (project == null) {
			throw new CommandMisconfiguredException(
					RpmText.SRPMImportCommand_ProjectNotSet);
		}
	}

	/**
	 * Calling method for this command.
	 * 
	 * @param monitor
	 *            Monitor for this command's runtime.
	 * @return The result of calling this command.
	 * @throws SRPMImportCommandException
	 */
	public SRPMImportResult call(IProgressMonitor monitor)
			throws SRPMImportCommandException {
		// install rpm to the project folder
		SRPMImportOperation sio;
		try {
			sio = new SRPMImportOperation(project, new File(srpm),
					RPMProjectLayout.FLAT);
			sio.run(new NullProgressMonitor());
		} catch (InvocationTargetException e) {
			throw new SRPMImportCommandException(e.getMessage(), e);
		}
		if (!sio.getStatus().isOK()) {
			Throwable e = sio.getStatus().getException();
			if (e != null) {
				throw new SRPMImportCommandException(e.getMessage(), e);
			} else {
				throw new SRPMImportCommandException(NLS.bind(
						RpmText.SRPMImportCommand_NonZeroExit, sio.getStatus()
								.getCode()));
			}
		}
		String[] cmdList = null;
		try {
			// get files in the srpm
			cmdList = new String[] { "rpm", "-qpl", srpm }; //$NON-NLS-1$ //$NON-NLS-2$
			ProcessBuilder pBuilder = new ProcessBuilder(cmdList);
			Process child = pBuilder.start();
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new BufferedInputStream(child.getInputStream())));
			try {
				if (child.waitFor() != 0) {
					throw new SRPMImportCommandException(NLS.bind(
							RpmText.SRPMImportCommand_NonZeroQueryExit,
							child.exitValue()));
				}
			} catch (InterruptedException e) {
				throw new OperationCanceledException();
			}
			List<String> uploadList = new ArrayList<String>();
			while (br.ready()) {
				uploadList.add(br.readLine());
			}
			uploadFiles = uploadList.toArray(new String[0]);
		} catch (IOException e) {
			throw new SRPMImportCommandException(NLS.bind(
					RpmText.SRPMImportCommand_IOError, srpm), e);
		}
		SRPMImportResult result = new SRPMImportResult(cmdList);
		result.setSuccess(true);
		return result;
	}

	/**
	 * Set the SRPM this command imports.
	 * 
	 * @param path
	 *            The path to the SRPM.
	 * @return This command.
	 */
	public SRPMImportCommand setSRPM(String path) {
		this.srpm = path;
		return this;
	}

	/**
	 * Get the files extracted from the SRPM
	 * 
	 * @return The names of the extracted files.
	 */
	public String[] getUploadFiles() {
		return uploadFiles;
	}

	/**
	 * Set the project to import the SRPM into.
	 * 
	 * @param project
	 *            The project being imported to.
	 * @return This command.
	 */
	public SRPMImportCommand setProject(IProject project) {
		this.project = project;
		return this;
	}
}
