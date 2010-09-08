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
package org.fedoraproject.eclipse.packager.cvs;

import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	
	private static final String BUNDLE_NAME = "org.fedoraproject.eclipse.packager.cvs.messages"; //$NON-NLS-1$
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);
	
	// FedoraCheckoutWizard Strings
	public static String fedoraCheckoutWizard_fedoraCVSWarning;
	public static String fedoraCheckoutWizard_fedoraCertNotFound;
	public static String fedoraCheckoutWizard_fedoraCVSProblem;
	public static String fedoraCheckoutWizard_fedoraCertRetrieveProblem;
	// CommitHandler Strings
	public static String commitHandler_jobName;
	public static String commitHandler_commitFedoraCVS;
	public static String commitHandler_prepCommitMsg;
	// TagHandler Strings
	public static String tagHandler_jobName;
	public static String tagHandler_tagTaskName;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		super();
	}
}
