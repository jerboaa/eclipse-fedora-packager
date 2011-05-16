package org.fedoraproject.eclipse.packager.rpm.api;

/**
 * Result of a call to {@link MockBuildCommand}.
 */
public class MockBuildResult extends Result {

	private String resultDir;
	
	private boolean success;
	
	/**
	 * @param cmdList
	 * @param resultDir The directory specified to put mock build results into.
	 */
	public MockBuildResult(String[] cmdList, String resultDir) {
		super(cmdList);
		this.resultDir = resultDir;
		// will be set to false by an observer if there was an error
		this.success = true;
	}
	
	/**
	 * 
	 */
	public void setFailure() {
		this.success = false;
	}
	
	/**
	 * 
	 */
	public void setSuccess() {
		this.success = true;
	}
	
	/**
	 *
	 * @return The relative path to the directory containing mock build results.
	 */
	public String getResultDirectoryPath() {
		return this.resultDir;
	}
	
	/* (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.api.ICommandResult#wasSuccessful()
	 */
	@Override
	public boolean wasSuccessful() {
		return success;
	}

}
