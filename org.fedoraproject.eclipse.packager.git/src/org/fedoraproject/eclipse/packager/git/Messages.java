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
	public static String FedoraCheckoutWizard_0;
	public static String FedoraCheckoutWizard_1;
	public static String FedoraCheckoutWizard_2;
	public static String SelectModulePage_0;
	public static String SelectModulePage_1;
	public static String SelectModulePage_2;
	public static String FedoraCheckoutWizard_createLocalBranchesJob;
	public static String FpGitProjectBits_FetchJobName;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
