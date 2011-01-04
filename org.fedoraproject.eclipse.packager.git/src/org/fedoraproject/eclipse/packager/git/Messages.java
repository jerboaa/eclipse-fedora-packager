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
package org.fedoraproject.eclipse.packager.git;

import org.eclipse.osgi.util.NLS;

/**
 * Standard messages class.
 *
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.fedoraproject.eclipse.packager.git.messages"; //$NON-NLS-1$
	
	// FedoraCheckoutWizard Strings
	public static String fedoraCheckoutWizard_cloneFail;
	public static String fedoraCheckoutWizard_cloneCancel;
	public static String fedoraCheckoutWizard_projectExists;
	public static String fedoraCheckoutWizard_filesystemResourceExists;
	public static String fedoraCheckoutWizard_createLocalBranchesJob;
	public static String fedoraCheckoutWizard_repositoryNotFound;
	public static String fedoraCheckoutWizard_wizardTitle;
	public static String fedoraCheckoutWizard_problem;
	// SelectModulePage Strings
	public static String selectModulePage_packageSelection;
	public static String selectModulePage_choosePackage;
	public static String selectModulePage_packageName;
	public static String selectModulePage_workingSets;
	// FpGitProjectBits Strings
	public static String fpGitProjectBits_fetchJobName;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		super();
	}
}
