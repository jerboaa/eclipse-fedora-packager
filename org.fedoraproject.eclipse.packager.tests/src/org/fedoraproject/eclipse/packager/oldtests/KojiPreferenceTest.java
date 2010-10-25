package org.fedoraproject.eclipse.packager.oldtests;

import org.eclipse.jface.preference.IPreferenceStore;
import org.fedoraproject.eclipse.packager.koji.KojiPlugin;
import org.fedoraproject.eclipse.packager.koji.preferences.PreferencesConstants;
import org.fedoraproject.eclipse.packager.oldtests.utils.AbstractTest;

public class KojiPreferenceTest extends AbstractTest {

	private static final String NEW_KOJI_WEB_URL = "http://koji.deltacloud.org";
	private static final String NEW_KOJI_HUB_URL = "https://koji.deltacloud.org/testxmlrpc";
	private IPreferenceStore pluginPrefStore;
	
	@SuppressWarnings("static-access")
	@Override
	public void setUp() throws Exception {
		pluginPrefStore = KojiPlugin.getDefault().getPreferenceStore();
	}
	
	@Override
	public void tearDown() throws Exception {
		// do nothing
	}
	
	public void testKojiHostPreference() {
		// check default preference
		String defaultWebUrl = pluginPrefStore.getString(PreferencesConstants.PREF_KOJI_WEB_URL);
		assertEquals(PreferencesConstants.DEFAULT_KOJI_WEB_URL, defaultWebUrl);
		// set to different url
		pluginPrefStore.setValue(PreferencesConstants.PREF_KOJI_WEB_URL, NEW_KOJI_WEB_URL);
		assertEquals(NEW_KOJI_WEB_URL, pluginPrefStore.getString(PreferencesConstants.PREF_KOJI_WEB_URL));
		
		// check default preference
		String defaultHubUrl = pluginPrefStore.getString(PreferencesConstants.PREF_KOJI_HUB_URL);
		assertEquals(PreferencesConstants.DEFAULT_KOJI_HUB_URL, defaultHubUrl);
		// set to different url
		pluginPrefStore.setValue(PreferencesConstants.PREF_KOJI_HUB_URL, NEW_KOJI_HUB_URL);
		assertEquals(NEW_KOJI_HUB_URL, pluginPrefStore.getString(PreferencesConstants.PREF_KOJI_HUB_URL));
	}

}
