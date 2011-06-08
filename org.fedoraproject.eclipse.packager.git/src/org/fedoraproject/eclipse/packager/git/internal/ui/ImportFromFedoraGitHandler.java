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
