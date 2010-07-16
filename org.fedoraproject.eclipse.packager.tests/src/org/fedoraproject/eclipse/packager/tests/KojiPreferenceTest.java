package org.fedoraproject.eclipse.packager.tests;

import org.eclipse.jface.preference.IPreferenceStore;
import org.fedoraproject.eclipse.packager.koji.KojiPlugin;
import org.fedoraproject.eclipse.packager.koji.preferences.PreferencesConstants;

public class KojiPreferenceTest extends AbstractTest {

	private static final String NEW_KOJI_HOST = "koji.deltacloud.org";
	private IPreferenceStore pluginPrefStore;
	
	@Override
	public void setUp() throws Exception {
		pluginPrefStore = KojiPlugin.getDefault().getPreferenceStore();
	}
	
	public void tearDown() throws Exception {
		// do nothing
	}
	
	public void testKojiHostPreference() {
		// check default preference should be "koji.fedoraproject.org"
		String defaultHost = pluginPrefStore.getString(PreferencesConstants.PREF_KOJI_HOST);
		assertEquals(PreferencesConstants.DEFAULT_KOJI_HOST, defaultHost);
		// set to different host
		pluginPrefStore.setValue(PreferencesConstants.PREF_KOJI_HOST, NEW_KOJI_HOST);
		assertEquals(NEW_KOJI_HOST, pluginPrefStore.getString(PreferencesConstants.PREF_KOJI_HOST));
	}

}
