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
public class NonTranslatableStrings {

	// NOTE:
	// This has been implemented this way for a reason. If you think this must
	// absolutely change, please ask first.
	
	/**
	 * @param root The project root.
	 * @return The name of this product.
	 */
	public static String getProductName(IProjectRoot root) {
		return getDistributionName(root) + " Packager"; //$NON-NLS-1$
	}
	
	/**
	 * @param root The project root.
	 * @return The name of this distribution.
	 */
	public static String getDistributionName(IProjectRoot root) {
		return "Fedora"; //$NON-NLS-1$
	}
	
	/**
	 * @param root The project root.
	 * @return The name of the build infrastructure.
	 */
	public static String getBuildToolName(IProjectRoot root) {
		return "Koji"; //$NON-NLS-1$
	}
	
	/**
	 * @param root The project root.
	 * @return The name of the update infrastructure. 
	 */
	public static String getUpdateToolName(IProjectRoot root) {
		return "Bodhi"; //$NON-NLS-1$
	}
}
