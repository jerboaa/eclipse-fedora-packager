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
package org.fedoraproject.eclipse.packager.git;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.egit.core.RepositoryCache;
import org.eclipse.egit.core.RepositoryUtil;
import org.eclipse.egit.core.op.CloneOperation;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.osgi.util.NLS;
import org.fedoraproject.eclipse.packager.git.api.errors.RemoteAlreadyExistsException;

/**
 * Convenience class for Fedora Git clones. All relevant Fedora specific things
 * should be done in here. This isn't really a command in the
 * FedoraPackagerCommand sense. Hence, the "Operation" postfix.
 */
public class FedoraPackagerGitCloneOperation {

	private URIish uri;
	private String packageName;
	private boolean runnable = false;
	private boolean hasRun = false;

	/**
	 * Set the URI to use for the clone.
	 *
	 * @param cloneUri
	 * @return This instance.
	 * @throws URISyntaxException
	 *             If the provided URL was invalid.
	 */
	public FedoraPackagerGitCloneOperation setCloneURI(String cloneUri) throws URISyntaxException {
		uri = new URIish(cloneUri);
		if (packageName != null) {
			// ready to run
			runnable = true;
		}
		return this;
	}


	/**
	 * Set the package name to use for cloning.
	 *
	 * @param packageName
	 * @return This instance.
	 */
	public FedoraPackagerGitCloneOperation setPackageName(String packageName) {
		this.packageName = packageName;
		if (uri != null) {
			runnable = true;
		}
		return this;
	}

	/**
	 * Execute the clone including local branch name creation.
	 *
	 * @param monitor
	 * @throws IllegalStateException
	 * @throws InvocationTargetException
	 * @throws InterruptedException
	 * @throws CoreException
	 * @throws IOException
	 * @return A Git API instance.
	 * @throws RemoteAlreadyExistsException
	 */
	public Git run(IProgressMonitor monitor) throws IllegalStateException,
			InvocationTargetException, InterruptedException, CoreException,
			IOException, RemoteAlreadyExistsException {
		if (!runnable || hasRun) {
			throw new IllegalStateException(
					NLS.bind(
							FedoraPackagerGitText.FedoraPackagerGitCloneOperation_operationMisconfiguredError,
							this.getClass().getName()));
		}

		final CloneOperation clone = new CloneOperation(uri, true, null,
				ResourcesPlugin.getWorkspace().getRoot().getLocation()
						.append(packageName).toFile(), Constants.R_HEADS
						+ Constants.MASTER, "origin", 0); //$NON-NLS-1$
		clone.run(monitor);
		if (monitor.isCanceled()) {
			throw new InterruptedException();
		}

		// Find repo we've just created and set gitRepo
		RepositoryCache repoCache = org.eclipse.egit.core.Activator
		.getDefault().getRepositoryCache();
		Git git = new Git(repoCache.lookupRepository(clone.getGitDir()));

		GitUtils.createLocalBranches(git, monitor);

		// Add cloned repository to the list of Git repositories so that it
		// shows up in the Git repositories view.
		final RepositoryUtil config = org.eclipse.egit.core.Activator.getDefault().getRepositoryUtil();
		config.addConfiguredRepository(clone.getGitDir());

		this.hasRun = true; // disallow two runs of the same instance

		return git;
	}
}
