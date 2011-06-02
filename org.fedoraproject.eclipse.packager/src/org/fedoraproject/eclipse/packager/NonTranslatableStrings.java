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
	public static String getProductName(FedoraProjectRoot root) {
		return getDistributionName(root) + " Packager"; //$NON-NLS-1$
	}
	
	/**
	 * @param root The project root.
	 * @return The name of this distribution.
	 */
	public static String getDistributionName(FedoraProjectRoot root) {
		return "Fedora"; //$NON-NLS-1$
	}
	
	/**
	 * @param root The project root.
	 * @return The name of the build infrastructure.
	 */
	public static String getBuildToolName(FedoraProjectRoot root) {
		return "Koji"; //$NON-NLS-1$
	}
	
	/**
	 * @param root The project root.
	 * @return The name of the update infrastructure. 
	 */
	public static String getUpdateToolName(FedoraProjectRoot root) {
		return "Bodhi"; //$NON-NLS-1$
	}
}
