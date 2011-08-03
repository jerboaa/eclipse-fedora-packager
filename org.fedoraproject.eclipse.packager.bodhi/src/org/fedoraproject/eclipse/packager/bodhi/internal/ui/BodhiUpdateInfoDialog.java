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
package org.fedoraproject.eclipse.packager.bodhi.internal.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.forms.widgets.FormText;
import org.fedoraproject.eclipse.packager.bodhi.BodhiPlugin;
import org.fedoraproject.eclipse.packager.bodhi.BodhiText;

/**
 * Message dialog showing the link to the bodhi Web page showing update info
 * 
 */
public class BodhiUpdateInfoDialog extends MessageDialog {
	
	private URL bodhiInstanceUrl;
	private String buildName;
	
	/**
	 * Creates a message info dialog. The link to the bodhi update
	 * is created by createCustomArea().
	 * 
	 * @param parentShell
	 * @param bodhiInstanceUrl The URL to the bodhi instance.
	 * @param buildName The name of the update just pushed.
	 * @param bodhiResponseMsg Placeholder for auxiliary response msg from Bodhi.
	 */
	public BodhiUpdateInfoDialog(Shell parentShell, URL bodhiInstanceUrl, String buildName, String bodhiResponseMsg) {
		super(parentShell, BodhiText.BodhiUpdateInfoDialog_updateResponseTitle,
				BodhiPlugin.getImageDescriptor("icons/Artwork_DesignService_bodhi-icon-16.png").createImage(), //$NON-NLS-1$
				bodhiResponseMsg,
				MessageDialog.NONE,
				new String[] { IDialogConstants.OK_LABEL },
				0);
		this.buildName = buildName;
		this.bodhiInstanceUrl = bodhiInstanceUrl;
	}

	@Override
	public Image getImage() {
		return BodhiPlugin.getImageDescriptor("icons/bodhi-icon-48.png") //$NON-NLS-1$
				.createImage();
	}

	/**
	 * Draw the link plus other response on area.
	 */
	@Override
	protected Control createCustomArea(Composite parent) {
		FormText taskLink = new FormText(parent, SWT.NONE);
		final String url = this.bodhiInstanceUrl.toString() + buildName;
		taskLink.setText("<form><p>" + //$NON-NLS-1$
				BodhiText.BodhiUpdateInfoDialog_updateStatusText + "</p><p>"+ url //$NON-NLS-1$ //$NON-NLS-2$
						+ "</p></form>", true, true); //$NON-NLS-1$
		taskLink.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				try {
					IWebBrowser browser = PlatformUI
							.getWorkbench()
							.getBrowserSupport()
							.createBrowser(
									IWorkbenchBrowserSupport.NAVIGATION_BAR
											| IWorkbenchBrowserSupport.LOCATION_BAR
											| IWorkbenchBrowserSupport.STATUS,
									"bodhi_update", null, null); //$NON-NLS-1$
					browser.openURL(new URL(url));
				} catch (PartInitException e) {
					e.printStackTrace();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		});
		return taskLink;
	}
}
