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

import static org.junit.Assert.fail;

import java.io.File;
import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.egit.core.op.ConnectProviderOperation;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.fedoraproject.eclipse.packager.LocalProjectType;
import org.fedoraproject.eclipse.packager.PackagerPlugin;
import org.fedoraproject.eclipse.packager.api.LocalFedoraPackagerProjectCreator;
import org.osgi.framework.FrameworkUtil;

/**
 * Fixture for Git based projects.
 */
public class GitConvertTestProject {
	private IProject project;
	private LocalFedoraPackagerProjectCreator mainProject;

	public GitConvertTestProject(final String packageName, final String fileName)
			throws Exception {

		// Create a base project for the test
		this.project = ResourcesPlugin.getWorkspace().getRoot()
				.getProject(packageName);
		this.project.create(null);
		this.project.open(null);

		mainProject = new LocalFedoraPackagerProjectCreator(project, null);

		// Find the test external file and install it
		URL url = FileLocator.find(FrameworkUtil
				.getBundle(GitConvertTestProject.class), new Path(
				"resources" + IPath.SEPARATOR + packageName + IPath.SEPARATOR + //$NON-NLS-1$
						fileName), null);
		if (url == null) {
			fail("Unable to find resource" + IPath.SEPARATOR + packageName //$NON-NLS-1$
					+ IPath.SEPARATOR + fileName);
		}

		File externalFile = new File(FileLocator.toFileURL(url).getPath());

		// poulate project using imported .spec file
		mainProject.create(externalFile, LocalProjectType.PLAIN);

		// Set persistent property so that we know when to show the context
		// menu item.
		project.setPersistentProperty(PackagerPlugin.PROJECT_LOCAL_PROP, "true" /* unused value */); //$NON-NLS-1$

		ConnectProviderOperation connect = new ConnectProviderOperation(project);
		connect.execute(null);

		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public void dispose() throws Exception {
		project.delete(true, true, null);
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
	}

	/**
	 * Get underlying IProject
	 *
	 * @return
	 */
	public IProject getProject() {
		return this.project;
	}

	/**
	 * Adds a remote repository to the existing local packager project
	 *
	 * @throws Exception
	 */
	public void addRemoteRepository(String uri, Git git) throws Exception {

		RemoteConfig config = new RemoteConfig(git.getRepository()
				.getConfig(), "origin"); //$NON-NLS-1$
		config.addURI(new URIish(uri));
		String dst = Constants.R_REMOTES + config.getName();
		RefSpec refSpec = new RefSpec();
		refSpec = refSpec.setForceUpdate(true);
		refSpec = refSpec.setSourceDestination(
				Constants.R_HEADS + "*", dst + "/*"); //$NON-NLS-1$ //$NON-NLS-2$

		config.addFetchRefSpec(refSpec);
		config.update(git.getRepository().getConfig());
		git.getRepository().getConfig().save();

		// fetch all the remote branches,
		// create corresponding branches locally and merge them
		FetchCommand fetch = git.fetch();
		fetch.setRemote("origin"); //$NON-NLS-1$
		fetch.setTimeout(0);
		fetch.setRefSpecs(refSpec);
		fetch.call();
		// refresh after checkout
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
	}
}
