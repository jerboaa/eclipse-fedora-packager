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
package org.fedoraproject.eclipse.packager.tests;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.fedoraproject.eclipse.packager.git.FedoraPackagerGitCloneOperation;
import org.fedoraproject.eclipse.packager.git.GitUtils;
import org.junit.Test;

public class FedoraPackagerGitCloneOperationTest {

	private Git git;
	
	@Test
	public void shouldThrowExceptionWhenURIInvalid() {
		FedoraPackagerGitCloneOperation cloneOp = new FedoraPackagerGitCloneOperation();
		try {
			cloneOp.setCloneURI("+ // + really bad URL");
			fail("Should have thrown URISyntaxException");
		} catch (URISyntaxException e) {
			// pass
		}
	}
	
	@Test(expected=IllegalStateException.class)
	public void shouldThrowExceptionWhenIllConfigured() throws Exception {
		FedoraPackagerGitCloneOperation cloneOp = new FedoraPackagerGitCloneOperation();
		cloneOp.run(null);
	}
	
	/**
	 * Fedora Git clones create local branches. Test for that.
	 * 
	 * @throws Exception
	 */
	@Test
	public void canCloneFromFedoraGit() {
		final FedoraPackagerGitCloneOperation cloneOp = new FedoraPackagerGitCloneOperation();
		final String fedoraPackager = "eclipse-fedorapackager";
		Job cloneJob = new Job("Clone Me!") {
			
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					git = cloneOp.setPackageName(fedoraPackager).setCloneURI(
							GitUtils.getFullGitURL(GitUtils.getAnonymousGitBaseUrl(),
									fedoraPackager)).run(monitor);
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (CoreException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
				return null;
			}
		};
		cloneJob.schedule();
		try {
			cloneJob.join(); // wait for it to finish
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		assertNotNull(git);
		ListBranchCommand ls = git.branchList();
		// should have created a local branch called "f14"
		boolean f14Found = false;
		for(Ref ref: ls.call()) {
			if (Repository.shortenRefName(ref.getName()).equals("f14")) {
				f14Found = true;
				break;
			}
		}
		assertTrue(f14Found);
	}

}
