/*******************************************************************************
 * Copyright (c) 2010 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.fedoraproject.eclipse.packager.rpm.internal.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.rpm.core.utils.Utils;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.fedoraproject.eclipse.packager.ConsoleWriterThread;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.IFpProjectBits;
import org.fedoraproject.eclipse.packager.PackagerPlugin;
import org.fedoraproject.eclipse.packager.api.DownloadSourceCommand;
import org.fedoraproject.eclipse.packager.api.FedoraPackager;
import org.fedoraproject.eclipse.packager.api.FedoraPackagerAbstractHandler;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
import org.fedoraproject.eclipse.packager.api.errors.DownloadFailedException;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandInitializationException;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandNotFoundException;
import org.fedoraproject.eclipse.packager.api.errors.InvalidCheckSumException;
import org.fedoraproject.eclipse.packager.api.errors.SourcesUpToDateException;
import org.fedoraproject.eclipse.packager.rpm.RPMPlugin;
import org.fedoraproject.eclipse.packager.rpm.RpmText;
import org.fedoraproject.eclipse.packager.utils.FedoraHandlerUtils;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;
import org.fedoraproject.eclipse.packager.utils.RPMUtils;

/**
 * Handler containing common functionality for implementations using rpm calls. 
 *
 */
public abstract class RpmBuildHandler extends FedoraPackagerAbstractHandler {
	protected static final QualifiedName KEY = new QualifiedName(
			RPMPlugin.PLUGIN_ID, "source"); //$NON-NLS-1$

	/**
	 * Name of the console used for displaying rpm build output.
	 */
	public static final String CONSOLE_NAME = RpmText.RpmBuildHandler_consoleName;

	protected IResource specfile;

	protected MessageConsole getConsole(String name) {
		MessageConsole ret = null;
		for (IConsole cons : ConsolePlugin.getDefault().getConsoleManager()
				.getConsoles()) {
			if (cons.getName().equals(name)) {
				ret = (MessageConsole) cons;
			}
		}
		// no existing console, create new one
		if (ret == null) {
			ret = new MessageConsole(name,
					PackagerPlugin.getImageDescriptor("icons/rpm.gif")); //$NON-NLS-1$
		}
		ret.clearConsole();
		return ret;
	}

	protected IStatus rpmBuild(FedoraProjectRoot fedoraprojectRoot, List<String> flags, IProgressMonitor monitor) {
		monitor.subTask(NLS.bind(
				RpmText.RpmBuildHandler_callRpmBuildMsg, specfile.getName()));
		IResource parent = specfile.getParent();
		String dir = parent.getLocation().toString();
		List<String> defines = RPMUtils.getRPMDefines(dir);
		IFpProjectBits projectBits = FedoraPackagerUtils.getVcsHandler(fedoraprojectRoot);

		List<String> distDefines = RPMUtils.getDistDefines(projectBits, parent.getName());

		defines.add(0, "rpmbuild"); //$NON-NLS-1$
		defines.addAll(distDefines);
		defines.addAll(flags);
		defines.add(specfile.getLocation().toString());

		InputStream is;
		IStatus status = null;
		try {
			is = Utils.runCommandToInputStream(defines.toArray(new String[0]));
			status = runShellCommand(is, monitor);
		} catch (IOException e) {
			e.printStackTrace();
			FedoraHandlerUtils.handleError(e);
		}
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}

		// refresh containing folder
		try {
			parent.refreshLocal(IResource.DEPTH_INFINITE,
					new NullProgressMonitor());
		} catch (CoreException e) {
			e.printStackTrace();
			FedoraHandlerUtils.handleError(e);
		}
		return status;
	}

	protected IStatus runShellCommand(InputStream is, IProgressMonitor mon) {
		boolean terminateMonitor = false;
		if (mon == null) {
			terminateMonitor = true;
			mon = new NullProgressMonitor();
			mon.beginTask(RpmText.RpmBuildHandler_runShellCmds, 1);
		}
		IStatus status;
		final MessageConsole console = getConsole(CONSOLE_NAME);
		IConsoleManager manager = ConsolePlugin.getDefault()
				.getConsoleManager();
		manager.addConsoles(new IConsole[] { console });
		console.activate();

		final MessageConsoleStream outStream = console.newMessageStream();
		final MessageConsoleStream errStream = console.newMessageStream();

		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				errStream.setColor(new Color(Display.getDefault(), 255, 0, 0));
			}

		});

		try {
			// create thread for reading inputStream (process' stdout)
			ConsoleWriterThread outThread = new ConsoleWriterThread(is,
					outStream);
			// start both threads
			outThread.start();

			while (!mon.isCanceled()) {
				try {
					// Don't waste system resources
					Thread.sleep(300);
					break;
				} catch (IllegalThreadStateException e) {
					// Do nothing
				}
			}

			if (mon.isCanceled()) {
				outThread.close();
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						MessageDialog.openError(
								new Shell(),
								RpmText.RpmBuildHandler_scriptCancelled,
								RpmText.RpmBuildHandler_userWarningMsg);
					}

				});
				FedoraHandlerUtils.handleError(RpmText.RpmBuildHandler_terminationMsg);
				return Status.CANCEL_STATUS;
			}

			if (terminateMonitor)
				mon.done();

			// finish reading whatever's left in the buffers
			outThread.join();

			status = Status.OK_STATUS;
		} catch (InterruptedException e) {
			e.printStackTrace();
			status = Status.OK_STATUS;
		}
		return status;
	}

	protected String rpmEval(String format) throws CoreException {
		String cmd[] = { "rpm", "--eval", "%{" + format + "}" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		String result;
		try {
			result = Utils.runCommandToString(cmd);
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					RPMPlugin.PLUGIN_ID, e.getMessage(), e));
		}

		return result.substring(0, result.indexOf('\n'));
	}

	protected IStatus makeSRPM(FedoraProjectRoot fedoraProjectRoot, IProgressMonitor monitor) {
		final FedoraPackager fp = new FedoraPackager(fedoraProjectRoot);
		// First download sources
		DownloadSourceCommand downloadCmd;
		try {
			downloadCmd = (DownloadSourceCommand)fp.getCommandInstance(DownloadSourceCommand.ID);
		} catch (FedoraPackagerCommandInitializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (FedoraPackagerCommandNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		try {
			downloadCmd.call(monitor);
		} catch (SourcesUpToDateException e1) {
			// TODO handle appropriately
		} catch (DownloadFailedException e1) {
			// TODO handle appropriately
		} catch (CommandListenerException e1) {
			// TODO handle appropriately
		} catch (CommandMisconfiguredException e1) {
			// TODO handle appropriately
		}
		IStatus result;
		ArrayList<String> flags = new ArrayList<String>();
		flags.add("--nodeps"); //$NON-NLS-1$
		flags.add("-bs"); //$NON-NLS-1$
		result = rpmBuild(fedoraProjectRoot, flags, monitor);
		return result;
	}
	
}
