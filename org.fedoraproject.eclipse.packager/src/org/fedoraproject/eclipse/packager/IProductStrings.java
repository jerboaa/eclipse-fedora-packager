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
 * Interface for the non translatable strings file, which might determine names dynamically.
 * Required for the nonTranslatableStrings extension point. See extension point documentation
 * for more info.
 *
 */
public interface IProductStrings {
	
	/**
	 * @return The name of this product.
	 */
	public String getProductName();
	
	/**
	 * @return The name of this distribution.
	 */
	public String getDistributionName();
	
	/**
	 * @return The name of the build infrastructure.
	 */
	public String getBuildToolName();
	
	/**
	 * @return The name of the update infrastructure. 
	 */
	public String getUpdateToolName();
}
