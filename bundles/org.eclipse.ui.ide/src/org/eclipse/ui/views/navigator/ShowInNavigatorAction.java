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
package org.eclipse.ui.views.navigator;


import java.util.*;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.ISetSelectionTarget;

/**
 * An action which shows the current selection in the Navigator view.
 * For each element in the selection, if it is an <code>IResource</code>
 * it uses it directly, otherwise if it is an <code>IMarker</code> it uses the marker's resource,
 * otherwise if it is an <code>IAdaptable</code>, it tries to get the <code>IResource.class</code> adapter.
 */
public class ShowInNavigatorAction extends SelectionProviderAction {
	private IWorkbenchPage page;
	public ShowInNavigatorAction(IWorkbenchPage page, ISelectionProvider viewer) {
		super(viewer, ResourceNavigatorMessages.getString("ShowInNavigator.text")); //$NON-NLS-1$
		Assert.isNotNull(page);
		this.page = page;
		setDescription(ResourceNavigatorMessages.getString("ShowInNavigator.toolTip")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, INavigatorHelpContextIds.SHOW_IN_NAVIGATOR_ACTION);
	}
/**
 * Returns the resources in the given selection.
 *
 * @return a list of <code>IResource</code>
 */
List getResources(IStructuredSelection selection) {
	List v = new ArrayList();
	for (Iterator i = selection.iterator(); i.hasNext();) {
		Object o = i.next();
		if (o instanceof IResource) {
			v.add(o);
		} else if (o instanceof IMarker) {
			IResource resource = ((IMarker) o).getResource();
			v.add(resource);
		} else if (o instanceof IAdaptable) {
			IResource resource = (IResource) ((IAdaptable) o).getAdapter(IResource.class);
			if (resource != null) {
				v.add(resource);
			}
		}
	}
	return v;
}
/*
 * (non-Javadoc)
 * Method declared on IAction.
 */
/**
 * Shows the Navigator view and sets its selection to the resources
 * selected in this action's selection provider.
 */
public void run() {
	List v = getResources(getStructuredSelection());
	if (v.isEmpty())
		return;
	try {
		IViewPart view = page.showView(IPageLayout.ID_RES_NAV);
		if (view instanceof ISetSelectionTarget) {
			ISelection selection = new StructuredSelection(v);
			((ISetSelectionTarget) view).selectReveal(selection);
		}
	} catch (PartInitException e) {
		ErrorDialog.openError(
			page.getWorkbenchWindow().getShell(),
			ResourceNavigatorMessages.getString("ShowInNavigator.errorMessage"), //$NON-NLS-1$
			e.getMessage(),
			e.getStatus());
	}
}
/*
 * (non-Javadoc)
 * Method declared on SelectionProviderAction.
 */
public void selectionChanged(IStructuredSelection selection) {
	setEnabled(!getResources(selection).isEmpty());
}
}
