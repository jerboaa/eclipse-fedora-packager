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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.commons.ssl.HttpSecureProtocol;
import org.apache.commons.ssl.TrustMaterial;
import org.eclipse.core.commands.AbstractHandler;
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
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.FedoraSSL;
import org.fedoraproject.eclipse.packager.IFpProjectBits;
import org.fedoraproject.eclipse.packager.SourcesFile;
import org.fedoraproject.eclipse.packager.api.errors.InvalidProjectRootException;
import org.fedoraproject.eclipse.packager.utils.FedoraHandlerUtils;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;
import org.fedoraproject.eclipse.packager.utils.httpclient.CoutingRequestEntity;
import org.fedoraproject.eclipse.packager.utils.httpclient.IRequestProgressListener;

/**
 * Class responsible for uploading source files (VCS independent bits).
 * 
 * @author Red Hat inc.
 *
 */
public class UploadHandler extends AbstractHandler {

	@Override
	/**
	 *  Performs upload of sources (independent of VCS used), updates "sources"
	 *  file and performs necessary CVS operations to bring branch in sync.
	 *  Checks if sources have changed.
	 *  
	 */
	public Object execute(final ExecutionEvent e) throws ExecutionException {

		final IResource resource = FedoraHandlerUtils.getResource(e);
		final FedoraProjectRoot fedoraProjectRoot;
		try {
			fedoraProjectRoot = FedoraPackagerUtils.getValidRoot(resource);
		} catch (InvalidProjectRootException e1) {
			// TODO handle appropriately
			e1.printStackTrace();
			return null;
		}
		final SourcesFile sourceFile = fedoraProjectRoot.getSourcesFile();
		final IFpProjectBits projectBits = FedoraPackagerUtils.getVcsHandler(fedoraProjectRoot);
		// do tasks as job
		Job job = new Job(FedoraPackagerText.get().uploadHandler_taskName) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				monitor.beginTask(FedoraPackagerText.get().uploadHandler_taskName, IProgressMonitor.UNKNOWN);

				// ensure file has changed if already listed in sources
				Map<String, String> sources = sourceFile.getSources();
				String filename = resource.getName();
				if (false /* TODO: check if file needs to be uploaded */) {
					// file already in sources
					return FedoraHandlerUtils.handleOK(MessageFormat.format(FedoraPackagerText.get().uploadHandler_versionExists, filename)
							, true);
				}

				// Do file sanity checks (non-empty, file extensions etc.)
				final File toAdd = resource.getLocation().toFile();
				if (false /* TODO check for upload file validity */) {
					return FedoraHandlerUtils.handleOK(MessageFormat.format(FedoraPackagerText.get().uploadHandler_invalidFile,
							toAdd.getName()), true);
				}

				// Do the file uploading
				// TODO execute upload command
				IStatus result = null;

				if (result.isOK()) {
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
				}

				// Update sources file
				// TODO: update sources
				if (!result.isOK()) {
					// fail updating sources file
					return FedoraHandlerUtils.handleError(FedoraPackagerText.get().uploadHandler_failUpdatSourceFile);
				}

				// Handle VCS specific stuff; Update .gitignore/.cvsignore
				// TODO: update vcs ignore file
				if (!result.isOK()) {
					// fail updating sources file
					return FedoraHandlerUtils.handleError(FedoraPackagerText.get().uploadHandler_failVCSUpdate);
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
}
