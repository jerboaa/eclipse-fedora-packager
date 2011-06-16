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
package org.fedoraproject.eclipse.packager;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerExtensionPointException;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils.ProjectType;

/**
 * Interface for project root implementations. Users are free to implement this
 * interface and use its implementation via the ProjectRootProvider extension
 * point.
 * 
 */
public interface IProjectRoot {

	/**
	 * Initialization method. Called on object creation. Put relevant
	 * initialization code into this method.
	 * 
	 * @param container
	 * @param type
	 * @throws FedoraPackagerExtensionPointException
	 *             If one or more of the dependent extension points have not
	 *             been implemented. {@code lookasideCacheProvider} and
	 *             {@code productNamesProvider} are required for this
	 *             initialization to work.
	 */
	public void initialize(IContainer container, ProjectType type) throws FedoraPackagerExtensionPointException;
	
	/**
	 * Returns the root container. This may be different from the project this
	 * container may be a member of.
	 * 
	 * @return The root container.
	 */
	public IContainer getContainer();
	
	/**
	 * Get the project containing this IProjectRoot.
	 * 
	 * @return The project for this FedoraProjectRoot instance.
	 */
	public IProject getProject();

	/**
	 * Returns the sources file containing the sources for the given srpm.
	 * 
	 * @return The sources file.
	 */
	public SourcesFile getSourcesFile();
	
	/**
	 * Returns the name of the package (i.e. the name of the SRPM)
	 * 
	 * @return The name of the package
	 */
	public String getPackageName();

	/**
	 * Returns the .spec file for the given project.
	 * 
	 * @return The .spec file or {@code null} if not found.
	 */
	public IFile getSpecFile();

	/**
	 * Returns the parsed .spec file model to ease retrieving data from it.
	 * 
	 * @return The parsed .spec file.
	 */
	public Specfile getSpecfileModel();

	/**
	 * Returns the project type.
	 * 
	 * @return The project type based on the VCS used.
	 */
	public ProjectType getProjectType();

	/**
	 * Get the ignore file based on the project type.
	 * 
	 * @return The ignore file. For now this is {@code .cvsignore} or {@code .gitignore}.
	 */
	public IFile getIgnoreFile();

	/**
	 * @return the lookAsideCache
	 */
	public ILookasideCache getLookAsideCache();
	
	/**
	 * 
	 * @return the product strings mapper.
	 */
	public IProductStrings getProductStrings();
}
