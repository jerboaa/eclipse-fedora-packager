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
package org.fedoraproject.eclipse.packager.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.ssl.HttpSecureProtocol;
import org.apache.commons.ssl.TrustMaterial;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.SSLUtils;
import org.fedoraproject.eclipse.packager.SourcesFile;
import org.fedoraproject.eclipse.packager.Messages;

/**
 * Class responsible for uploading source files (VCS independent bits).
 * 
 * @author Red Hat inc.
 *
 */
public abstract class UploadHandler extends WGetHandler {

	/**
	 * Upload source files as job.
	 * 
	 * @param toAdd
	 * @param filename
	 * @param monitor
	 * @param fedoraProjectRoot
	 * @return
	 */
	protected IStatus performUpload(final File toAdd, final String filename,
			IProgressMonitor monitor, FedoraProjectRoot fedoraProjectRoot) {
		IStatus status;
		try {
			registerProtocol();

			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			// first check remote status to see if file is already uploaded
			monitor.subTask(NLS.bind(
					Messages.getString("UploadHandler.3"), filename)); //$NON-NLS-1$
			HttpClient client = new HttpClient();
			client.getHttpConnectionManager().getParams()
					.setConnectionTimeout(30000);
			PostMethod postMethod = new PostMethod(uploadURL);
			NameValuePair[] data = {
					new NameValuePair(
							"name", getValidRoot(resource).getSpecfileModel().getName()), //$NON-NLS-1$
					new NameValuePair("md5sum", SourcesFile.getMD5(toAdd)), //$NON-NLS-1$
					new NameValuePair("filename", filename) }; //$NON-NLS-1$
			postMethod.setRequestBody(data);
			if (client.executeMethod(postMethod) != HttpURLConnection.HTTP_OK) {
				status = handleError(NLS.bind(
						Messages.getString("UploadHandler.4"), filename)); //$NON-NLS-1$
			} else {
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				InputStream responseStream = postMethod
						.getResponseBodyAsStream();
				String response = parseResponse(responseStream);

				// if we're in debug mode, forget this check
				if (response.toLowerCase().equals("available") && !debug) { //$NON-NLS-1$
					status = handleOK(
							NLS.bind(
									Messages.getString("UploadHandler.6"), filename), true); //$NON-NLS-1$
				} else if (response.toLowerCase().equals("missing") || debug) { //$NON-NLS-1$
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
					monitor.subTask(NLS.bind(
							Messages.getString("UploadHandler.9"), filename)); //$NON-NLS-1$
					status = upload(toAdd, fedoraProjectRoot);
				} else {
					status = handleError(response);
				}
			}
		} catch (HttpException e) {
			e.printStackTrace();
			status = handleError(e);
		} catch (IOException e) {
			e.printStackTrace();
			status = handleError(e);
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
			status = handleError(e);
		}

		return status;
	}

	protected void registerProtocol() throws GeneralSecurityException,
			IOException, NoSuchAlgorithmException, KeyStoreException,
			KeyManagementException, CertificateException {
		HttpSecureProtocol protocol = new HttpSecureProtocol();
		protocol.setKeyMaterial(SSLUtils.getKeyMaterial());
		protocol.setTrustMaterial(TrustMaterial.TRUST_ALL);
		Protocol.registerProtocol("https", new Protocol("https", //$NON-NLS-1$ //$NON-NLS-2$
				(ProtocolSocketFactory) protocol, 443));
	}

	protected IStatus upload(File file, FedoraProjectRoot fedoraProjectRoot) {
		IStatus status;
		byte[] bytes = new byte[(int) file.length()];
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);

			String filename = file.getName();

			fis.read(bytes);

			HttpClient client = new HttpClient();
			client.getHttpConnectionManager().getParams()
					.setConnectionTimeout(30000);
			PostMethod postMethod = new PostMethod(uploadURL);

			Part[] data = { new StringPart("name", fedoraProjectRoot.getSpecfileModel().getName()), //$NON-NLS-1$
					new StringPart("md5sum", SourcesFile.getMD5(file)), //$NON-NLS-1$
					new FilePart("file", file) }; //$NON-NLS-1$

			postMethod.setRequestEntity(new MultipartRequestEntity(data,
					postMethod.getParams()));

			int code = client.executeMethod(postMethod);
			if (code != HttpURLConnection.HTTP_OK) {
				status = handleError(NLS
						.bind(Messages.getString("UploadHandler.33"), filename, postMethod.getStatusLine())); //$NON-NLS-1$
			} else {
				status = Status.OK_STATUS;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			status = handleError(e);
		} catch (HttpException e) {
			e.printStackTrace();
			status = handleError(e);
		} catch (IOException e) {
			e.printStackTrace();
			status = handleError(e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
					status = handleError(e);
				}
			}
		}
		return status;
	}

	protected String parseResponse(InputStream responseStream)
			throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(
				responseStream));

		String responseText = ""; //$NON-NLS-1$
		String line;
		try {
			line = br.readLine();
			while (line != null) {
				responseText += line + "\n"; //$NON-NLS-1$
				line = br.readLine();
			}
		} finally {
			br.close();
		}

		return responseText.trim();
	}

	protected File getFileFor(String name) throws IOException {
		File ret = null;
		IResource res = specfile.getParent().findMember(name, true);
		if (res == null) {
			ret = new File(specfile.getParent().getLocation().toString()
					+ IPath.SEPARATOR + name);
			if (!ret.createNewFile()) {
				throw new IOException(NLS.bind(
						Messages.getString("UploadHandler.12"), name)); //$NON-NLS-1$
			}
		} else {
			ret = res.getLocation().toFile();
		}
		return ret;
	}

}
