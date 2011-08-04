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

	// AddNewBuildDialog
	/****/ public static String AddNewBuildDialog_addAnotherBuild;
	/****/ public static String AddNewBuildDialog_addBtn;
	/****/ public static String AddNewBuildDialog_buildsFormatErrorMsg;
	/****/ public static String AddNewBuildDialog_buildsToolTip;
	/****/ public static String AddNewBuildDialog_cancelBtn;
	/****/ public static String AddNewBuildDialog_dialogTitle;
	/****/ public static String AddNewBuildDialog_packageBuildsLbl;
	// BodhiClient Strings
	/****/ public static String BodhiClient_rawJsonStringMsg;
	// PushUpdateCommand
	/****/ public static String PushUpdateCommand_configErrorNoClient;
	/****/ public static String PushUpdateCommand_configErrorNoUpdateType;
	/****/ public static String PushUpdateCommand_configErrorNoFedoraRelease;
	/****/ public static String PushUpdateCommand_configErrorNoUpdateComment;
	/****/ public static String PushUpdateCommand_configErrorNoBuilds;
	/****/ public static String PushUpdateCommand_configErrorUsernamePasswordUnset;
	/****/ public static String PushUpdateCommand_pushingBodhiUpdateTaskMsg;
	/****/ public static String PushUpdateCommand_loggingIn;
	/****/ public static String PushUpdateCommand_pushingUpdate;
	/****/ public static String PushUpdateCommand_loggingOut;
	// BodhiNewHandler
	/****/ public static String BodhiNewHandler_createUpdateMsg;
	/****/ public static String BodhiNewHandler_unpushedChangesJobMsg;
	/****/ public static String BodhiNewHandler_unpushedChangesQuestion;
	/****/ public static String BodhiNewHandler_validationJobName;
	/****/ public static String BodhiNewHandler_credentialsErrorMsg;
	/****/ public static String BodhiNewHandler_pushingUpdateFailedMsg;
	/****/ public static String BodhiNewHandler_systemPropertyUrlInvalid;
	/****/ public static String BodhiNewHandler_updateLoginMsg;
	/****/ public static String BodhiNewHandler_updateCreatedLogMsg;
	// BodhiNewUpdateDialog
	/****/ public static String BodhiNewUpdateDialog_addBuildsBtn;
	/****/ public static String BodhiNewUpdateDialog_addBuildsBtnTooltip;
	/****/ public static String BodhiNewUpdateDialog_bugsLbl;
	/****/ public static String BodhiNewUpdateDialog_bugsTooltip;
	/****/ public static String BodhiNewUpdateDialog_buildsSelectionErrorMsg;
	/****/ public static String BodhiNewUpdateDialog_buildsTooltip;
	/****/ public static String BodhiNewUpdateDialog_cancelUpdateBtn;
	/****/ public static String BodhiNewUpdateDialog_closeBugsBtn;
	/****/ public static String BodhiNewUpdateDialog_closeBugsTooltip;
	/****/ public static String BodhiNewUpdateDialog_createNewUpdateTitle;
	/****/ public static String BodhiNewUpdateDialog_enableKarmaAutomatismLbl;
	/****/ public static String BodhiNewUpdateDialog_enableKarmaAutomatismTooltip;
	/****/ public static String BodhiNewUpdateDialog_invalidBugsErrorMsg;
	/****/ public static String BodhiNewUpdateDialog_invalidNotesErrorMsg;
	/****/ public static String BodhiNewUpdateDialog_invalidRequestTypeErrorMsg;
	/****/ public static String BodhiNewUpdateDialog_invalidStableKarmaErrorMsg;
	/****/ public static String BodhiNewUpdateDialog_invalidUnstableKarmaMsg;
	/****/ public static String BodhiNewUpdateDialog_notesHtmlTooltipTxt;
	/****/ public static String BodhiNewUpdateDialog_notesLbl;
	/****/ public static String BodhiNewUpdateDialog_packageLbl;
	/****/ public static String BodhiNewUpdateDialog_requestTypeLbl;
	/****/ public static String BodhiNewUpdateDialog_saveUpdateBtn;
	/****/ public static String BodhiNewUpdateDialog_stableKarmaThresholdLbl;
	/****/ public static String BodhiNewUpdateDialog_stableKarmaTooltip;
	/****/ public static String BodhiNewUpdateDialog_suggestRebootLbl;
	/****/ public static String BodhiNewUpdateDialog_suggestRebootTooltip;
	/****/ public static String BodhiNewUpdateDialog_typeLbl;
	/****/ public static String BodhiNewUpdateDialog_unstableKarmaThresholdLbl;
	/****/ public static String BodhiNewUpdateDialog_unstableKarmaTooltip;
	// BodhiUpdateInfoDialog
	/****/ public static String BodhiUpdateInfoDialog_updateResponseTitle;
	/****/ public static String BodhiUpdateInfoDialog_updateStatusText;
	// UserValidationDialog
	/****/ public static String UserValidationDialog_passwordRequired;
	/****/ public static String UserValidationDialog_server;
	/****/ public static String UserValidationDialog_savePassword;
	/****/ public static String UserValidationDialog_password;
	/****/ public static String UserValidationDialog_username;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, BodhiText.class);
	}
}
