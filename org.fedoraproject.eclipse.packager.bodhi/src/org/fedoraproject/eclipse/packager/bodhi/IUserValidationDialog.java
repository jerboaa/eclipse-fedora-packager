package org.fedoraproject.eclipse.packager.bodhi;

public interface IUserValidationDialog {

	/**
	 * Returns the password entered by the user, or null if the user canceled.
	 * 
	 * @return the entered password
	 */
	public abstract String getPassword();

	/**
	 * Returns the user name entered by the user, or null if the user canceled.
	 * 
	 * @return the entered user name
	 */
	public abstract String getUsername();

	/**
	 * Returns <code>true</code> if the save password checkbox was selected.
	 * 
	 * @return <code>true</code> if the save password checkbox was selected and
	 *         <code>false</code> otherwise.
	 */
	public abstract boolean getAllowCaching();

	public abstract int open();

	public abstract int getReturnCode();

}