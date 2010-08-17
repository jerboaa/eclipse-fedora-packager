/*******************************************************************************
 * Copyright (c) 2010 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
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
