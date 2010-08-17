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
package org.fedoraproject.eclipse.packager.test_utils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.egit.core.op.CloneOperation;
import org.eclipse.egit.core.op.ConnectProviderOperation;
import org.eclipse.egit.ui.RepositoryUtil;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.URIish;

public class GitTestProject {	
	private IProject project;
	
	public GitTestProject(String packageName) throws CoreException, URISyntaxException, InvocationTargetException, InterruptedException {
		final URIish uri = new URIish(getGitURL(packageName));
		final CloneOperation clone = new CloneOperation(uri, true,
				new ArrayList<Ref>(), new File(ResourcesPlugin
						.getWorkspace().getRoot().getLocation().toFile(),
						packageName), "refs/heads/master", // TODO: use constants
				"origin");
		clone.run(null); // clone project
		// Add cloned repository to the list of Git repositories so that it
		// shows up in the Git repositories view.
		final RepositoryUtil config = org.eclipse.egit.ui.Activator.getDefault().getRepositoryUtil();
		config.addConfiguredRepository(clone.getGitDir());
		project = ResourcesPlugin.getWorkspace().getRoot()
				.getProject(packageName);
		project.create(null);
		project.open(null);
		ConnectProviderOperation connect = new ConnectProviderOperation(
				project);
		connect.execute(null);
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
	}
	
	public void dispose() throws Exception {
		project.delete(true, true, null);
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
	}
	
	public IProject getProject() {
		return this.project;
	}
	
	private String getGitURL(String packageName) {
		return "git://pkgs.fedoraproject.org/" + packageName + ".git";
	}
}
