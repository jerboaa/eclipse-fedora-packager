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
package org.fedoraproject.eclipse.packager.tests;


import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.fedoraproject.eclipse.packager.koji.KojiMessageDialog;
import org.fedoraproject.eclipse.packager.koji.KojiPlugin;
import org.junit.Test;

public class KojiMessageDialogTest {
	
	private KojiMessageDialog dialog;
	private Image titleImage;
	private Image contentImage;

	/**
	 * Useful test for bugs with the KojiDialog. This test is not intended to be
	 * run in a Hudson job, since it will block the test run until the OK button
	 * of the dialog is pressed.
	 */
	@Test
	public void canOpenMessageDialog() {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				ImageDescriptor descriptor = KojiPlugin
						.getImageDescriptor("icons/Artwork_DesignService_koji-icon-16.png"); //$NON-NLS-1$
				titleImage = descriptor.createImage();
				contentImage = KojiPlugin.getImageDescriptor(
						"icons/koji.png") //$NON-NLS-1$
						.createImage();
				try {
					dialog = new KojiMessageDialog(null,
							"blah", titleImage, MessageDialog.NONE,
							new String[] { IDialogConstants.OK_LABEL }, 0,
							new URL("http://test.com"), 1, "koji",
							contentImage, "koji");
					dialog.open();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
}
