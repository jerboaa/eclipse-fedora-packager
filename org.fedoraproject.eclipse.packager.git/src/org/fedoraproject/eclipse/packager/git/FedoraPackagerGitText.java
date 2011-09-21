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
package org.fedoraproject.eclipse.packager.git;

import org.eclipse.osgi.util.NLS;

/**
 * Standard messages class.
 *
 */
public class FedoraPackagerGitText extends NLS {
	
	private static final String BUNDLE_NAME = "org.fedoraproject.eclipse.packager.git.fedorapackagergittext"; //$NON-NLS-1$
	// FedoraCheckoutWizard Strings
	/****/ public static String FedoraPackagerGitCloneWizard_authFail;
	/****/ public static String FedoraPackagerGitCloneWizard_cloneFail;
	/****/ public static String FedoraPackagerGitCloneWizard_cloneCancel;
	/****/ public static String FedoraPackagerGitCloneWizard_confirmDialogTitle;
	/****/ public static String FedoraPackagerGitCloneWizard_confirmOverwirteProjectExists;
	/****/ public static String FedoraPackagerGitCloneWizard_filesystemResourceExistsQuestion;
	/****/ public static String FedoraPackagerGitCloneWizard_createLocalBranchesJob;
	/****/ public static String FedoraPackagerGitCloneWizard_repositoryNotFound;
	/****/ public static String FedoraPackagerGitCloneWizard_wizardTitle;
	/****/ public static String FedoraPackagerGitCloneWizard_problem;
	/****/ public static String FedoraPackagerGitCloneWizard_badURIError;
	/****/ public static String FedoraPackagerGitCloneWizard_switchPerspectiveQuestionTitle;
	/****/ public static String FedoraPackagerGitCloneWizard_switchPerspectiveQuestionMsg;

	// SelectModulePage Strings
	/****/ public static String SelectModulePage_anonymousCheckout;
	/****/ public static String SelectModulePage_badPackageName;
	/****/ public static String SelectModulePage_packageSelection;
	/****/ public static String SelectModulePage_choosePackage;
	/****/ public static String SelectModulePage_packageName;
	/****/ public static String SelectModulePage_workingSets;
	/****/ public static String SelectModulePage_optionsGroup;
	/****/ public static String SelectModulePage_anonymousCloneInfoMsg;
	/****/ public static String SelectModulePage_sshCloneInfoMsg;
	/****/ public static String SelectModulePage_userSelectedAnonymousCloneInfoMsg;
	// FedoraPackagerGitCloneOperation
	/****/ public static String FedoraPackagerGitCloneOperation_operationMisconfiguredError;
	// FedoraPackagerGitPreferencesPage
	/****/ public static String FedoraPackagerGitPreferencePage_description;
	/****/ public static String FedoraPackagerGitPreferencePage_cloneBaseURLLabel;
	/****/ public static String FedoraPackagerGitPreferencePage_gitGroupName;
	/****/ public static String FedoraPackagerGitPreferencePage_invalidBaseURLMsg;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, FedoraPackagerGitText.class);
	}

	private FedoraPackagerGitText() {
		super();
	}
}
