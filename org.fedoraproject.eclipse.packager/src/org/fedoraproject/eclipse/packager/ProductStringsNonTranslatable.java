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
package org.fedoraproject.eclipse.packager;

/**
 * Class to retrieve plug-in specific names.
 */
public class ProductStringsNonTranslatable implements IProductStrings {

	@SuppressWarnings("unused")
	private IProjectRoot root;
	
	/**
	 * @param root
	 * needed to be public to enable LocalFedoraPackager to use it
	 */
	// NOTE:
	// This has been implemented this way for a reason. If you think this must
	// absolutely change, please ask first.
	
	public ProductStringsNonTranslatable(IProjectRoot root) {
		this.root = root;
	}
	
	/**
	 * @return The name of this product.
	 */
	@Override
	public String getProductName() {
		return getDistributionName() + " Packager"; //$NON-NLS-1$
	}
	
	/**
	 * @return The name of this distribution.
	 */
	@Override
	public String getDistributionName() {
		return "Fedora"; //$NON-NLS-1$
	}
	
	/**
	 * @return The name of the build infrastructure.
	 */
	@Override
	public String getBuildToolName() {
		return "Koji"; //$NON-NLS-1$
	}
	
	/**
	 * @return The name of the update infrastructure. 
	 */
	@Override
	public String getUpdateToolName() {
		return "Bodhi"; //$NON-NLS-1$
	}
}
