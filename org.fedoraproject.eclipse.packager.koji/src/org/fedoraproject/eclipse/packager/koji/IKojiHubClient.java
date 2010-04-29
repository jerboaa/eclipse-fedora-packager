/*******************************************************************************
 * Copyright (c) 2010 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.fedoraproject.eclipse.packager.koji;

import java.net.MalformedURLException;

import org.apache.xmlrpc.XmlRpcException;

public interface IKojiHubClient {

	public abstract String sslLogin() throws XmlRpcException,
			MalformedURLException;

	public abstract void logout() throws MalformedURLException, XmlRpcException;

	public abstract String build(String target, String scmURL, boolean scratch)
			throws XmlRpcException;

}