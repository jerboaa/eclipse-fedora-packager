package org.fedoraproject.eclipse.packager.koji;

import java.net.MalformedURLException;

import org.apache.xmlrpc.XmlRpcException;

public interface IKojiHubClient {

	public abstract String sslLogin() throws XmlRpcException,
			MalformedURLException;

	public abstract void logout() throws MalformedURLException, XmlRpcException;

	public abstract String build(String target, String scmURL)
			throws XmlRpcException;

}