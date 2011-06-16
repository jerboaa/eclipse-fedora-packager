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
package org.fedoraproject.eclipse.packager.bodhi;

import org.eclipse.jface.resource.ImageDescriptor;
import org.fedoraproject.eclipse.packager.PackagerPlugin;

/**
 * Bodhi plugin activator.
 *
 */
public class BodhiPlugin extends PackagerPlugin {

	/** The plug-in ID */
	public static final String PLUGIN_ID = "org.fedoraproject.eclipse.packager.bodhi"; //$NON-NLS-1$

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}
