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
package org.fedoraproject.eclipse.packager.rpm.api;

import org.fedoraproject.eclipse.packager.api.UploadSourceCommand;

/**
 * Callback interface for SRPM imports.
 *
 */
public interface ISRPMImportCommandSLLPolicyCallback {

	/**
	 * Implementors can may set the SSL policy for uploads via this callback. The {@link SRPMImportCommand} is responsible for calling it where it needs to.
	 * @param uploadCmd
	 * @param uploadUrl
	 */
	public void setSSLPolicy(UploadSourceCommand uploadCmd, String uploadUrl);
}
