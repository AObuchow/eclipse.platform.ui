package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.*;
import org.eclipse.jface.viewers.*;

/**
 * This class adds to the PluginAction support by
 * setting itself up for work within a WorkbenchPart.
 * The main difference is that it is capable of
 * processing local selection changes within a part.
 */
public class PartPluginAction extends PluginAction {
/**
 * PartPluginAction constructor.
 *
 */
public PartPluginAction(IConfigurationElement actionElement, String runAttribute) {
	super(actionElement, runAttribute);
}
/**
 * Handles the provided selection. We are interested
 * only in structured selections.
 */
protected void handleSelection(ISelection selection) {
	if (selection instanceof IStructuredSelection)
	   selectionChanged((IStructuredSelection)selection);
	else
	   selectionChanged(new StructuredSelection());
}
/**
 * Registers this action as a listener of the workbench part.
 */
protected void registerSelectionListener(IWorkbenchPart aPart) {
	ISelectionProvider selectionProvider = aPart.getSite().getSelectionProvider();
	if (selectionProvider != null) {
		selectionProvider.addSelectionChangedListener(this);
		handleSelection(selectionProvider.getSelection());
	}
}
/**
 * Handles selection change event as a selection listener.
 */
public void selectionChanged(SelectionChangedEvent event) {
	ISelection selection = event.getSelection();
	handleSelection(selection);
}
/**
 * Unregisters this action as a listener of the workbench part.
 */
protected void unregisterSelectionListener(IWorkbenchPart aPart) {
	ISelectionProvider selectionProvider = aPart.getSite().getSelectionProvider();
	if (selectionProvider != null) {
		selectionProvider.removeSelectionChangedListener(this);
	}
}
}
