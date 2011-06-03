package org.fedoraproject.eclipse.packager.tests.utils.cvs;

public class BranchNotFoundException extends Exception {

	private static final long serialVersionUID = 5230238987253108386L;

	public BranchNotFoundException(String name) {
		super("Branch " + name + " not found!"); //$NON-NLS-1$, $NON-NLS-2$
	}
}
