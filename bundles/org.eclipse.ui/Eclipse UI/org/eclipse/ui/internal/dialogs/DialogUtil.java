package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.ui.PartInitException;
import org.eclipse.swt.widgets.Shell;

/**
 * Utility class to help with dialogs.
 */
public class DialogUtil {
/**
 * DialogUtil constructor comment.
 */
public DialogUtil() {
	super();
}
/**
 * Open an error style dialog for PartInitException by
 * including any extra information from the nested
 * CoreException if present.
 */
public static void openError(Shell parent, String title, String message, PartInitException exception) {
	// Check for a nested CoreException
	CoreException nestedException = null;
	IStatus status = exception.getStatus();
	if (status != null && status.getException() instanceof CoreException)
		nestedException = (CoreException)status.getException();

	if (nestedException != null) {
		// Open an error dialog and include the extra
		// status information from the nested CoreException
		ErrorDialog.openError(
			parent,
			title,
			message,
			nestedException.getStatus());
	}
	else {
		// Open a regular error dialog since there is no
		// extra information to display
		MessageDialog.openError(
			parent,
			title,
			message);
	}
}
}
