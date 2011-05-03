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

import org.eclipse.osgi.util.NLS;

/**
 * Text for the koji plug-in.
 *
 */
public class KojiText extends NLS {
	
	/**
	 * Do not in-line this into the static initializer as the
	 * "Find Broken Externalized Strings" tool will not be
	 * able to find the corresponding bundle file.
	 * 
	 * This is the path to the file containing externalized strings.
	 */
	private static final String BUNDLE_NAME = "org.fedoraproject.eclipse.packager.koji.kojitext"; //$NON-NLS-1$
	
	// KojiBuildHandler Strings
	/****/ public static String KojiBuildHandler_unexpectedSessionId;
	/****/ public static String KojiBuildHandler_unpushedChanges;
	/****/ public static String KojiBuildHandler_tagBeforeSendingBuild;
	/****/ public static String KojiBuildHandler_sendBuildToKoji;
	/****/ public static String KojiBuildHandler_jobName;
	/****/ public static String KojiBuildHandler_kojiBuild;
	/****/ public static String KojiBuildHandler_connectKojiMsg;
	/****/ public static String KojiBuildHandler_kojiLogin;
	/****/ public static String KojiBuildHandler_sendBuildCmd;
	/****/ public static String KojiBuildHandler_kojiLogout;
	/****/ public static String KojiBuildHandler_fallbackBuildMsg;
	/****/ public static String KojiBuildHandler_buildTaskIdError;
	// KojiMessageDialog Strings
	/****/ public static String KojiMessageDialog_buildNumberMsg;
	/****/ public static String KojiMessageDialog_buildResponseMsg;
	// KojiHubClient
	/****/ public static String KojiHubClient_invalidHubUrl;
	
	static {
		initializeMessages(BUNDLE_NAME,	KojiText.class);
	}
}
