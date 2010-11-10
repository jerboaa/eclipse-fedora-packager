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
package org.fedoraproject.eclipse.packager.koji;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
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

/**
 * Koji Base client.
 */
public abstract class AbstractKojiHubClient implements IKojiHubClient {
	
	private final String CONSOLE_NAME = KojiPlugin.PLUGIN_ID + ".console";
	/**
	 * URL of the Koji Hub/XMLRPC interface
	 */
	private URL kojiHubUrl;
	/**
	 * URL of the Koji Web interface
	 */
	private URL kojiWebUrl;
	protected XmlRpcClientConfigImpl xmlRpcConfig;
	protected XmlRpcClient xmlRpcClient;
	
	/**
	 * @return the kojiWebUrl
	 */
	@Override
	public URL getWebUrl() {
		return kojiWebUrl;
	}
	
	/**
	 * @return the kojiHubUrl
	 */
	@Override
	public URL getHubUrl() {
		return kojiHubUrl;
	}

	/**
	 * @param kojiHubUrl the kojiHubUrl to set
	 * @throws KojiHubClientInitException if the provided URL is invalid.
	 */
	@Override
	public void setHubUrl(String kojiHubUrl) throws KojiHubClientInitException {
		try {
			this.kojiHubUrl = new URL(kojiHubUrl);
		} catch (MalformedURLException e) {
			throw new KojiHubClientInitException(e);
		}
	}
	
	/**
	 * @param kojiWebUrl the kojiHubUrl to set
	 * @throws KojiHubClientInitException if the provided URL is invalid.
	 */
	@Override
	public void setWebUrl(String kojiWebUrl) throws KojiHubClientInitException {
		try {
			this.kojiWebUrl = new URL(kojiWebUrl);
		} catch (MalformedURLException e) {
			throw new KojiHubClientInitException(e);
		}
	}

	/**
	 * Store session info in XMLRPC configuration. 
	 *
	 * @param sessionKey
	 * @param sessionID
	 * @throws MalformedURLException
	 */
	@Override
	public void saveSessionInfo(String sessionKey, String sessionID)
			throws MalformedURLException {
		xmlRpcConfig.setServerURL(new URL(this.kojiHubUrl.toString() + "?session-key=" + sessionKey //$NON-NLS-1$
				+ "&session-id=" + sessionID)); //$NON-NLS-1$
	}

	private void discardSession() {
		xmlRpcConfig.setServerURL(this.kojiHubUrl);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.fedoraproject.eclipse.packager.IKojiHubClient#sslLogin()
	 */
	public abstract HashMap<?, ?> login() throws KojiHubClientLoginException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.fedoraproject.eclipse.packager.IKojiHubClient#logout()
	 */
	@Override
	public void logout() throws XmlRpcException {
		ArrayList<String> params = new ArrayList<String>();
		xmlRpcClient.execute("logout", params); //$NON-NLS-1$
		discardSession();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.fedoraproject.eclipse.packager.IKojiHubClient#build(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public String build(String target, String scmURL, boolean scratch) throws XmlRpcException {
		ArrayList<Object> params = new ArrayList<Object>();
		params.add(scmURL);
		params.add(target);
		if (scratch) {
			Map<String, Boolean> scratchParam = new HashMap<String, Boolean>();
			scratchParam.put("scratch", true);
			params.add(scratchParam);
		}
		Object result = xmlRpcClient.execute("build", params); //$NON-NLS-1$
		return result.toString();
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
	
	/**
	 * Configure XMLRPC connection
	 */
	protected void setupXmlRpcConfig() {
		xmlRpcConfig = new XmlRpcClientConfigImpl();
		xmlRpcConfig.setServerURL(this.kojiHubUrl);
		xmlRpcConfig.setEnabledForExtensions(true);
		xmlRpcConfig.setConnectionTimeout(30000);
	}
	
	/**
	 * Set up XMLRPC client.
	 * 
	 * @throws IllegalStateException If XMLRPC configuration hasn't been
	 *         properly set up.
	 */
	protected void setupXmlRpcClient() throws IllegalStateException {
		if (xmlRpcConfig == null) {
			// TODO: Externalize
			throw new IllegalStateException("XMLRPC not configured.");
		}
		xmlRpcClient = new XmlRpcClient();
		xmlRpcClient.setTypeFactory(new KojiTypeFactory(this.xmlRpcClient));
		xmlRpcClient.setConfig(this.xmlRpcConfig);
	}
	
}
