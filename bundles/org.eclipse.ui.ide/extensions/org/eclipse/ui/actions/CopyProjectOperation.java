/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.actions;

import java.lang.reflect.InvocationTargetException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ProjectLocationSelectionDialog;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.progress.ProgressMonitorJobsDialog;

/**
 * Implementation class to perform the actual copying of project resources from the clipboard 
 * when paste action is invoked.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class CopyProjectOperation {

	/**
	 * Status containing the errors detected when running the operation or
	 * <code>null</code> if no errors detected.
	 */
	private MultiStatus errorStatus;

	/**
	 * The parent shell used to show any dialogs.
	 */
	private Shell parentShell;

    private String newName;

    private boolean cancelled;

	/** 
	 * Create a new operation initialized with a shell. 
	 * 
	 * @param shell parent shell for error dialogs
	 */	
	public CopyProjectOperation(Shell shell) {
		parentShell = shell;
	}
		
	/**
	 * Paste a copy of the project on the clipboard to the workspace.
	 */
	public void copyProject(IProject project) {

	    newName = null;
	    cancelled = false;
		errorStatus = null;
	
		//Get the project name and location in a two element list
		ProjectLocationSelectionDialog dialog =
			new ProjectLocationSelectionDialog(parentShell, project);
		dialog.setTitle(IDEWorkbenchMessages.getString("CopyProjectOperation.copyProject")); //$NON-NLS-1$
		if (dialog.open() != Dialog.OK) {
		    cancelled = true;
			return;
		}
		
		Object[] destinationPaths = dialog.getResult();
		if (destinationPaths == null) { 
		    cancelled = true;
			return;
		}
			
		newName = (String) destinationPaths[0];
		IPath newLocation = new Path((String) destinationPaths[1]);

		boolean completed = performProjectCopy(project, newName, newLocation);
	
		if (!completed) {// ie.- canceled
		    cancelled = true;
			return; // not appropriate to show errors
		}
	
		// If errors occurred, open an Error dialog
		if (errorStatus != null) {
		    cancelled = true;
			ErrorDialog.openError(
				parentShell, 
				IDEWorkbenchMessages.getString("CopyProjectOperation.copyFailedTitle"), //$NON-NLS-1$
				null, 
				errorStatus);
			errorStatus = null; 
		}
	}
	
	/**
	 * Returns whether the operation was cancelled or failed due to error.
	 * 
	 * @return <code>true</code> if the operation ws cancelled or failed due to an error,
	 *   <code>false</code> if the operation was successful
	 * @since 3.0.1
	 */
	public boolean isCancelled() {
	    return cancelled;
	}
	
	/**
	 * Returns the new name of the project.  Valid only if the operation was successful
	 * (<code>isCancelled</code> returns false).
	 * 
	 * @return the new name of the project, or <code>null</code> if the new name is unknown
	 * @since 3.0.1
	 */
	public String getNewName() {
	    return newName;
	}
	
	/**
	 * Copies the project to the new values.
	 *
	 * @param project the project to copy
	 * @param projectName the name of the copy
	 * @param newLocation IPath
	 * @return <code>true</code> if the copy operation completed, and 
	 *   <code>false</code> if it was abandoned part way
	 */
	private boolean performProjectCopy(
		final IProject project,
		final String projectName,
		final IPath newLocation) {
		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) {
	
				monitor.beginTask(IDEWorkbenchMessages.getString("CopyProjectOperation.progressTitle"), 100); //$NON-NLS-1$
				try {
					if (monitor.isCanceled())
						throw new OperationCanceledException();
	
					//Get a copy of the current description and modify it
					IProjectDescription newDescription =
						createProjectDescription(project, projectName, newLocation);
					monitor.worked(50);
	
					project.copy(newDescription, IResource.SHALLOW | IResource.FORCE, monitor);
	
					monitor.worked(50);
	
				} catch (CoreException e) {
					recordError(e); // log error
				} finally {
					monitor.done();
				}
			}
		};
	
		try {
			new ProgressMonitorJobsDialog(parentShell).run(true, true, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			final String message = e.getTargetException().getMessage();
			parentShell.getDisplay().syncExec(new Runnable() {
				public void run() {
					MessageDialog.openError(
						parentShell,
						IDEWorkbenchMessages.getString("CopyProjectOperation.copyFailedTitle"), //$NON-NLS-1$
						IDEWorkbenchMessages.format(
							"CopyProjectOperation.internalError", //$NON-NLS-1$
							new Object[] {message})); 
				}
			});		
			return false;
		}
	
		return true;
	}
	
	/**
	 * Create a new IProjectDescription for the copy using the auto-generated
	 * name and path.
	 * 
	 * @return IProjectDescription
	 * @param project the source project
	 * @param projectName the name for the new project
	 * @param rootLocation the path the new project will be stored under.
	 */
	private IProjectDescription createProjectDescription(
		IProject project,
		String projectName,
		IPath rootLocation)
		throws CoreException {
		//Get a copy of the current description and modify it
		IProjectDescription newDescription = project.getDescription();
		newDescription.setName(projectName);
	
		//If the location is the default then set the location to null
		if(rootLocation.equals(Platform.getLocation()))
			newDescription.setLocation(null);
		else
			newDescription.setLocation(rootLocation);
			
		return newDescription;
	}
	
		
	/**
	 * Records the core exception to be displayed to the user
	 * once the action is finished.
	 *
	 * @param exception a <code>CoreException</code>
	 */
	private void recordError(CoreException error) {
	
		if (errorStatus == null)
			errorStatus = new MultiStatus(
				PlatformUI.PLUGIN_ID, 
				IStatus.ERROR, 
				IDEWorkbenchMessages.getString("CopyProjectOperation.copyFailedMessage"), //$NON-NLS-1$
				error);
	
		errorStatus.merge(error.getStatus());
	}
}

