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
 * Thrown if CVS tagging of sources failed.
 *
 */
public class TagSourcesException extends FedoraPackagerAPIException {

	private static final long serialVersionUID = -7183217099259208591L;

	/**
	 * @param msg
	 */
	public TagSourcesException(String msg) {
		super(msg);
	}
	
	/**
	 * @param msg
	 * @param cause
	 */
	public TagSourcesException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
