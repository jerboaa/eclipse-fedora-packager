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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Common super class for Eclipse Fedora Packager Handlers.
 */
public abstract class FedoraPackagerAbstractHandler extends AbstractHandler {

	/**
	 * Retrieve a valid shell from the given {@link ExecutionEvent}.
	 * 
	 * @param event
	 * @return The shell, which is never {@code null}
	 * @throws ExecutionException
	 * @see {@link HandlerUtil#getActiveShellChecked(ExecutionEvent)}
	 */
	protected Shell getShell(ExecutionEvent event) throws ExecutionException {
		return HandlerUtil.getActiveShellChecked(event);
	}
}
