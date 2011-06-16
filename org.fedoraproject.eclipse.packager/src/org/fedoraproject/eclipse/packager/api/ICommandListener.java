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

import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;

/**
 * Implement this interface and register the listener for the desired command,
 * in order for you to get pre/post actions executed. If more than one Listener
 * is registered for a command they get executed in the order they were added.
 */
public interface ICommandListener {

	/**
	 * Called after configuration checks for a specific command, but before any
	 * real action of a FedoraPackagerCommand is executed. Use this if you need
	 * to do something before a command executes.
	 * 
	 * @throws CommandListenerException
	 *             May be used to signify pre-execution problems.
	 */
	public void preExecution() throws CommandListenerException;

	/**
	 * Called just before a command finished execution. Use this if you need to
	 * do something immediately after a command executes.
	 * 
	 * @throws CommandListenerException
	 *             May be used to signify post execution problems.
	 */
	public void postExecution() throws CommandListenerException;
}
