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
package org.fedoraproject.eclipse.packager.utils;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.EditorPart;

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
}
