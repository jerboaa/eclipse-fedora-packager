package org.fedoraproject.eclipse.packager.rpm.internal.core;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Observes status prints on the console and updates the progress monitor.
 *
 */
public class MockBuildStatusObserver implements Observer {

	private IProgressMonitor monitor;
	
	/**
	 * @param monitor
	 */
	public MockBuildStatusObserver(IProgressMonitor monitor) {
		this.monitor = monitor;
	}

	@Override
	public void update(Observable object, Object arg) {
		if (arg instanceof String) {
			// update the subtask
			monitor.subTask((String)arg);
		}
	}

}
