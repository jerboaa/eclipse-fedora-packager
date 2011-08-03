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

import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;

/**
 * Tooltip with HTML enablement.
 *
 */
public class HtmlTooltip extends ToolTip {

	private String html;
	private int width;
	private int height;
	
	/**
	 * 
	 * @param control The control to which to attach to.
	 * @param html HTML content for tooltip.
	 * @param width The width of the tooltip.
	 * @param height The height of the tooltip.
	 */
	public HtmlTooltip(Control control, String html, int width, int height) {
		super(control);
		this.html = html;
		this.width = width;
		this.height = height;
	}

	@Override
	protected Composite createToolTipContentArea(Event event, Composite parent) {
		Composite comp = new Composite(parent, SWT.BALLOON);
		GridLayout l = new GridLayout(1, false);
		l.horizontalSpacing = 0;
		l.marginWidth = 0;
		l.marginHeight = 0;
		l.verticalSpacing = 0;

		comp.setLayout(l);
		Browser browser = new Browser(comp, SWT.BORDER);
		browser.setJavascriptEnabled(false);
		browser.setBackgroundMode(SWT.INHERIT_DEFAULT);
		browser.setText(html);
		browser.setLayoutData(new GridData(width, height));
		comp.pack();
		return comp;
	}

}
