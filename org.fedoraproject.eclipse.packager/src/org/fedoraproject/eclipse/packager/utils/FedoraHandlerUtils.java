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
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.EditorPart;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.PackagerPlugin;

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
	 * @param message
	 * @return A newly created Status instance.
	 */
	public static IStatus error(String message) {
		return new Status(IStatus.ERROR, PackagerPlugin.PLUGIN_ID, message);
	}

	/**
	 * Create a MessageDialog 
	 * @param message
	 * @param exception
	 * @param isError
	 * @param showInDialog
	 * @return
	 */
	private static IStatus handleError(final String message, Throwable exception,
			final boolean isError, boolean showInDialog) {
		// do not ask for user interaction while in debug mode
		if (showInDialog) {
			if (Display.getCurrent() == null) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						if (isError) {
							MessageDialog.openError(null, FedoraPackagerText.get().commonHandler_fedoraPackagerName,
									message);
						} else {
							MessageDialog.openInformation(null,
									FedoraPackagerText.get().commonHandler_fedoraPackagerName, message);
						}
					}
				});
			} else {
				if (isError) {
					MessageDialog.openError(null, FedoraPackagerText.get().commonHandler_fedoraPackagerName, message);
				} else {
					MessageDialog.openInformation(null, FedoraPackagerText.get().commonHandler_fedoraPackagerName,
							message);
				}
			}
		}
		return new Status(isError ? IStatus.ERROR : IStatus.OK,
				PackagerPlugin.PLUGIN_ID, message, exception);
	}

	/**
	 * Create a user-friendly IStatus error message.
	 * 
	 * @param message
	 * 		The error which occurred.
	 * @return The IStatus object.
	 */
	public static IStatus handleError(String message) {
		return handleError(message, null, true, false);
	}

	/**
	 * Create a user-friendly IStatus error message.
	 * @param message
	 * 		The error which occurred.
	 * @param showInDialog
	 * 		Show error inline?
	 * @return The IStatus object.
	 */
	public static IStatus handleError(String message, boolean showInDialog) {
		return handleError(message, null, true, showInDialog);
	}

	/**
	 * Create a user-friendly IStatus message.
	 * @param message
	 * 		The message for this status.
	 * @param showInDialog
	 * 		Show dialog inline?
	 * @return The IStatus object.
	 */
	public static IStatus handleOK(String message, boolean showInDialog) {
		return handleError(message, null, false, showInDialog);
	}

	/**
	 * Create a user-friendly IStatus error message.
	 * @param e
	 * 		The Exception which occurred.
	 * @return The IStatus object.
	 */
	public static IStatus handleError(Exception e) {
		return handleError(e.getMessage(), e, true, false);
	}

	/**
	 * Create a user-friendly IStatus error message.
	 * @param e
	 * 		The Exception which occurred.
	 * @param showInDialog
	 * 		Show error inline?
	 * @return The IStatus object.
	 */
	public static IStatus handleError(Exception e, boolean showInDialog) {
		return handleError(e.getMessage(), e, true, showInDialog);
	}

}
