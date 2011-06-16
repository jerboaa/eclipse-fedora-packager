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
package org.fedoraproject.eclipse.packager.tests.utils;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.fedoraproject.eclipse.packager.IProjectRoot;
import org.fedoraproject.eclipse.packager.SourcesFile;
import org.fedoraproject.eclipse.packager.api.ICommandListener;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;

/**
 * Action to intentionally alter the MD5sum of a downloaded
 * source file. This should be used for testing purposes only.
 *
 */
public class CorruptDownload implements ICommandListener {
	
	private IProjectRoot fedoraProjectRoot;
	
	public CorruptDownload(IProjectRoot fpRoot) {
		this.fedoraProjectRoot = fpRoot;
	}

	@Override
	public void preExecution() throws CommandListenerException {
		// nothing
	}

	/**
	 * Intentionally destroy MD5sums of sources files.
	 */
	@Override
	public void postExecution() throws CommandListenerException {
		String extraContents = "0xbeef";
		ByteArrayInputStream inputStream = new ByteArrayInputStream(extraContents.getBytes());
		SourcesFile sources = fedoraProjectRoot.getSourcesFile();
		for (String filename: sources.getAllSources()) {
			IFile sourceFile;
				sourceFile = (IFile)fedoraProjectRoot.getContainer().findMember(new Path(filename));
			if (sourceFile != null) {
				try {
					sourceFile.appendContents(inputStream, IResource.FORCE, new NullProgressMonitor());
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}
		try {
			fedoraProjectRoot.getContainer().refreshLocal(IResource.DEPTH_ONE, null);
		} catch (CoreException e) {
			// ignore
		}

	}

}
