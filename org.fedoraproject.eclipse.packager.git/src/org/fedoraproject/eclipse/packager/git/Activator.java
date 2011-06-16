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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	/** The symbolic ID of the plugin	 */
	public static final String PLUGIN_ID = "org.fedoraproject.eclipse.packager.git"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	/**
	 * Handle an error. The error is logged. If <code>show</code> is
	 * <code>true</code> the error is shown to the user.
	 *
	 * @param message 		a localized message
	 * @param throwable
	 * @param show
	 */
	public static void handleError(String message, Throwable throwable,
			boolean show) {
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, message,
				throwable);
		int style = StatusManager.LOG;
		if (show)
			style |= StatusManager.SHOW;
		StatusManager.getManager().handle(status, style);
	}
	
	/**
	 * @param prefKey
	 * @return The set preference for the given key or {@code null} if not set.
	 */
	public static String getStringPreference(String prefKey) {
		IPreferenceStore store = getDefault().getPreferenceStore();
		String candidate = store.getString(prefKey);
		if (candidate.equals(IPreferenceStore.STRING_DEFAULT_DEFAULT)) {
			return null;
		}
		return candidate;
	}

	/** 
	 * Initializes a preference store with default preference values 
	 * for this plug-in.
	 */
	@Override
	protected void initializeDefaultPreferences(IPreferenceStore store) {
		store.setDefault(GitPreferencesConstants.PREF_CLONE_BASE_URL, GitUtils.getDefaultGitBaseUrl());
	}

}
