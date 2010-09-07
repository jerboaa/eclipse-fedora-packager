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
package org.fedoraproject.eclipse.packager.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.fedoraproject.eclipse.packager.PackagerPlugin;

/**
 * Class for initialization of Eclipse Fedora Packager preferences.
 */
public class PackagerPluginPreferenceInitializer extends AbstractPreferenceInitializer {

	/**
	 * Set Fedora Packager plug-in default preferences.
	 */
	@Override
	public void initializeDefaultPreferences() {
		// set default preferences for this plug-in
		IEclipsePreferences node = new DefaultScope().getNode(PackagerPlugin.PLUGIN_ID);
		node.put(PreferencesConstants.PREF_LOOKASIDE_DOWNLOAD_URL, PreferencesConstants.DEFAULT_LOOKASIDE_DOWNLOAD_URL);
		node.put(PreferencesConstants.PREF_LOOKASIDE_UPLOAD_URL, PreferencesConstants.DEFAULT_LOOKASIDE_UPLOAD_URL);
	}

}
