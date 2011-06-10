package org.fedoraproject.eclipse.packager;

/**
 * Interface for the non translatable strings file, which might determine names dynamically.
 * Required for the nonTranslatableStrings extension point. See extension point documentation
 * for more info.
 *
 */
public interface IProductStrings {

	/**
	 * Called on object creation in order to perform initialization.
	 * 
	 * @param root
	 */
	public void initialize(IProjectRoot root);
	
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
