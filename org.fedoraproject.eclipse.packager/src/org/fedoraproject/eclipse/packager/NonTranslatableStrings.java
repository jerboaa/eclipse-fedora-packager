package org.fedoraproject.eclipse.packager;

/**
 * Class to retrieve plug-in specific names.
 */
public class NonTranslatableStrings {

	// NOTE:
	// This has been implemented this way for a reason. If you think this must
	// absolutely change, please ask first.
	
	/**
	 * @return The name of this product.
	 */
	public static String getProductName() {
		return getDistributionName() + " Packager"; //$NON-NLS-1$
	}
	
	/**
	 * @return The name of this product.
	 */
	public static String getDistributionName() {
		return "Fedora"; //$NON-NLS-1$
	}
	
	/**
	 * @return The name of the build infrastructure.
	 */
	public static String getBuildToolName() {
		return "Koji"; //$NON-NLS-1$
	}
	
	/**
	 * @return The name of the update infrastructure. 
	 */
	public static String getUpdateToolName() {
		return "Bodhi"; //$NON-NLS-1$
	}
}
