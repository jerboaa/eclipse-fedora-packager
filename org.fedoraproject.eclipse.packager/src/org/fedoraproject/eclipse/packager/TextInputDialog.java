package org.fedoraproject.eclipse.packager;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * An Eclipse forms based text input dialog.
 *
 */
public class TextInputDialog extends InputDialog implements Runnable {

	/**
	 * 
	 * @param shell
	 * @param dialogTitle 
	 * @param dialogMessage 
	 */
	public TextInputDialog(Shell shell, String dialogTitle, String dialogMessage) {
		super(shell, dialogTitle, dialogMessage, "", null); //$NON-NLS-1$
	}

	@Override
	public void run() {
		open();
	}
	
}
