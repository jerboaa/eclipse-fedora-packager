package org.fedoraproject.eclipse.packager;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterFactory;

/**
 * Converts an IResource into a FpProject
 * 
 * @author Red Hat Inc.
 *
 */
public class IResourceAdapterFactory implements IAdapterFactory {

	private static Class<?>[] SUPPORTED_TYPES = new Class[] { FpProject.class };

	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		// We can only adapt to FpProject
		if (FpProject.class.equals(adapterType) && adaptableObject instanceof IResource) {
			return new FpProject((IResource) adaptableObject);
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return SUPPORTED_TYPES;
	}

}
