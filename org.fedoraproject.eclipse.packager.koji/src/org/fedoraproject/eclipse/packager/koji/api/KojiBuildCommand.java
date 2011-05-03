package org.fedoraproject.eclipse.packager.koji.api;

import org.eclipse.core.runtime.IProgressMonitor;
import org.fedoraproject.eclipse.packager.api.FedoraPackagerCommand;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerAPIException;

/**
 * Fedora Packager koji build command. Supports scratch builds
 * and regular builds.
 */
public class KojiBuildCommand extends FedoraPackagerCommand<BuildResult> {

	/**
	 *  The unique ID of this command.
	 */
	public static final String ID = "KojiBuildCommand"; //$NON-NLS-1$

	private IKojiHubClient kojiClient;
	private boolean isScratchBuild = false;
	
	@Override
	protected void checkConfiguration() throws CommandMisconfiguredException {
		// require a client
		if (kojiClient == null) {
			// TODO: externalize
			throw new CommandMisconfiguredException("no koji client set");
		}
	}
	
	/**
	 * Sets the XMLRPC based client, which will be used for koji interaction.
	 * 
	 * @param client
	 * @return This instance.
	 */
	public KojiBuildCommand setKojiClient(IKojiHubClient client) {
		this.kojiClient = client;
		return this;
	}
	
	/**
	 * Set this to {@code true} if a scratch build should be pushed instead
	 * of a regular build.
	 * 
	 * @param newValue
	 * @return This instance.
	 */
	public KojiBuildCommand setScratchBuild(boolean newValue) {
		this.isScratchBuild = newValue;
		return this;
	}

	@Override
	public BuildResult call(IProgressMonitor monitor) throws FedoraPackagerAPIException {
		try {
			callPreExecListeners();
		} catch (CommandListenerException e) {
			if (e.getCause() instanceof CommandMisconfiguredException) {
				// explicitly throw the specific exception
				throw (CommandMisconfiguredException)e.getCause();
			}
			throw e;
		}
		BuildResult result = new BuildResult();
		// TODO: Implement
		
		// Call post-exec listeners
		callPostExecListeners();
		setCallable(false);
		return result;
	}

}
