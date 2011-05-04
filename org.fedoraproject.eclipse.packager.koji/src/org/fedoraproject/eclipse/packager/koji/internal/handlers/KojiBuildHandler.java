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
package org.fedoraproject.eclipse.packager.koji.internal.handlers;

import java.net.MalformedURLException;
import java.util.HashMap;

import org.apache.xmlrpc.XmlRpcException;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.IFpProjectBits;
import org.fedoraproject.eclipse.packager.NonTranslatableStrings;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;
import org.fedoraproject.eclipse.packager.utils.FedoraHandlerUtils;
import org.fedoraproject.eclipse.packager.utils.RPMUtils;
import org.fedoraproject.eclipse.packager.api.FedoraPackager;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerAPIException;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandInitializationException;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandNotFoundException;
import org.fedoraproject.eclipse.packager.api.errors.InvalidProjectRootException;
import org.fedoraproject.eclipse.packager.koji.KojiMessageDialog;
import org.fedoraproject.eclipse.packager.koji.KojiPlugin;
import org.fedoraproject.eclipse.packager.koji.KojiText;
import org.fedoraproject.eclipse.packager.koji.api.IKojiHubClient;
import org.fedoraproject.eclipse.packager.koji.api.KojiBuildCommand;
import org.fedoraproject.eclipse.packager.koji.api.KojiHubClientLoginException;
import org.fedoraproject.eclipse.packager.koji.api.KojiSSLHubClient;

/**
 * Handler to perform a Koji build.
 * 
 */
public class KojiBuildHandler extends AbstractHandler {
	@SuppressWarnings("unused")
	private String dist;
	private IKojiHubClient koji;
	private Job job;
	private MessageDialog kojiMsgDialog;
	private Shell shell;
	
	/**
	 * Indicates if stub or real client should be returned by getKoji()
	 */
	public static boolean inTestingMode = false;

	@Override
	public Object execute(final ExecutionEvent e) throws ExecutionException {
		final FedoraProjectRoot fedoraProjectRoot;
		try {
			IResource eventResource = FedoraHandlerUtils.getResource(e);
			fedoraProjectRoot = FedoraPackagerUtils
					.getProjectRoot(eventResource);
		} catch (InvalidProjectRootException e1) {
			// TODO handle appropriately
			e1.printStackTrace();
			return null;
		}
		FedoraPackager packager = new FedoraPackager(fedoraProjectRoot);
		KojiBuildCommand buildCmd = null;
		try {
			buildCmd = (KojiBuildCommand) packager.getCommandInstance(KojiBuildCommand.ID);
		} catch (FedoraPackagerCommandInitializationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FedoraPackagerCommandNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		try {
			buildCmd.call(null);
		} catch (FedoraPackagerAPIException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
	}

	private boolean promptForTag() {
		YesNoRunnable op = new YesNoRunnable(
				KojiText.KojiBuildHandler_tagBeforeSendingBuild);
		Display.getDefault().syncExec(op);
		return op.isOkPressed();
	}

	protected IStatus newBuild(FedoraProjectRoot fedoraProjectRoot,
			IProgressMonitor monitor) {
		IStatus status;
		IFpProjectBits projectBits = FedoraPackagerUtils
				.getVcsHandler(fedoraProjectRoot);
		try {
			// for testing use the stub instead
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			monitor.subTask(KojiText.KojiBuildHandler_connectKojiMsg);

			String scmURL = projectBits.getScmUrlForKoji(fedoraProjectRoot);
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			// login via SSL
			monitor.subTask(KojiText.KojiBuildHandler_kojiLogin);
			HashMap<?, ?> sessionData = null;
			try {
				sessionData = getKoji().login();
			} catch (KojiHubClientLoginException e) {
				e.printStackTrace();
				return FedoraHandlerUtils.handleError(e);
			}
			// store session in URL
			try {
				Object sessionId = sessionData.get("session-id"); //$NON-NLS-1$
				String sid = null;
				if (sessionId instanceof Integer) {
					sid = ((Integer)sessionId).toString();
				} else if (sessionId instanceof String) {
					// if we get a String it should be convertible to an int
					try {
						Integer.parseInt((String)sessionId);
						sid = (String)sessionId;
					} catch (NumberFormatException e) {
						// something is wrong should have gotten an int or String
						return FedoraHandlerUtils.handleError(KojiText.KojiBuildHandler_unexpectedSessionId + sessionId.toString());
					}
				}
				//getKoji().saveSessionInfo((String)sessionData.get("session-key"), sid); //$NON-NLS-1$
			} catch (ClassCastException e) {
				return FedoraHandlerUtils.handleError(e);
			}

			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			// push build
			monitor.subTask(KojiText.KojiBuildHandler_sendBuildCmd);
			String nvr= null;
			try {
				nvr = RPMUtils.getNVR(fedoraProjectRoot);
			} catch (CoreException e1) {
				e1.printStackTrace();
			}
			String result = null;// = getKoji().build(projectBits.getTarget(), scmURL, nvr, isScratch());
			// if we get an int (that is our taskId)
			int taskId = -1;
			try {
				taskId = Integer.parseInt(result);
			} catch (NumberFormatException e) {
				// ignore
			}
			if (taskId != -1) {
				status = new Status(IStatus.OK, KojiPlugin.PLUGIN_ID,
						new Integer(taskId).toString());
			} else{ // Error
				status = new Status(IStatus.INFO, KojiPlugin.PLUGIN_ID, result);
			}
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			// logout
			monitor.subTask(KojiText.KojiBuildHandler_kojiLogout);
			getKoji().logout();
		} catch (XmlRpcException e) {
			e.printStackTrace();
			status = FedoraHandlerUtils.handleError(e);
		}
		return status;
	}

	/**
	 * ATM this is the preferred way to get the koji client instance.
	 * When in testing mode, this will return the stubbed client.
	 * 
	 * @return The real or the stubbed koji client depending on the
	 *         mode (inTestingMode)
	 */
	public IKojiHubClient getKoji() {
		if (!inTestingMode) {
			// return actual client
			return koji;
		} else {
			return null;
		}
	}

	/**
	 * @return the kojiMsgDialog
	 */
	public MessageDialog getKojiMsgDialog() {
		return kojiMsgDialog;
	}

	/**
	 * Set the MessageDialog for build status reporting.
	 * 
	 * @param CmdId The command ID which triggered this handler.
	 * @param taskId The task ID to use for the message.
	 */
	public void setKojiMsgDialog(String CmdId, String taskId) {
		ImageDescriptor descriptor = KojiPlugin
			.getImageDescriptor("icons/Artwork_DesignService_koji-icon-16.png"); //$NON-NLS-1$
		Image titleImage = descriptor.createImage();
		KojiMessageDialog msgDialog = new KojiMessageDialog(
				shell,
				KojiText.KojiBuildHandler_kojiBuild,
				titleImage,
				MessageDialog.NONE,
				new String[] { IDialogConstants.OK_LABEL },
				0, "http://www.example.com", taskId);
		this.kojiMsgDialog = msgDialog;
	}
	
	/**
	 * Protected setter for kojiMsgDialog.
	 */
	protected void setKojiMsgDialog(MessageDialog msgDialog) {
		this.kojiMsgDialog = msgDialog;
	}

	/**
	 * Set the koji client. Pass in hint in order to be able to use different
	 * client depending on actual cmdId.
	 * 
	 * @param cmdId Command ID which triggered this handler. This is useful
	 *              for Koji client implementations for different authentication
	 *              schemes. E.g. username and password authentication client.
	 *              We only need to override this method if there's more than one
	 *              authentication scheme supported.
	 * @throws MalformedURLException 
	 */
	public void setKojiClient(String cmdId) throws MalformedURLException {
		this.koji = new KojiSSLHubClient("bad-url ----> :)");
	}
	
	/**
	 * Setter for Koji client instance variable
	 */
	protected void setKojiClient(IKojiHubClient client) {
		this.koji = client;
	}

	protected boolean isScratch() {
		return false;
	}

	/**
	 * @param event
	 * @return the shell
	 * @throws ExecutionException
	 */
	private Shell getShell(ExecutionEvent event) throws ExecutionException {
		return HandlerUtil.getActiveShellChecked(event);
	}
	
	public final class YesNoRunnable implements Runnable {
		private final String question;
		private boolean okPressed;

		public YesNoRunnable(String question) {
			this.question = question;
		}

		@Override
		public void run() {
			okPressed = MessageDialog.openQuestion(shell,
					NonTranslatableStrings.getProductName(),
					question);
		}

		public boolean isOkPressed() {
			return okPressed;
		}
	}
}
