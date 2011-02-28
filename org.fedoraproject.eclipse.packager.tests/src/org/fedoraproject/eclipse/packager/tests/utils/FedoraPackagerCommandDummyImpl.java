package org.fedoraproject.eclipse.packager.tests.utils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.api.FedoraPackagerCommand;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;

/**
 * Fixture for {@link FedoraPackagerCommandTest}. It is a very basic
 * implementation of {@link FedoraPackagerCommand}.
 * 
 */
public class FedoraPackagerCommandDummyImpl extends FedoraPackagerCommand<DummyResult> {
	
	// some dummy state.
	private boolean configured = false;
	
	public FedoraPackagerCommandDummyImpl(FedoraProjectRoot root) {
		super(root);
	}

	@Override
	protected void checkConfiguration() throws CommandMisconfiguredException {
		// pretend to require configured set to true
		if (!configured) {
			throw new CommandMisconfiguredException(
					"Dummy command implementation is not configured!"); //$NON-NLS-1$
		}
	}
	
	public void setConfiguration(boolean configured) {
		this.configured = configured;
	}

	/**
	 * Basic template for command implementation.
	 */
	@Override
	public DummyResult call(IProgressMonitor monitor)
			throws CommandMisconfiguredException, CommandListenerException {
		try {
			callPreExecListeners();
		} catch (CommandListenerException e) {
			if (e.getCause() instanceof CommandMisconfiguredException) {
				throw (CommandMisconfiguredException)e.getCause();
			}
			// rethrow
			throw e;
		}
		callPostExecListeners();
		setCallable(false);
		return new DummyResult(true);
	}

}
