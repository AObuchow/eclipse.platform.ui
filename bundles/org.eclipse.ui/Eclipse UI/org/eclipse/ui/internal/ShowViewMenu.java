package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.*;
import org.eclipse.ui.internal.dialogs.*;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.window.*;
import java.util.*;

/**
 * A <code>ShowViewMenu</code> is used to populate a menu manager with
 * Show View actions.  The visible views are determined by user preference
 * from the Perspective Customize dialog. 
 */
public class ShowViewMenu extends ShortcutMenu {
	private Action showDlgAction = new Action(WorkbenchMessages.getString("ShowView.title")) { //$NON-NLS-1$
		public void run() {
			showOther();
		}
	};
	private Map actions = new HashMap(21);
/**
 * Create a show view menu.
 * <p>
 * If the menu will appear on a semi-permanent basis, for instance within
 * a toolbar or menubar, the value passed for <code>register</code> should be true.
 * If set, the menu will listen to perspective activation and update itself
 * to suit.  In this case clients are expected to call <code>deregister</code> 
 * when the menu is no longer needed.  This will unhook any perspective
 * listeners.
 * </p>
  *
 * @param innerMgr the location for the shortcut menu contents
 * @param window the window containing the menu
 * @param register if <code>true</code> the menu listens to perspective changes in
 * 		the window
 */
public ShowViewMenu(IMenuManager innerMgr, IWorkbenchWindow window, boolean register) {
	super(innerMgr, window, register);
	fillMenu(); // Must be done after constructor to ensure field initialization.
}
/* (non-Javadoc)
 * Fills the menu with views.
 */
protected void fillMenu() {
	// Remove all.
	IMenuManager innerMgr = getMenuManager();
	innerMgr.removeAll();

	// If no page disable all.
	IWorkbenchPage page = getWindow().getActivePage();
	if (page == null)
		return;
		
	// Get visible actions.
	List actions = ((WorkbenchPage) page).getShowViewActions();
	if (actions != null) {
		for (Iterator i = actions.iterator(); i.hasNext();) {
			String id = (String) i.next();
			IAction action = getAction(id);
			if (action != null) {
				innerMgr.add(action);
			}
		}
	}

	// Add other ..
	innerMgr.add(new Separator());
	innerMgr.add(showDlgAction);
}
/**
 * Returns the action for the given view id, or null if not found.
 */
private IAction getAction(String id) {
	// Keep a cache, rather than creating a new action each time,
	// so that image caching in ActionContributionItem works.
	IAction action = (IAction) actions.get(id);
	if (action == null) {
		IViewRegistry reg = WorkbenchPlugin.getDefault().getViewRegistry();
		IViewDescriptor desc = reg.find(id);
		if (desc != null) {
			action = new ShowViewAction(getWindow(), desc);
			actions.put(id, action);
		}
	}
	return action;
}
/**
 * Opens the view selection dialog.
 */
private void showOther() {
	IWorkbenchWindow window = getWindow();
	IWorkbenchPage page = window.getActivePage();
	if (page == null)
		return;
	ShowViewDialog dlg = new ShowViewDialog(window.getShell(),
		WorkbenchPlugin.getDefault().getViewRegistry());
	dlg.open();
	if (dlg.getReturnCode() == Window.CANCEL)
		return;
	IViewDescriptor desc = dlg.getSelection();
	if (desc != null) {
		try {
			page.showView(desc.getID());
		} catch (PartInitException e) {
			MessageDialog.openError(window.getShell(), WorkbenchMessages.getString("ShowView.errorTitle"), //$NON-NLS-1$
				e.getMessage());
		}
	}
		
}
}
