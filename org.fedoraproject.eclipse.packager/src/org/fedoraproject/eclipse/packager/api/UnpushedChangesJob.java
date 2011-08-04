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
package org.fedoraproject.eclipse.packager.api;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.fedoraproject.eclipse.packager.IProjectRoot;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
import org.fedoraproject.eclipse.packager.api.errors.UnpushedChangesException;

/**
 * Job checking for unpushed changes.
 *
 */
public class UnpushedChangesJob extends Job {

	// Check for unpushed changes
	private boolean unpushedChanges = false;
	private IProjectRoot fedoraProjectRoot;
	
	/**
	 * @param name
	 * @param fedoraProjectRoot
	 */
	public UnpushedChangesJob(String name, IProjectRoot fedoraProjectRoot) {
		super(name);
		this.fedoraProjectRoot = fedoraProjectRoot;
	}
				
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		UnpushedChangesListener unpushedChangesListener = new UnpushedChangesListener(
				fedoraProjectRoot, monitor);
		monitor.beginTask(getName(), 30);
		try {
			unpushedChangesListener.preExecution();
		} catch (CommandListenerException e) {
			if (e.getCause() instanceof UnpushedChangesException) {
				this.unpushedChanges = true;
			}
		}
		monitor.done();
		return Status.OK_STATUS;
	}
	
	/**
	 * @return {@code true} if there were unpushed changes, {@code false}
	 *         otherwise.
	 */
	public boolean isUnpushedChanges() {
		return this.unpushedChanges;
	}
				
}
