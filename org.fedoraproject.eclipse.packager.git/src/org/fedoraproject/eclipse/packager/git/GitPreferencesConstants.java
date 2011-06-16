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

/**
 * Constants for Git related preferences.
 */
public final class GitPreferencesConstants {

	/*
	 * -------------------------------------------------
	 *                Prefences keys
	 * -------------------------------------------------
	 */
	/***/ public static final String PREF_CLONE_BASE_URL =
			"gitCloneBaseURL"; //$NON-NLS-1$
	
	/*
	 * -------------------------------------------------
	 *          Default values for preferences
	 * -------------------------------------------------
	 */
	
	/***/ public static final String DEFAULT_CLONE_BASE_URL = 
		"pkgs.fedoraproject.org/"; //$NON-NLS-1$
}
