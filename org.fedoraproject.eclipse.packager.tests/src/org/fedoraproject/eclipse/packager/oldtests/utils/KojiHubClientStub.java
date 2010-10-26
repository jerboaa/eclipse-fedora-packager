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
package org.fedoraproject.eclipse.packager.oldtests.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;

import org.apache.xmlrpc.XmlRpcException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.fedoraproject.eclipse.packager.PackagerPlugin;
import org.fedoraproject.eclipse.packager.koji.IKojiHubClient;

/**
 * @deprecated This stub is unused. Only exists for old tests.
 *
 */
public class KojiHubClientStub implements IKojiHubClient {
	public String target;
	public String scmURL;
	private final String CONSOLE_NAME = "Fedora Packager";
	
	@SuppressWarnings("unused")
	public KojiHubClientStub() throws IOException, GeneralSecurityException {
		// Do this just to pretend this throws the same exceptions than the real
		// client.
		super();
	}
	
	public String build(String target, String scmURL, boolean scratch) throws XmlRpcException {
		this.target = target;
		this.scmURL = scmURL;
		System.out.print("Sleeping ...");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // sleep
		System.out.println(" done!");
		return "1337";
	}

	public void logout() throws MalformedURLException, XmlRpcException {
	}

	public String sslLogin() throws XmlRpcException, MalformedURLException {
		return null;
	}
	
	public String getWebUrl() {
		return "http://www.example.com";
	}

	/**
	 * Create MessageConsole if not found.
	 * 
	 * @param name
	 * @return
	 */
	private MessageConsole findConsole(String name) {
	      IConsoleManager conMan = ConsolePlugin.getDefault().getConsoleManager();
	      IConsole[] existing = conMan.getConsoles();
	      for (int i = 0; i < existing.length; i++)
	         if (name.equals(existing[i].getName()))
	            return (MessageConsole) existing[i];
	      //no console found, so create a new one
	      MessageConsole myConsole = new MessageConsole(name, null);
	      conMan.addConsoles(new IConsole[]{myConsole});
	      return myConsole;
	 }
	
	/**
	 * Utility method to write to Eclipse console if not present.
	 * 
	 * @param message
	 */
	public void writeToConsole(String message) {
		MessageConsole console = findConsole(CONSOLE_NAME);
		MessageConsoleStream out = console.newMessageStream();
		out.setActivateOnWrite(true);
		out.println(message);
		
		// Show console view
		IWorkbenchPage page = PackagerPlugin.getDefault().getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		String id = IConsoleConstants.ID_CONSOLE_VIEW;
		IConsoleView view = null;
		try {
			view = (IConsoleView) page.showView(id);
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		view.display(console);
	}

}
