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
import java.io.File;
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
import org.fedoraproject.eclipse.packager.handlers.FedoraHandlerUtils;
import org.fedoraproject.eclipse.packager.handlers.FedoraHandlerUtils.ProjectType;

/**
 * This class is representing a root directory for fedora package in given
 * branch. It can be a folder in the cvs case or a project in the git case.
 * 
 */
public class FedoraProjectRoot {

	private IContainer rootContainer;
	private SourcesFile sourcesFile;
	private ProjectType type;
	private LookasideCache lookAsideCache; // The lookaside cache abstraction

	/**
	 * Creates the FedoraProjectRoot using the given container.
	 * 
	 * @param container
	 *            The root container either IFolder(cvs) or IProject(git).
	 * @param cmdId
	 * 			  The command ID for which a FedoraProjectRoot is requested.
	 */
	public FedoraProjectRoot(IContainer container, String cmdId /* unused */) {
		this.rootContainer = container;
		this.sourcesFile = new SourcesFile(rootContainer.getFile(new Path(
				"sources"))); //$NON-NLS-1$
		this.type = FedoraHandlerUtils.getProjectType(container);
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
	 * Creates a tag name from the given specfile.
	 * 
	 * @return The created tag name.
	 */
	public String makeTagName() {
		Specfile specfile = getSpecfileModel();
		String name = specfile.getName().replaceAll("^[0-9]+", ""); //$NON-NLS-1$ //$NON-NLS-2$
		String version = specfile.getVersion();
		String release = specfile.getRelease();
		return (name + "-" + version + "-" + release).replaceAll("\\.", "_"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
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
	 * Returns the ignore file based on the project type.
	 * @return The ignore file (.cvsignore or .gitignore).
	 */
	//TODO remove this method once we are sure we can use git or cvs methods to manipulate their ignore files.
	public File getIgnoreFile() {
		File ignoreFile = null;
		switch (type) {
		case GIT:
			ignoreFile = getFileMember(".gitignore"); //$NON-NLS-1$
			break;
		case CVS:
			ignoreFile = getFileMember(".cvsignore"); //$NON-NLS-1$
			break;

		default:
			break;
		}
		return ignoreFile;
	}

	private File getFileMember(String ignoreFileName) {
		IResource resource = rootContainer.findMember(ignoreFileName);
		if (resource instanceof IFile) {
			return ((IFile) resource).getLocation().toFile();
		}
		return null;
	}

	/**
	 * @return the lookAsideCache
	 */
	public LookasideCache getLookAsideCache() {
		return lookAsideCache;
	}
}
