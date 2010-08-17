package org.fedoraproject.eclipse.packager;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
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
			// try to adapt
			FpProject adaptedResource;
			try {
				adaptedResource = FpProject.doAdapt((IResource) adaptableObject);
			} catch (CoreException e) {
				// Something is seriously wrong, can't adapt.
				return null;
			}
			if (adaptedResource != null)
				return adaptedResource;
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return SUPPORTED_TYPES;
	}

}
