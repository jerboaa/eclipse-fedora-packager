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

import org.fedoraproject.eclipse.packager.api.FedoraPackagerCommand;

/**
 * Thrown if a FedoraPackager command was not properly configured prior calling
 * {@link FedoraPackagerCommand#call(org.eclipse.core.runtime.IProgressMonitor)}.
 */
public class CommandMisconfiguredException extends FedoraPackagerAPIException {
	
	private static final long serialVersionUID = 2429930146036876634L;
	/**
	 * @param message
	 * @param cause
	 */
	public CommandMisconfiguredException(String message, Throwable cause) {
		super(message, cause);
	}
	/**
	 * @param message
	 */
	public CommandMisconfiguredException(String message) {
		super(message);
	}
}
