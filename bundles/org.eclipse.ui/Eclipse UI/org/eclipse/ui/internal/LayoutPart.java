package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.*;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.window.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

/**
 * A presentation part is used to build the presentation for the
 * workbench.  Common subclasses are pane and folder.
 */
abstract public class LayoutPart implements IPartDropTarget {
	protected ILayoutContainer container;
	protected String id;
/**
 * PresentationPart constructor comment.
 */
public LayoutPart(String id) {
	super();
	this.id = id;
}
/**
 * Creates the SWT control
 */
abstract public void createControl(Composite parent);
/** 
 * Disposes the SWT control
 */
public void dispose() {
}
/**
 * Gets the presentation bounds.
 */
public Rectangle getBounds() {
	return new Rectangle(0, 0, 0, 0);
}
/**
 * Gets the parent for this part.
 */
public ILayoutContainer getContainer() {
	return container;
}
/**
 * Get the part control.  This method may return null.
 */
abstract public Control getControl();

/**
 * Gets the ID for this part.
 */
public String getID() {
	return id;
}
/**
 * Return the place the preferences used by layout parts reside.
 * @return IPreferenceStore
 */
/*package*/ IPreferenceStore getPreferenceStore() {
	return ((AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID))
		.getPreferenceStore();
}
/**
 * Gets the presentation size.
 */
public Point getSize() {
	Rectangle r = getBounds();
	Point ptSize = new Point(r.width, r.height);
	return ptSize;
}
/**
 * Returns the top level window for a part.
 */
public Window getWindow() {
	Control ctrl = getControl();
	if (ctrl != null) {
		Object data = ctrl.getShell().getData();
		if (data instanceof Window)
			return (Window)data;
	}
	return null;
}
/**
 * Returns the workbench window window for a part.
 */
public IWorkbenchWindow getWorkbenchWindow() {
	Window parentWindow = getWindow();
	if (parentWindow instanceof IWorkbenchWindow)
		return (IWorkbenchWindow)parentWindow;
	if (parentWindow instanceof DetachedWindow)
		return ((DetachedWindow)parentWindow).getWorkbenchPage().getWorkbenchWindow();
	return null;
}
/**
 *	Allow the layout part to determine if they are in
 * an acceptable state to start a drag & drop operation.
 */
public boolean isDragAllowed() {
	return true;
}
/**
 * Returns true if this part is visible.  A part is visible if it has a control.
 */
public boolean isVisible() {
	return true;
}
/**
 * Move the control over another one.
 */
public void moveAbove(Control refControl) {
}
/**
 * Reparent a part.
 */
public void reparent(Composite newParent) {
	if (!newParent.isReparentable())
		return;
		
	Control control = getControl();
	if ((control == null) || (control.getParent() == newParent))
		return;
		
	// make control small in case it is not resized with other controls
	control.setBounds(0, 0, 0, 0);
	// By setting the control to disabled before moving it,
	// we ensure that the focus goes away from the control and its children
	// and moves somewhere else
	boolean enabled = control.getEnabled();
	control.setEnabled(false);
	control.setParent(newParent);
	control.setEnabled(enabled);
}
/**
 * Sets the presentation bounds.
 */
final public void setBounds(int x, int y, int w, int h) {
	setBounds(new Rectangle(x, y, w, h));
}
/**
 * Sets the presentation bounds.
 */
public void setBounds(Rectangle r) {
	Control ctrl = getControl();
	if (ctrl != null)
		ctrl.setBounds(r);
}
/**
 * Sets the parent for this part.
 */
public void setContainer(ILayoutContainer container) {
	this.container = container;
}
/**
 * Sets focus to this part.
 */
public void setFocus() {
}
/** 
 * Sets the part ID.
 */
public void setID(String str) {
	id = str;
}
/**
 * @see IPartDropTarget::targetPartFor
 */
public LayoutPart targetPartFor(LayoutPart dragSource) {
	return null;
}
}
