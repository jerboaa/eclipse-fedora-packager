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
package org.fedoraproject.eclipse.packager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileParser;
import org.fedoraproject.eclipse.packager.LookasideCache.CacheType;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils.ProjectType;

/**
 * This class is representing a root directory for a Fedora package in a given
 * branch. It can be a folder in the cvs case or a project in the git case.
 * 
 */
public class FedoraProjectRoot {

	private IContainer rootContainer;
	private SourcesFile sourcesFile;
	private ProjectType type;
	private LookasideCache lookAsideCache; // The lookaside cache abstraction

	/**
	 * Creates the FedoraProjectRoot using the given container. It is
	 * discouraged to use this constructor directly.
	 * {@link FedoraPackagerUtils#getProjectRoot(IResource)} should be used
	 * instead.
	 * 
	 * @param container
	 *            The root container either IFolder(cvs) or IProject(git).
	 * @param type The project type (either Git or CVS).
	 * @see FedoraPackagerUtils#getProjectRoot(IResource)
	 */
	public FedoraProjectRoot(IContainer container, ProjectType type) {
		this.rootContainer = container;
		this.sourcesFile = new SourcesFile(rootContainer.getFile(new Path(
				"sources"))); //$NON-NLS-1$
		assert type != null;
		this.type = type;
		this.lookAsideCache = new LookasideCache(CacheType.FEDORA);
	}

	/**
	 * Returns the root container.
	 * 
	 * @return The root container.
	 */
	public IContainer getContainer() {
		return rootContainer;
	}
	
	/**
	 * Get the project containing this FedoraProjectRoot.
	 * 
	 * @return The project for this FedoraProjectRoot instance.
	 */
	public IProject getProject() {
		return this.rootContainer.getProject();
	}

	/**
	 * Returns the sources file containing the sources for the given srpm.
	 * 
	 * @return The sources file.
	 */
	public SourcesFile getSourcesFile() {
		return sourcesFile;
	}

	/**
	 * Returns the .spec file for the given project.
	 * 
	 * @return The specfile
	 */
	public IFile getSpecFile() {
		try {
			for (IResource resource : rootContainer.members()) {
				if (resource instanceof IFile) {
					String ext = ((IFile) resource).getFileExtension();
					if (ext != null && ext.equals("spec")) //$NON-NLS-1$
						return (IFile) resource;
				}
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Returns the parsed .spec file model to ease retrieving data from it.
	 * 
	 * @return The parsed .spec file.
	 */
	public Specfile getSpecfileModel() {
		SpecfileParser parser = new SpecfileParser();
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					getSpecFile().getContents()));
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n"); //$NON-NLS-1$
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Specfile specfile = parser.parse(sb.toString());
		return specfile;
	}

	/**
	 * Returns the project type.
	 * 
	 * @return The project type based on the VCS used.
	 */
	public ProjectType getProjectType() {
		return type;
	}

	/**
	 * Get the ignore file based on the project type.
	 * 
	 * @return The ignore file (.cvsignore or .gitignore).
	 */
	public IFile getIgnoreFile() {
		String ignoreFileName = null;
		switch (type) {
		case GIT:
			ignoreFileName = ".gitignore"; //$NON-NLS-1$
			break;
		case CVS:
			ignoreFileName = ".cvsignore"; //$NON-NLS-1$
			break;
		default:
			break;
		}
		assert ignoreFileName != null;
		IFile ignoreFile = getFileMember(ignoreFileName);
		// If not existent, return a IFile handle from the container
		if (ignoreFile == null) {
			return this.rootContainer.getFile(new Path(ignoreFileName));
		}
		assert ignoreFile != null;
		return ignoreFile;
	}

	/**
	 * @return the lookAsideCache
	 */
	public LookasideCache getLookAsideCache() {
		return lookAsideCache;
	}

	/**
	 * Find the ignore file in the root container if any.
	 * 
	 * @param ignoreFileName The name of the VCS ignore file.
	 * 
	 * @return The file handle if found. {@code null} otherwise.
	 */
	private IFile getFileMember(String ignoreFileName) {
		IResource resource = rootContainer.findMember(ignoreFileName);
		if (resource instanceof IFile) {
			return ((IFile) resource);
		}
		return null;
	}
}
