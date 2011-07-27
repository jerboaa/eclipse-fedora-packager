/*******************************************************************************
 * Copyright (c) 2010-2011 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.fedoraproject.eclipse.packager.utils;

import java.util.HashSet;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.EditorPart;
import org.fedoraproject.eclipse.packager.IProjectRoot;

/**
 * Handler bound utility class.
 */
public class FedoraHandlerUtils {
	
	/**
	 * Extract the IResource that was selected when the event was fired.
	 * @param event The fired execution event. 
	 * @return The resource that was selected.
	 */
	public static IResource getResource(ExecutionEvent event) {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part == null) {
			return null;
		}
		if (part instanceof EditorPart) {
			IEditorInput input = ((EditorPart) part).getEditorInput();
			if (input instanceof IFileEditorInput) {
				return ((IFileEditorInput) input).getFile();
			} else {
				return null;
			}
		}
		IWorkbenchSite site = part.getSite();
		if (site == null) {
			return null;
		}
		ISelectionProvider provider = site.getSelectionProvider();
		if (provider == null) {
			return null;
		}
		ISelection selection = provider.getSelection();
		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection)
					.getFirstElement();
			if (element instanceof IResource) {
				return (IResource) element;
			} else if (element instanceof IAdaptable) {
				IAdaptable adaptable = (IAdaptable) element;
				Object adapted = adaptable.getAdapter(IResource.class);
				return (IResource) adapted;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	/**
	 * Create an IStatus error
	 * 
	 * @param plugInID
	 *            The plug-in ID to be used.
	 * @param message
	 *            The error message for the status.
	 * @return A newly created Status instance.
	 */
	public static IStatus errorStatus(String plugInID, String message) {
		return new Status(IStatus.ERROR, plugInID, message);
	}
	
	/**
	 * Create an IStatus error
	 * 
	 * @param plugInID
	 *            The plug-in ID to be used.
	 * @param message
	 *            The error message for the status.
	 * @param e The exception occurred (if any).
	 * @return A newly created Status instance.
	 */
	public static IStatus errorStatus(String plugInID, String message, Throwable e) {
		return new Status(IStatus.ERROR, plugInID, message, e);
	}

	/**
	 * Show an information dialog.
	 * 
	 * @param shell A valid shell
	 * @param title The information dialog title
	 * @param message The message to be displayed.
	 * 
	 */
	public static void showInformationDialog(final Shell shell,
			final String title, final String message) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				MessageDialog.openInformation(shell, title, message);
			}
		});
	}
	
	/**
	 * Show an error dialog.
	 * 
	 * @param shell A valid shell
	 * @param title The error dialog title
	 * @param message The message to be displayed.
	 */
	public static void showErrorDialog(final Shell shell,
			final String title, final String message) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				MessageDialog.openError(shell, title, message);
			}
		});
	}
	
	/**
	 * Prompt user to select a file from within a root filtered by a given extension.
	 * @param shell The shell to run the prompt in.
	 * @param fedoraProjectRoot The root to find the file in.
	 * @param extension The extension filter for the file list.
	 * @param message The message that the accompanies the prompt.
	 * @return The path to the chosen file.
	 * @throws CoreException occurs if the root container cannot be found.
	 * @throws OperationCanceledException thrown if user cancels prompt.
	 */
	public static IPath chooseRootFileOfType(Shell shell, 
			IProjectRoot fedoraProjectRoot, String extension, 
			final String message) 
	throws CoreException, OperationCanceledException{
		HashSet<IResource> options = new HashSet<IResource>();
		for (IResource resource : fedoraProjectRoot.getContainer()
				.members(IContainer.INCLUDE_PHANTOMS)){
			if (resource.getName().endsWith(extension)){
				options.add(resource);
			}
		}
		if (options.size() == 0){
			return null;
		}
		final IResource[] syncOptions = options.toArray(new IResource[0]);
		final ListDialog ld = new ListDialog(shell);
		shell.getDisplay().syncExec(new Runnable() {
			@Override
			public void run(){
				ld.setContentProvider(new ArrayContentProvider());
				ld.setLabelProvider(new WorkbenchLabelProvider());
				ld.setInput(syncOptions);
				ld.setMessage(message);
				ld.open();
			}
		});
		if (ld.getReturnCode() == Window.CANCEL){
			throw new OperationCanceledException();
		}
		return ((IResource)ld.getResult()[0]).getLocation();
	}
}
