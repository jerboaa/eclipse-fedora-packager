package org.fedoraproject.eclipse.packager.koji;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
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

public class KojiMessageDialog extends MessageDialog {
	String taskNo;

	public KojiMessageDialog(Shell parentShell, String dialogTitle,
			Image dialogTitleImage, String taskNo, int dialogImageType,
			String[] dialogButtonLabels, int defaultIndex) {
		super(parentShell, dialogTitle, dialogTitleImage,
				NLS.bind(Messages.getString("KojiMessageDialog.0"), taskNo), //$NON-NLS-1$
				dialogImageType, dialogButtonLabels, defaultIndex);
		this.taskNo = taskNo;
	}

	@Override
	public Image getImage() {
		// TODO Auto-generated method stub
		return KojiPlugin.getImageDescriptor("icons/koji.png") //$NON-NLS-1$
				.createImage();
	}

	@Override
	protected Control createCustomArea(Composite parent) {
		// Composite composite = new Composite(parent, SWT.NONE);
		// GridLayout layout = new GridLayout();
		// layout.marginHeight = 0;
		// layout.marginWidth = 0;
		// composite.setLayout(layout);
		// GridData data = new GridData(GridData.FILL_BOTH);
		// data.horizontalSpan = 2;
		// composite.setLayoutData(data);

		FormText taskLink = new FormText(parent, SWT.NONE);
		final String url = KojiHubClient.KOJI_WEB_URL + "/taskinfo?taskID=" //$NON-NLS-1$
				+ taskNo;
		taskLink.setText("<form><p>" +  //$NON-NLS-1$
				Messages.getString("KojiMessageDialog.3") + "</p><p>"+ url //$NON-NLS-1$ //$NON-NLS-2$
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
									"koji_task", null, null); //$NON-NLS-1$
					browser.openURL(new URL(url));
				} catch (PartInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		return taskLink;
	}
}
