package org.fedoraproject.eclipse.packager.rpm.api;

/**
 * Result of a call to {@link RpmBuildCommand}.
 */
public class RpmBuildResult extends Result {

	private boolean success;
	
	/**
	 * 
	 * @param cmdList
	 */
	public RpmBuildResult(String[] cmdList) {
		super(cmdList);
	}
	
	/**
	 * @param success the success to set
	 */
	public void setSuccess(boolean success) {
		this.success = success;
	}

	/* (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.api.ICommandResult#wasSuccessful()
	 */
	@Override
	public boolean wasSuccessful() {
		return this.success;
	}

}
