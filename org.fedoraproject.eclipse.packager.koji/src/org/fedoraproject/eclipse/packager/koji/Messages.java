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

import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

/**
 * Messages class for String externalization.
 *
 */
public class Messages extends NLS {
	
	private static final String BUNDLE_NAME = "org.fedoraproject.eclipse.packager.koji.messages"; //$NON-NLS-1$
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
	
	// KojiBuildHandler Strings
	public static String kojiBuildHandler_tagBeforeSendingBuild;
	public static String kojiBuildHandler_sendBuildToKoji;
	public static String kojiBuildHandler_jobName;
	public static String kojiBuildHandler_kojiBuild;
	public static String kojiBuildHandler_connectKojiMsg;
	public static String kojiBuildHandler_kojiLogin;
	public static String kojiBuildHandler_sendBuildCmd;
	public static String kojiBuildHandler_kojiLogout;
	// KojiMessageDialog Strings
	public static String kojiMessageDialog_buildNumberMsg;
	public static String kojiMessageDialog_buildResponseMsg;
	// KojiHubClient
	public static String kojiHubClient_invalidHubUrl;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		super();
	}
}
