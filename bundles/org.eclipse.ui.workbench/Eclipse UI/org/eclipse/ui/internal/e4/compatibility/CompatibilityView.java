/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import java.util.List;
import javax.inject.Inject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.renderers.swt.MenuManagerRenderer;
import org.eclipse.e4.ui.workbench.renderers.swt.StackRenderer;
import org.eclipse.e4.ui.workbench.renderers.swt.ToolBarManagerRenderer;
import org.eclipse.e4.ui.workbench.swt.factories.IRendererFactory;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.ViewReference;
import org.eclipse.ui.internal.WorkbenchPartReference;

public class CompatibilityView extends CompatibilityPart {

	private ViewReference reference;

	@Inject
	CompatibilityView(MPart part, ViewReference ref) {
		super(part);
		reference = ref;
	}

	public IViewPart getView() {
		return (IViewPart) getPart();
	}

	@Override
	void updateImages(MPart part) {
		EModelService ms = part.getContext().get(EModelService.class);
		MWindow topWin = ms.getTopLevelWindowFor(part);
		List<MPlaceholder> partRefs = ms.findElements(topWin, part.getElementId(),
				MPlaceholder.class, null);
		for (MUIElement ref : partRefs) {
			updateTabImages(ref);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.e4.compatibility.CompatibilityPart#getReference()
	 */
	@Override
	public WorkbenchPartReference getReference() {
		return reference;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.e4.compatibility.CompatibilityPart#createPartControl
	 * (org.eclipse.ui.IWorkbenchPart, org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createPartControl(IWorkbenchPart legacyPart, Composite parent) {
		// Some views (i.e. Console) require that the actual ToolBar be
		// instantiated before they are
		IActionBars actionBars = ((IViewPart) legacyPart).getViewSite().getActionBars();
		ToolBarManager tbm = (ToolBarManager) actionBars.getToolBarManager();
		Composite toolBarParent = new Composite(parent, SWT.NONE);
		tbm.createControl(toolBarParent);

		super.createPartControl(legacyPart, parent);

		// dispose the tb, it will be re-created when the tab is shown
		toolBarParent.dispose();

		IEclipseContext context = getModel().getContext();
		IRendererFactory rendererFactory = context.get(IRendererFactory.class);

		MenuManager mm = (MenuManager) actionBars.getMenuManager();
		MMenu menu = null;
		for (MMenu me : part.getMenus()) {
			if (me.getTags().contains(StackRenderer.TAG_VIEW_MENU)) {
				menu = me;
				break;
			}
		}
		if (menu == null) {
			menu = MenuFactoryImpl.eINSTANCE.createMenu();
			menu.setElementId(part.getElementId());

			menu.getTags().add(StackRenderer.TAG_VIEW_MENU);
			menu.getTags().add(ContributionsAnalyzer.MC_MENU);
			part.getMenus().add(menu);

		}
		AbstractPartRenderer apr = rendererFactory.getRenderer(menu, parent);
		if (apr instanceof MenuManagerRenderer) {
			((MenuManagerRenderer) apr).linkModelToManager(menu, mm);
		}

		// Construct the toolbar (if necessary)
		MToolBar toolbar = part.getToolbar();
		if (toolbar == null) {
			toolbar = MenuFactoryImpl.eINSTANCE.createToolBar();
			toolbar.setElementId(part.getElementId());
			part.setToolbar(toolbar);
		}
		apr = rendererFactory.getRenderer(toolbar, parent);
		if (apr instanceof ToolBarManagerRenderer) {
			((ToolBarManagerRenderer) apr).linkModelToManager(toolbar, tbm);
		}
	}
}
