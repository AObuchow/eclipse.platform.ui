package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.*;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.actions.GlobalBuildAction;

/**
 * This is used to add actions to the workbench.
 */
public class WorkbenchActionBuilder implements IPropertyChangeListener {
	// target
	private WorkbenchWindow window;

	// actions
	private NewWizardAction newWizardAction;
	private NewWizardDropDownAction newWizardDropDownAction;
	private NewWizardMenu newWizardMenu;
	private CloseEditorAction closeAction;
	private CloseAllAction closeAllAction;
	private ImportResourcesAction importResourcesAction;
	private ExportResourcesAction exportResourcesAction;
	private GlobalBuildAction rebuildAllAction; // Full build
	private GlobalBuildAction buildAction; // Incremental build
	private SaveAction saveAction;
	private SaveAllAction saveAllAction;
	private AboutAction aboutAction;
	private OpenPreferencesAction openPreferencesAction;
	private QuickStartAction quickStartAction;
	private SaveAsAction saveAsAction;
	private ToggleEditorsVisibilityAction hideShowEditorAction;
	private SelectWorkingSetAction selectWorkingSetAction;
	private RemoveWorkingSetAction removeWorkingSetAction;
	private SavePerspectiveAction savePerspectiveAction;
	private ResetPerspectiveAction resetPerspectiveAction;
	private EditActionSetsAction editActionSetAction;
	private ClosePerspectiveAction closePerpsAction;
	private CloseAllPerspectivesAction closeAllPerspsAction;
	private PinEditorAction pinEditorAction;
	private ShowViewMenuAction showViewMenuAction;
	private ShowPartPaneMenuAction showPartPaneMenuAction;
	private CyclePartAction nextPartAction;
	private CyclePartAction prevPartAction;
	private CycleEditorAction nextEditorAction;
	private CycleEditorAction prevEditorAction;
	private ActivateEditorAction activateEditorAction;
	private WorkbenchEditorsAction workbenchEditorsAction;

	// retarget actions.
	private RetargetAction undoAction;
	private RetargetAction redoAction;
	private RetargetAction cutAction;
	private RetargetAction copyAction;
	private RetargetAction pasteAction;
	private RetargetAction deleteAction;
	private RetargetAction selectAllAction;
	private RetargetAction findAction;
	private RetargetAction addBookmarkAction;
	private RetargetAction printAction;

	// decorator factory
	private DecoratorActionFactory decoratorFactory;
	
	/**
	 * WorkbenchActionBuilder constructor comment.
	 */
	public WorkbenchActionBuilder() {
	}
	
	/**
	 * Add the manual incremental build action
	 * to both the menu bar and the tool bar.
	 */
	protected void addManualIncrementalBuildAction() {
		MenuManager menubar = window.getMenuBarManager();
		IMenuManager manager = menubar.findMenuUsingPath(IWorkbenchActionConstants.M_WORKBENCH);
		if (manager != null) {
			try {
				manager.insertBefore(IWorkbenchActionConstants.REBUILD_ALL, buildAction);
			} catch (IllegalArgumentException e) {
				// action not found!
			}
		}

		ToolBarManager toolbar = window.getToolBarManager();
		try {
			toolbar.prependToGroup(IWorkbenchActionConstants.BUILD_EXT, buildAction);
			toolbar.prependToGroup(IWorkbenchActionConstants.BUILD_EXT, new Separator());
			toolbar.update(true);
		} catch (IllegalArgumentException e) {
			// group not found
		}
	}
	
	/**
	 * Build the workbench actions.
	 */
	public void buildActions(WorkbenchWindow win) {
		window = win;
		makeActions();
		createMenuBar();
		createToolBar();
		createShortcutBar();

		// Listen for preference property changes to
		// update the menubar and toolbar
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		store.addPropertyChangeListener(this);

		final IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				String property = event.getProperty();
				if (IWorkbenchPage.CHANGE_WORKING_SET_REPLACE.equals(property)) {
					removeWorkingSetAction.setEnabled(event.getNewValue() != null);
				}
			}
		};
		// Listen to workbench page lifecycle methods to enable
		// and disable the perspective menu items as needed.
		window.addPageListener(new IPageListener() {
			public void pageActivated(IWorkbenchPage page) {
				enableActions(page.getPerspective() != null);
				removeWorkingSetAction.setEnabled(page.getWorkingSet().getName().length() > 0);
			}
			public void pageClosed(IWorkbenchPage page) {
				IWorkbenchPage pg = window.getActivePage();
				enableActions(pg != null && pg.getPerspective() != null);
				removeWorkingSetAction.setEnabled(false);
				((WorkbenchPage) page).removePropertyChangeListener(propertyChangeListener);				
			}
			public void pageOpened(IWorkbenchPage page) {
				removeWorkingSetAction.setEnabled(page.getWorkingSet().getName().length() > 0);
				((WorkbenchPage) page).addPropertyChangeListener(propertyChangeListener);
			}
		});
							
		// Listen to workbench perspective lifecycle methods to enable
		// and disable the perspective menu items as needed.
		window.getPerspectiveService().addPerspectiveListener(new IInternalPerspectiveListener() {
			public void perspectiveClosed(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
				enableActions(page.getPerspective() != null);
			}
			public void perspectiveOpened(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
			}
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
		selectWorkingSetAction.setEnabled(value);		
		savePerspectiveAction.setEnabled(value);
		resetPerspectiveAction.setEnabled(value);
		editActionSetAction.setEnabled(value);
		closePerpsAction.setEnabled(value);
		closeAllPerspsAction.setEnabled(value);
		newWizardMenu.setEnabled(value);
		newWizardDropDownAction.setEnabled(value);
	}
	
	/**
	 * Creates the menu bar.
	 */
	private void createMenuBar() {
		// Get main menu.
		MenuManager menubar = window.getMenuBarManager();
		menubar.add(createFileMenu());
		menubar.add(createEditMenu());
		menubar.add(createPerspectiveMenu());
		menubar.add(createWorkbenchMenu());
		// Define section for additions.
		menubar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		menubar.add(createWindowMenu());
		menubar.add(createHelpMenu());
	}

	/**
	 * Creates and returns the File menu.
	 */
	private MenuManager createFileMenu() {
		MenuManager menu = new MenuManager(WorkbenchMessages.getString("Workbench.file"), IWorkbenchActionConstants.M_FILE); //$NON-NLS-1$
		menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_START));
		{
			MenuManager newMenu = new MenuManager(WorkbenchMessages.getString("Workbench.new")); //$NON-NLS-1$
			menu.add(newMenu);
			this.newWizardMenu = new NewWizardMenu(newMenu, window, true);
		}
		menu.add(new GroupMarker(IWorkbenchActionConstants.NEW_EXT));
		menu.add(new Separator());
		{
			MenuManager openRecentMenu = new DynamicMenuManager(WorkbenchMessages.getString("Workbench.openRecent")); //$NON-NLS-1$
			menu.add(openRecentMenu);
			openRecentMenu.add(new ReopenEditorMenu(window, ((Workbench) window.getWorkbench()).getEditorHistory(), false));
		}
		menu.add(closeAction);
		menu.add(closeAllAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.CLOSE_EXT));
		menu.add(new Separator());
		menu.add(saveAction);
		menu.add(saveAsAction);
		menu.add(saveAllAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.SAVE_EXT));
		menu.add(new Separator());
		menu.add(printAction);
		menu.add(new Separator());
		menu.add(importResourcesAction);
		menu.add(exportResourcesAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.IMPORT_EXT));
		// put additions relative to the MRU group at its old location,
		// next to the additions group
		menu.add(new GroupMarker(IWorkbenchActionConstants.MRU));  
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(new Separator());
		menu.add(new QuitAction(window.getWorkbench()));
		menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_END));
		return menu;
	}

	/**
	 * Creates and returns the Edit menu.
	 */
	private MenuManager createEditMenu() {
		MenuManager menu = new MenuManager(WorkbenchMessages.getString("Workbench.edit"), IWorkbenchActionConstants.M_EDIT); //$NON-NLS-1$
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
		menu.add(new Separator());
		menu.add(addBookmarkAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_END));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		return menu;
	}

	/**
	 * Creates and returns the Perspective menu.
	 */
	private MenuManager createPerspectiveMenu() {
		MenuManager menu = new MenuManager(WorkbenchMessages.getString("Workbench.perspective"), IWorkbenchActionConstants.M_VIEW); //$NON-NLS-1$
		{
			MenuManager changePerspMenuMgr = new MenuManager(WorkbenchMessages.getString("Workbench.change")); //$NON-NLS-1$
			ChangeToPerspectiveMenu changePerspMenuItem = new ChangeToPerspectiveMenu(window);
			changePerspMenuMgr.add(changePerspMenuItem);
			menu.add(changePerspMenuMgr);
		}
		{
			MenuManager subMenu = new MenuManager(WorkbenchMessages.getString("Workbench.showView")); //$NON-NLS-1$
			menu.add(subMenu);
			new ShowViewMenu(subMenu, window, true);
		}
		menu.add(hideShowEditorAction = new ToggleEditorsVisibilityAction(window));
		menu.add(selectWorkingSetAction = new SelectWorkingSetAction(window));
		menu.add(removeWorkingSetAction = new RemoveWorkingSetAction(window));
		menu.add(new Separator());
		menu.add(savePerspectiveAction = new SavePerspectiveAction(window));
		menu.add(editActionSetAction = new EditActionSetsAction(window));
		menu.add(resetPerspectiveAction = new ResetPerspectiveAction(window));
		menu.add(new Separator());
		menu.add(closePerpsAction = new ClosePerspectiveAction(window));
		menu.add(closeAllPerspsAction = new CloseAllPerspectivesAction(window));
		menu.add(new Separator(IWorkbenchActionConstants.VIEW_EXT));
		menu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(new Separator());
		menu.add(new OpenedPerspectivesMenu(window, false));
		return menu;
	}
	
	
	/**
	 * Creates and returns the Workbench menu.
	 */
	private MenuManager createWorkbenchMenu() {
		MenuManager menu = new MenuManager(WorkbenchMessages.getString("Workbench.workbench"), IWorkbenchActionConstants.M_WORKBENCH); //$NON-NLS-1$
		menu.add(new GroupMarker(IWorkbenchActionConstants.WB_START));
		// Only add the manual incremental build if auto build off
		if (!ResourcesPlugin.getWorkspace().isAutoBuilding())
			menu.add(buildAction);
		menu.add(rebuildAllAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.WB_END));
		menu.add(new Separator());
		menu.add(openPreferencesAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		return menu;
	}

	/**
	 * Creates and returns the Window menu.
	 */
	private MenuManager createWindowMenu() {
		MenuManager menu = new MenuManager(WorkbenchMessages.getString("Workbench.window"), IWorkbenchActionConstants.M_WINDOW); //$NON-NLS-1$
		menu.add(new OpenNewWindowAction(window));
		MenuManager launchWindowMenu = new MenuManager(WorkbenchMessages.getString("Workbench.launch"), IWorkbenchActionConstants.M_LAUNCH); //$NON-NLS-1$
		launchWindowMenu.add(new GroupMarker(IWorkbenchActionConstants.LAUNCH_EXT));
		menu.add(launchWindowMenu);

		//Navigation submenu
		MenuManager subMenu = new MenuManager(WorkbenchMessages.getString("Workbench.navigation")); //$NON-NLS-1$
		menu.add(subMenu);
		subMenu.add(activateEditorAction);
		subMenu.add(showPartPaneMenuAction);
		subMenu.add(showViewMenuAction);
		subMenu.add(nextEditorAction);
		subMenu.add(prevEditorAction);
		subMenu.add(nextPartAction);
		subMenu.add(prevPartAction);

		//Decorators submenu
		decoratorFactory.fillMenu(menu);

		menu.add(new Separator(IWorkbenchActionConstants.WINDOW_EXT));
		menu.add(workbenchEditorsAction = new WorkbenchEditorsAction(window));
		menu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(new SwitchToWindowMenu(window, true));
		
		return menu;
	}

	/**
	 * Creates and returns the Help menu.
	 */
	private MenuManager createHelpMenu() {
		MenuManager menu = new MenuManager(WorkbenchMessages.getString("Workbench.help"), IWorkbenchActionConstants.M_HELP); //$NON-NLS-1$
		// See if a welcome page is specified
		if (quickStartAction != null)
			menu.add(quickStartAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.HELP_START));
		menu.add(new GroupMarker(IWorkbenchActionConstants.HELP_END));
		menu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		// about should always be at the bottom
		menu.add(aboutAction);
		return menu;
	}
	
	/**
	 * Fills the shortcut bar
	 */
	private void createShortcutBar() {
		ToolBarManager shortcutBar = window.getShortcutBar();
		shortcutBar.add(new Separator(window.GRP_PAGES));
		shortcutBar.add(new Separator(window.GRP_PERSPECTIVES));
		shortcutBar.add(new Separator(window.GRP_FAST_VIEWS));
		shortcutBar.add(new ShowFastViewContribution(window));
	}
	/**
	 * Fills the menu bar by merging all the individual viewers' contributions
	 * and invariant (static) menus and menu items, as defined in MenuConstants interface.
	 */
	private void createToolBar() {
		ToolBarManager toolbar = window.getToolBarManager();
		toolbar.add(newWizardDropDownAction);
		toolbar.add(new GroupMarker(IWorkbenchActionConstants.NEW_EXT));
		toolbar.add(new Separator());
		toolbar.add(saveAction);
		toolbar.add(saveAsAction);
		toolbar.add(new GroupMarker(IWorkbenchActionConstants.SAVE_EXT));
		toolbar.add(printAction);
		// Only add the manual incremental build if auto build off
		if (!ResourcesPlugin.getWorkspace().isAutoBuilding()) {
			toolbar.add(new Separator());
			toolbar.add(buildAction);
		}
		toolbar.add(new GroupMarker(IWorkbenchActionConstants.BUILD_EXT));
		toolbar.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		toolbar.add(pinEditorAction);
	}
	/**
	 * Remove the property change listener
	 */
	public void dispose() {
		// Listen for preference property changes to
		// update the menubar and toolbar
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		store.removePropertyChangeListener(this);
	}
	/**
	 * Returns true if the menu with the given ID should
	 * be considered OLE container menu. Container menus
	 * are preserved in OLE menu merging.
	 */
	public static boolean isContainerMenu(String menuId) {
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
	 * Create actions for the menu bar and toolbar
	 */
	private void makeActions() {

		// The actions in jface do not have menu vs. enable, vs. disable vs. color
		// There are actions in here being passed the workbench - problem 

		// Get services for notification.
		IPartService partService = window.getPartService();

		// Many actions need the workbench.
		IWorkbench workbench = window.getWorkbench();

		newWizardAction = new NewWizardAction();
		// images for this action are set in its constructor

		newWizardDropDownAction = new NewWizardDropDownAction(workbench, newWizardAction);
		newWizardDropDownAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_NEW_WIZ));
		newWizardDropDownAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_NEW_WIZ_HOVER));
		newWizardDropDownAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_NEW_WIZ_DISABLED));

		importResourcesAction = new ImportResourcesAction(workbench);
		importResourcesAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_IMPORT_WIZ));
		importResourcesAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_IMPORT_WIZ_HOVER));
		importResourcesAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_IMPORT_WIZ_DISABLED));

		exportResourcesAction = new ExportResourcesAction(workbench);
		exportResourcesAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_EXPORT_WIZ));
		exportResourcesAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_EXPORT_WIZ_HOVER));
		exportResourcesAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_EXPORT_WIZ_DISABLED));

		rebuildAllAction = new GlobalBuildAction(workbench, window.getShell(), IncrementalProjectBuilder.FULL_BUILD);
		// 1G82IWC - a new icon is needed for Rebuild All or Build
		//	rebuildAllAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_BUILD_EXEC));
		//	rebuildAllAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_BUILD_EXEC_HOVER));
		//	rebuildAllAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_BUILD_EXEC_DISABLED));

		buildAction = new GlobalBuildAction(workbench, window.getShell(), IncrementalProjectBuilder.INCREMENTAL_BUILD);
		buildAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_BUILD_EXEC));
		buildAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_BUILD_EXEC_HOVER));
		buildAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_BUILD_EXEC_DISABLED));

		saveAction = new SaveAction(window);
		saveAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_SAVE_EDIT));
		saveAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_SAVE_EDIT_HOVER));
		saveAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_SAVE_EDIT_DISABLED));
		partService.addPartListener(saveAction);

		saveAsAction = new SaveAsAction(window);
		saveAsAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_SAVEAS_EDIT));
		saveAsAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_SAVEAS_EDIT_HOVER));
		saveAsAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_SAVEAS_EDIT_DISABLED));
		partService.addPartListener(saveAsAction);

		saveAllAction = new SaveAllAction(window);
		saveAllAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_SAVEALL_EDIT));
		saveAllAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_SAVEALL_EDIT_HOVER));
		saveAllAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_SAVEALL_EDIT_DISABLED));
		partService.addPartListener(saveAllAction);

		undoAction = new LabelRetargetAction(IWorkbenchActionConstants.UNDO, WorkbenchMessages.getString("Workbench.undo")); //$NON-NLS-1$
		undoAction.setToolTipText(WorkbenchMessages.getString("Workbench.undoToolTip")); //$NON-NLS-1$
		undoAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_UNDO_EDIT));
		undoAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_UNDO_EDIT_HOVER));
		undoAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_UNDO_EDIT_DISABLED));
		undoAction.setAccelerator(SWT.CTRL | 'z');
		partService.addPartListener(undoAction);

		redoAction = new LabelRetargetAction(IWorkbenchActionConstants.REDO, WorkbenchMessages.getString("Workbench.redo")); //$NON-NLS-1$
		redoAction.setToolTipText(WorkbenchMessages.getString("Workbench.redoToolTip")); //$NON-NLS-1$
		redoAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_REDO_EDIT));
		redoAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_REDO_EDIT_HOVER));
		redoAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_REDO_EDIT_DISABLED));
		redoAction.setAccelerator(SWT.CTRL | 'y');
		partService.addPartListener(redoAction);

		cutAction = new RetargetAction(IWorkbenchActionConstants.CUT, WorkbenchMessages.getString("Workbench.cut")); //$NON-NLS-1$
		cutAction.setToolTipText(WorkbenchMessages.getString("Workbench.cutToolTip")); //$NON-NLS-1$
		cutAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_CUT_EDIT));
		cutAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_CUT_EDIT_HOVER));
		cutAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_CUT_EDIT_DISABLED));
		cutAction.setAccelerator(SWT.CTRL | 'x');
		partService.addPartListener(cutAction);

		copyAction = new RetargetAction(IWorkbenchActionConstants.COPY, WorkbenchMessages.getString("Workbench.copy")); //$NON-NLS-1$
		copyAction.setToolTipText(WorkbenchMessages.getString("Workbench.copyToolTip")); //$NON-NLS-1$
		copyAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_COPY_EDIT));
		copyAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_COPY_EDIT_HOVER));
		copyAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_COPY_EDIT_DISABLED));
		copyAction.setAccelerator(SWT.CTRL | 'c');
		partService.addPartListener(copyAction);

		pasteAction = new RetargetAction(IWorkbenchActionConstants.PASTE, WorkbenchMessages.getString("Workbench.paste")); //$NON-NLS-1$
		pasteAction.setToolTipText(WorkbenchMessages.getString("Workbench.pasteToolTip")); //$NON-NLS-1$
		pasteAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_PASTE_EDIT));
		pasteAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_PASTE_EDIT_HOVER));
		pasteAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_PASTE_EDIT_DISABLED));
		pasteAction.setAccelerator(SWT.CTRL | 'v');
		partService.addPartListener(pasteAction);

		printAction = new RetargetAction(IWorkbenchActionConstants.PRINT, WorkbenchMessages.getString("Workbench.print")); //$NON-NLS-1$
		printAction.setToolTipText(WorkbenchMessages.getString("Workbench.printToolTip")); //$NON-NLS-1$
		printAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_PRINT_EDIT));
		printAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_PRINT_EDIT_HOVER));
		printAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_PRINT_EDIT_DISABLED));
		printAction.setAccelerator(SWT.CTRL | 'p');
		partService.addPartListener(printAction);

		selectAllAction = new RetargetAction(IWorkbenchActionConstants.SELECT_ALL, WorkbenchMessages.getString("Workbench.selectAll")); //$NON-NLS-1$
		selectAllAction.setToolTipText(WorkbenchMessages.getString("Workbench.selectAllToolTip")); //$NON-NLS-1$
		selectAllAction.setAccelerator(SWT.CTRL | 'a');
		partService.addPartListener(selectAllAction);

		findAction = new RetargetAction(IWorkbenchActionConstants.FIND, WorkbenchMessages.getString("Workbench.findReplace")); //$NON-NLS-1$
		findAction.setToolTipText(WorkbenchMessages.getString("Workbench.findReplaceToolTip")); //$NON-NLS-1$
		findAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_SEARCH_SRC));
		findAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_SEARCH_SRC_HOVER));
		findAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_SEARCH_SRC_DISABLED));
		findAction.setAccelerator(SWT.CONTROL | 'f');
		partService.addPartListener(findAction);

		closeAction = new CloseEditorAction(window);
		partService.addPartListener(closeAction);

		closeAllAction = new CloseAllAction(window);
		partService.addPartListener(closeAllAction);

		pinEditorAction = new PinEditorAction(window);
		partService.addPartListener(pinEditorAction);
		pinEditorAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_PIN_EDITOR));
		pinEditorAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_PIN_EDITOR_HOVER));
		pinEditorAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_PIN_EDITOR_DISABLED));

		aboutAction = new AboutAction(window);
		aboutAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_OBJS_DEFAULT_PROD));

		openPreferencesAction = new OpenPreferencesAction(window);

		addBookmarkAction = new RetargetAction(IWorkbenchActionConstants.BOOKMARK, WorkbenchMessages.getString("Workbench.addBookMark")); //$NON-NLS-1$
		addBookmarkAction.setToolTipText(WorkbenchMessages.getString("Workbench.addBookMarkToolTip")); //$NON-NLS-1$
		partService.addPartListener(addBookmarkAction);

		deleteAction = new RetargetAction(IWorkbenchActionConstants.DELETE, WorkbenchMessages.getString("Workbench.delete")); //$NON-NLS-1$
		deleteAction.setToolTipText(WorkbenchMessages.getString("Workbench.deleteToolTip")); //$NON-NLS-1$
		deleteAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_DELETE_EDIT));
		deleteAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_DELETE_EDIT_HOVER));
		deleteAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_DELETE_EDIT_DISABLED));
		deleteAction.enableAccelerator(false);
		WorkbenchHelp.setHelp(deleteAction, IHelpContextIds.DELETE_RETARGET_ACTION);
		partService.addPartListener(deleteAction);

		// See if a welcome page is specified
		if (((Workbench) PlatformUI.getWorkbench()).getProductInfo().getWelcomePageURL() != null)
			quickStartAction = new QuickStartAction(workbench);

		// Actions for invisible accelerators
		showViewMenuAction = new ShowViewMenuAction(window);
		showPartPaneMenuAction = new ShowPartPaneMenuAction(window);
		nextEditorAction = new CycleEditorAction(window, true);
		prevEditorAction = new CycleEditorAction(window, false);
		nextPartAction = new CyclePartAction(window, true);
		prevPartAction = new CyclePartAction(window, false);
		activateEditorAction = new ActivateEditorAction(window);

		decoratorFactory = new DecoratorActionFactory();
		decoratorFactory.makeActions();
	}
	/**
	 * Update the menubar and toolbar when
	 * changes to the preferences are done.
	 */
	public void propertyChange(PropertyChangeEvent event) {
		// Allow manual incremental build only if the
		// auto build setting is off.
		if (event.getProperty() == IPreferenceConstants.AUTO_BUILD) {
			boolean autoBuildOn = ((Boolean) event.getNewValue()).booleanValue();
			if (autoBuildOn)
				removeManualIncrementalBuildAction();
			else
				addManualIncrementalBuildAction();
		} else if (event.getProperty() == IPreferenceConstants.REUSE_EDITORS) {
			pinEditorAction.updateState();
		} else if (event.getProperty() == IPreferenceConstants.RECENT_FILES) {
			Workbench wb = (Workbench) (Workbench) window.getWorkbench();
			// work around the fact that the property change event values come as 
			// both Strings and Integers
			int newValue = (new Integer(event.getNewValue().toString())).intValue();
			wb.getEditorHistory().reset(newValue);
			if (newValue == 0) {
				// the open recent menu item can go from enabled to disabled
				window.updateActionBars();
			}
		}
	}
	/**
	 * Remove the manual incremental build action
	 * from both the menu bar and the tool bar.
	 */
	protected void removeManualIncrementalBuildAction() {
		MenuManager menubar = window.getMenuBarManager();
		IMenuManager manager = menubar.findMenuUsingPath(IWorkbenchActionConstants.M_WORKBENCH);
		if (manager != null) {
			try {
				manager.remove(IWorkbenchActionConstants.BUILD);
			} catch (IllegalArgumentException e) {
				// action was not in menu
			}
		}

		ToolBarManager toolbar = window.getToolBarManager();
		try {
			toolbar.remove(IWorkbenchActionConstants.BUILD);
			toolbar.update(true);
		} catch (IllegalArgumentException e) {
			// action was not in toolbar
		}
	}
}