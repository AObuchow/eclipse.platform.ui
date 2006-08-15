/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.RetargetAction;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.CommonViewerSiteFactory;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.IDescriptionProvider;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.NavigatorActionService;

/**
 * <p>
 * Manages the non-viewer responsibilities of the Common Navigator View Part,
 * including the display and population of the context menu and the registration
 * of extensions for opening content.
 * </p>
 * <p>
 * This class is not intended to be instantiated or subclassed by clients
 * </p>
 * 
 * @since 3.2
 */
public final class CommonNavigatorManager implements ISelectionChangedListener {

	private final CommonNavigator commonNavigator;

	private final INavigatorContentService contentService;

	private NavigatorActionService actionService;

	private final IDescriptionProvider commonDescriptionProvider;

	private final IStatusLineManager statusLineManager;

	private final ILabelProvider labelProvider;

	/**
	 * <p>
	 * Adds listeners to aNavigator to listen for selection changes and respond
	 * to mouse events.
	 * </p>
	 * 
	 * @param aNavigator
	 *            The CommonNavigator managed by this class. Requires a non-null
	 *            value.
	 */
	public CommonNavigatorManager(CommonNavigator aNavigator) {
		super();
		commonNavigator = aNavigator;
		contentService = commonNavigator.getNavigatorContentService();
		statusLineManager = commonNavigator.getViewSite().getActionBars()
				.getStatusLineManager();
		commonDescriptionProvider = contentService
				.createCommonDescriptionProvider();
		labelProvider = (ILabelProvider) commonNavigator.getCommonViewer()
				.getLabelProvider();
		init();
	}

	private void init() {
		CommonViewer commonViewer = commonNavigator.getCommonViewer();
		commonViewer.addPostSelectionChangedListener(this);
		updateStatusBar(commonViewer.getSelection());

		ICommonViewerSite commonViewerSite = CommonViewerSiteFactory
				.createCommonViewerSite(commonNavigator.getViewSite());
		actionService = new NavigatorActionService(commonViewerSite,
				commonViewer, commonViewer.getNavigatorContentService());

		initContextMenu();
		initViewMenu();

		final RetargetAction openAction = new RetargetAction(
				ICommonActionConstants.OPEN,
				CommonNavigatorMessages.Open_action_label);
		commonNavigator.getViewSite().getPage().addPartListener(openAction);
		openAction.setActionDefinitionId(ICommonActionConstants.OPEN);

		commonNavigator.getCommonViewer().addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				actionService.setContext(new ActionContext(commonNavigator.getCommonViewer().getSelection()));		
				actionService.fillActionBars(commonNavigator.getViewSite().getActionBars());							
				openAction.run();
			}
		}); 

	}

	/**
	 * <p>
	 * Called by {@link CommonNavigator} when the View Part is disposed.
	 * 
	 */
	public void dispose() {
		commonNavigator.getCommonViewer().removeSelectionChangedListener(this);
		actionService.dispose();
	}

	/**
	 * 
	 * @param anEvent
	 *            An event indicating the current selection of the
	 *            {@link CommonViewer}
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent anEvent) {
		updateStatusBar(anEvent.getSelection());
		if (anEvent.getSelection() instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) anEvent
					.getSelection();
			actionService.setContext(new ActionContext(structuredSelection));
			actionService.fillActionBars(commonNavigator.getViewSite()
					.getActionBars());
		}
	}

	/**
	 * @param aMemento
	 *            Used to restore state of action extensions via the
	 *            {@link NavigatorActionService}.
	 */
	public void restoreState(IMemento aMemento) {
		actionService.restoreState(aMemento);
		
		if(commonNavigator.getCommonViewer().getInput() != null) {
			actionService.setContext(new ActionContext(new StructuredSelection(commonNavigator.getCommonViewer().getInput())));		
			actionService.fillActionBars(commonNavigator.getViewSite().getActionBars());
		}
	}

	/**
	 * @param aMemento
	 *            Used to save state of action extensions via the
	 *            {@link NavigatorActionService}.
	 */
	public void saveState(IMemento aMemento) {
		actionService.saveState(aMemento);
	}

	/**
	 * <p>
	 * Fills aMenuManager with menu contributions from the
	 * {@link NavigatorActionService}.
	 * </p>
	 * 
	 * @param aMenuManager
	 *            A popup menu
	 * @see NavigatorActionService#fillContextMenu(IMenuManager)
	 * 
	 */
	protected void fillContextMenu(IMenuManager aMenuManager) {
		ISelection selection = commonNavigator.getCommonViewer().getSelection();
		actionService.setContext(new ActionContext(selection));
		actionService.fillContextMenu(aMenuManager);
	}

	/**
	 * <p>
	 * Initializes and registers the context menu.
	 * </p>
	 */
	protected void initContextMenu() {
		MenuManager menuMgr = new MenuManager(contentService
				.getViewerDescriptor().getPopupMenuId());
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {

			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
		TreeViewer commonViewer = commonNavigator.getCommonViewer();
		Menu menu = menuMgr.createContextMenu(commonViewer.getTree());

		commonViewer.getTree().setMenu(menu);

		actionService.prepareMenuForPlatformContributions(menuMgr,
				commonViewer, false);

	}

	protected void initViewMenu() {
		IMenuManager viewMenu = commonNavigator.getViewSite().getActionBars()
				.getMenuManager();
		viewMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		viewMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS
				+ "-end"));//$NON-NLS-1$	
	}

	/**
	 * @param aSelection
	 *            The current selection from the {@link CommonViewer}
	 */
	protected void updateStatusBar(ISelection aSelection) {

		Image img = null;
		if (aSelection != null && !aSelection.isEmpty()
				&& aSelection instanceof IStructuredSelection) {
			img = labelProvider.getImage(((IStructuredSelection) aSelection)
					.getFirstElement());
		}

		statusLineManager.setMessage(img, commonDescriptionProvider
				.getDescription(aSelection));
	}

	/**
	 * 
	 * @return The action service used by this manager
	 */
	public NavigatorActionService getNavigatorActionService() {
		return actionService;
	}

}
