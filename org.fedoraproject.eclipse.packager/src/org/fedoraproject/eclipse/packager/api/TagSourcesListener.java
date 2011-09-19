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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.fedoraproject.eclipse.packager.BranchConfigInstance;
import org.fedoraproject.eclipse.packager.FedoraPackagerLogger;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.IFpProjectBits;
import org.fedoraproject.eclipse.packager.IProjectRoot;
import org.fedoraproject.eclipse.packager.QuestionMessageDialog;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;

/**
 * A listener for SCM-tagging sources (only required for CVS at the moment).
 */
public class TagSourcesListener implements ICommandListener {

	/**
	 * The fedora project root to work with.
	 */
	private IProjectRoot projectRoot;
	private IProgressMonitor mainMonitor;
	private Shell shell;
	private BranchConfigInstance bci;

	/**
	 * Create tag SCM listener.
	 * 
	 * @param root
	 *            The Fedora project root.
	 * @param monitor
	 *            The main monitor to create a submonitor from.
	 * @param shell
	 *            The shell to be used for message dialog prompting
	 * @param bci
	 *            The configuration for the branch the listened command.
	 */
	public TagSourcesListener(IProjectRoot root, IProgressMonitor monitor,
			Shell shell, BranchConfigInstance bci) {
		this.projectRoot = root;
		this.mainMonitor = monitor;
		this.shell = shell;
		this.bci = bci;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.fedoraproject.eclipse.packager.api.ICommandListener#preExecution()
	 */
	@Override
	public void preExecution() throws CommandListenerException {
		// indicate some progress, by creating a subtask
		mainMonitor
				.subTask(FedoraPackagerText.TagSourcesListener_tagSourcesMsg);
		IFpProjectBits projectBits = FedoraPackagerUtils
				.getVcsHandler(projectRoot);
		if (projectBits.needsTag()) {
			// Do VCS tagging if so requested.
			if (askIfShouldTag()) {
				FedoraPackagerLogger logger = FedoraPackagerLogger
						.getInstance();
				logger.logDebug(FedoraPackagerText.TagSourcesListener_tagSourcesMsg);
				projectBits.tagVcs(projectRoot, mainMonitor, bci);
			}
		}
		mainMonitor.worked(20);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.fedoraproject.eclipse.packager.api.ICommandListener#postExecution()
	 */
	@Override
	public void postExecution() throws CommandListenerException {
		// nothing
	}

	/**
	 * Ask for tagging of sources. This is only necessary for CVS based source
	 * control.
	 * 
	 * @return {@code true} if the user requested a tag of sources.
	 */
	private boolean askIfShouldTag() {
		QuestionMessageDialog op = new QuestionMessageDialog(
				FedoraPackagerText.TagSourcesListener_tagBeforeSendingBuild,
				shell, this.projectRoot);
		Display.getDefault().syncExec(op);
		return op.isOkPressed();
	}

}
