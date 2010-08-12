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
package org.fedoraproject.eclipse.packager.cvs;

import java.io.IOException;
import java.util.Arrays;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.wizards.CommitWizard;
import org.eclipse.team.internal.core.subscribers.ActiveChangeSet;
import org.eclipse.team.internal.core.subscribers.ActiveChangeSetManager;
import org.fedoraproject.eclipse.packager.handlers.CommonHandler;

@SuppressWarnings("restriction")
public class CommitHandler extends CommonHandler {

	@Override
	public IStatus doExecute(final ExecutionEvent event, IProgressMonitor monitor) throws ExecutionException {
		monitor.subTask(Messages.getString("CommitHandler.0")); //$NON-NLS-1$
		// check all out of sync resources in this project for a specfile
		IProject project = resource.getProject();
		final SyncInfoSet set = new SyncInfoSet();
		RepositoryProvider.getProvider(project,
		"org.eclipse.team.cvs.core.cvsnature").getSubscriber() //$NON-NLS-1$
		.collectOutOfSync(new IResource[] { project },
				IResource.DEPTH_INFINITE, set,
				new NullProgressMonitor());
		IResource[] outOfSync = set.getResources();

		final ActiveChangeSetManager manager = CVSUIPlugin.getPlugin()
		.getChangeSetManager();
		final ActiveChangeSet cs = new ActiveChangeSet(
				manager, "FedoraCVS"); //$NON-NLS-1$

		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		if (Arrays.asList(outOfSync).contains(specfile)) {
			try {
				cs.add(new IResource[] { specfile });
			} catch (CoreException e) {
				e.printStackTrace();
				return handleError(e);
			}
			// add the most recent entry from the specfile's changelog to the
			// commit message
			try {
				cs.setComment(getClog());
			} catch (IOException e) {
				e.printStackTrace();
				return handleError(e);
			}
		}

		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		manager.add(cs);
		// pass off control to CVS wizard
		IJobChangeListener listener = new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						try {
							CommitWizard.run(shell, set, null);
							manager.remove(cs);
						} catch (CVSException e) {
							e.printStackTrace();
							handleError(e, true);
						}
					}			
				});
			}
		};
		job.addJobChangeListener(listener);

		return Status.OK_STATUS;
	}

	@Override
	protected String getTaskName() {
		return Messages.getString("CommitHandler.3"); //$NON-NLS-1$
	}
}
