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
package org.fedoraproject.eclipse.packager.test_utils;

import java.net.MalformedURLException;

import org.apache.xmlrpc.XmlRpcException;
import org.fedoraproject.eclipse.packager.koji.IKojiHubClient;

public class KojiHubClientStub implements IKojiHubClient {
	public String target;
	public String scmURL;
	
	public String build(String target, String scmURL, boolean scratch) throws XmlRpcException {
		this.target = target;
		this.scmURL = scmURL;
		return "1337";
	}

	public void logout() throws MalformedURLException, XmlRpcException {
	}

	public String login() throws XmlRpcException, MalformedURLException {
		return null;
	}
	
	public String getWebUrl() {
		return "http://www.example.com";
	}

}
