package org.fedoraproject.eclipse.packager.koji.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.fedoraproject.eclipse.packager.koji.preferences.PreferencesConstants;
import org.fedoraproject.eclipse.packager.koji.KojiPlugin;;

/**
 * Class for initialization of Eclipse Fedora Packager Koji preferences.
 */
public class KojiPluginPreferenceInitializer extends
		AbstractPreferenceInitializer {

	/** 
	 * Initializes a preference store with default preference values 
	 * for this plug-in.
	 */
	@Override
	public void initializeDefaultPreferences() {
		// set default preferences for this plug-in
		IEclipsePreferences node = new DefaultScope().getNode(KojiPlugin.PLUGIN_ID);
		node.put(PreferencesConstants.PREF_KOJI_WEB_URL, PreferencesConstants.DEFAULT_KOJI_WEB_URL);
		node.put(PreferencesConstants.PREF_KOJI_HUB_URL, PreferencesConstants.DEFAULT_KOJI_HUB_URL);
	}

}
