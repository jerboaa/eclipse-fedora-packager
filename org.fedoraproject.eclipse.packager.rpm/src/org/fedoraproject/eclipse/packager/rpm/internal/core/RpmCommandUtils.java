package org.fedoraproject.eclipse.packager.rpm.internal.core;

import java.io.InputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.fedoraproject.eclipse.packager.rpm.api.FedoraPackagerConsole;

/**
 * Rpm command related utilities.
 *
 */
public class RpmCommandUtils {

	/**
	 * Run a shell command using {@link ConsoleWriterThread}.
	 * @param is Input stream of the command output.
	 * @param mon A progress monitor.
	 */
	public static void runShellCommand(InputStream is, IProgressMonitor mon) {
		final MessageConsole console = FedoraPackagerConsole.getConsole();
		IConsoleManager manager = ConsolePlugin.getDefault()
				.getConsoleManager();
		manager.addConsoles(new IConsole[] { console });
		console.activate();

		final MessageConsoleStream outStream = console.newMessageStream();

		try {
			// create thread for reading inputStream (process' stdout)
			ConsoleWriterThread consoleWriter = new ConsoleWriterThread(is,
					outStream);
			consoleWriter.start();

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
				consoleWriter.close();
				throw new OperationCanceledException();
			}

			// finish reading whatever's left in the buffers
			consoleWriter.join();

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
