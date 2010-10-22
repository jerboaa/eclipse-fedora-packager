package org.fedoraproject.eclipse.packager.tests.utils.swtbot;

import java.util.StringTokenizer;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

public class PackageExplorer {
	
	private static SWTWorkbenchBot bot = new SWTWorkbenchBot();
	
	/**
	 * Opens Window => Show View => Other... => Java => Package Explorer
	 * view.
	 */
	public static void openView() throws Exception {
		// Open Package Explorer view
		bot.menu("Window").menu("Show View").menu("Other...").click();
		SWTBotShell shell = bot.shell("Show View");
		shell.activate();
		bot.tree().expandNode("Java").select("Package Explorer");
		bot.button("OK").click();
	}
	
	/**
	 * Assumes Package Explorer view is shown on the current perspective.
	 * 
	 * @return The tree of the Package Explorer view
	 */
	public static SWTBotTree getTree() {
		// Make sure view is active
		SWTBotView packageExplorer = bot.viewByTitle("Package Explorer");
		packageExplorer.show();
		packageExplorer.setFocus();
		return packageExplorer.bot().tree();
	}
	
	/**
	 * @param projectExplorerTree
	 * @param project
	 *            name of a project
	 * @return the project item pertaining to the project
	 */
	public static SWTBotTreeItem getProjectItem(SWTBotTree projectExplorerTree,
			String project) {
		for (SWTBotTreeItem item : projectExplorerTree.getAllItems()) {
			String itemText = item.getText();
			StringTokenizer tok = new StringTokenizer(itemText, " ");
			String name = tok.nextToken();
			// may be a dirty marker
			if (name.equals(">"))
				name = tok.nextToken();
			if (project.equals(name))
				return item;
		}
		return null;
	}
}
