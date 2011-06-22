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
package org.fedoraproject.eclipse.packager.cvs;

import org.eclipse.osgi.util.NLS;

/**
 * String externalizations.
 */
public class CVSText extends NLS {
	
	private static final String BUNDLE_NAME = "org.fedoraproject.eclipse.packager.cvs.cvstext"; //$NON-NLS-1$
	
	// FedoraPackagerCheckoutWizard Strings
	/****/ public static String FedoraPackagerCheckoutWizard_wizardTitle;
	/****/ public static String FedoraPackagerCheckoutWizard_checkoutFailed;
	/****/ public static String FedoraPackagerCheckoutWizard_cancelled;
	// SelectModulePage Strings
	/****/ public static String SelectModulePage_packageSelection;
	/****/ public static String SelectModulePage_choosePackage;
	/****/ public static String SelectModulePage_packageName;
	/****/ public static String SelectModulePage_workingSets;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, CVSText.class);
	}
}
