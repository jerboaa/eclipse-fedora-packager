package org.fedoraproject.eclipse.packager.koji.stubs;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

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
import org.fedoraproject.eclipse.packager.koji.KojiHubClientInitException;
import org.fedoraproject.eclipse.packager.koji.KojiHubClientLoginException;

/**
 * A Koji client stub implementation.
 */
public class KojiHubClientStub implements IKojiHubClient {
	private final String CONSOLE_NAME = "Fedora Packager";
	
	public String build(String target, String scmURL, String nvr,  boolean scratch) throws XmlRpcException {
		try {
			// pretend to do some work, sleep
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return "1337";
	}

	public void logout() throws XmlRpcException {
	}

	public HashMap<?, ?> login() throws KojiHubClientLoginException {
		Map<String, String> retval = new HashMap<String, String>();
		retval.put("session-id", "1209212");
		retval.put("session-key", "1209-flc2lQOdPTZwmoqjoFi");
		return (HashMap<?, ?>)retval;
	}
	
	public URL getWebUrl() {
		URL url = null;
		try {
			url = new URL("http://www.example.com/test");
		} catch (MalformedURLException e) {
			// ignore
		}
		return url;
	}
	
	public URL getHubUrl() {
		URL url = null;
		try {
			url = new URL("http://www.example.com/hub");
		} catch (MalformedURLException e) {
			// ignore
		}
		return url;
	}
	
	public void setHubUrl(String url) {
		// do nothing
	}
	
	public void setWebUrl(String url) {
		// do nothing;
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
			e.printStackTrace();
		}
		view.display(console);
	}

	@Override
	public void setUrlsFromPreferences() throws KojiHubClientInitException {
		// do nothing
	}

	@Override
	public void saveSessionInfo(String sessionKey, String sessionID)
			throws MalformedURLException {
		// do nothing		
	}

	@Override
	public Map getBuild(String nvr) throws XmlRpcException {
		// TODO Auto-generated method stub
		return null;
	}
}
