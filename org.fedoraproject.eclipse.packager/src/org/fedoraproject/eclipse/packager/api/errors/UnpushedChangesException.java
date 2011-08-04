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
 * Thrown if a build has been attempted to be pushed to Koji, but there have
 * been unpushed changes on the current branch of the local repository.
 */
public class UnpushedChangesException extends FedoraPackagerAPIException {

	private static final long serialVersionUID = 8776718118316661590L;

	/**
	 * Default constructor.
	 * 
	 * @param msg
	 */
	public UnpushedChangesException(String msg) {
		super(msg);
	}
}
