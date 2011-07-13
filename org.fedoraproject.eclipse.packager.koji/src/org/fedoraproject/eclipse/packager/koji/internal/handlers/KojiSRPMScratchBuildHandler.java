package org.fedoraproject.eclipse.packager.koji.internal.handlers;

import java.util.HashSet;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.fedoraproject.eclipse.packager.FedoraPackagerLogger;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.api.FileDialogRunable;
import org.fedoraproject.eclipse.packager.api.errors.InvalidProjectRootException;
import org.fedoraproject.eclipse.packager.koji.KojiPlugin;
import org.fedoraproject.eclipse.packager.koji.KojiText;
import org.fedoraproject.eclipse.packager.koji.api.KojiSRPMBuildJob;
import org.fedoraproject.eclipse.packager.utils.FedoraHandlerUtils;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;

/**
 * Class that handles KojiBuildCommand in conjunction with KojiUploadSRPMCommand
 *
 */
public class KojiSRPMScratchBuildHandler extends KojiBuildHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
		this.shell = getShell(event);
		IResource eventResource = FedoraHandlerUtils.getResource(event);
		try {
			fedoraProjectRoot = FedoraPackagerUtils
					.getProjectRoot(eventResource);
		} catch (InvalidProjectRootException e) {
			logger.logError(FedoraPackagerText.invalidFedoraProjectRootError, 
					e);
			FedoraHandlerUtils.showErrorDialog(shell, "Error", //$NON-NLS-1$
					FedoraPackagerText.invalidFedoraProjectRootError);
			return null;
		}
		HashSet<IResource> options = new HashSet<IResource>();
		try {
			for (IResource resource : fedoraProjectRoot.getContainer()
					.members(IContainer.INCLUDE_PHANTOMS)){
				if (resource.getName().endsWith(".src.rpm")){ //$NON-NLS-1$
					options.add(resource);
				}
			}
		} catch (CoreException e) {
			// should not occur
			logger.logError(e.getMessage(), e);
			return FedoraHandlerUtils.errorStatus(KojiPlugin.PLUGIN_ID,
					e.getMessage(), e);
		}
		IPath srpmPath;
		if (options.size() == 0){
			FileDialogRunable fdr = new FileDialogRunable("*.src.rpm",  //$NON-NLS-1$
					KojiText.KojiSRPMScratchBuildHandler_UploadFileDialogTitle);
			shell.getDisplay().syncExec(fdr);
			String srpm = fdr.getFile();
			if (srpm == null){
				return Status.CANCEL_STATUS;
			}
			srpmPath = new Path(srpm);
		} else {
			final IResource[] syncOptions = options.toArray(new IResource[0]);
			final ListDialog ld = new ListDialog(shell);
			shell.getDisplay().syncExec(new Runnable() {
				@Override
				public void run(){
					ld.setContentProvider(new ArrayContentProvider());
					ld.setLabelProvider(new WorkbenchLabelProvider());
					ld.setInput(syncOptions);
					ld.setMessage(KojiText.KojiSRPMBuildJob_ChooseSRPM);
					ld.open();
				}
			});
			if (ld.getReturnCode() == Window.CANCEL){
				return Status.CANCEL_STATUS;
			}
			srpmPath = ((IResource)ld.getResult()[0]).getLocation();
		}
		Job job = new KojiSRPMBuildJob(
				fedoraProjectRoot.getProductStrings().getProductName(), 
				getShell(event), fedoraProjectRoot, srpmPath);
		job.addJobChangeListener(getJobChangeListener());
		job.setUser(true);
		job.schedule();
		return null; // must be null
	}

}
