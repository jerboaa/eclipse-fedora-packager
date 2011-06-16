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
package org.fedoraproject.eclipse.packager;


/**
 * Constants for preferences.
 */
public final class FedoraPackagerPreferencesConstants {

	/*
	 * -------------------------------------------------
	 *                Prefences keys
	 * -------------------------------------------------
	 */
	/***/ public static final String PREF_DEBUG_MODE =
		"debug"; //$NON-NLS-1$
	/***/ public static final String PREF_LOOKASIDE_DOWNLOAD_URL =
			"lookasideDownloadURL"; //$NON-NLS-1$
	/***/ public static final String PREF_LOOKASIDE_UPLOAD_URL =
			"lookasideUploadURL"; //$NON-NLS-1$
	/***/ public static final String PREF_KOJI_WEB_URL = "kojiWebURL"; //$NON-NLS-1$
	/***/ public static final String PREF_KOJI_HUB_URL = "kojiHubURL"; //$NON-NLS-1$
	
	/*
	 * -------------------------------------------------
	 *          Default values for preferences
	 * -------------------------------------------------
	 */
	
	/***/ public static final boolean DEFAULT_DEBUG_MODE = false;
	/***/ public static final String DEFAULT_LOOKASIDE_DOWNLOAD_URL = 
			LookasideCache.DEFAULT_FEDORA_DOWNLOAD_URL;
	/***/ public static final String DEFAULT_LOOKASIDE_UPLOAD_URL = 
			LookasideCache.DEFAULT_FEDORA_UPLOAD_URL;
	/***/ public static final String DEFAULT_KOJI_WEB_URL = 
		"http://koji.fedoraproject.org/koji"; //$NON-NLS-1$
	/***/ public static final String DEFAULT_KOJI_HUB_URL = 
		"https://koji.fedoraproject.org/kojihub"; //$NON-NLS-1$
}
