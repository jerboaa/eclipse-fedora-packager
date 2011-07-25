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
package org.fedoraproject.eclipse.packager.bodhi;

import org.eclipse.osgi.util.NLS;

/**
 * Utility class for String externalization
 * of the Bodhi plug-in.
 *
 */
public class BodhiText extends NLS {
	
	/**
	 * Do not in-line this into the static initializer as the
	 * "Find Broken Externalized Strings" tool will not be
	 * able to find the corresponding bundle file.
	 * 
	 * This is the path to the file containing externalized strings.
	 */
	private static final String BUNDLE_NAME = "org.fedoraproject.eclipse.packager.bodhi.bodhitext"; //$NON-NLS-1$
	
	// BodhiClient Strings
	/****/ public static String BodhiClient_serverResponseMsg;
	/****/ public static String BodhiClient_rawJsonStringMsg;
	/****/ public static String BodhiClient_httpResponseStringMsg;
	// BodhiNewDialog Strings
	/****/ public static String BodhiNewDialog_dialogTitle;
	/****/ public static String BodhiNewDialog_build;
	/****/ public static String BodhiNewDialog_release;
	/****/ public static String BodhiNewDialog_type;
	/****/ public static String BodhiNewDialog_security;
	/****/ public static String BodhiNewDialog_bugfix;
	/****/ public static String BodhiNewDialog_enhancement;
	/****/ public static String BodhiNewDialog_request;
	/****/ public static String BodhiNewDialog_testing;
	/****/ public static String BodhiNewDialog_stable;
	/****/ public static String BodhiNewDialog_bugIds;
	/****/ public static String BodhiNewDialog_bugIdsMsg;
	/****/ public static String BodhiNewDialog_notes;
	/****/ public static String BodhiNewDialog_invalidBugIds;
	/****/ public static String BodhiNewDialog_invalidBugIdsMsg;
	// BodhiNewHandler
	/****/ public static String BodhiNewHandler_jobName;
	/****/ public static String BodhiNewHandler_createUpdateMsg;
	/****/ public static String BodhiNewHandler_checkTagMsg;
	/****/ public static String BodhiNewHandler_querySpecFileMsg;
	/****/ public static String BodhiNewHandler_updateLoginMsg;
	/****/ public static String BodhiNewHandler_notCorrectTagFail;
	/****/ public static String BodhiNewHandler_connectToBodhi;
	/****/ public static String BodhiNewHandler_loginBodhi;
	/****/ public static String BodhiNewHandler_sendNewUpdate;
	/****/ public static String BodhiNewHandler_logoutMsg;
	// BodhiUpdateInfoDialog
	/****/ public static String BodhiUpdateInfoDialog_updateResponseTitle;
	/****/ public static String BodhiUpdateInfoDialog_updateStatusText;
	// UserValidationDialog
	/****/ public static String userValidationDialog_passwordRequired;
	/****/ public static String userValidationDialog_server;
	/****/ public static String userValidationDialog_savePassword;
	/****/ public static String userValidationDialog_password;
	/****/ public static String userValidationDialog_username;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, BodhiText.class);
	}
}
