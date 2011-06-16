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
package org.fedoraproject.eclipse.packager.tests.utils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.fedoraproject.eclipse.packager.IProjectRoot;
import org.fedoraproject.eclipse.packager.api.FedoraPackagerCommand;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandInitializationException;

/**
 * Fixture for {@link FedoraPackagerCommandTest}. It is a very basic
 * implementation of {@link FedoraPackagerCommand}.
 * 
 */
public class FedoraPackagerCommandDummyImpl extends FedoraPackagerCommand<DummyResult> {
	
	// some dummy state.
	private boolean configured = false;

	@Override
	public void initialize(IProjectRoot root) {
		try {
			super.initialize(root);
		} catch (FedoraPackagerCommandInitializationException e) {
			// ignore
		}
	}
	
	@Override
	protected void checkConfiguration() throws CommandMisconfiguredException {
		// pretend to require configured set to true
		if (!configured) {
			throw new CommandMisconfiguredException(
					"Dummy command implementation is not configured!"); //$NON-NLS-1$
		}
	}
	
	public void setConfiguration(boolean configured) {
		this.configured = configured;
	}

	/**
	 * Basic template for command implementation.
	 */
	@Override
	public DummyResult call(IProgressMonitor monitor)
			throws CommandMisconfiguredException, CommandListenerException {
		try {
			callPreExecListeners();
		} catch (CommandListenerException e) {
			if (e.getCause() instanceof CommandMisconfiguredException) {
				throw (CommandMisconfiguredException)e.getCause();
			}
			// rethrow
			throw e;
		}
		callPostExecListeners();
		setCallable(false);
		return new DummyResult(true);
	}

}
