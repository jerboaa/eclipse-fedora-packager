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
package org.fedoraproject.eclipse.packager.rpm;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.fedoraproject.eclipse.packager.handlers.CommonHandler;
import org.fedoraproject.eclipse.packager.handlers.DownloadHandler;
import org.fedoraproject.eclipse.packager.handlers.FedoraHandlerUtils;

public abstract class RPMHandler extends CommonHandler {
	protected static final QualifiedName KEY = new QualifiedName(
			RPMPlugin.PLUGIN_ID, "source"); //$NON-NLS-1$

	public static final String CONSOLE_NAME = Messages
			.getString("RPMHandler.1"); //$NON-NLS-1$

	protected Map<String, String> sources;
	protected IResource specfile;

	//TODO remove the following 2 methods once we decouple rpm handler from CommonHandler
	@Override
	public IStatus doExecute(ExecutionEvent event, IProgressMonitor monitor)
			throws ExecutionException {
		return Status.OK_STATUS;
	}
	
	@Override
	protected String getTaskName() {
		return ""; //$NON-NLS-1$
	}

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
					RPMPlugin.getImageDescriptor("icons/rpm.gif")); //$NON-NLS-1$
		}
		return ret;
	}

	protected IStatus rpmBuild(List<String> flags, IProgressMonitor monitor) {
		monitor.subTask(NLS.bind(
				Messages.getString("RPMHandler.17"), specfile.getName())); //$NON-NLS-1$
		IResource parent = specfile.getParent();
		String dir = parent.getLocation().toString();
		List<String> defines = FedoraHandlerUtils.getRPMDefines(dir);

		List<String> distDefines = getDistDefines(branches, parent.getName());

		defines.add(0, "rpmbuild");
		defines.addAll(distDefines);
		defines.addAll(flags);
		defines.add(specfile.getLocation().toString());

		InputStream is;
		IStatus status = null;
		try {
			is = Utils.runCommandToInputStream(defines.toArray(new String[0]));
			status = runShellCommand(is, monitor); //$NON-NLS-1$
		} catch (IOException e) {
			e.printStackTrace();
			handleError(e);
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
			handleError(e);
		}
		return status;
	}

	protected IStatus runShellCommand(InputStream is, IProgressMonitor mon) {
		boolean terminateMonitor = false;
		if (mon == null) {
			terminateMonitor = true;
			mon = new NullProgressMonitor();
			mon.beginTask(Messages.getString("RPMHandlerMockBuild"), 1); //$NON-NLS-1$
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
								Messages.getString("RPMHandlerScriptCancelled"), //$NON-NLS-1$
								Messages.getString("RPMHandlerUserWarning")); //$NON-NLS-1$
					}

				});
				handleError(Messages
						.getString("RPMHandlerTerminationErrorHandling")); //$NON-NLS-1$
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
		String cmd[] = { "rpm", "--eval", "%{" + format + "}" }; //$NON-NLS-1$ //$NON-NLS-2$

		String result;
		try {
			result = Utils.runCommandToString(cmd);
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					RPMPlugin.PLUGIN_ID, e.getMessage(), e));
		}

		return result.substring(0, result.indexOf('\n'));
	}

	protected IStatus makeSRPM(FedoraProjectRoot fedoraProjectRoot,
			IProgressMonitor monitor) {
		DownloadHandler dh = new DownloadHandler();
		IStatus result = null;
		// retrieve sources
		result = dh.doExecute(fedoraProjectRoot, monitor);
		if (result.isOK()) {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			ArrayList<String> flags = new ArrayList<String>();
			flags.add("--nodeps");
			flags.add("-bs");
			result = rpmBuild(flags, monitor); //$NON-NLS-1$
		}
		return result;
	}
	
}
