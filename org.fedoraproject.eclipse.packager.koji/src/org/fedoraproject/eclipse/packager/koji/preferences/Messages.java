/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.fedoraproject.eclipse.packager.koji.preferences;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.fedoraproject.eclipse.packager.koji.preferences.messages"; //$NON-NLS-1$
	public static String kojiPreferencesPage_kojiWebURLLabel;
	public static String kojiPreferencesPage_kojiHubURLLabel;
	public static String kojiPreferencesPage_description;
	public static String kojiPreferencesPage_kojiWebURLInvalidMsg;
	public static String kojiPreferencesPage_kojiHubURLInvalidMsg;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		super();
	}
}
