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

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.PackagerPlugin;
import org.fedoraproject.eclipse.packager.QuestionMessageDialog;

/**
 * Utility class for UI related items
 *
 */
public class UiUtils {

	/**
	 * Opens Fedora Packaging specific perspective
	 * @param shell
	 * @throws WorkbenchException
	 */
	public static void openPerspective(Shell shell) throws WorkbenchException {
		// Finally ask if the Fedora Packaging perspective should be opened
		// if not already open.
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		IPerspectiveDescriptor perspective = window.getActivePage().getPerspective();
		if (!perspective.getId().equals(PackagerPlugin.FEDORA_PACKAGING_PERSPECTIVE_ID)) {
			// Ask if Fedora Packager perspective should be opened.
			QuestionMessageDialog op = new QuestionMessageDialog(
					FedoraPackagerText.UiUtils_switchPerspectiveQuestionTitle,
					FedoraPackagerText.UiUtils_switchPerspectiveQuestionMsg,
					shell);
			Display.getDefault().syncExec(op);
			if (op.isOkPressed()) {
				// open the perspective
				workbench.showPerspective(PackagerPlugin.FEDORA_PACKAGING_PERSPECTIVE_ID, window);
			}
		}
	}
}
