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

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Preferences;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;

import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.actions.NewWizardMenu;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.ide.IIDEActionConstants;

import org.eclipse.ui.internal.AboutInfo;
import org.eclipse.ui.internal.EditorWorkbook;
import org.eclipse.ui.internal.EditorsDropDownAction;
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
	private IWorkbenchAction quitAction;
	private IWorkbenchAction moveAction;
	private IWorkbenchAction renameAction;
	private IWorkbenchAction goIntoAction;
	private IWorkbenchAction backAction;
	private IWorkbenchAction forwardAction;
	private IWorkbenchAction upAction;
	private IWorkbenchAction nextAction;
	private IWorkbenchAction previousAction;
    private IWorkbenchAction categoryAction;

	// @issue editorsDropDownAction should be provided by ActionFactory
	private EditorsDropDownAction editorsDropDownAction;
	
	// IDE-specific actions
	private IWorkbenchAction projectPropertyDialogAction;
	private IWorkbenchAction newWizardAction;
	private IWorkbenchAction newWizardDropDownAction;
	private IWorkbenchAction importResourcesAction;
	private IWorkbenchAction exportResourcesAction;

	private IWorkbenchAction rebuildAllAction; // Full build
	private IWorkbenchAction buildAllAction; // Incremental build
	private IWorkbenchAction quickStartAction;
	private IWorkbenchAction tipsAndTricksAction;
	
	// IDE-specific retarget actions
	private IWorkbenchAction addBookmarkAction;
	private IWorkbenchAction addTaskAction;
	private IWorkbenchAction buildProjectAction;
	private IWorkbenchAction rebuildProjectAction;
	private IWorkbenchAction openProjectAction;
	private IWorkbenchAction closeProjectAction;

	// contribution items
	// @issue should obtain from ContributionItemFactory
	private NewWizardMenu newWizardMenu;
	
	// @issue class is workbench internal
	private StatusLineContributionItem statusLineItem;
	
	private Preferences.IPropertyChangeListener prefListener;

	private IWorkbenchAction introAction;
	
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
				// do nothing
			}
		});

		getWindow().addPerspectiveListener(new IPerspectiveListener() {
			public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
				enableActions(true);
			}
			public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
				// do nothing
			}
		});
		
		prefListener = new Preferences.IPropertyChangeListener() {
			public void propertyChange(Preferences.PropertyChangeEvent event) {
				if (event.getProperty().equals(ResourcesPlugin.PREF_AUTO_BUILDING)) {
					final boolean autoBuild = ResourcesPlugin.getWorkspace().isAutoBuilding();
					if (window.getShell() != null && !window.getShell().isDisposed()) {
						// this property change notification could be from a non-ui thread
						window.getShell().getDisplay().syncExec(new Runnable() {
							public void run() {
								if (autoBuild) {
									removeManualIncrementalBuildAction();
								}
								else {
									addManualIncrementalBuildAction();
								}
							}
						});
					}
				}
			}
		};
		ResourcesPlugin.getPlugin().getPluginPreferences().addPropertyChangeListener(prefListener);
		
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
		boolean autoBuild = ResourcesPlugin.getWorkspace().isAutoBuilding();
		if (!autoBuild) {
			addManualIncrementalBuildAction();
		}
		populateStatusLine(actionBarConfigurer);
		hookListeners();
	}
	
	
	/**
	 * Fills the coolbar with the workbench actions.
	 */
	public void populateCoolBar(IActionBarConfigurer configurer) {
		ICoolBarManager cbManager = configurer.getCoolBarManager();
		
		{ // Set up the context Menu
			IMenuManager popUpMenu = new MenuManager();
			popUpMenu.add(new ActionContributionItem(lockToolBarAction));
			popUpMenu.add(new ActionContributionItem(editActionSetAction));
			cbManager.setContextMenuManager(popUpMenu);
		}
		cbManager.add(new GroupMarker(IIDEActionConstants.GROUP_FILE));
		{ // File Group
			IToolBarManager fileToolBar = new ToolBarManager(cbManager.getStyle());
			fileToolBar.add(new Separator(IWorkbenchActionConstants.NEW_GROUP));
			fileToolBar.add(newWizardDropDownAction);
			fileToolBar.add(new GroupMarker(IWorkbenchActionConstants.NEW_EXT));
			fileToolBar.add(new GroupMarker(IWorkbenchActionConstants.SAVE_GROUP));
			fileToolBar.add(saveAction);
			fileToolBar.add(new GroupMarker(IWorkbenchActionConstants.SAVE_EXT));
			fileToolBar.add(printAction);
			fileToolBar.add(new GroupMarker(IWorkbenchActionConstants.PRINT_EXT));
			
			fileToolBar.add(new Separator(IWorkbenchActionConstants.BUILD_GROUP));	
			fileToolBar.add(new GroupMarker(IWorkbenchActionConstants.BUILD_EXT));
			fileToolBar.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
			
			// Add to the cool bar manager
			cbManager.add(new ToolBarContributionItem(fileToolBar,IWorkbenchActionConstants.TOOLBAR_FILE));
		}
		
		cbManager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		
		cbManager.add(new GroupMarker(IIDEActionConstants.GROUP_NAV));
		{ // Navigate group
			IToolBarManager navToolBar = new ToolBarManager(cbManager.getStyle());
			navToolBar.add(new Separator(IWorkbenchActionConstants.HISTORY_GROUP));
			navToolBar.add(new GroupMarker(IWorkbenchActionConstants.GROUP_APP));
			navToolBar.add(backwardHistoryAction);
			navToolBar.add(forwardHistoryAction);
			navToolBar.add(new Separator(IWorkbenchActionConstants.PIN_GROUP));
			navToolBar.add(ContributionItemFactory.PIN_EDITOR.create(getWindow()));
			
			// Add to the cool bar manager
			cbManager.add(new ToolBarContributionItem(navToolBar,IWorkbenchActionConstants.TOOLBAR_NAVIGATE));
		}
		
		cbManager.add(new GroupMarker(IWorkbenchActionConstants.GROUP_EDITOR));
		
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
		menu.add(quitAction);
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
		menu.add(rebuildProjectAction);
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

        //Only add it if role filtering is on
        if (categoryAction != null) 
            menu.add(categoryAction);

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
		if (editorsDropDownAction != null) {
			subMenu.add(editorsDropDownAction);
		}
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
		// See if a welcome or intro page is specified
		if (introAction != null)
			menu.add(introAction);
		else if (quickStartAction != null)
			menu.add(quickStartAction);
		
        // See if a tips and tricks page is specified
		if (tipsAndTricksAction != null)
			menu.add(tipsAndTricksAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.HELP_START));
		menu.add(new GroupMarker(IWorkbenchActionConstants.HELP_END));
		menu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		// about should always be at the bottom				
		menu.add(new Separator());
		menu.add(aboutAction);
		
		/*		
		final IMutableContextActivationService contextActivationServiceA = ContextActivationServiceFactory.getMutableContextActivationService();
		contextActivationServiceA.setActiveContextIds(new HashSet(Collections.singletonList("A")));

		final IMutableContextActivationService contextActivationServiceB = ContextActivationServiceFactory.getMutableContextActivationService();
		contextActivationServiceB.setActiveContextIds(new HashSet(Collections.singletonList("B")));				
		
		menu.add(new Separator());
		
		menu.add(new Action("Add context A to the workbench") {
			public void run() {
				IWorkbench workbench = PlatformUI.getWorkbench();
				IWorkbenchContextSupport workbenchContextSupport = (IWorkbenchContextSupport) workbench.getContextSupport();
				workbenchContextSupport.getCompoundContextActivationService().addContextActivationService(contextActivationServiceA);
			}
		});

		menu.add(new Action("Remove context A from the workbench") {
			public void run() {
				IWorkbench workbench = PlatformUI.getWorkbench();
				IWorkbenchContextSupport workbenchContextSupport = (IWorkbenchContextSupport) workbench.getContextSupport();
				workbenchContextSupport.getCompoundContextActivationService().removeContextActivationService(contextActivationServiceA);
			}
		});
		
		menu.add(new Action("Add context B to the workbench") {
			public void run() {
				IWorkbench workbench = PlatformUI.getWorkbench();
				IWorkbenchContextSupport workbenchContextSupport = (IWorkbenchContextSupport) workbench.getContextSupport();
				workbenchContextSupport.getCompoundContextActivationService().addContextActivationService(contextActivationServiceB);
			}
		});

		menu.add(new Action("Remove context B from the workbench") {
			public void run() {
				IWorkbench workbench = PlatformUI.getWorkbench();
				IWorkbenchContextSupport workbenchContextSupport = (IWorkbenchContextSupport) workbench.getContextSupport();
				workbenchContextSupport.getCompoundContextActivationService().removeContextActivationService(contextActivationServiceB);
			}
		});
		
		menu.add(new Separator());
		
		menu.add(new Action("Add context A to the workbench page") {
			public void run() {
				IWorkbench workbench = PlatformUI.getWorkbench();
				IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();

				if (workbenchWindow != null) {
					IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
					
					if (workbenchPage != null) {					
						IWorkbenchPageContextSupport workbenchPageContextSupport = (IWorkbenchPageContextSupport) workbenchPage.getContextSupport();
						workbenchPageContextSupport.getCompoundContextActivationService().addContextActivationService(contextActivationServiceA);
					}
				}
			}
		});
		
		menu.add(new Action("Remove context A from the workbench page") {
			public void run() {
				IWorkbench workbench = PlatformUI.getWorkbench();
				IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();

				if (workbenchWindow != null) {
					IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
					
					if (workbenchPage != null) {					
						IWorkbenchPageContextSupport workbenchPageContextSupport = (IWorkbenchPageContextSupport) workbenchPage.getContextSupport();
						workbenchPageContextSupport.getCompoundContextActivationService().removeContextActivationService(contextActivationServiceA);
					}
				}
			}
		});		
		
		menu.add(new Action("Add context B to the workbench page") {
			public void run() {
				IWorkbench workbench = PlatformUI.getWorkbench();
				IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();

				if (workbenchWindow != null) {
					IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
					
					if (workbenchPage != null) {					
						IWorkbenchPageContextSupport workbenchPageContextSupport = (IWorkbenchPageContextSupport) workbenchPage.getContextSupport();
						workbenchPageContextSupport.getCompoundContextActivationService().addContextActivationService(contextActivationServiceB);
					}
				}
			}
		});

		menu.add(new Action("Remove context B from the workbench page") {
			public void run() {
				IWorkbench workbench = PlatformUI.getWorkbench();
				IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();

				if (workbenchWindow != null) {
					IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
					
					if (workbenchPage != null) {					
						IWorkbenchPageContextSupport workbenchPageContextSupport = (IWorkbenchPageContextSupport) workbenchPage.getContextSupport();
						workbenchPageContextSupport.getCompoundContextActivationService().removeContextActivationService(contextActivationServiceB);
					}
				}
			}
		});
		
		IHandler handlerA = new IHandler() {
			public void execute() {
			}
			
			public void execute(Event event) {
			}
			
			public boolean isEnabled() {
				return false;
			}
		};
		
		IHandler handlerB = new IHandler() {
			public void execute() {
			}
			
			public void execute(Event event) {
			}
			
			public boolean isEnabled() {
				return false;
			}
		};		
		
		final IMutableCommandHandlerService commandHandlerServiceA = CommandHandlerServiceFactory.getMutableCommandHandlerService();
		commandHandlerServiceA.setHandlersByCommandId(new HashMap(Collections.singletonMap("command", handlerA)));

		final IMutableCommandHandlerService commandHandlerServiceB = CommandHandlerServiceFactory.getMutableCommandHandlerService();
		commandHandlerServiceB.setHandlersByCommandId(new HashMap(Collections.singletonMap("command", handlerB)));				
				
		menu.add(new Separator());
		
		menu.add(new Action("Add handler A to the workbench") {
			public void run() {
				IWorkbench workbench = PlatformUI.getWorkbench();
				IWorkbenchCommandSupport workbenchCommandSupport = (IWorkbenchCommandSupport) workbench.getCommandSupport();
				workbenchCommandSupport.getCompoundCommandHandlerService().addCommandHandlerService(commandHandlerServiceA);
			}
		});

		menu.add(new Action("Remove handler A from the workbench") {
			public void run() {
				IWorkbench workbench = PlatformUI.getWorkbench();
				IWorkbenchCommandSupport workbenchCommandSupport = (IWorkbenchCommandSupport) workbench.getCommandSupport();
				workbenchCommandSupport.getCompoundCommandHandlerService().removeCommandHandlerService(commandHandlerServiceA);
			}
		});
		
		menu.add(new Action("Add handler B to the workbench") {
			public void run() {
				IWorkbench workbench = PlatformUI.getWorkbench();
				IWorkbenchCommandSupport workbenchCommandSupport = (IWorkbenchCommandSupport) workbench.getCommandSupport();
				workbenchCommandSupport.getCompoundCommandHandlerService().addCommandHandlerService(commandHandlerServiceB);
			}
		});

		menu.add(new Action("Remove handler B from the workbench") {
			public void run() {
				IWorkbench workbench = PlatformUI.getWorkbench();
				IWorkbenchCommandSupport workbenchCommandSupport = (IWorkbenchCommandSupport) workbench.getCommandSupport();
				workbenchCommandSupport.getCompoundCommandHandlerService().removeCommandHandlerService(commandHandlerServiceB);
			}
		});

		menu.add(new Separator());
		
		menu.add(new Action("Add handler A to the workbench page") {
			public void run() {
				IWorkbench workbench = PlatformUI.getWorkbench();
				IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();

				if (workbenchWindow != null) {
					IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
					
					if (workbenchPage != null) {					
						IWorkbenchPageCommandSupport workbenchPageCommandSupport = (IWorkbenchPageCommandSupport) workbenchPage.getCommandSupport();
						workbenchPageCommandSupport.getCompoundCommandHandlerService().addCommandHandlerService(commandHandlerServiceA);
					}
				}
			}
		});

		menu.add(new Action("Remove handler A from the workbench page") {
			public void run() {
				IWorkbench workbench = PlatformUI.getWorkbench();
				IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();

				if (workbenchWindow != null) {
					IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
					
					if (workbenchPage != null) {					
						IWorkbenchPageCommandSupport workbenchPageCommandSupport = (IWorkbenchPageCommandSupport) workbenchPage.getCommandSupport();
						workbenchPageCommandSupport.getCompoundCommandHandlerService().removeCommandHandlerService(commandHandlerServiceA);
					}
				}
			}
		});
		
		menu.add(new Action("Add handler B to the workbench page") {
			public void run() {
				IWorkbench workbench = PlatformUI.getWorkbench();
				IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();

				if (workbenchWindow != null) {
					IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
					
					if (workbenchPage != null) {					
						IWorkbenchPageCommandSupport workbenchPageCommandSupport = (IWorkbenchPageCommandSupport) workbenchPage.getCommandSupport();
						workbenchPageCommandSupport.getCompoundCommandHandlerService().addCommandHandlerService(commandHandlerServiceB);
					}
				}
			}
		});

		menu.add(new Action("Remove handler B from the workbench page") {
			public void run() {
				IWorkbench workbench = PlatformUI.getWorkbench();
				IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();

				if (workbenchWindow != null) {
					IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
					
					if (workbenchPage != null) {					
						IWorkbenchPageCommandSupport workbenchPageCommandSupport = (IWorkbenchPageCommandSupport) workbenchPage.getCommandSupport();
						workbenchPageCommandSupport.getCompoundCommandHandlerService().removeCommandHandlerService(commandHandlerServiceB);
					}
				}
			}
		});
		*/
		
		return menu;
	}

	/**
	 * Disposes any resources and unhooks any listeners that are no longer needed.
	 * Called when the window is closed.
	 */
	public void dispose() {
		actionBarConfigurer.getStatusLineManager().remove(statusLineItem);
		if (prefListener != null) {
			ResourcesPlugin.getPlugin().getPluginPreferences().removePropertyChangeListener(
				prefListener);
		}
		closeAction.dispose();
		closeAllAction.dispose();
		closeAllSavedAction.dispose();
		saveAction.dispose();
		saveAllAction.dispose();
		aboutAction.dispose();
		openPreferencesAction.dispose();
		saveAsAction.dispose();
		hideShowEditorAction.dispose();
		savePerspectiveAction.dispose();
		resetPerspectiveAction.dispose();
		editActionSetAction.dispose();
		closePerspAction.dispose();
		lockToolBarAction.dispose();
		closeAllPerspsAction.dispose();
		showViewMenuAction.dispose();
		showPartPaneMenuAction.dispose();
		nextPartAction.dispose();
		prevPartAction.dispose();
		nextEditorAction.dispose();
		prevEditorAction.dispose();
		nextPerspectiveAction.dispose();
		prevPerspectiveAction.dispose();
		activateEditorAction.dispose();
		maximizePartAction.dispose();
		workbenchEditorsAction.dispose();
		backwardHistoryAction.dispose();
		forwardHistoryAction.dispose();
		undoAction.dispose();
		redoAction.dispose();
		cutAction.dispose();
		copyAction.dispose();
		pasteAction.dispose();
		deleteAction.dispose();
		selectAllAction.dispose();
		findAction.dispose();
		printAction.dispose();
		revertAction.dispose();
		refreshAction.dispose();
		propertiesAction.dispose();
		quitAction.dispose();
		moveAction.dispose();
		renameAction.dispose();
		goIntoAction.dispose();
		backAction.dispose();
		forwardAction.dispose();
		upAction.dispose();
		nextAction.dispose();
		previousAction.dispose();
		if (categoryAction != null) {
			categoryAction.dispose();
		}

		// editorsDropDownAction is not currently an IWorkbenchAction		
		// editorsDropDownAction.dispose();
		projectPropertyDialogAction.dispose();
		newWizardAction.dispose();
		newWizardDropDownAction.dispose();
		importResourcesAction.dispose();
		exportResourcesAction.dispose();
		rebuildAllAction.dispose();
		buildAllAction.dispose();
		if (quickStartAction != null) {
			quickStartAction.dispose();
		}
		if (tipsAndTricksAction != null) {
			tipsAndTricksAction.dispose();
		}
		addBookmarkAction.dispose();
		addTaskAction.dispose();
		buildProjectAction.dispose();
		rebuildProjectAction.dispose();
		openProjectAction.dispose();
		closeProjectAction.dispose();
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

		buildAllAction = IDEActionFactory.BUILD.create(getWindow());
		registerGlobalAction(buildAllAction);

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
		registerGlobalAction(copyAction);

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

		aboutAction = IDEActionFactory.ABOUT.create(getWindow());
		AboutInfo aboutInfo = IDEWorkbenchPlugin.getDefault().getPrimaryInfo();
		String productName = null;
		if (aboutInfo != null) {
			productName = aboutInfo.getProductName();
		}
		if (productName == null) {
			productName = ""; //$NON-NLS-1$
		}
		aboutAction.setText(IDEWorkbenchMessages.format("AboutAction.text", new Object[] { productName })); //$NON-NLS-1$
		aboutAction.setToolTipText(IDEWorkbenchMessages.format("AboutAction.toolTip", new Object[] { productName})); //$NON-NLS-1$
		aboutAction.setImageDescriptor(
			IDEInternalWorkbenchImages.getImageDescriptor(
				IDEInternalWorkbenchImages.IMG_OBJS_DEFAULT_PROD));
		registerGlobalAction(aboutAction);

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

		AboutInfo[] infos = IDEWorkbenchPlugin.getDefault().getFeatureInfos();
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

		quitAction = ActionFactory.QUIT.create(getWindow());
		registerGlobalAction(quitAction);

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

		buildProjectAction = IDEActionFactory.BUILD_PROJECT.create(getWindow());
		registerGlobalAction(buildProjectAction);
		
		rebuildProjectAction = IDEActionFactory.REBUILD_PROJECT.create(getWindow());
		registerGlobalAction(rebuildProjectAction);

		openProjectAction = IDEActionFactory.OPEN_PROJECT.create(getWindow());
		registerGlobalAction(openProjectAction);

		closeProjectAction = IDEActionFactory.CLOSE_PROJECT.create(getWindow());
		registerGlobalAction(closeProjectAction);
		
		projectPropertyDialogAction = IDEActionFactory.OPEN_PROJECT_PROPERTIES.create(getWindow());
		registerGlobalAction(projectPropertyDialogAction);

		//Only add the role manager action if we are using role support
		
		IWorkbenchActivitySupport workbenchActivitySupport = getWindow().getWorkbench().getActivitySupport();

		
		IActivityManager activityManager = workbenchActivitySupport.getActivityManager();
				
		if (!activityManager.getDefinedCategoryIds().isEmpty()) { 
			categoryAction = ActionFactory.CONFIGURE_ACTIVITIES.create(getWindow());
        	registerGlobalAction(categoryAction);
		}		

		if (EditorWorkbook.usingNewDropDown()) {
			editorsDropDownAction = new EditorsDropDownAction(window);
			registerGlobalAction(editorsDropDownAction);
		}
		
		if (getWindow().getWorkbench().getIntroRegistry().getIntroCount() > 0) {
			introAction = ActionFactory.INTRO.create(window);
			registerGlobalAction(introAction);
		}
	}

	private void setCurrentActionBarConfigurer(IActionBarConfigurer actionBarConfigurer)
	{
		this.actionBarConfigurer = actionBarConfigurer;
	}
	
	private void registerGlobalAction(IAction action) {
		actionBarConfigurer.registerGlobalAction(action);
	}
	
	/**
	 * Add the manual incremental build action
	 * to both the menu bar and the tool bar.
	 */
	private void addManualIncrementalBuildAction() {
		IMenuManager menubar = actionBarConfigurer.getMenuManager();
		IMenuManager manager = menubar.findMenuUsingPath(IWorkbenchActionConstants.M_PROJECT);
		if (manager != null) {
			try {
				manager.insertBefore(IDEActionFactory.REBUILD_PROJECT.getId(), buildProjectAction);
				manager.insertBefore(IDEActionFactory.REBUILD_ALL.getId(), buildAllAction);
			} catch (IllegalArgumentException e) {
				// action not found!
			}
			manager.update(false);
		}
		
		ICoolBarManager coolBarManager = actionBarConfigurer.getCoolBarManager();
		IContributionItem cbItem = coolBarManager.find(IWorkbenchActionConstants.TOOLBAR_FILE);
		if (!(cbItem instanceof ToolBarContributionItem)) {
			// This should not happen
			IDEWorkbenchPlugin.log("File toolbar contribution item is missing"); //$NON-NLS-1$
			return;
		}
		ToolBarContributionItem toolBarItem = (ToolBarContributionItem)cbItem;
		IToolBarManager toolBarManager = toolBarItem.getToolBarManager();
		if (toolBarManager == null) {
			// error if this happens, file toolbar assumed to always exist
			IDEWorkbenchPlugin.log("File toolbar is missing"); //$NON-NLS-1$
			return;
		}
		
		toolBarManager.appendToGroup(IWorkbenchActionConstants.BUILD_GROUP, buildAllAction);
		toolBarManager.update(false);
		toolBarItem.update(ICoolBarManager.SIZE);
	}
	
	/**
	 * Remove the manual incremental build action
	 * from both the menu bar and the tool bar.
	 */
	private void removeManualIncrementalBuildAction() {
		IMenuManager menubar = actionBarConfigurer.getMenuManager();
		IMenuManager manager = menubar.findMenuUsingPath(IWorkbenchActionConstants.M_PROJECT);
		if (manager != null) {
			try {
				manager.remove(IDEActionFactory.BUILD.getId());
				manager.remove(IDEActionFactory.BUILD_PROJECT.getId());
			} catch (IllegalArgumentException e) {
				// action was not in menu
			}
			manager.update(false);
		}
		
		ICoolBarManager coolBarManager = actionBarConfigurer.getCoolBarManager();
		IContributionItem cbItem = coolBarManager.find(IWorkbenchActionConstants.TOOLBAR_FILE);
		if (!(cbItem instanceof ToolBarContributionItem)) {
			// This should not happen
			IDEWorkbenchPlugin.log("File toolbar contribution item is missing"); //$NON-NLS-1$
			return;
		}
		ToolBarContributionItem toolBarItem = (ToolBarContributionItem)cbItem;
		IToolBarManager toolBarManager = toolBarItem.getToolBarManager();
		if (toolBarManager == null) {
			// error if this happens, file toolbar assumed to always exist
			IDEWorkbenchPlugin.log("File toolbar is missing"); //$NON-NLS-1$
			return;
		}
		
		// Remove build item
		if (buildAllAction != null) {
			toolBarManager.remove(buildAllAction.getId());
			toolBarManager.update(false);
			toolBarItem.update(ICoolBarManager.SIZE);
		}
		
	}
}
