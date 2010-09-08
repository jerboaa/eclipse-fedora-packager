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
package org.fedoraproject.eclipse.packager.bodhi;

import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

/**
 * String externalization helper.
 *
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.fedoraproject.eclipse.packager.bodhi.messages"; //$NON-NLS-1$
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);
	
	// BodhiClient Strings
	public static String bodhiClient_serverResponseMsg;
	// BodhiNewDialog Strings
	public static String bodhiNewDialog_dialogTitle;
	public static String bodhiNewDialog_build;
	public static String bodhiNewDialog_release;
	public static String bodhiNewDialog_type;
	public static String bodhiNewDialog_security;
	public static String bodhiNewDialog_bugfix;
	public static String bodhiNewDialog_enhancement;
	public static String bodhiNewDialog_request;
	public static String bodhiNewDialog_testing;
	public static String bodhiNewDialog_stable;
	public static String bodhiNewDialog_bugIds;
	public static String bodhiNewDialog_bugIdsMsg;
	public static String bodhiNewDialog_notes;
	public static String bodhiNewDialog_invalidBugIds;
	public static String bodhiNewDialog_invalidBugIdsMsg;
	// BodhiNewHandler
	public static String bodhiNewHandler_jobName;
	public static String bodhiNewHandler_createUpdateMsg;
	public static String bodhiNewHandler_checkTagMsg;
	public static String bodhiNewHandler_querySpecFileMsg;
	public static String bodhiNewHandler_updateLoginMsg;
	public static String bodhiNewHandler_notCorrectTagFail;
	public static String bodhiNewHandler_connectToBodhi;
	public static String bodhiNewHandler_loginBodhi;
	public static String bodhiNewHandler_sendNewUpdate;
	public static String bodhiNewHandler_logoutMsg;
	// UserValidationDialog
	public static String userValidationDialog_passwordRequired;
	public static String userValidationDialog_server;
	public static String userValidationDialog_savePassword;
	public static String userValidationDialog_password;
	public static String userValidationDialog_username;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
	private Messages() {
		super();
	}
}
