package org.fedoraproject.eclipse.packager.tests.utils;

import org.fedoraproject.eclipse.packager.api.FedoraPackagerCommand;
import org.fedoraproject.eclipse.packager.api.ICommandListener;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;

/**
 * Fixture for
 * {@link FedoraPackagerCommand#addCommandListener(ICommandListener)} testing.
 * 
 */
public class DummyPostExecCmdListener implements ICommandListener {

	public static final String EXCEPTION_MSG = "postExecTest";
	
	@Override
	public void preExecution() throws CommandListenerException {
		// nothing
	}

	@Override
	public void postExecution() throws CommandListenerException {
		// throw some arbitrary exception wrapped as cmd listener ex
		throw new CommandListenerException(new IllegalStateException(EXCEPTION_MSG));
	}

}
