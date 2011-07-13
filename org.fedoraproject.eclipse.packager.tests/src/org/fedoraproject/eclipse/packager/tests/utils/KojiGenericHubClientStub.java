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
package org.fedoraproject.eclipse.packager.tests.utils;

import java.util.HashMap;
import java.util.Map;

import org.fedoraproject.eclipse.packager.koji.api.IKojiHubClient;
import org.fedoraproject.eclipse.packager.koji.api.KojiBuildInfo;
import org.fedoraproject.eclipse.packager.koji.api.errors.KojiHubClientException;
import org.fedoraproject.eclipse.packager.koji.api.errors.KojiHubClientLoginException;

/**
 * Stub client, which essentially does nothing. Used for KojiBuildCommand
 * testing.
 *
 */
public class KojiGenericHubClientStub implements IKojiHubClient {

	/*
	 * (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.koji.api.IKojiHubClient#login()
	 */
	@Override
	public HashMap<?, ?> login() throws KojiHubClientLoginException {
		// nothing
		return new HashMap<String, Object>();
	}

	/*
	 * (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.koji.api.IKojiHubClient#logout()
	 */
	@Override
	public void logout() throws KojiHubClientException {
		// nothing
	}

	/*
	 * (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.koji.api.IKojiHubClient#build(java.lang.String, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public int build(String target, String scmURL, String nvr, boolean scratch)
			throws KojiHubClientException {
		return 0xdead; // fake taskId
	}

	/*
	 * (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.koji.api.IKojiHubClient#getBuild(java.lang.String)
	 */
	@Override
	public KojiBuildInfo getBuild(String unused) throws KojiHubClientException {
		Map<String, Object> rawBuildinfoMap = new HashMap<String, Object>();
		rawBuildinfoMap.put("state", new Integer(2));
		rawBuildinfoMap.put("task_id", new Integer(3333));
		rawBuildinfoMap.put("package_id", new Integer(9999));
		rawBuildinfoMap.put("package_name", "eclipse-fedorapackager");
		rawBuildinfoMap.put("epoch", new Integer(1));
		rawBuildinfoMap.put("version", "0.1.12");
		rawBuildinfoMap.put("release", "2.fc15");
		rawBuildinfoMap.put("nvr", "eclipse-fedorapackager-0.1.12-2.fc15");
		return new KojiBuildInfo(rawBuildinfoMap);
	}
	
	@Override
	public boolean uploadFile(String path, String name, int size, String md5sum, int offset, String data){ return true; }

}
