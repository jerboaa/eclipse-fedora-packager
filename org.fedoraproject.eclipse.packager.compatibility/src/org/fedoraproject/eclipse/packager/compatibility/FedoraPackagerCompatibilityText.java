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
package org.fedoraproject.eclipse.packager.compatibility;

import org.eclipse.osgi.util.NLS;

public class FedoraPackagerCompatibilityText extends NLS {
	private static final String BUNDLE_NAME = "org.fedoraproject.eclipse.packager.compatibility.fedorapackagercompatibilitytext"; //$NON-NLS-1$

	// TrackExistingAction
	/****/ public static String TrackExistingAction_Description;
	/****/ public static String TrackExistingAction_Error;
	/****/ public static String TrackExistingAction_TrackAddingFailure;
	/****/ public static String TrackExistingAction_ListHeader;
	/****/ public static String TrackExistingAction_NotificationTitle;

	
	static {
		initializeMessages(BUNDLE_NAME,	FedoraPackagerCompatibilityText.class);
	}
}
