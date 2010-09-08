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
package org.fedoraproject.eclipse.packager.rpm;

import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * Utility class for String externalization.
 *
 */
public class Messages extends NLS {
	
	private static final String BUNDLE_NAME = "org.fedoraproject.eclipse.packager.rpm.messages"; //$NON-NLS-1$
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);
	
	// LocalBuildHandler Strings
	public static String localBuildHandler_buildForLocalArch;
	public static String localBuildHandler_jobName;
	// MockBuildHandler Strings
	public static String mockBuildHandler_jobName;
	public static String mockBuildHandler_testLocalBuildWithMock;
	public static String mockBuildHandler_callMockMsg;
	public static String mockBuildHandler_mockNotInstalled;
	// PrepHandler Strings
	public static String prepHandler_attemptApplyPatchMsg;
	public static String prepHandler_jobName;
	// RPMHandler Strings
	public static String rpmHandler_consoleName;
	public static String rpmHandler_callRpmBuildMsg;
	public static String rpmHandler_runShellCmds;
	public static String rpmHandler_scriptCancelled;
	public static String rpmHandler_userWarningMsg;
	public static String rpmHandler_terminationMsg;
	// SRPMHandler Strings
	public static String srpmHandler_jobName;
	public static String srpmHandler_buildSrpm;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
	private Messages() {
		super();
	}
}
