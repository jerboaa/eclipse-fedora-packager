package org.fedoraproject.eclipse.packager.rpm.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.linuxtools.rpm.core.utils.Utils;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.fedoraproject.eclipse.packager.FedoraPackagerLogger;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.api.FedoraPackagerCommand;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandInitializationException;
import org.fedoraproject.eclipse.packager.rpm.RpmText;
import org.fedoraproject.eclipse.packager.rpm.api.errors.RpmBuildCommandException;
import org.fedoraproject.eclipse.packager.rpm.internal.core.ConsoleWriter;
import org.fedoraproject.eclipse.packager.rpm.internal.core.RpmConsoleFilterObserver;
import org.fedoraproject.eclipse.packager.utils.RPMUtils;

/**
 * Command for executing prep, binary builds and source RPM builds.
 *
 */
public class RpmBuildCommand extends FedoraPackagerCommand<RpmBuildResult> {
	
	/**
	 *  The unique ID of this command.
	 */
	public static final String ID = "RpmBuildCommand"; //$NON-NLS-1$
	/**
	 * No dependencies flag
	 */
	public static final String NO_DEPS = "--nodeps"; //$NON-NLS-1$
	
	private static final String RPMBUILD_CMD = "rpmbuild"; //$NON-NLS-1$
	private static final String SOURCEBUILD_OPTION = "-bs"; //$NON-NLS-1$
	private static final String BINARYBUILD_OPTION = "-ba"; //$NON-NLS-1$
	private static final String PREP_OPTION = "-bp"; //$NON-NLS-1$
	
	private List<String> fullRpmBuildCommand;
	private List<String> buildTypeFlags;
	private BuildType buildType;
	private List<String> distDefines;
	private List<String> flags;
	
	/**
	 * The build type.
	 *
	 */
	public enum BuildType {
		/**
		 * Corresponds to rpmbuild's -ba
		 */
		BINARY,
		/**
		 * Corresponds to rpmbuild's -bs
		 */
		SOURCE,
		/**
		 * Corresponds to rpmbuild's -bp
		 */
		PREP
	}
	
	/**
	 * Sets the build type which is triggered.
	 * 
	 * @param type
	 * @return This instance.
	 */
	public RpmBuildCommand buildType(BuildType type) {
		this.buildType = type;
		buildTypeFlags = new ArrayList<String>(1);
		switch (type) {
		case BINARY:
			buildTypeFlags.add(BINARYBUILD_OPTION);
			break;
		case SOURCE:
			buildTypeFlags.add(SOURCEBUILD_OPTION);
			break;
		case PREP:
			buildTypeFlags.add(PREP_OPTION);
			break;
		}
		return this;
	}
	
	/**
	 * Set the distribution specific defines.
	 * 
	 * @param distDefines
	 * @return This instance
	 * @throws IllegalArgumentException If {@code null} was passed.
	 */
	public RpmBuildCommand distDefines(List<String> distDefines)
			throws IllegalArgumentException {
		if (distDefines == null) {
			throw new IllegalArgumentException(
					RpmText.RpmBuildCommand_distDefinesNullError);
		}
		this.distDefines = distDefines;
		return this;
	}
	
	/**
	 * Set some additional flags.
	 * 
	 * @param flags
	 * @return This instance
	 * @throws IllegalArgumentException If {@code null} was passed.
	 */
	public RpmBuildCommand flags(List<String> flags)
			throws IllegalArgumentException {
		if (flags == null) {
			throw new IllegalArgumentException(
					RpmText.RpmBuildCommand_flagsNullError);
		}
		this.flags = flags;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.fedoraproject.eclipse.packager.api.FedoraPackagerCommand#
	 * checkConfiguration()
	 */
	@Override
	protected void checkConfiguration() throws CommandMisconfiguredException {
		// built type is the only required config
		if (buildTypeFlags == null) {
			throw new CommandMisconfiguredException(RpmText.RpmBuildCommand_buildTypeRequired);
		}
	}

	/**
	 * Implementation of rpm build command. Triggers a build as configured.
	 * 
	 * @throws CommandMisconfiguredException
	 *             If the command isn't properly configured.
	 * @throws CommandListenerException
	 *             If a command listener failed.
	 * @throws RpmBuildCommandException
	 *             If some error occurred while building.
	 */
	@Override
	public RpmBuildResult call(IProgressMonitor monitor)
			throws CommandMisconfiguredException, CommandListenerException,
			RpmBuildCommandException {
		try {
			callPreExecListeners();
		} catch (CommandListenerException e) {
			if (e.getCause() instanceof CommandMisconfiguredException) {
				// explicitly throw the specific exception
				throw (CommandMisconfiguredException)e.getCause();
			}
			throw e;
		}
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		monitor.subTask(NLS.bind(
				RpmText.RpmBuildCommand_callRpmBuildMsg, this.projectRoot.getSpecFile().getName()));
		// log the build command
		logCommand();
		
		InputStream is;
		String[] cmdList = getBuildCommandList();
		RpmBuildResult result = new RpmBuildResult(cmdList, buildType);
		try {
			is = Utils.runCommandToInputStream(cmdList);
			
		} catch (IOException e) {
			throw new RpmBuildCommandException(e.getMessage(), e);
		}
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		
		// Do the console writing
		final MessageConsole console = FedoraPackagerConsole.getConsole();
		IConsoleManager manager = ConsolePlugin.getDefault()
				.getConsoleManager();
		manager.addConsoles(new IConsole[] { console });
		console.activate();

		final MessageConsoleStream outStream = console.newMessageStream();

		try {
			// First create observable console writer
			ConsoleWriter worker = new ConsoleWriter(is, outStream);
			// add observer for SRPM builds and binary builds
			if (this.buildType == BuildType.SOURCE || this.buildType == BuildType.BINARY) {
				worker.addObserver(new RpmConsoleFilterObserver(result));
			}
			// create the thread for process input processing
			Thread consoleWriterThread = new Thread(worker);
			consoleWriterThread.start();

			while (!monitor.isCanceled()) {
				try {
					// Don't waste system resources
					Thread.sleep(300);
					break;
				} catch (IllegalThreadStateException e) {
					// Do nothing
				}
			}

			if (monitor.isCanceled()) {
				worker.stop(); // Stop the worker thread
				throw new OperationCanceledException();
			}

			// finish reading whatever's left in the buffers
			consoleWriterThread.join();

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		

		// refresh containing folder
		try {
			this.projectRoot.getContainer().refreshLocal(IResource.DEPTH_INFINITE,
					monitor);
		} catch (CoreException ignored) {
			// ignore
		}
		
		callPostExecListeners();
		setCallable(false); // reuse of instance's call() not allowed
		monitor.done();
		result.setSuccess(true);
		return result;
	}
	
	/**
	 * Do some extra initialization. Set build command and required defines
	 * for this Fedora project root.
	 */
	@Override
	public void initialize(FedoraProjectRoot fp) throws FedoraPackagerCommandInitializationException {
		super.initialize(fp);
		fullRpmBuildCommand = new ArrayList<String>();
		fullRpmBuildCommand.add(RPMBUILD_CMD);
		List<String> defaultDefines = RPMUtils.getRPMDefines(fp.getContainer().getLocation().toOSString());
		fullRpmBuildCommand.addAll(defaultDefines);
	}
	
	/**
	 * 
	 * @return An array of the configured command lists.
	 */
	private String[] getBuildCommandList() {
		assert buildTypeFlags != null;
		// Prep does not need dist defines
		if (distDefines != null) {
			fullRpmBuildCommand.addAll(distDefines);
		}
		// Not all variations use flags
		if (flags != null) {
			fullRpmBuildCommand.addAll(flags);
		}
		fullRpmBuildCommand.addAll(buildTypeFlags);
		fullRpmBuildCommand.add(this.projectRoot.getSpecFile().getLocation().toOSString());
		return fullRpmBuildCommand.toArray(new String[0]);
	}
	
	private void logCommand() {
		String cmd = new String();
		for (String token: getBuildCommandList()) {
			cmd += token + " "; //$NON-NLS-1$;
		}
		FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
		logger.logInfo(NLS.bind(RpmText.RpmBuildCommand_commandStringMsg, cmd));
	}

}
