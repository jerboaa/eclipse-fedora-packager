package org.fedoraproject.eclipse.packager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.console.MessageConsoleStream;

public class ConsoleWriterThread extends Thread {
	BufferedReader in;
	MessageConsoleStream out;
	private boolean terminate;

	public ConsoleWriterThread(InputStream in, MessageConsoleStream out) {
		this.out = out;
		this.in = new BufferedReader(new InputStreamReader(in));
		terminate=false;
	}

	public void run() {
		int ch;
		try {
			while (!terminate && (ch = in.read()) != -1) {
				out.write(ch);
			}
		} catch (IOException e) {
			//Log error, but do nothing about it
			PackagerPlugin.getDefault().getLog().log(new Status(IStatus.WARNING,
				      PackagerPlugin.PLUGIN_ID, 0,
				      "I/O failed. This may be because you cancelled a command.", e));
		}
	}

	public void close() {
		terminate=true;
	}

}
