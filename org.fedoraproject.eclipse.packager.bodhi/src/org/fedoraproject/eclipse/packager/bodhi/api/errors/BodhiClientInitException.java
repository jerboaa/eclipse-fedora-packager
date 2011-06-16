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
package org.fedoraproject.eclipse.packager.bodhi.api.errors;

import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerAPIException;

/**
 * Thrown if bodhi client initialization failed.
 *
 */
public class BodhiClientInitException extends FedoraPackagerAPIException {

	private static final long serialVersionUID = -5959602555911802342L;
	
	/**
	 * @param msg
	 * @param cause
	 */
	public BodhiClientInitException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
