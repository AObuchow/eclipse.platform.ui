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
package org.eclipse.ui.internal.ide;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.AboutInfo;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.actions.NewWizardMenu;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.ide.IDEContributionItemFactory;
import org.eclipse.ui.internal.roles.RoleManager;
import org.eclipse.ui.internal.util.StatusLineContributionItem;

/**
 * Adds actions to a workbench window.
 */
public final class WorkbenchActionBuilder {


	private IWorkbenchWindow window;

	/** 
	 * A convience variable and method so that the actionConfigurer doesn't need to
	 * get passed into registerGlobalAction every time it's called.
	 */
	private IActionBarConfigurer actionBarConfigurer;
	
	
	// generic actions
	private IWorkbenchAction closeAction;
	private IWorkbenchAction closeAllAction;
	private IWorkbenchAction closeAllSavedAction;
	private IWorkbenchAction saveAction;
	private IWorkbenchAction saveAllAction;
	private IWorkbenchAction aboutAction;
	private IWorkbenchAction openPreferencesAction;
	private IWorkbenchAction saveAsAction;
	private IWorkbenchAction hideShowEditorAction;
	private IWorkbenchAction savePerspectiveAction;
	private IWorkbenchAction resetPerspectiveAction;
	private IWorkbenchAction editActionSetAction;
	private IWorkbenchAction closePerspAction;
	private IWorkbenchAction lockToolBarAction;
	private IWorkbenchAction closeAllPerspsAction;
	private IWorkbenchAction showViewMenuAction;
	private IWorkbenchAction showPartPaneMenuAction;
	private IWorkbenchAction nextPartAction;
	private IWorkbenchAction prevPartAction;
	private IWorkbenchAction nextEditorAction;
	private IWorkbenchAction prevEditorAction;
	private IWorkbenchAction nextPerspectiveAction;
	private IWorkbenchAction prevPerspectiveAction;
	private IWorkbenchAction activateEditorAction;
	private IWorkbenchAction maximizePartAction;
	private IWorkbenchAction workbenchEditorsAction;
	private IWorkbenchAction backwardHistoryAction;
	private IWorkbenchAction forwardHistoryAction;

	// generic retarget actions
	private IWorkbenchAction undoAction;
	private IWorkbenchAction redoAction;
	private IWorkbenchAction cutAction;
	private IWorkbenchAction copyAction;
	private IWorkbenchAction pasteAction;
	private IWorkbenchAction deleteAction;
	private IWorkbenchAction selectAllAction;
	private IWorkbenchAction findAction;
	private IWorkbenchAction printAction;
	private IWorkbenchAction revertAction;
	private IWorkbenchAction refreshAction;
	private IWorkbenchAction propertiesAction;
	private IWorkbenchAction moveAction;
	private IWorkbenchAction renameAction;
	private IWorkbenchAction goIntoAction;
	private IWorkbenchAction backAction;
	private IWorkbenchAction forwardAction;
	private IWorkbenchAction upAction;
	private IWorkbenchAction nextAction;
	private IWorkbenchAction previousAction;

	// IDE-specific actions
	private IWorkbenchAction projectPropertyDialogAction;
	private IWorkbenchAction newWizardAction;
	private IWorkbenchAction newWizardDropDownAction;
	private IWorkbenchAction importResourcesAction;
	private IWorkbenchAction exportResourcesAction;

	private IWorkbenchAction rebuildAllAction; // Full build
	private IWorkbenchAction quickStartAction;
	private IWorkbenchAction tipsAndTricksAction;
	private IWorkbenchAction roleManagerAction;
	
	// IDE-specific retarget actions
	private IWorkbenchAction addBookmarkAction;
	private IWorkbenchAction addTaskAction;
	private IWorkbenchAction rebuildProjectAction;
	private IWorkbenchAction openProjectAction;
	private IWorkbenchAction closeProjectAction;

	// contribution items
	// @issue should obtain from ContributionItemFactory
	private NewWizardMenu newWizardMenu;
	
	// @issue class is workbench internal
	private StatusLineContributionItem statusLineItem;
	
	
	/**
	 * Constructs a new action builder which contributes actions
	 * to the given window.
	 * 
	 * @param window the window
	 */
	public WorkbenchActionBuilder(IWorkbenchWindow window) {
		this.window = window;
	}

	/**
	 * Returns the window to which this action builder is contributing.
	 */
	private IWorkbenchWindow getWindow() {
		return window;
	}
	
	/**
	 * Hooks listeners on the preference store and the window's page, perspective and selection services.
	 */
	private void hookListeners() {

		// Listen to workbench page lifecycle methods to enable
		// and disable the perspective menu items as needed.
		getWindow().addPageListener(new IPageListener() {
			public void pageActivated(IWorkbenchPage page) {
				enableActions(page.getPerspective() != null);
			}
			public void pageClosed(IWorkbenchPage page) {
				IWorkbenchPage pg = getWindow().getActivePage();
				enableActions(pg != null && pg.getPerspective() != null);
			}
			public void pageOpened(IWorkbenchPage page) {
			}
		});

		getWindow().addPerspectiveListener(new IPerspectiveListener() {
			public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
				enableActions(true);
			}
			public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
			}
		});
	}

	/**
	 * Enables the menu items dependent on an active
	 * page and perspective.
	 * Note, the show view action already does its own 
	 * listening so no need to do it here.
	 */
	private void enableActions(boolean value) {
		hideShowEditorAction.setEnabled(value);
		savePerspectiveAction.setEnabled(value);
		lockToolBarAction.setEnabled(value);
		resetPerspectiveAction.setEnabled(value);
		editActionSetAction.setEnabled(value);
		closePerspAction.setEnabled(value);
		closeAllPerspsAction.setEnabled(value);
		newWizardMenu.setEnabled(value);
		newWizardDropDownAction.setEnabled(value);
		importResourcesAction.setEnabled(value);
		exportResourcesAction.setEnabled(value);
	}
	
	/**
	 * Builds the actions and contributes them to the given window.
	 */
	public void makeAndPopulateActions(IWorkbenchConfigurer windowConfigurer, IActionBarConfigurer actionBarConfigurer) {
		makeActions(windowConfigurer, actionBarConfigurer);
		populateMenuBar(actionBarConfigurer);
		populateCoolBar(actionBarConfigurer);
		populateStatusLine(actionBarConfigurer);
		hookListeners();
	}
	
	/**
	 * Fills the coolbar with the workbench actions.
	 */
	public void populateCoolBar(IActionBarConfigurer configurer) {
		configurer.addToToolBarMenu(new ActionContributionItem(lockToolBarAction));
		configurer.addToToolBarMenu(new ActionContributionItem(editActionSetAction));

		IToolBarManager tBarMgr = configurer.addToolBar(IWorkbenchActionConstants.TOOLBAR_FILE);
		configurer.addToolBarGroup(tBarMgr, IWorkbenchActionConstants.NEW_GROUP, true);
		tBarMgr.add(newWizardDropDownAction);
		configurer.addToolBarGroup(tBarMgr, IWorkbenchActionConstants.NEW_EXT, false);
		configurer.addToolBarGroup(tBarMgr, IWorkbenchActionConstants.SAVE_GROUP, false);
		tBarMgr.add(saveAction);
		tBarMgr.add(saveAsAction);
		configurer.addToolBarGroup(tBarMgr, IWorkbenchActionConstants.SAVE_EXT, false);
		tBarMgr.add(printAction);
		configurer.addToolBarGroup(tBarMgr, IWorkbenchActionConstants.PRINT_EXT, false);
		configurer.addToolBarGroup(tBarMgr, IWorkbenchActionConstants.BUILD_GROUP, true);
		IContributionItem item = IDEContributionItemFactory.BUILD.create(getWindow());
		registerGlobalAction(((ActionContributionItem) item).getAction());
		tBarMgr.add(item);
		configurer.addToolBarGroup(tBarMgr, IWorkbenchActionConstants.BUILD_EXT, false);
		configurer.addToolBarGroup(tBarMgr, IWorkbenchActionConstants.MB_ADDITIONS, true);

		tBarMgr = configurer.addToolBar(IWorkbenchActionConstants.TOOLBAR_NAVIGATE);
		configurer.addToolBarGroup(tBarMgr, IWorkbenchActionConstants.HISTORY_GROUP, true);
		tBarMgr.add(backwardHistoryAction);
		tBarMgr.add(forwardHistoryAction);
		configurer.addToolBarGroup(tBarMgr, IWorkbenchActionConstants.PIN_GROUP, true);
		tBarMgr.add(ContributionItemFactory.PIN_EDITOR.create(getWindow()));
	}
	
	/**
	 * Fills the menu bar with the workbench actions.
	 */
	public void populateMenuBar(IActionBarConfigurer configurer) {		
		IMenuManager menubar = configurer.getMenuManager();
		menubar.add(createFileMenu());
		menubar.add(createEditMenu());
		menubar.add(createNavigateMenu());
		menubar.add(createProjectMenu());
		menubar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		menubar.add(createWindowMenu());
		menubar.add(createHelpMenu());
	}
	
	/**
	 * Creates and returns the File menu.
	 */
	private MenuManager createFileMenu() {
		MenuManager menu = new MenuManager(IDEWorkbenchMessages.getString("Workbench.file"), IWorkbenchActionConstants.M_FILE); //$NON-NLS-1$
		menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_START));
		{
			this.newWizardMenu = new NewWizardMenu(getWindow());
			MenuManager newMenu = new MenuManager(IDEWorkbenchMessages.getString("Workbench.new")); //$NON-NLS-1$
			newMenu.add(this.newWizardMenu);
			menu.add(newMenu);
		}

		menu.add(new GroupMarker(IWorkbenchActionConstants.NEW_EXT));
		menu.add(new Separator());

		menu.add(closeAction);
		menu.add(closeAllAction);
		//		menu.add(closeAllSavedAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.CLOSE_EXT));
		menu.add(new Separator());
		menu.add(saveAction);
		menu.add(saveAsAction);
		menu.add(saveAllAction);

		menu.add(revertAction);
		menu.add(new Separator());
		menu.add(moveAction);
		menu.add(renameAction);
		menu.add(new Separator());
		menu.add(refreshAction);

		menu.add(new GroupMarker(IWorkbenchActionConstants.SAVE_EXT));
		menu.add(new Separator());
		menu.add(printAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.PRINT_EXT));
		menu.add(new Separator());
		menu.add(importResourcesAction);
		menu.add(exportResourcesAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.IMPORT_EXT));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

		menu.add(new Separator());
		menu.add(propertiesAction);

		menu.add(ContributionItemFactory.REOPEN_EDITORS.create(getWindow()));
		menu.add(new GroupMarker(IWorkbenchActionConstants.MRU));
		menu.add(new Separator());
		menu.add(ActionFactory.QUIT.create(getWindow()));
		menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_END));
		return menu;
	}

	/**
	 * Creates and returns the Edit menu.
	 */
	private MenuManager createEditMenu() {
		MenuManager menu = new MenuManager(IDEWorkbenchMessages.getString("Workbench.edit"), IWorkbenchActionConstants.M_EDIT); //$NON-NLS-1$
		menu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_START));

		menu.add(undoAction);
		menu.add(redoAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.UNDO_EXT));
		menu.add(new Separator());

		menu.add(cutAction);
		menu.add(copyAction);
		menu.add(pasteAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.CUT_EXT));
		menu.add(new Separator());

		menu.add(deleteAction);
		menu.add(selectAllAction);
		menu.add(new Separator());

		menu.add(findAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.FIND_EXT));
		menu.add(new Separator());

		menu.add(addBookmarkAction);
		menu.add(addTaskAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.ADD_EXT));

		menu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_END));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		return menu;
	}

	/**
	 * Creates and returns the Navigate menu.
	 */
	private MenuManager createNavigateMenu() {
		MenuManager menu = new MenuManager(IDEWorkbenchMessages.getString("Workbench.navigate"), IWorkbenchActionConstants.M_NAVIGATE); //$NON-NLS-1$
		menu.add(new GroupMarker(IWorkbenchActionConstants.NAV_START));
		menu.add(goIntoAction);

		MenuManager goToSubMenu = new MenuManager(IDEWorkbenchMessages.getString("Workbench.goTo"), IWorkbenchActionConstants.GO_TO); //$NON-NLS-1$
		menu.add(goToSubMenu);
		goToSubMenu.add(backAction);
		goToSubMenu.add(forwardAction);
		goToSubMenu.add(upAction);
		goToSubMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

		menu.add(new Separator(IWorkbenchActionConstants.OPEN_EXT));
		for (int i = 2; i < 5; ++i) {
			menu.add(new Separator(IWorkbenchActionConstants.OPEN_EXT + i));
		}
		menu.add(new Separator(IWorkbenchActionConstants.SHOW_EXT));
		{
			MenuManager showInSubMenu = new MenuManager(IDEWorkbenchMessages.getString("Workbench.showIn")); //$NON-NLS-1$
			showInSubMenu.add(ContributionItemFactory.VIEWS_SHOW_IN.create(getWindow()));
			menu.add(showInSubMenu);
		}
		for (int i = 2; i < 5; ++i) {
			menu.add(new Separator(IWorkbenchActionConstants.SHOW_EXT + i));
		}
		menu.add(new Separator());
		menu.add(nextAction);
		menu.add(previousAction);
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(new GroupMarker(IWorkbenchActionConstants.NAV_END));

		//TBD: Location of this actions
		menu.add(new Separator());
		menu.add(backwardHistoryAction);
		menu.add(forwardHistoryAction);
		return menu;
	}

	/**
	 * Creates and returns the Project menu.
	 */
	private MenuManager createProjectMenu() {
		MenuManager menu = new MenuManager(IDEWorkbenchMessages.getString("Workbench.project"), IWorkbenchActionConstants.M_PROJECT); //$NON-NLS-1$
		menu.add(new Separator(IWorkbenchActionConstants.PROJ_START));

		menu.add(openProjectAction);
		menu.add(closeProjectAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.OPEN_EXT));
		menu.add(new Separator());

		IContributionItem item = IDEContributionItemFactory.BUILD_PROJECT.create(getWindow());
		registerGlobalAction(((ActionContributionItem)item).getAction());
		menu.add(item);
		
		menu.add(rebuildProjectAction);

		item = IDEContributionItemFactory.BUILD.create(getWindow());
		registerGlobalAction(((ActionContributionItem)item).getAction());
		menu.add(item);

		menu.add(rebuildAllAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.BUILD_EXT));
		menu.add(new Separator());

		menu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(new GroupMarker(IWorkbenchActionConstants.PROJ_END));
		menu.add(new Separator());
		menu.add(projectPropertyDialogAction);
		return menu;
	}

	/**
	 * Creates and returns the Window menu.
	 */
	private MenuManager createWindowMenu() {
		MenuManager menu = new MenuManager(IDEWorkbenchMessages.getString("Workbench.window"), IWorkbenchActionConstants.M_WINDOW); //$NON-NLS-1$

		IWorkbenchAction action = ActionFactory.OPEN_NEW_WINDOW.create(getWindow());
		action.setText(IDEWorkbenchMessages.getString("Workbench.openNewWindow")); //$NON-NLS-1$
		menu.add(action);
		menu.add(new Separator());
		addPerspectiveActions(menu);
		menu.add(new Separator());
		addKeyboardShortcuts(menu);
		menu.add(new Separator());
		menu.add(workbenchEditorsAction);
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS + "end")); //$NON-NLS-1$
		menu.add(openPreferencesAction);
		menu.add(ContributionItemFactory.OPEN_WINDOWS.create(getWindow()));
		return menu;
	}

	/**
	 * Adds the perspective actions to the specified menu.
	 */
	private void addPerspectiveActions(MenuManager menu) {
		{
			String openText = IDEWorkbenchMessages.getString("Workbench.openPerspective"); //$NON-NLS-1$
			MenuManager changePerspMenuMgr = new MenuManager(openText);
			IContributionItem changePerspMenuItem = 
				ContributionItemFactory.PERSPECTIVES_SHORTLIST.create(getWindow());
			changePerspMenuMgr.add(changePerspMenuItem);
			menu.add(changePerspMenuMgr);
		}
		{
			MenuManager showViewMenuMgr = new MenuManager(IDEWorkbenchMessages.getString("Workbench.showView")); //$NON-NLS-1$
			IContributionItem showViewMenu = ContributionItemFactory.VIEWS_SHORTLIST.create(getWindow());
			showViewMenuMgr.add(showViewMenu);
			menu.add(showViewMenuMgr);
		}
		menu.add(hideShowEditorAction);
		menu.add(lockToolBarAction);
		menu.add(new Separator());
		menu.add(editActionSetAction);
		menu.add(savePerspectiveAction);
		menu.add(resetPerspectiveAction);
		menu.add(closePerspAction);
		menu.add(closeAllPerspsAction);
	}

	/**
	 * Adds the keyboard navigation submenu to the specified menu.
	 */
	private void addKeyboardShortcuts(MenuManager menu) {
		MenuManager subMenu = new MenuManager(IDEWorkbenchMessages.getString("Workbench.shortcuts")); //$NON-NLS-1$
		menu.add(subMenu);
		subMenu.add(showPartPaneMenuAction);
		subMenu.add(showViewMenuAction);
		subMenu.add(new Separator());
		subMenu.add(maximizePartAction);
		subMenu.add(new Separator());
		subMenu.add(activateEditorAction);
		subMenu.add(nextEditorAction);
		subMenu.add(prevEditorAction);
		subMenu.add(new Separator());
		subMenu.add(nextPartAction);
		subMenu.add(prevPartAction);
		subMenu.add(new Separator());
		subMenu.add(nextPerspectiveAction);
		subMenu.add(prevPerspectiveAction);
	}

	/**
	 * Creates and returns the Help menu.
	 */
	private MenuManager createHelpMenu() {
		MenuManager menu = new MenuManager(IDEWorkbenchMessages.getString("Workbench.help"), IWorkbenchActionConstants.M_HELP); //$NON-NLS-1$
		// See if a welcome page is specified
		if (quickStartAction != null)
			menu.add(quickStartAction);

		//Only add it if role filtering is on
		if(roleManagerAction != null)
			menu.add(roleManagerAction);
		
		// See if a tips and tricks page is specified
		if (tipsAndTricksAction != null)
			menu.add(tipsAndTricksAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.HELP_START));
		menu.add(new GroupMarker(IWorkbenchActionConstants.HELP_END));
		menu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		// about should always be at the bottom
		menu.add(new Separator());
		menu.add(aboutAction);
		return menu;
	}

	/**
	 * Disposes any resources and unhooks any listeners that are no longer needed.
	 * Called when the window is closed.
	 */
	public void dispose() {
		actionBarConfigurer.getStatusLineManager().remove(statusLineItem);
	}

	void updateModeLine(final String text) {
		statusLineItem.setText(text);
	}

	/**
	 * Returns true if the menu with the given ID should
	 * be considered as an OLE container menu. Container menus
	 * are preserved in OLE menu merging.
	 */
	public boolean isContainerMenu(String menuId) {
		if (menuId.equals(IWorkbenchActionConstants.M_FILE))
			return true;
		if (menuId.equals(IWorkbenchActionConstants.M_VIEW))
			return true;
		if (menuId.equals(IWorkbenchActionConstants.M_WORKBENCH))
			return true;
		if (menuId.equals(IWorkbenchActionConstants.M_WINDOW))
			return true;
		return false;
	}
	/**
	 * Return whether or not given id matches the id of the coolitems that
	 * the workbench creates.
	 */
	public boolean isWorkbenchCoolItemId(String id) {
		if (IWorkbenchActionConstants.TOOLBAR_FILE.equalsIgnoreCase(id)) return true;
		if (IWorkbenchActionConstants.TOOLBAR_NAVIGATE.equalsIgnoreCase(id)) return true;
		return false;
	}

	/**
	 * Fills the status line with the workbench contribution items.
	 */
	public void populateStatusLine(IActionBarConfigurer configurer) {
		IStatusLineManager statusLine = configurer.getStatusLineManager();
		statusLine.add(statusLineItem);
	}
	
	/**
	 * Creates actions (and contribution items) for the menu bar, toolbar and status line.
	 */
	private void makeActions(IWorkbenchConfigurer workbenchConfigurer, IActionBarConfigurer actionBarConfigurer) {

		// The actions in jface do not have menu vs. enable, vs. disable vs. color
		// There are actions in here being passed the workbench - problem 
		setCurrentActionBarConfigurer(actionBarConfigurer);
		
		// @issue should obtain from ConfigurationItemFactory
		statusLineItem = new StatusLineContributionItem("ModeContributionItem"); //$NON-NLS-1$
		
		newWizardAction = ActionFactory.NEW.create(getWindow());
		registerGlobalAction(newWizardAction);

		newWizardDropDownAction = IDEActionFactory.NEW_WIZARD_DROP_DOWN.create(getWindow());

		importResourcesAction = ActionFactory.IMPORT.create(getWindow());
		registerGlobalAction(importResourcesAction);

		exportResourcesAction = ActionFactory.EXPORT.create(getWindow());
		registerGlobalAction(exportResourcesAction);
		
		rebuildAllAction = IDEActionFactory.REBUILD_ALL.create(getWindow());
		registerGlobalAction(rebuildAllAction);

		saveAction = ActionFactory.SAVE.create(getWindow());
		registerGlobalAction(saveAction);

		saveAsAction = ActionFactory.SAVE_AS.create(getWindow());
		registerGlobalAction(saveAsAction);

		saveAllAction = ActionFactory.SAVE_ALL.create(getWindow());
		registerGlobalAction(saveAllAction);
		
		undoAction = ActionFactory.UNDO.create(getWindow());
		registerGlobalAction(undoAction);

		redoAction = ActionFactory.REDO.create(getWindow());
		registerGlobalAction(redoAction);

		cutAction = ActionFactory.CUT.create(getWindow());
		registerGlobalAction(cutAction);

		copyAction = ActionFactory.COPY.create(getWindow());
		registerGlobalAction(redoAction);

		pasteAction = ActionFactory.PASTE.create(getWindow());
		registerGlobalAction(pasteAction);

		printAction = ActionFactory.PRINT.create(getWindow());
		registerGlobalAction(printAction);

		selectAllAction = ActionFactory.SELECT_ALL.create(getWindow());
		registerGlobalAction(selectAllAction);
		
		findAction = ActionFactory.FIND.create(getWindow());
		registerGlobalAction(findAction);

		closeAction = ActionFactory.CLOSE.create(getWindow());
		registerGlobalAction(closeAction);

		closeAllAction = ActionFactory.CLOSE_ALL.create(getWindow());
		registerGlobalAction(closeAllAction);

		closeAllSavedAction = ActionFactory.CLOSE_ALL_SAVED.create(getWindow());
		registerGlobalAction(closeAllSavedAction);

		try {
			aboutAction = IDEActionFactory.ABOUT.create(getWindow());
			AboutInfo aboutInfo = workbenchConfigurer.getPrimaryFeatureAboutInfo();
			String productName = aboutInfo.getProductName();
			if (productName == null) {
				productName = ""; //$NON-NLS-1$
			}
			aboutAction.setText(IDEWorkbenchMessages.format("AboutAction.text", new Object[] { productName })); //$NON-NLS-1$
			aboutAction.setToolTipText(IDEWorkbenchMessages.format("AboutAction.toolTip", new Object[] { productName})); //$NON-NLS-1$
			aboutAction.setImageDescriptor(
				IDEInternalWorkbenchImages.getImageDescriptor(
					IDEInternalWorkbenchImages.IMG_OBJS_DEFAULT_PROD));
			registerGlobalAction(aboutAction);
		} catch (WorkbenchException e) {
			// do nothing
		}

		openPreferencesAction = ActionFactory.PREFERENCES.create(getWindow());
		registerGlobalAction(openPreferencesAction);

		addBookmarkAction = IDEActionFactory.BOOKMARK.create(getWindow());
		registerGlobalAction(addBookmarkAction);

		addTaskAction = IDEActionFactory.ADD_TASK.create(getWindow());
		registerGlobalAction(addTaskAction);

		deleteAction = ActionFactory.DELETE.create(getWindow());
		// don't register the delete action with the key binding service.
		// doing so would break cell editors that listen for keyPressed SWT 
		// events.
		// registerGlobalAction(deleteAction);

		try {
			AboutInfo[] infos = workbenchConfigurer.getAllFeaturesAboutInfo();
			// See if a welcome page is specified
			for (int i = 0; i < infos.length; i++) {
				if (infos[i].getWelcomePageURL() != null) {
					quickStartAction = IDEActionFactory.QUICK_START.create(getWindow());
					registerGlobalAction(quickStartAction);
					break;
				}
			}
			// See if a tips and tricks page is specified
			for (int i = 0; i < infos.length; i++) {
				if (infos[i].getTipsAndTricksHref() != null) {
					tipsAndTricksAction = IDEActionFactory.TIPS_AND_TRICKS.create(getWindow());
					registerGlobalAction(tipsAndTricksAction);
					break;
				}
			}
			
		} catch (WorkbenchException e) {
			IDEWorkbenchPlugin.log("Failed to read about info for all installed features.", e.getStatus()); //$NON-NLS-1$
		}

		// Actions for invisible accelerators
		showViewMenuAction = ActionFactory.SHOW_VIEW_MENU.create(getWindow());
		registerGlobalAction(showViewMenuAction);

		showPartPaneMenuAction = ActionFactory.SHOW_PART_PANE_MENU.create(getWindow());
		registerGlobalAction(showPartPaneMenuAction);

		nextEditorAction = ActionFactory.NEXT_EDITOR.create(getWindow());
		prevEditorAction = ActionFactory.PREVIOUS_EDITOR.create(getWindow());
		ActionFactory.linkCycleActionPair(nextEditorAction, prevEditorAction);
		registerGlobalAction(nextEditorAction);
		registerGlobalAction(prevEditorAction);

		nextPartAction = ActionFactory.NEXT_PART.create(getWindow());
		prevPartAction = ActionFactory.PREVIOUS_PART.create(getWindow());
		ActionFactory.linkCycleActionPair(nextPartAction, prevPartAction);
		registerGlobalAction(nextPartAction);
		registerGlobalAction(prevPartAction);

		nextPerspectiveAction = ActionFactory.NEXT_PERSPECTIVE.create(getWindow());
		prevPerspectiveAction = ActionFactory.PREVIOUS_PERSPECTIVE.create(getWindow());
		ActionFactory.linkCycleActionPair(nextPerspectiveAction, prevPerspectiveAction);
		registerGlobalAction(nextPerspectiveAction);
		registerGlobalAction(prevPerspectiveAction);

		activateEditorAction = ActionFactory.ACTIVATE_EDITOR.create(getWindow());
		registerGlobalAction(activateEditorAction);

		maximizePartAction = ActionFactory.MAXIMIZE.create(getWindow());
		registerGlobalAction(maximizePartAction);
		
		workbenchEditorsAction = ActionFactory.SHOW_OPEN_EDITORS.create(getWindow());
		registerGlobalAction(workbenchEditorsAction);

		hideShowEditorAction = ActionFactory.SHOW_EDITOR.create(getWindow());
		registerGlobalAction(hideShowEditorAction);
		savePerspectiveAction = ActionFactory.SAVE_PERSPECTIVE.create(getWindow());
		registerGlobalAction(savePerspectiveAction);
		editActionSetAction = ActionFactory.EDIT_ACTION_SETS.create(getWindow());
		registerGlobalAction(editActionSetAction);
		lockToolBarAction = ActionFactory.LOCK_TOOL_BAR.create(getWindow());
		registerGlobalAction(lockToolBarAction);
		resetPerspectiveAction = ActionFactory.RESET_PERSPECTIVE.create(getWindow());
		registerGlobalAction(resetPerspectiveAction);
		closePerspAction = ActionFactory.CLOSE_PERSPECTIVE.create(getWindow());
		registerGlobalAction(closePerspAction);
		closeAllPerspsAction = ActionFactory.CLOSE_ALL_PERSPECTIVES.create(getWindow());
		registerGlobalAction(closeAllPerspsAction);

		forwardHistoryAction = ActionFactory.FORWARD_HISTORY.create(getWindow());
		registerGlobalAction(forwardHistoryAction);

		backwardHistoryAction = ActionFactory.BACKWARD_HISTORY.create(getWindow());
		registerGlobalAction(backwardHistoryAction);

		revertAction = ActionFactory.REVERT.create(getWindow());
		registerGlobalAction(revertAction);

		refreshAction = ActionFactory.REFRESH.create(getWindow());
		registerGlobalAction(refreshAction);

		propertiesAction = ActionFactory.PROPERTIES.create(getWindow());
		registerGlobalAction(propertiesAction);

		moveAction = ActionFactory.MOVE.create(getWindow());
		registerGlobalAction(moveAction);

		renameAction = ActionFactory.RENAME.create(getWindow());
		registerGlobalAction(renameAction);

		goIntoAction = ActionFactory.GO_INTO.create(getWindow());
		registerGlobalAction(goIntoAction);

		backAction = ActionFactory.BACK.create(getWindow());
		registerGlobalAction(backAction);

		forwardAction = ActionFactory.FORWARD.create(getWindow());
		registerGlobalAction(forwardAction);

		upAction = ActionFactory.UP.create(getWindow());
		registerGlobalAction(upAction);

		nextAction = ActionFactory.NEXT.create(getWindow());
		nextAction.setImageDescriptor(
			IDEInternalWorkbenchImages.getImageDescriptor(
				IDEInternalWorkbenchImages.IMG_CTOOL_NEXT_NAV));
		registerGlobalAction(nextAction);

		previousAction = ActionFactory.PREVIOUS.create(getWindow());
		previousAction.setImageDescriptor(
			IDEInternalWorkbenchImages.getImageDescriptor(
				IDEInternalWorkbenchImages.IMG_CTOOL_PREVIOUS_NAV));
		registerGlobalAction(previousAction);

		rebuildProjectAction = IDEActionFactory.REBUILD_PROJECT.create(getWindow());
		registerGlobalAction(rebuildProjectAction);

		openProjectAction = IDEActionFactory.OPEN_PROJECT.create(getWindow());
		registerGlobalAction(openProjectAction);

		closeProjectAction = IDEActionFactory.CLOSE_PROJECT.create(getWindow());
		registerGlobalAction(closeProjectAction);
		
		projectPropertyDialogAction = IDEActionFactory.OPEN_PROJECT_PROPERTIES.create(getWindow());
		registerGlobalAction(projectPropertyDialogAction);

		//Only add the role manager action if we are using role support
		// @issue RoleManager is internal
		if(RoleManager.getInstance().isFiltering()){
			roleManagerAction = ActionFactory.ROLE_CONFIGURATION.create(getWindow());
			registerGlobalAction(roleManagerAction);
		}
	}

	private void setCurrentActionBarConfigurer(IActionBarConfigurer actionBarConfigurer)
	{
		this.actionBarConfigurer = actionBarConfigurer;
	}
	
	private void registerGlobalAction(IAction action) {
		actionBarConfigurer.registerGlobalAction(action);
	}
}
