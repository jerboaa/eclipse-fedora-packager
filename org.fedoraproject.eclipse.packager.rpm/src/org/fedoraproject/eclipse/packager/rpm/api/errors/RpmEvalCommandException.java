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
import org.fedoraproject.eclipse.packager.rpm.RpmText;
import org.fedoraproject.eclipse.packager.rpm.api.RpmEvalCommand;

/**
 * Thrown if {@link RpmEvalCommand} failed.
 *
 */
public class RpmEvalCommandException extends FedoraPackagerAPIException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2967309558080827035L;

	/**
	 * @param cause
	 */
	public RpmEvalCommandException(Throwable cause) {
		super(RpmText.RpmEvalCommandException_msg, cause);
	}
	
}
