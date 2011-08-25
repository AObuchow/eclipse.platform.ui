/*******************************************************************************
 * Copyright (c) 2010, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MGenericStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.widgets.CTabFolder;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.SubActionBars;
import org.eclipse.ui.services.IServiceLocator;

public class ActionBars extends SubActionBars {

	private IToolBarManager toolbarManager;

	private IMenuManager menuManager;

	private MPart part;

	public ActionBars(final IActionBars parent, final IServiceLocator serviceLocator, MPart part) {
		super(parent, serviceLocator);
		this.part = part;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionBars#getMenuManager()
	 */
	public IMenuManager getMenuManager() {
		if (menuManager == null) {
			menuManager = new MenuManager();
		}
		return menuManager;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionBars#getToolBarManager()
	 */
	public IToolBarManager getToolBarManager() {
		if (toolbarManager == null) {
			toolbarManager = new ToolBarManager(SWT.FLAT | SWT.RIGHT | SWT.WRAP);
		}
		return toolbarManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionBars#updateActionBars()
	 */
	public void updateActionBars() {
		// FIXME compat: updateActionBars : should do something useful
		getStatusLineManager().update(false);
		getMenuManager().update(false);
		if (toolbarManager != null) {
			//			System.err.println("update toolbar manager for " + part.getElementId()); //$NON-NLS-1$
			if (toolbarManager instanceof ToolBarManager) {
				ToolBarManager tbm = (ToolBarManager) toolbarManager;
				Control tbCtrl = tbm.getControl();
				if (tbCtrl == null || tbCtrl.isDisposed()) {
					if (part.getContext() != null) {
						// TODO what to do
					}
				} else {
					tbm.update(true);
					if (!tbCtrl.isDisposed()) {
						getPackParent(tbCtrl).pack();
					}
				}
			} else {
				toolbarManager.update(false);
			}
		}
		super.updateActionBars();
	}

	private Control getPackParent(Control control) {
		Composite parent = control.getParent();
		while (parent != null) {
			if (parent instanceof CTabFolder) {
				Control topRight = ((CTabFolder) parent).getTopRight();
				if (topRight != null) {
					return topRight;
				}
				break;
			}
			parent = parent.getParent();
		}
		return control.getParent();
	}

	boolean isSelected(MPart part) {
		MElementContainer<?> parent = part.getParent();
		if (parent == null) {
			MPlaceholder placeholder = part.getCurSharedRef();
			if (placeholder == null) {
				return false;
			}

			parent = placeholder.getParent();
			return parent instanceof MGenericStack ? parent.getSelectedElement() == placeholder
					: parent != null;
		}
		return parent instanceof MGenericStack ? parent.getSelectedElement() == part
				: parent != null;
	}

}
