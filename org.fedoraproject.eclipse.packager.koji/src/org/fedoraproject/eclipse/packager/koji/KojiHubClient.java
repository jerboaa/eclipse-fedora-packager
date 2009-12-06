package org.fedoraproject.eclipse.packager.koji;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.fedoraproject.eclipse.packager.SSLUtils;

// TODO Catch all Exceptions and return a unified error message
public class KojiHubClient implements IKojiHubClient {
	private static final String KOJI_HOST = "koji.fedoraproject.org"; //$NON-NLS-1$
	public static final String KOJI_HUB_URL = "https://" + KOJI_HOST //$NON-NLS-1$
			+ "/kojihub"; //$NON-NLS-1$
	public static final String KOJI_WEB_URL = "http://" + KOJI_HOST + "/koji"; //$NON-NLS-1$ //$NON-NLS-2$
	private XmlRpcClientConfigImpl config;
	private XmlRpcClient client;

	public KojiHubClient() throws GeneralSecurityException, IOException {
		SSLUtils.initSSLConnection();

		config = new XmlRpcClientConfigImpl();
		try {
			config.setServerURL(new URL(KOJI_HUB_URL));
			config.setEnabledForExtensions(true);
			config.setConnectionTimeout(30000);
			client = new XmlRpcClient();
			client.setTypeFactory(new KojiTypeFactory(client));
			client.setConfig(config);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	private void setSession(String sessionKey, String sessionID)
			throws MalformedURLException {
		config.setServerURL(new URL(KOJI_HUB_URL + "?session-key=" + sessionKey //$NON-NLS-1$
				+ "&session-id=" + sessionID)); //$NON-NLS-1$
	}

	private void discardSession() throws MalformedURLException {
		config.setServerURL(new URL(KOJI_HUB_URL));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.fedoraproject.eclipse.packager.IKojiHubClient#sslLogin()
	 */
	public String sslLogin() throws XmlRpcException, MalformedURLException {
		ArrayList<String> params = new ArrayList<String>();
		Object result = client.execute("sslLogin", params); //$NON-NLS-1$
		HashMap<?, ?> hashMap = (HashMap<?, ?>) result;
		String sessionKey = hashMap.get("session-key").toString(); //$NON-NLS-1$
		String sessionID = hashMap.get("session-id").toString(); //$NON-NLS-1$
		setSession(sessionKey, sessionID);
		return result.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.fedoraproject.eclipse.packager.IKojiHubClient#logout()
	 */
	public void logout() throws MalformedURLException, XmlRpcException {
		ArrayList<String> params = new ArrayList<String>();
		client.execute("logout", params); //$NON-NLS-1$
		discardSession();
	}

	public String showSession() throws XmlRpcException {
		ArrayList<String> params = new ArrayList<String>();
		Object result = client.execute("showSession", params); //$NON-NLS-1$
		return result.toString();
	}

	public String getLoggedInUser() throws XmlRpcException {
		ArrayList<String> params = new ArrayList<String>();
		Object result = client.execute("getLoggedInUser", params); //$NON-NLS-1$
		return result.toString();
	}

	public String listUsers() throws XmlRpcException {
		ArrayList<String> params = new ArrayList<String>();
		Object result = client.execute("listUsers", params); //$NON-NLS-1$
		return Arrays.asList((Object[]) result).toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.fedoraproject.eclipse.packager.IKojiHubClient#build(java.lang.String,
	 * java.lang.String)
	 */
	public String build(String target, String scmURL) throws XmlRpcException {
		ArrayList<Object> params = new ArrayList<Object>();
		params.add(target);
		params.add(scmURL);
		Object result = client.execute("build", params); //$NON-NLS-1$
		return result.toString();
	}

	public String hello() throws XmlRpcException {
		ArrayList<String> params = new ArrayList<String>();
		Object result = client.execute("hello", params); //$NON-NLS-1$
		return (String) result;
	}

	public List<Object> getBuildTargets() throws XmlRpcException {
		Object result = client.execute("getBuildTargets", new Object[0]); //$NON-NLS-1$
		return Arrays.asList((Object[]) result);
	}

}
