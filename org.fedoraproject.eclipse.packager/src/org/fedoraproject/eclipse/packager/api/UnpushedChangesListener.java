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
import org.fedoraproject.eclipse.packager.FedoraPackagerLogger;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.IFpProjectBits;
import org.fedoraproject.eclipse.packager.IProjectRoot;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
import org.fedoraproject.eclipse.packager.api.errors.UnpushedChangesException;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;

/**
 * A listener which checks if there have been unpushed changes on the current
 * branch. This is useful to check prior pushing a build to Koji or an update to
 * Bodhi. FIXME: This should also check for uncommitted changes (i.e. .spec file
 * is different from what's in the Git index).
 */
public class UnpushedChangesListener implements ICommandListener {

	/**
	 * The fedora project root to work with.
	 */
	private IProjectRoot projectRoot;
	private IProgressMonitor mainMonitor;
	
	/**
	 * Create an unpushed changes checker
	 * 
	 * @param root The Fedora project root.
	 * @param monitor The main monitor to create a submonitor from.
	 */
	public UnpushedChangesListener(IProjectRoot root, IProgressMonitor monitor) {
		this.projectRoot = root;
		this.mainMonitor = monitor;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.api.ICommandListener#preExecution()
	 */
	@Override
	public void preExecution() throws CommandListenerException {
		// indicate some progress, by creating a subtask
		FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
		logger.logDebug(FedoraPackagerText.UnpushedChangesListener_checkUnpushedChangesMsg);
		mainMonitor.subTask(FedoraPackagerText.UnpushedChangesListener_checkUnpushedChangesMsg);
		IFpProjectBits projectBits = FedoraPackagerUtils.getVcsHandler(projectRoot);
		if (projectBits.hasLocalChanges(projectRoot)) {
			throw new CommandListenerException(new UnpushedChangesException(FedoraPackagerText.UnpushedChangesListener_unpushedChangesError));
		}
		mainMonitor.worked(15);
	}

	/*
	 * (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.api.ICommandListener#postExecution()
	 */
	@Override
	public void postExecution() throws CommandListenerException {
		// nothing
	}

}
