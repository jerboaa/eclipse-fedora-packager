package org.fedoraproject.eclipse.packager.koji;

import org.eclipse.jface.resource.ImageDescriptor;
import org.fedoraproject.eclipse.packager.PackagerPlugin;

public class KojiPlugin extends PackagerPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.fedoraproject.eclipse.packager.koji"; //$NON-NLS-1$

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
