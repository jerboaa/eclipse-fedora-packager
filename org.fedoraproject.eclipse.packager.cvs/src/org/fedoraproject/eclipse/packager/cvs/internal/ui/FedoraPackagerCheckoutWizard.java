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
package org.fedoraproject.eclipse.packager.cvs.internal.ui;


import org.eclipse.core.resources.IProject;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.fedoraproject.eclipse.packager.FedoraPackagerLogger;
import org.fedoraproject.eclipse.packager.PackagerPlugin;
import org.fedoraproject.eclipse.packager.cvs.CVSCheckoutOperation;
import org.fedoraproject.eclipse.packager.cvs.CVSText;
import org.fedoraproject.eclipse.packager.utils.FedoraHandlerUtils;

/**
 * Wizard to checkout package content from Fedora CVS.
 *
 */
public class FedoraPackagerCheckoutWizard extends Wizard implements IImportWizard {

	private SelectModulePage page;
	private IStructuredSelection selection;

	/**
	 * Creates the wizards and sets that it needs progress monitor.
	 */
	public FedoraPackagerCheckoutWizard() {
		super();
		// Set title of wizard window
		setWindowTitle(CVSText.FedoraPackagerCheckoutWizard_wizardTitle);
	}

	@Override
	public void addPages() {
		page = new SelectModulePage();
		addPage(page);
		page.init(selection);
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}

	@Override
	public boolean performFinish() {
		try {
			// Prepare the checkout
			CVSCheckoutOperation cloneOp = new CVSCheckoutOperation();
			cloneOp.setModuleName(page.getPackageName());
			
			// set the runnable and the project
			cloneOp.prepareRunable();
			IRunnableWithProgress op = cloneOp.getRunnable();
			IProject newProject = cloneOp.getProject();
			
			// Finally, run the runnable :)
			getContainer().run(true, true, op);
			
			
			// Set persistent property so that we know when to show the context
			// menu item.
			newProject.setPersistentProperty(PackagerPlugin.PROJECT_PROP,
					"true" /* unused value */); //$NON-NLS-1$

			// Add new project to working sets, if requested
			IWorkingSet[] workingSets = page.getWorkingSets();
			if (workingSets.length > 0) {
				PlatformUI.getWorkbench().getWorkingSetManager()
						.addToWorkingSets(newProject, workingSets);
			}
			return true;
		} catch (InterruptedException e) {
			FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
			logger.logDebug(e.getMessage(), e);
			FedoraHandlerUtils.showInformationDialog(getShell(), "Cancelled", CVSText.FedoraPackagerCheckoutWizard_cancelled);
			return false;
		} catch (Exception e) {
			FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
			logger.logError(e.getMessage(), e);
			FedoraHandlerUtils.showErrorDialog(getShell(), "Error", NLS.bind(CVSText.FedoraPackagerCheckoutWizard_checkoutFailed, e.getMessage()));
			return false;
		}
	}
}
