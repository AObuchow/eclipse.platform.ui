package org.eclipse.ui.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.part.WorkbenchPart;
import org.eclipse.ui.actions.*;

/**
 * Show the menu on top of the icon in the
 * view or editor label.
 */
public class ShowPartPaneMenuAction extends PartEventAction {

/**
 * Constructor for ShowPartPaneMenuAction.
 * @param text
 */
public ShowPartPaneMenuAction(WorkbenchWindow window) {
	super("");
	initText();
	window.getPartService().addPartListener(this);
}
/**
 * Initialize the menu text and tooltip.
 */
protected void initText() {
	setText(WorkbenchMessages.getString("ShowPartPaneMenuAction.text"));
	setToolTipText(WorkbenchMessages.getString("ShowPartPaneMenuAction.toolTip"));
}
/**
 * Show the pane title menu.
 */
protected void showMenu(PartPane pane) {
	pane.showPaneMenu();
}
/**
 * Updates the enabled state.
 */
protected void updateState() {
	setEnabled(getActivePart() != null);
}
/**
 * See Action
 */
public void run() {
	IWorkbenchPart part = getActivePart();
	if(part != null)
		showMenu(((PartSite)part.getSite()).getPane());
}
/**
 * See IPartListener
 */
public void partOpened(IWorkbenchPart part) {
	super.partOpened(part);
	updateState();
}
/**
 * See IPartListener
 */
public void partClosed(IWorkbenchPart part) {
	super.partClosed(part);
	updateState();
}
/**
 * See IPartListener
 */
public void partActivated(IWorkbenchPart part) {
	super.partActivated(part);
	updateState();
}
/**
 * See IPartListener
 */
public void partDeactivated(IWorkbenchPart part) {
	super.partDeactivated(part);
	updateState();
}
}

