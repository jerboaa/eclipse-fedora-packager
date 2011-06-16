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
package org.fedoraproject.eclipse.packager.api.errors;

/**
 * Common exception superclass for command listeners.
 *
 */
public class CommandListenerException extends FedoraPackagerAPIException {

	private static final long serialVersionUID = -8660306046206103772L;

	/**
	 * Wrap a listener exception in a CommandListenerException. 
	 * 
	 * @param cause
	 */
	public CommandListenerException(Throwable cause) {
		super("unused", cause); //$NON-NLS-1$
	}
}
