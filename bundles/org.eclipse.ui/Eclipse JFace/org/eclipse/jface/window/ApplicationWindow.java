package org.eclipse.jface.window;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.operation.*;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import java.lang.reflect.InvocationTargetException;

/**
 * An application window is a high-level "main window", with built-in
 * support for an optional menu bar with standard menus, an optional toolbar,
 * and an optional status line.
 * <p>
 * Creating an application window involves the following steps:
 * <ul>
 *   <li>creating an instance of <code>ApplicationWindow</code>
 *   </li>
 *   <li>assigning the window to a window manager (optional)
 *   </li>
 *   <li>opening the window by calling <code>open</code>
 *   </li>
 * </ul>
 * Only on the last step, when the window is told to open, are
 * the window's shell and widget tree created. When the window is
 * closed, the shell and widget tree are disposed of and are no longer
 * referenced, and the window is automatically removed from its window
 * manager. Like all windows, an application window may be reopened.
 * </p>
 * <p>
 * An application window is also a suitable context in which to perform 
 * long-running operations (that is, it implements <code>IRunnableContext</code>).
 * </p>
 */
public class ApplicationWindow extends Window implements IRunnableContext {

	/**
	 * Menu bar manager, or <code>null</code> if none (default).
	 *
	 * @see #addMenuBar
	 */
	private MenuManager menuBarManager = null;

	/**
	 * Tool bar manager, or <code>null</code> if none (default).
	 *
	 * @see #addToolBar
	 */
	private ToolBarManager toolBarManager = null;

	/**
	 * Status line manager, or <code>null</code> if none (default).
	 *
	 * @see #addStatusLine
	 */
	private StatusLineManager statusLineManager = null;
	
	/**
	 * Internal application window layout class.
	 * This vertical layout supports a tool bar area (fixed size),
	 * a separator line, the content area (variable size), and a 
	 * status line (fixed size).
	 */
	/*package*/ class ApplicationWindowLayout extends Layout {
	
		static final int VGAP = 2;
			
		protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
			if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT)
				return new Point(wHint, hHint);
				
			Point result= new Point(0, 0);
			Control[] ws= composite.getChildren();
			for (int i= 0; i < ws.length; i++) {
				Control w= ws[i];
				
				boolean hide= false;
				if (toolBarManager != null && toolBarManager.getControl() == w) {
					if (w instanceof ToolBar && ((ToolBar)w).getItemCount() <= 0) {
						hide= true;
						result.y+= 23;	// REVISIT
					}
				} else if (statusLineManager != null && statusLineManager.getControl() == w) {
				} else if (i > 0) { /* we assume this window is contents */
					hide= false;
				}
				
				if (! hide) {
					Point e= w.computeSize(wHint, hHint, flushCache);
					result.x= Math.max(result.x, e.x);
					result.y+= e.y + VGAP;
				}
			}
			
			if (wHint != SWT.DEFAULT)
				result.x= wHint;
			if (hHint != SWT.DEFAULT)
				result.y= hHint;
			return result;
		}

		protected void layout(Composite composite, boolean flushCache) {
			Rectangle clientArea= composite.getClientArea();
			
			Control[] ws= composite.getChildren();
			
			for (int i= 0; i < ws.length; i++) {
				Control w= ws[i];
				
				if (i == 0) { // Separator
					Point e= w.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
					w.setBounds(clientArea.x, clientArea.y, clientArea.width, e.y);
					clientArea.y+= e.y;
					clientArea.height-= e.y;
				} else if (toolBarManager != null && toolBarManager.getControl() == w) {
					if (!(w instanceof ToolBar) || ((ToolBar)w).getItemCount() > 0) {
						Point e= w.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
						w.setBounds(clientArea.x, clientArea.y, clientArea.width, e.y);
						clientArea.y+= e.y + VGAP;
						clientArea.height-= e.y + VGAP;
					}
				} else if (statusLineManager != null && statusLineManager.getControl() == w) {
					Point e= w.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
					w.setBounds(clientArea.x, clientArea.y+clientArea.height-e.y, clientArea.width, e.y);
					clientArea.height-= e.y + VGAP;
				} else {
					w.setBounds(clientArea.x, clientArea.y + VGAP, clientArea.width, clientArea.height - VGAP);
				}
			}
		}
	}
	
/**
 * Create an application window instance, whose shell will be created under the
 * given parent shell.
 * Note that the window will have no visual representation (no widgets)
 * until it is told to open. By default, <code>open</code> does not block.
 *
 * @param parentShell the parent shell, or <code>null</code> to create a top-level shell
 */
public ApplicationWindow(Shell parentShell) {
	super(parentShell);
}
/**
 * Configures this window to have a menu bar.
 * Does nothing if it already has one.
 * This method must be called before this window's shell is created.
 */
protected void addMenuBar() {
	if ((getShell() == null) && (menuBarManager == null)) {
		menuBarManager = createMenuManager();
	}
}
/**
 * Configures this window to have a status line.
 * Does nothing if it already has one.
 * This method must be called before this window's shell is created.
 */
protected void addStatusLine() {
	if ((getShell() == null) && (statusLineManager == null)) {
		statusLineManager = createStatusLineManager();
	}
}
/**
 * Configures this window to have a tool bar.
 * Does nothing if it already has one.
 * This method must be called before this window's shell is created.
 */
protected void addToolBar(int style) {
	if ((getShell() == null) && (toolBarManager == null)) {
		toolBarManager = createToolBarManager(style);
	}
}
/* (non-Javadoc)
 * Method declared on Window.
 */
public boolean close() {
	if (super.close()) {
		menuBarManager = null;
		toolBarManager = null;
		statusLineManager = null;
		return true;
	}
	return false;
}
/* (non-Javadoc)
 * Method declared on Window.
 * Sets the ApplicationWindows's content layout.
 * This vertical layout supports a fixed size Toolbar area, a separator line,
 * the variable size content area,
 * and a fixed size status line.
 */
protected void configureShell(Shell shell) {

	super.configureShell(shell);
	
	if (menuBarManager != null) {
		menuBarManager.updateAll(true);
		shell.setMenuBar(menuBarManager.createMenuBar(shell));
	}

	// we need a special layout
	shell.setLayout(new ApplicationWindowLayout());

	new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);

	Font font = getFont();
	if (toolBarManager != null) {
		Control control = toolBarManager.createControl(shell);
	}

	if (statusLineManager != null) {
		Control control = statusLineManager.createControl(shell);
	}
}
/**
 * Returns a new menu manager for the window.
 * <p>
 * Subclasses may override this method to customize the menu manager.
 * </p>
 * @return a menu manager
 */
protected MenuManager createMenuManager() {
	return new MenuManager();
}
/**
 * Returns a new status line manager for the window.
 * <p>
 * Subclasses may override this method to customize the status line manager.
 * </p>
 * @return a status line manager
 */
protected StatusLineManager createStatusLineManager() {
	return new StatusLineManager();
}
/**
 * Returns a new tool bar manager for the window.
 * <p>
 * Subclasses may override this method to customize the tool bar manager.
 * </p>
 * @return a tool bar manager
 */
protected ToolBarManager createToolBarManager(int style) {
	return new ToolBarManager(style);
}
/**
 * Returns the default font used for this window.
 * <p>
 * The default implementation of this framework method
 * obtains the symbolic name of the font from the
 * <code>getSymbolicFontName</code> framework method
 * and retrieves this font from JFace's font
 * registry using <code>JFaceResources.getFont</code>.
 * Subclasses may override to use a different registry,
 * etc.
 * </p>
 *
 * @return the default font, or <code>null</code> if none
 */
protected Font getFont() {
	return JFaceResources.getFont(getSymbolicFontName());
}
/**
 * Returns the menu bar manager for this window (if it has one).
 *
 * @return the menu bar manager, or <code>null</code> if
 *   this window does not have a menu bar
 * @see #addMenuBar
 */
public MenuManager getMenuBarManager() {
	return menuBarManager;
}
/**
 * Returns the status line manager for this window (if it has one).
 *
 * @return the status line manager, or <code>null</code> if
 *   this window does not have a status line
 * @see #addStatusLine
 */
protected StatusLineManager getStatusLineManager() {
	return statusLineManager;
}

/**
 * Returns the symbolic font name of the font to be
 * used to display text in this window.
 * 
 * @return the symbolic font name
 */
public String getSymbolicFontName() {
	return JFaceResources.DEFAULT_FONT;
}
/**
 * Returns the tool bar manager for this window (if it has one).
 *
 * @return the tool bar manager, or <code>null</code> if
 *   this window does not have a tool bar
 * @see #addToolBar
 */
public ToolBarManager getToolBarManager() {
	return toolBarManager;
}
/* (non-Javadoc)
 * Method declared on Window.
 */
protected void handleFontChange(final PropertyChangeEvent event) {
}
/* (non-Javadoc)
 * Method declared on IRunnableContext.
 */
public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
	StatusLineManager mgr = getStatusLineManager();
	if (mgr == null) {
		runnable.run(new NullProgressMonitor());
	}
	Control contents = getContents();
	boolean contentsWasEnabled = contents.isEnabled();
	Menu menuBar = getMenuBarManager().getMenu();
	boolean menuBarWasEnabled = menuBar.isEnabled();
	boolean cancelWasEnabled = mgr.isCancelEnabled();
	try {
		contents.setEnabled(false);
		menuBar.setEnabled(false);
		mgr.setCancelEnabled(cancelable);
		ModalContext.run(runnable, fork, mgr.getProgressMonitor(), contents.getDisplay());
	} finally {
		if (!contents.isDisposed())
			contents.setEnabled(contentsWasEnabled);
		if (!menuBar.isDisposed())
			menuBar.setEnabled(menuBarWasEnabled);
		mgr.setCancelEnabled(cancelWasEnabled);
	}
}
/**
 * Sets or clears the message displayed in this window's status
 * line (if it has one). This method has no effect if the
 * window does not have a status line.
 *
 * @param message the status message, or <code>null</code> to clear it
 */
public void setStatus(String message) {
	if (statusLineManager != null) {
		statusLineManager.setMessage(message);
	}
}
}
