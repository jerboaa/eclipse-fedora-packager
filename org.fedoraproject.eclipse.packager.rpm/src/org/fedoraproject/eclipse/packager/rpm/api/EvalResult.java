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
