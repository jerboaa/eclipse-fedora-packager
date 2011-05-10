package org.fedoraproject.eclipse.packager.rpm.api;

/**
 * Result of a call to {@link MockBuildCommand}.
 */
public class MockBuildResult extends Result {

	
	/**
	 * 
	 * @param cmdList
	 */
	public MockBuildResult(String[] cmdList) {
		super(cmdList);
	}
	
	/* (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.api.ICommandResult#wasSuccessful()
	 */
	@Override
	public boolean wasSuccessful() {
		// TODO Auto-generated method stub
		return false;
	}

}
