package org.fedoraproject.eclipse.packager.rpm.api;


/**
 * Result of {@link RpmEvalCommand}.
 *
 */
public class EvalResult extends Result {

	private String evalResult;
	
	/**
	 * @param cmdList 
	 * @param result
	 */
	public EvalResult(String[] cmdList, String result) {
		super(cmdList);
		this.evalResult = result;
	}
	
	/**
	 * Get the result of the evaluation.
	 * 
	 * @return The eval result.
	 */
	public String getEvalResult() {
		assert evalResult != null;
		return evalResult.substring(0, evalResult.indexOf('\n'));
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.api.ICommandResult#wasSuccessful()
	 */
	@Override
	public boolean wasSuccessful() {
		return evalResult != null;
	}

}
