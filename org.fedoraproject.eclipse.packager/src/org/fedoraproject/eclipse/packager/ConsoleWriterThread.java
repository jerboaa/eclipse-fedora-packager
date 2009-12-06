package org.fedoraproject.eclipse.packager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.ui.console.MessageConsoleStream;

public class ConsoleWriterThread extends Thread {
	InputStreamReader in;
	MessageConsoleStream out;

	public ConsoleWriterThread(InputStream in, MessageConsoleStream out) {
		this.out = out;
		this.in = new InputStreamReader(in);
	}

	public void run() {
		int ch;
		try {
			while ((ch = in.read()) != -1) {
				out.write(ch);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
