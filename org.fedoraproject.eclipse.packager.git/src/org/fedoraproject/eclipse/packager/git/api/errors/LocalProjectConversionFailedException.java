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
package org.fedoraproject.eclipse.packager.git.api.errors;

import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerAPIException;

/**
 * Thrown if converting a local project to the 
 * corresponding remote one was not successful
 *
 */
public class LocalProjectConversionFailedException extends
		FedoraPackagerAPIException {

	private static final long serialVersionUID = 9098621863061603960L;

	/**
	 * @param message
	 * @param cause
	 */
	public LocalProjectConversionFailedException(String message,
			Throwable cause) {
		super(message, cause);
	}
}
