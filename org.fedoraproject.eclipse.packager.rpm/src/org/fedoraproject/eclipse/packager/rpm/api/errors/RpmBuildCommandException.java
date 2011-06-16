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
package org.fedoraproject.eclipse.packager.rpm.api.errors;

import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerAPIException;

/**
 * Thrown when something caused an RPM build command to fail.
 *
 */
public class RpmBuildCommandException extends FedoraPackagerAPIException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1189026188243127676L;

	/**
	 * 
	 * @param msg A message indicating what went wrong.
	 * @param cause The actual cause.
	 */
	public RpmBuildCommandException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
