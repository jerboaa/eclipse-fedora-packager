/*******************************************************************************
 * Copyright (c) 2010-2011 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.fedoraproject.eclipse.packager.internal.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.fedoraproject.eclipse.packager.FedoraPackagerPreferencesConstants;
import org.fedoraproject.eclipse.packager.PackagerPlugin;

/**
 * Class for initialization of Eclipse Fedora Packager preferences.
 */
public class FedoraPackagerPreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		// set default preferences for this plug-in
		IEclipsePreferences node = new DefaultScope().getNode(PackagerPlugin.PLUGIN_ID);
		// General
		node.putBoolean(FedoraPackagerPreferencesConstants.PREF_DEBUG_MODE, FedoraPackagerPreferencesConstants.DEFAULT_DEBUG_MODE);
		// Lookaside prefs
		node.put(FedoraPackagerPreferencesConstants.PREF_LOOKASIDE_DOWNLOAD_URL,
				FedoraPackagerPreferencesConstants.DEFAULT_LOOKASIDE_DOWNLOAD_URL);
		node.put(FedoraPackagerPreferencesConstants.PREF_LOOKASIDE_UPLOAD_URL,
				FedoraPackagerPreferencesConstants.DEFAULT_LOOKASIDE_UPLOAD_URL);
		// Koji prefs
		node.put(FedoraPackagerPreferencesConstants.PREF_KOJI_WEB_URL, FedoraPackagerPreferencesConstants.DEFAULT_KOJI_WEB_URL);
		node.put(FedoraPackagerPreferencesConstants.PREF_KOJI_HUB_URL, FedoraPackagerPreferencesConstants.DEFAULT_KOJI_HUB_URL);
	}

}
