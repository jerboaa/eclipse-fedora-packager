package org.fedoraproject.eclipse.packager.tests;

import java.net.MalformedURLException;

import org.apache.xmlrpc.XmlRpcException;
import org.fedoraproject.eclipse.packager.koji.IKojiHubClient;

public class KojiHubClientStub implements IKojiHubClient {
	public String target;
	public String scmURL;
	
	public String build(String target, String scmURL, boolean scratch) throws XmlRpcException {
		this.target = target;
		this.scmURL = scmURL;
		return "1337";
	}

	public void logout() throws MalformedURLException, XmlRpcException {
	}

	public String sslLogin() throws XmlRpcException, MalformedURLException {
		return null;
	}

}
