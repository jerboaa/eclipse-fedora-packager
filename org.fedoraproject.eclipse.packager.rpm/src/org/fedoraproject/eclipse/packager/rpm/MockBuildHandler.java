package org.fedoraproject.eclipse.packager.rpm;

import java.util.HashMap;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.NLS;

public class MockBuildHandler extends RPMHandler implements IHandler {
	@Override
	public IStatus doExecute(ExecutionEvent event, IProgressMonitor monitor) throws ExecutionException {
		// build fresh SRPM
		IStatus result = makeSRPM(event, monitor);
		if (result.isOK()) {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			result = createMockJob(monitor);
		}

		return result;
	}

	protected IStatus createMockJob(IProgressMonitor monitor) {
		// get buildarch
		String buildarch;
		try {
			buildarch = rpmEval("_arch"); //$NON-NLS-1$
			final String mockcfg = getMockcfg(buildarch);

			monitor.subTask(NLS.bind(Messages.getString("MockBuildHandler.1"), specfile.getName())); //$NON-NLS-1$
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			return mockBuild(mockcfg);
		} catch (CoreException e) {
			e.printStackTrace();
			return handleError(e);
		}
	}

	protected IStatus mockBuild(String mockcfg) {
		IStatus status;
		IResource parent = specfile.getParent();
		String dir = parent.getLocation().toString();

		try {
			String cmd = "mock -r " + mockcfg + " --resultdir=" + dir //$NON-NLS-1$ //$NON-NLS-2$
			+ Path.SEPARATOR + makeTagName() + " rebuild " + dir //$NON-NLS-1$
			+ Path.SEPARATOR + rpmQuery(specfile, "NAME") + "-" //$NON-NLS-1$ //$NON-NLS-2$
			+ rpmQuery(specfile, "VERSION") + "-" //$NON-NLS-1$ //$NON-NLS-2$
			+ rpmQuery(specfile, "RELEASE") + ".src.rpm"; //$NON-NLS-1$ //$NON-NLS-2$

			String script = createShellScript(cmd);

			status = runShellCommand("sh " + script); //$NON-NLS-1$

			// refresh containing folder
			parent.refreshLocal(IResource.DEPTH_INFINITE,
					new NullProgressMonitor());
		} catch (CoreException e) {
			e.printStackTrace();
			status = handleError(e);
		}
		return status;
	}

	// USED FOR TESTING
	public int getExitStatus() throws Exception {
		if (proc != null) {
			return proc.exitValue();
		} else {
			throw new Exception(Messages.getString("MockBuildHandler.12")); //$NON-NLS-1$
		}
	}

	private String getMockcfg(String buildarch) throws CoreException {
		HashMap<String, String> branch = branches.get(specfile.getParent()
				.getName());
		String distvar = branch.get("distvar"); //$NON-NLS-1$
		String distval = branch.get("distval"); //$NON-NLS-1$
		String mockcfg = null;
		if (distvar.equals("rhel")) { //$NON-NLS-1$
			mockcfg = "fedora-" + distval + "-" + buildarch + "-epel"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} else {
			mockcfg = "fedora-" + distval + "-" + buildarch; //$NON-NLS-1$ //$NON-NLS-2$
			if (distval.equals("4") || distval.equals("5") //$NON-NLS-1$ //$NON-NLS-2$
					|| distval.equals("6")) { //$NON-NLS-1$
				mockcfg += "-core"; //$NON-NLS-1$
			}
			if (getBranchName(specfile.getParent().getName()).equals("devel")) { //$NON-NLS-1$
				mockcfg = "fedora-devel-" + buildarch; //$NON-NLS-1$
			}
		}
		return mockcfg;
	}

	@Override
	protected String getTaskName() {
		return Messages.getString("MockBuildHandler.27"); //$NON-NLS-1$
	}
}
