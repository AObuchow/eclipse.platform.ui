/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.markers.internal;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import org.eclipse.jface.operation.IRunnableWithProgress;

import org.eclipse.ui.internal.ide.StatusUtil;

/**
 * Represents a job that can be restarted. When a job is "restarted", the currently running
 * instance is cancelled and a new instance is scheduled once the previous one terminates.
 * This does not inherit from the Jobs API. Instead of subclassing this class, a pointer to
 * a IRunnableWithProgress should be passed into the constructor. 
 */
public final class RestartableJob {
	IRunnableWithProgress runnable;
	
	Job theJob;
	boolean restartNeeded = false;
	private Object lock = new Object();
	private IProgressMonitor currentMonitor = null;
	
	/**
	 * Constructs a new RestartableJob with the given name that will run the given
	 * runnable.
	 * 
	 * @param name
	 * @param newRunnable
	 */
	public RestartableJob(String name, IRunnableWithProgress newRunnable) {
		this.runnable = newRunnable;

		createJob(name);		
		
		theJob.addJobChangeListener(new JobChangeAdapter() {
			public void done(IJobChangeEvent e) {
				synchronized(lock) {
					currentMonitor = null;
					if (restartNeeded) {
						theJob.schedule();
					}
				}
			}
		});
	}
	
	/**
	 * Instantiates the actual Job object.
	 * 
	 * @param name
	 */
	private void createJob(String name) {
		theJob = new Job(name) {
			protected IStatus run(IProgressMonitor innerMonitor) {
				try {
					synchronized(lock) {
						restartNeeded = false;
						currentMonitor = innerMonitor;
					}
					runnable.run(innerMonitor);
				} catch (InvocationTargetException e) {
					return StatusUtil.newStatus(IStatus.ERROR, e.toString(), e.getTargetException());
				} catch (InterruptedException e) {
					return Status.CANCEL_STATUS;
				}
				if (innerMonitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				} else {
					return Status.OK_STATUS;
				}
			}
		};
	}
	
	/**
	 * Aborts the currently running job (if any) by cancelling its progress
	 * monitor, and reschedules it. If there is no currently running job,
	 * it will be started.
	 */
	public void restart() {
		synchronized(lock) {
			if (currentMonitor == null) {
				theJob.schedule();
			} else if (!restartNeeded) {
				restartNeeded = true;
				theJob.cancel();
			}
		}
	}

	/**
	 * Schedules the job. Does nothing if the job is already running. 
	 */
	public void schedule() {
		synchronized(lock) {
			if (currentMonitor == null) {
				theJob.schedule();
			} else {
				if (currentMonitor.isCanceled()) {
					restartNeeded = true;
				}
			}
		}
	}
	
	/**
	 * Cancels the job. If the job is currently running, it will be
	 * terminated as soon as possible.
	 */
	public void cancel() {
		synchronized(lock) {
			theJob.cancel();
			restartNeeded = false;
		}
	}
}
