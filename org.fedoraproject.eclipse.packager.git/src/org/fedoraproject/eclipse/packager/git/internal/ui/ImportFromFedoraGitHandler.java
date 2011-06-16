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
package org.fedoraproject.eclipse.packager.git.internal.ui;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.fedoraproject.eclipse.packager.api.FedoraPackagerAbstractHandler;

/**
 * Handler to be able to kick off the "Import from Fedora Git"
 * dialog from a keyboard shortcut.
 *
 */
public class ImportFromFedoraGitHandler extends FedoraPackagerAbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		WizardDialog importDialog = new WizardDialog(getShell(event), new FedoraPackagerGitCloneWizard());
		importDialog.open();
		return null;
	}

}
