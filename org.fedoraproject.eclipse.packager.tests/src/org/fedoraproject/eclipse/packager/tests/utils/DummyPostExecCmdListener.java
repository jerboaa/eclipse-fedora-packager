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

import org.fedoraproject.eclipse.packager.api.FedoraPackagerCommand;
import org.fedoraproject.eclipse.packager.api.ICommandListener;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;

/**
 * Fixture for
 * {@link FedoraPackagerCommand#addCommandListener(ICommandListener)} testing.
 * 
 */
public class DummyPostExecCmdListener implements ICommandListener {

	public static final String EXCEPTION_MSG = "postExecTest";
	
	@Override
	public void preExecution() throws CommandListenerException {
		// nothing
	}

	@Override
	public void postExecution() throws CommandListenerException {
		// throw some arbitrary exception wrapped as cmd listener ex
		throw new CommandListenerException(new IllegalStateException(EXCEPTION_MSG));
	}

}
