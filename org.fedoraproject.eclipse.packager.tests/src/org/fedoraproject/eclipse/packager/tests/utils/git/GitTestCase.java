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
package org.fedoraproject.eclipse.packager.tests.utils.git;

import org.eclipse.core.resources.IProject;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.IProjectRoot;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;

import junit.framework.TestCase;

public class GitTestCase extends TestCase {
	
	private GitTestProject project;
	private IProject iProject;
	private IProjectRoot fedoraprojectRoot;
	
	@Override
	protected void setUp() throws Exception {
		project = new GitTestProject("eclipse-rpm-editor");
		iProject = project.getProject();
		// create a fedoraprojectRoot for this project
		fedoraprojectRoot = FedoraPackagerUtils.getProjectRoot((iProject));		
	}

	/**
	 * @return the fedoraprojectRoot
	 */
	public IProjectRoot getFedoraprojectRoot() {
		return fedoraprojectRoot;
	}

	/**
	 * @param fedoraprojectRoot the fedoraprojectRoot to set
	 */
	public void setFedoraprojectRoot(FedoraProjectRoot fedoraprojectRoot) {
		this.fedoraprojectRoot = fedoraprojectRoot;
	}

	@Override
	protected void tearDown() throws Exception {
		project.dispose();
	}

	/**
	 * @return the project
	 */
	public GitTestProject getProject() {
		return project;
	}

	/**
	 * @param project the project to set
	 */
	public void setProject(GitTestProject project) {
		this.project = project;
	}

	/**
	 * @return the iProject
	 */
	public IProject getiProject() {
		return iProject;
	}

	/**
	 * @param iProject the iProject to set
	 */
	public void setiProject(IProject iProject) {
		this.iProject = iProject;
	}
}
