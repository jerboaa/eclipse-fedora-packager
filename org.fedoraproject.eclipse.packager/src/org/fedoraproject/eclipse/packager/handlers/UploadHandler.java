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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.commons.ssl.HttpSecureProtocol;
import org.apache.commons.ssl.TrustMaterial;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.FedoraSSL;
import org.fedoraproject.eclipse.packager.IFpProjectBits;
import org.fedoraproject.eclipse.packager.Messages;
import org.fedoraproject.eclipse.packager.SourcesFile;
import org.fedoraproject.eclipse.packager.httpclient_utils.IProgressListener;
import org.fedoraproject.eclipse.packager.httpclient_utils.CoutingRequestEntity;

/**
 * Class responsible for uploading source files (VCS independent bits).
 * 
 * @author Red Hat inc.
 *
 */
public class UploadHandler extends WGetHandler {

	@Override
	/**
	 *  Performs upload of sources (independent of VCS used), updates "sources"
	 *  file and performs necessary CVS operations to bring branch in sync.
	 *  Checks if sources have changed.
	 *  
	 */
	public Object execute(final ExecutionEvent e) throws ExecutionException {

		final IResource resource = FedoraHandlerUtils.getResource(e);
		final FedoraProjectRoot fedoraProjectRoot = FedoraHandlerUtils.getValidRoot(e);
		final SourcesFile sourceFile = fedoraProjectRoot.getSourcesFile();
		final IFpProjectBits projectBits = FedoraHandlerUtils.getVcsHandler(fedoraProjectRoot);
		// do tasks as job
		Job job = new Job(Messages.uploadHandler_taskName) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				monitor.beginTask(Messages.uploadHandler_taskName, IProgressMonitor.UNKNOWN);

				// ensure file has changed if already listed in sources
				Map<String, String> sources = sourceFile.getSources();
				String filename = resource.getName();
				if (sources.containsKey(filename)
						&& SourcesFile
								.checkMD5(sources.get(filename), resource)) {
					// file already in sources
					return FedoraHandlerUtils.handleOK(NLS.bind(Messages.uploadHandler_versionExists, filename)
							, true);
				}

				// Do file sanity checks (non-empty, file extensions etc.)
				final File toAdd = resource.getLocation().toFile();
				if (!FedoraHandlerUtils.isValidUploadFile(toAdd)) {
					return FedoraHandlerUtils.handleOK(NLS.bind(Messages.uploadHandler_invalidFile,
							toAdd.getName()), true);
				}

				// Do the file uploading
				IStatus result = performUpload(toAdd, filename, monitor,
						fedoraProjectRoot);

				if (result.isOK()) {
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
				}

				// Update sources file
				result = updateSources(sourceFile, toAdd);
				if (!result.isOK()) {
					// fail updating sources file
					return FedoraHandlerUtils.handleError(Messages.uploadHandler_failUpdatSourceFile);
				}

				// Handle VCS specific stuff; Update .gitignore/.cvsignore
				result = updateIgnoreFile(fedoraProjectRoot.getIgnoreFile(), toAdd);
				if (!result.isOK()) {
					// fail updating sources file
					return FedoraHandlerUtils.handleError(Messages.uploadHandler_failVCSUpdate);
				}

				// Do VCS update
				result = projectBits.updateVCS(fedoraProjectRoot, monitor);
				if (result.isOK()) {
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
				}
				
				// Refresh project
				IProject project = fedoraProjectRoot.getProject();
				if (project != null) {
					try {
						project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
					} catch (CoreException e) {
						return FedoraHandlerUtils.handleError(e);
					}
				}
				return result;
			}

		};
		job.setUser(true);
		job.schedule();
		return null;
	}
	
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
		
		HttpParams params = new BasicHttpParams();
		params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000L);
		HttpClient client = new DefaultHttpClient(params);
		
		try {
			//registerProtocol();

			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			// first check remote status to see if file is already uploaded
			monitor.subTask(NLS.bind(
					Messages.uploadHandler_checkingRemoteStatus, filename));

			// get upload URL from lookaside cache 
			String uploadUrl = fedoraProjectRoot.getLookAsideCache().getUploadUrl();
			// make sure we have a valid URL, this would fail later anyways
			try {
				@SuppressWarnings("unused")
				URL dummy = new URL(uploadUrl);
			} catch (MalformedURLException e) {
				return FedoraHandlerUtils.handleError(NLS.bind(Messages.uploadHandler_invalidUrlError,
						e.getMessage()));
			}
			//HttpPost httppost = new HttpPost(uploadUrl); // TODO use this!
			HttpPost post = new HttpPost("http://upload-cgi.yyz.redhat.com/cgi-bin/upload.cgi");
			//FileBody binUploadFile = new FileBody(toAdd);

			// See if we need to upload anything
            MultipartEntity reqEntity = new MultipartEntity();
            reqEntity.addPart("filename", new StringBody(filename));
            reqEntity.addPart("name", new StringBody(fedoraProjectRoot.getSpecfileModel().getName()));
            reqEntity.addPart("md5sum", new StringBody(SourcesFile.getMD5(toAdd)));
			
			// not sure why it's content-length * 2, but that's what it is...
			final double totalSize = reqEntity.getContentLength() * 2;

            IProgressListener progL = new IProgressListener() {
				
				public void transferred(long num) {
					// TODO: use monitor
					System.out.println(String.format("Wrote %.2f%%", (((double)num)/totalSize)*100));
				}
			};
			CoutingRequestEntity countingEntity = new CoutingRequestEntity(reqEntity, progL);
            post.setEntity(countingEntity); 
			
            HttpResponse response = client.execute(post);
            HttpEntity resEntity = response.getEntity();
            int returnCode = response.getStatusLine().getStatusCode();
            
            System.out.println("----------------------------------------");
            System.out.println(response.getStatusLine());
            if (resEntity != null) {
                System.out.println("Response content length: " + resEntity.getContentLength());
                System.out.println("Chunked?: " + resEntity.isChunked());
                if (resEntity != null) {
                    InputStream instream = resEntity.getContent();
                    BufferedReader in = new BufferedReader(new InputStreamReader(instream));
                    String line;
                    // print response
                    while ((line = in.readLine()) != null) {
                    	System.out.println(line);
                    }
                }
            }
            
            // TODO: use EntityUtils.comsume(resEntity);!!!
			if (returnCode != HttpURLConnection.HTTP_OK) {
				return FedoraHandlerUtils.handleError(NLS.bind(
						Messages.uploadHandler_uploadFail, filename, returnCode));
			} else {
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				String resString = "N/A";
				if (resEntity != null) {
					resString = parseResponse(resEntity);
				}

				// if we're in debug mode, forget this check
				if (resString.toLowerCase().equals("available") && !debug) { //$NON-NLS-1$
					return FedoraHandlerUtils.handleOK(
							NLS.bind(
									Messages.uploadHandler_fileAlreadyUploaded, filename), true);
				} else if (resString.toLowerCase().equals("missing") || debug) { //$NON-NLS-1$
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
					monitor.subTask(NLS.bind(
							Messages.uploadHandler_progressMsg, filename));
					return upload(toAdd, fedoraProjectRoot);
				} else {
					return FedoraHandlerUtils.handleError(resString);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return FedoraHandlerUtils.handleError(e);
		} /*catch (GeneralSecurityException e) {
			e.printStackTrace();
			return FedoraHandlerUtils.handleError(e);
		}*/ finally {
			// When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            client.getConnectionManager().shutdown();
		}
	}

	/**
	 * Set up SSL context.
	 * 
	 * @throws GeneralSecurityException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws KeyManagementException
	 * @throws CertificateException
	 */
//	protected void registerProtocol() throws GeneralSecurityException,
//			IOException, NoSuchAlgorithmException, KeyStoreException,
//			KeyManagementException, CertificateException {
//		HttpSecureProtocol protocol = new HttpSecureProtocol();
//		protocol.setKeyMaterial(new FedoraSSL(
//				new File(FedoraSSL.DEFAULT_CERT_FILE),
//				new File(FedoraSSL.DEFAULT_UPLOAD_CA_CERT),
//				new File(FedoraSSL.DEFAULT_SERVER_CA_CERT)).getFedoraCertKeyMaterial());
//		protocol.setTrustMaterial(TrustMaterial.TRUST_ALL);
//		Protocol.registerProtocol("https", new Protocol("https", //$NON-NLS-1$ //$NON-NLS-2$
//				(ProtocolSocketFactory) protocol, 443));
//	}

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
			// get upload URL from lookaside cache 
			String uploadUrl = fedoraProjectRoot.getLookAsideCache().getUploadUrl();
			PostMethod postMethod = new PostMethod(uploadUrl);

			Part[] data = { new StringPart("name", fedoraProjectRoot.getSpecfileModel().getName()), //$NON-NLS-1$
					new StringPart("md5sum", SourcesFile.getMD5(file)), //$NON-NLS-1$
					new FilePart("file", file) }; //$NON-NLS-1$

			postMethod.setRequestEntity(new MultipartRequestEntity(data,
					postMethod.getParams()));

			int code = client.executeMethod(postMethod);
			if (code != HttpURLConnection.HTTP_OK) {
				status = FedoraHandlerUtils.handleError(NLS
						.bind(Messages.uploadHandler_uploadFail, filename, postMethod.getStatusLine()));
			} else {
				status = Status.OK_STATUS;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			status = FedoraHandlerUtils.handleError(e);
		} catch (HttpException e) {
			e.printStackTrace();
			status = FedoraHandlerUtils.handleError(e);
		} catch (IOException e) {
			e.printStackTrace();
			status = FedoraHandlerUtils.handleError(e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
					status = FedoraHandlerUtils.handleError(e);
				}
			}
		}
		return status;
	}

	/**
	 * Helper to read response from response entity.
	 * 
	 * @param responseEntity
	 * @return
	 * @throws IOException
	 */
	protected String parseResponse(HttpEntity responseEntity)
			throws IOException {
		
		BufferedReader br = new BufferedReader(new InputStreamReader(
				responseEntity.getContent()));
		String responseText = ""; //$NON-NLS-1$
		String line;
		line = br.readLine();
		while (line != null) {
			responseText += line + "\n"; //$NON-NLS-1$
			line = br.readLine();
		}
		return responseText.trim();
	}
	
	/**
	 * Update the ignore file .cvsignore or .gitignore file. Appends to file.
	 * 
	 * @param ignoreFile
	 * @param toAdd
	 * @return
	 */
	protected IStatus updateIgnoreFile(File ignoreFile, File toAdd) {
		return updateIgnoreFile(ignoreFile, toAdd, false);
	}

	/**
	 * Actually writes to .cvsignore. ATM this method is never called with
	 * <code>forceOverwrite</code> set to true.
	 * 
	 * @param cvsignore
	 * @param toAdd
	 * @param forceOverwrite
	 * @return Status of the performed operation.
	 */
	private IStatus updateIgnoreFile(File ignoreRile, File toAdd,
			boolean forceOverwrite) {
		IStatus status;
		String filename = toAdd.getName();
		ArrayList<String> ignoreFiles = new ArrayList<String>();
		BufferedReader br = null;
		PrintWriter pw = null;
		try {
			if (forceOverwrite) {
				pw = new PrintWriter(new FileWriter(ignoreRile, false));
				pw.println(filename);
				status = Status.OK_STATUS;
			} else {
				// only append to file if not already present
				br = new BufferedReader(new FileReader(ignoreRile));

				String line = br.readLine();
				while (line != null) {
					ignoreFiles.add(line);
					line = br.readLine();
				}

				if (!ignoreFiles.contains(filename)) {
					pw = new PrintWriter(new FileWriter(ignoreRile, true));
					pw.println(filename);
				}
				status = Status.OK_STATUS;
			}
		} catch (IOException e) {
			e.printStackTrace();
			status = FedoraHandlerUtils.handleError(e);
		} finally {
			if (pw != null) {
				pw.close();
			}
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
					status = FedoraHandlerUtils.handleError(e);
				}
			}
		}
		return status;
	}
}
