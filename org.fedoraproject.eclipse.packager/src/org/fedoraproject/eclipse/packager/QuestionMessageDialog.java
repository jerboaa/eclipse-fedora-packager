package org.fedoraproject.eclipse.packager;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Wrapper class for question based (yes/no) message dialogs.
 *
 */
public class QuestionMessageDialog implements Runnable {

	private final String question;
	private boolean okPressed;
	private Shell shell;
	private FedoraProjectRoot root;

	/**
	 * 
	 * @param question
	 * @param shell
	 * @param fpRoot 
	 */
	public QuestionMessageDialog(String question, Shell shell, FedoraProjectRoot fpRoot) {
		this.question = question;
		this.shell = shell;
		this.root = fpRoot;
	}

	@Override
	public void run() {
		okPressed = MessageDialog.openQuestion(shell,
				NonTranslatableStrings.getProductName(root),
				question);
	}

	/**
	 * @return {@code true} if and only if yes was pressed by the user.
	 */
	public boolean isOkPressed() {
		return okPressed;
	}
}
