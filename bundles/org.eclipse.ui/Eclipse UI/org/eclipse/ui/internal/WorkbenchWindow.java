package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.*;
import org.eclipse.ui.help.*;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.ui.internal.dialogs.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.misc.*;
import org.eclipse.ui.internal.model.WorkbenchAdapter;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.actions.OpenNewWindowMenu;
import org.eclipse.ui.actions.OpenNewPageMenu;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.window.*;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * A window within the workbench.
 */
// NOTE: Once IDesktopWindow is removed, then the WorkbenchWindow class definition should change
// to say IWorkbenchWindow instead. When making the change to IWorkbenchWindow, do not 
// forget to remove the old methods from IDesktopWindow that were kept for backward compatibility.
public class WorkbenchWindow extends ApplicationWindow
	implements IWorkbenchWindow
{
	private int number;
	private Workbench workbench;
	private WorkbenchPage activePage;
	private List pageTable = new ArrayList(4);
	private PageListenerList pageListeners = new PageListenerList();
	private PerspectiveListenerListOld perspectiveListeners = new PerspectiveListenerListOld();
	private WWinPerspectiveService perspectiveService = new WWinPerspectiveService(this);
	private WWinPartService partService = new WWinPartService();
	private IMemento deferredRestoreState;
	private ActionPresentation actionPresentation;
	private WWinActionBars actionBars;
	private Label separator2;
	private Label separator3;
	private ToolBarManager shortcutBar;
	private WorkbenchActionBuilder builder;
	final private String TAG_INPUT = "input";//$NON-NLS-1$
	final private String TAG_LAYOUT = "layout";//$NON-NLS-1$
	final private String TAG_FOCUS = "focus";//$NON-NLS-1$
	final private String TAG_FACTORY_ID = "factoryID";//$NON-NLS-1$
	final protected String GRP_PAGES = "pages";//$NON-NLS-1$
	final protected String GRP_FAST_VIEWS = "fastViews";//$NON-NLS-1$

	// static fields for inner classes.
	static final int VGAP= 0;
	static final int CLIENT_INSET = 3;
	static final int BAR_SIZE = 23;

	/**
	 * The window toolbar must relayout whenever an update occurs, as items are
	 * added and removed dynamically.
	 */
	class WindowToolBarManager extends ToolBarManager {
		public WindowToolBarManager(int style) {
			super(style);
		}
		protected void relayout(ToolBar toolBar, int oldCount, int newCount) {
			Composite parent= toolBar.getParent();
			parent.layout();
		}       
	}
	
	/**
	 * This vertical layout supports a fixed size Toolbar area, a separator line,
	 * the variable size content area,
	 * and a fixed size status line.
	 */
	class WorkbenchWindowLayout extends Layout {
	
		protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) 
		{
			if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT)
				return new Point(wHint, hHint);
				
			Point result= new Point(0, 0);
			Control[] ws= composite.getChildren();
			for (int i= 0; i < ws.length; i++) {
				Control w= ws[i];
				boolean skip = false;
				if (getToolBarManager() != null && w == getToolBarManager().getControl()) {
					skip = true;
					result.y+= BAR_SIZE;  
				} else if (w == shortcutBar.getControl()) {
					skip = true;
				} 
				if (!skip) {
					Point e= w.computeSize(wHint, hHint, flushCache);
					result.x= Math.max(result.x, e.x);
					result.y+= e.y + VGAP;
				}
			}

			result.x += BAR_SIZE; // For shortcut bar.
			if (wHint != SWT.DEFAULT)
				result.x= wHint;
			if (hHint != SWT.DEFAULT)
				result.y= hHint;
			return result;
		}

		protected void layout(Composite composite, boolean flushCache) 
		{
			Rectangle clientArea= composite.getClientArea();
		
			// Loop through the children.  
			// Expected order == sep1, toolbar, status, sep2, shortcuts, sep3, client
			Control[] ws= composite.getChildren();
			for (int i= 0; i < ws.length; i++) {
				Control w= ws[i];
				if (i == 0 || w == separator2) { // Separators
					Point e= w.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
					w.setBounds(clientArea.x, clientArea.y, clientArea.width, e.y);
					clientArea.y+= e.y;
					clientArea.height-= e.y;
				} else if (getToolBarManager() != null && w == getToolBarManager().getControl()) {
					int height = BAR_SIZE;
					if (w instanceof ToolBar) {
						ToolBar bar = (ToolBar) w;
						if (bar.getItemCount() > 0) { 
							Point e = bar.computeSize(clientArea.width, SWT.DEFAULT, flushCache);
							height = e.y;
						}
					}
					w.setBounds(clientArea.x, clientArea.y, clientArea.width, height);
					clientArea.y+= height;
					clientArea.height-= height;
				} else if (getStatusLineManager() != null && w == getStatusLineManager().getControl()) {
					int width = BAR_SIZE;
					if (shortcutBar != null) {
						Widget widget = shortcutBar.getControl();
						if (widget != null) {
							if (widget instanceof ToolBar) {
								ToolBar bar = (ToolBar) widget;
								if (bar.getItemCount() > 0) {
									ToolItem item = bar.getItem(0);
									width = item.getWidth();
									Rectangle trim = bar.computeTrim(0,0,width,width);
									width = trim.width + 2; // Add 2 pixels around shortcut bar
								}
							}
						}	
					}	
					Point e= w.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
					w.setBounds(clientArea.x + width, clientArea.y+clientArea.height-e.y, clientArea.width - width, e.y);
					clientArea.height-= e.y + VGAP;
				} else if (w == shortcutBar.getControl()) {
					int width = BAR_SIZE;
					if (w instanceof ToolBar) {
						ToolBar bar = (ToolBar) w;
						if (bar.getItemCount() > 0) {
							ToolItem item = bar.getItem(0);
							width = item.getWidth();
							Rectangle trim = bar.computeTrim(0,0,width,width);
							width = trim.width + 2; // Add 2 pixels around shortcut bar
						}
					}
					w.setBounds(clientArea.x, clientArea.y, width, clientArea.height);
					clientArea.x+= width + VGAP;
					clientArea.width-= width + VGAP;
				} else if (w == separator3) {
					Point e= w.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
					w.setBounds(clientArea.x, clientArea.y, e.x, clientArea.height);
					clientArea.x+= e.x;
				} else {
					// Must be client.
					// Inset client area by 3 pixels 
					w.setBounds(clientArea.x + CLIENT_INSET, clientArea.y + CLIENT_INSET + VGAP, clientArea.width - ( 2 * CLIENT_INSET), clientArea.height - VGAP - (2 * CLIENT_INSET));
				}
			}
		}
	}
	
/**
 * WorkbenchWindow constructor comment.
 * @param workbench Workbench
 */
public WorkbenchWindow(Workbench workbench, int number) {
	super(null);
	this.workbench = workbench;
	this.number = number;

	// Setup window.
	addMenuBar();
	addToolBar(SWT.FLAT | SWT.WRAP);
	addStatusLine();
	addShortcutBar(SWT.FLAT | SWT.WRAP);

	// Add actions.
	actionPresentation = new ActionPresentation(this);
	builder = new WorkbenchActionBuilder();
	builder.buildActions(this);
}
/*
 * Adds an listener to the part service.
 */
public void addPageListener(IPageListener l) {
	pageListeners.addPageListener(l);
}
/*
 * Adds an listener to the perspective service.
 *
 * NOTE: Internally, please use getPerspectiveService instead.
 */
public void addPerspectiveListener(org.eclipse.ui.IPerspectiveListener l) {
	perspectiveListeners.addPerspectiveListener(l);
}
/**
 * add a shortcut for the page.
 */
private void addShortcut(WorkbenchPage page) {
	SetPageAction action = new SetPageAction(page);
	shortcutBar.appendToGroup(GRP_PAGES, action);
	shortcutBar.update(false);
}
/**
 * Configures this window to have a shortcut bar.
 * Does nothing if it already has one.
 * This method must be called before this window's shell is created.
 */
protected void addShortcutBar(int style) {
	if ((getShell() == null) && (shortcutBar == null)) {
		shortcutBar = new ToolBarManager(style);
	}
}
/**
 * Close the window.
 * 
 * Assumes that busy cursor is active.
 */
private boolean busyClose() {
	// Only do the check if it is OK to close if we are not closing via the
	// workbench as the workbench will call this itself
	int count = workbench.getWorkbenchWindowCount();
	if (count <= 1 && !workbench.isClosing())
		return workbench.close();
	else {
		if (!okToClose())
			return false;
		return hardClose();
	}
}
/**
 * Opens a new page.
 *
 * Assumes that busy cursor is active.
 */
private IWorkbenchPage busyOpenPage(String perspID, IAdaptable input) 
	throws org.eclipse.ui.WorkbenchException 
{
	// Create page.
	WorkbenchPage result = new WorkbenchPage(this, perspID, input);
	pageTable.add(result);
	firePageOpened(result);

	// Add shortcut.
	addShortcut(result);

	// Activate.
	setActivePage(result);
	
	return result;
}
/**
 * @see IWorkbenchWindow
 */
public boolean close() {
	final boolean [] ret = new boolean[1];
	BusyIndicator.showWhile(null, new Runnable() {
		public void run() {
			ret[0] = busyClose();
		}
	});
	return ret[0];
}
/**
 * Close all of the pages.
 */
private void closeAllPages() 
{
	// Deactivate active page.
	setActivePage(null);

	// Clone and deref all so that calls to getPages() returns
	// empty list (if call by pageClosed event handlers)
	List clone = (List)((ArrayList)pageTable).clone();
	pageTable.clear();

	// Close all.
	Iterator enum = clone.iterator();
	while (enum.hasNext()) {
		WorkbenchPage page = (WorkbenchPage)enum.next();
		removeShortcut(page);
		firePageClosed(page);
		page.dispose();
	}
}
/**
 * Save and close all of the pages.
 */
public void closeAllPages(boolean save) {
	if (save) {
		boolean ret = saveAllPages(true);
		if (!ret) return;
	}
	closeAllPages();
}
/**
 * closePerspective method comment.
 */
protected boolean closePage(IWorkbenchPage in, boolean save) {
	// Validate the input.
	if (!pageTable.contains(in))
		return false;
	WorkbenchPage oldPage = (WorkbenchPage)in;

	// Save old perspective.
	if (save && oldPage.isSaveNeeded()) {
		if (!oldPage.saveAllEditors(true))
			return false;
	}

	// If old page is activate deactivate.
	boolean oldIsActive = (oldPage == activePage);
	if (oldIsActive)
		setActivePage(null);
		
	// Close old page.
	int nIndex = pageTable.indexOf(oldPage);
	pageTable.remove(oldPage);
	removeShortcut(oldPage);
	firePageClosed(oldPage);
	oldPage.dispose();
	
	// Activate new page.
	if (oldIsActive) {
		WorkbenchPage newPage = null;
		int nMaxIndex = pageTable.size() - 1;
		if (nIndex > nMaxIndex)
			nIndex = nMaxIndex;
		if (nIndex >= 0)
			newPage = (WorkbenchPage)pageTable.get(nIndex);
		if (newPage != null)
			setActivePage(newPage);
	}

	return true;
}
/**
 * Sets the ApplicationWindows's content layout.
 * This vertical layout supports a fixed size Toolbar area, a separator line,
 * the variable size content area,
 * and a fixed size status line.
 */
protected void configureShell(Shell shell) {
	super.configureShell(shell);
	shell.setLayout(new WorkbenchWindowLayout());
	shell.setSize(800, 600);
	separator2 = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
	createShortcutBar(shell);
	separator3 = new Label(shell, SWT.SEPARATOR | SWT.VERTICAL);

	WorkbenchHelp.setHelp(shell, new String[] {IHelpContextIds.WORKBENCH_WINDOW});

	// If the user clicks on toolbar, status bar, or shortcut bar
	// hide the fast view.
	Listener listener = new Listener() {
		public void handleEvent(Event event) {
			if (activePage != null) {
				if (event.type == SWT.MouseDown) {
					if (event.widget instanceof ToolBar) {
						// Ignore mouse down on actual tool bar buttons
						Point pt = new Point(event.x, event.y);
						ToolBar toolBar = (ToolBar)event.widget;
						if (toolBar.getItem(pt) != null)
							return;
					}
					activePage.toggleFastView(null);
				}
			}
		}
	};
	getToolBarManager().getControl().addListener(SWT.MouseDown, listener);
	Control[] children = ((Composite)getStatusLineManager().getControl()).getChildren();
	for (int i = 0; i < children.length; i++) {
		if (children[i] != null)
			children[i].addListener(SWT.MouseDown, listener);
	}
	getShortcutBar().getControl().addListener(SWT.MouseDown, listener);
}
/**
 * Create the shortcut toolbar control
 */
private void createShortcutBar(Shell shell) {
	// Create control.
	if (shortcutBar == null)
		return;
	shortcutBar.createControl(shell);

	// Define shortcut part.  This is for drag and drop.
	new ShortcutBarPart(shortcutBar);
	
	// Add right mouse button support.
	ToolBar tb = shortcutBar.getControl();
	tb.addMouseListener(new MouseAdapter() {
		public void mouseDown(MouseEvent e) {
			if (e.button == 3)
				showShortcutBarPopup(e);
		}
	});
}
/* (non-Javadoc)
 * Method declared on ApplicationWindow.
 */
protected ToolBarManager createToolBarManager(int style) {
	return new WindowToolBarManager(style);
}
/**
 * Returns the shortcut for a page.
 */
private IContributionItem findShortcut(IWorkbenchPage page) {
	IContributionItem [] array = shortcutBar.getItems();
	int length = array.length;
	for (int nX = 0; nX < length; nX ++) {
		IContributionItem item = array[nX];
		if (item instanceof ActionContributionItem) {
			IAction action = ((ActionContributionItem)item).getAction();
			if (action instanceof SetPageAction) {
				SetPageAction sp = (SetPageAction)action;
				if (sp.getPage() == page)
					return item;	
			}
		}
	}
	return null;
}
/**
 * Fires page activated
 */
private void firePageActivated(IWorkbenchPage page) {
	pageListeners.firePageActivated(page);
	partService.pageActivated(page);
}
/**
 * Fires page closed
 */
private void firePageClosed(IWorkbenchPage page) {
	pageListeners.firePageClosed(page);
	partService.pageClosed(page);
}
/**
 * Fires page opened
 */
private void firePageOpened(IWorkbenchPage page) {
	pageListeners.firePageOpened(page);
	partService.pageOpened(page);
}
/**
 * Fires perspective activated
 */
void firePerspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
	perspectiveListeners.firePerspectiveActivated(page, perspective);
	perspectiveService.firePerspectiveActivated(page, perspective);
}
/**
 * Fires perspective changed
 */
void firePerspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
	perspectiveListeners.firePerspectiveChanged(page, perspective, changeId);
}
/**
 * Fires perspective reset
 */
void firePerspectiveReset(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
	perspectiveService.firePerspectiveReset(page, perspective);
}
/**
 * Returns the action bars for this window.
 */
public IActionBars getActionBars() {
	if (actionBars == null) {
		actionBars = new WWinActionBars(this);
	}
	return actionBars;
}
/**
 * Returns the active page.
 *
 * @return the active page
 */
public IWorkbenchPage getActivePage() {
	return activePage;
}
/**
 * Get the workbench client area.
 */
protected Composite getClientComposite() {
	return (Composite)getContents();
}
/**
 * Returns the menu bar manager for this window (if it has one).
 * <p>
 * [Issue: Could this be made a protected framework method?]
 * </p>
 *
 * @return the menu bar manager, or <code>null</code> if
 *   this window does not have a menu bar
 * @see #addMenuBar
 */
public MenuManager getMenuBarManager() {
	return super.getMenuBarManager();
}
/**
 * Answer the menu manager for this window.
 */
public MenuManager getMenuManager() {
	return getMenuBarManager();
}
/**
 * Returns the number.  This corresponds to a page number in a window or a
 * window number in the workbench.
 */
public int getNumber() {
	return number;
}
/**
 * Returns an array of the pages in the workbench window.
 *
 * @return an array of pages
 */
public IWorkbenchPage[] getPages() {
	int nSize = pageTable.size();
	IWorkbenchPage [] retArray = new IWorkbenchPage[nSize];
	pageTable.toArray(retArray);
	return retArray;
}
/**
 * @see IWorkbenchWindow
 */
public IPartService getPartService() {
	return partService;
}
/**
 * @see IWorkbenchWindow
 */
public IPerspectiveService getPerspectiveService() {
	return perspectiveService;
}
/**
 * @see IWorkbenchWindow
 */
public ISelectionService getSelectionService() {
	return partService.getSelectionService();
}
/**
 * Returns the shortcut bar.
 */
public ToolBarManager getShortcutBar() {
	return shortcutBar;
}
/**
 * Returns the status line manager for this window (if it has one).
 *
 *
 * @return the status line manager, or <code>null</code> if
 *   this window does not have a status line
 * @see #addStatusLine
 */
protected StatusLineManager getStatusLineManager() {
	return super.getStatusLineManager();
}
/**
 * Returns the tool bar manager for this window (if it has one).
 *
 * @return the tool bar manager, or <code>null</code> if
 *   this window does not have a tool bar
 * @see #addToolBar
 */
public ToolBarManager getToolBarManager() {
	return super.getToolBarManager();
}
/**
 * @see IWorkbenchWindow
 */
public IWorkbench getWorkbench() {
	return workbench;
}
/**
 * Unconditionally close this window.
 */
public boolean hardClose() {
	closeAllPages();
	builder.dispose();
	return super.close();
}
/**
 * @see IWorkbenchWindow
 */
public boolean isApplicationMenu(String menuID) {
	return WorkbenchActionBuilder.isContainerMenu(menuID);
}
/**
 * Called when this window is about to be closed.
 *
 * Subclasses may overide to add code that returns <code>false</code> 
 * to prevent closing under certain conditions.
 */
public boolean okToClose() {
	// Save all of the editors.
	if (!saveAllPages(true))
		return false;
	return true;
}
/**
 * Opens a new page. 
 */
public IWorkbenchPage openPage(final String perspID, final IAdaptable input) 
	throws WorkbenchException 
{
	// Run op in busy cursor.
	final Object [] result = new Object[1];
	BusyIndicator.showWhile(null, new Runnable() {
		public void run() {
			try {
				result[0] = busyOpenPage(perspID, input);
			} catch (WorkbenchException e) {
				result[0] = e;
			}
		}
	});
	if (result[0] instanceof IWorkbenchPage)
		return (IWorkbenchPage)result[0];
	else if (result[0] instanceof WorkbenchException)
		throw (WorkbenchException)result[0];
	else
		throw new WorkbenchException(WorkbenchMessages.getString("WorkbenchWindow.exceptionMessage")); //$NON-NLS-1$
}
/**
 * Opens a new page. 
 */
public IWorkbenchPage openPage(IAdaptable input)
	throws WorkbenchException 
{
	return openPage(workbench.getPerspectiveRegistry().getDefaultPerspective(), 
		input);
}
/*
 * Removes an listener from the part service.
 */
public void removePageListener(IPageListener l) {
	pageListeners.removePageListener(l);
}
/*
 * Removes an listener from the perspective service.
 *
 * NOTE: Internally, please use getPerspectiveService instead.
 */
public void removePerspectiveListener(org.eclipse.ui.IPerspectiveListener l) {
	perspectiveListeners.removePerspectiveListener(l);
}
/**
 * Remove the shortcut for a page.
 */
private void removeShortcut(IWorkbenchPage page) {
	IContributionItem item = findShortcut(page);
	if (item != null) {
		shortcutBar.remove(item);
		shortcutBar.update(false);
	}
}
/**
 * @see IPersistable.
 */
public void restoreState(IMemento memento) {
	Assert.isNotNull(getShell());
	
	// Read the bounds.
	if("true".equals(memento.getString("maximized"))) {//$NON-NLS-2$//$NON-NLS-1$
		getShell().setMaximized(true);
	} else {
		Integer bigInt;
		bigInt = memento.getInteger(IWorkbenchConstants.TAG_X);
		int x = bigInt.intValue();
		bigInt = memento.getInteger(IWorkbenchConstants.TAG_Y);
		int y = bigInt.intValue();
		bigInt = memento.getInteger(IWorkbenchConstants.TAG_WIDTH);
		int width = bigInt.intValue();
		bigInt = memento.getInteger(IWorkbenchConstants.TAG_HEIGHT);
		int height = bigInt.intValue();
		// Set the bounds.
		getShell().setBounds(x, y, width, height);
	}

	// Recreate each perspective in the window. 
	IWorkbenchPage newActivePage = null;
	IMemento [] pageArray = memento.getChildren(IWorkbenchConstants.TAG_PAGE);
	for (int i = 0; i < pageArray.length; i ++) {
		IMemento pageMem = pageArray[i];

		// Get the input factory.
		IMemento inputMem = pageMem.getChild(IWorkbenchConstants.TAG_INPUT);
		String factoryID = inputMem.getString(IWorkbenchConstants.TAG_FACTORY_ID);
		if (factoryID == null) {
			WorkbenchPlugin.log("Unable to restore page - no input factory ID.");//$NON-NLS-1$
			continue;
		}
		IElementFactory factory = WorkbenchPlugin.getDefault().getElementFactory(factoryID);
		if (factory == null) {
			WorkbenchPlugin.log("Unable to restore pagee - cannot instantiate input factory: " + factoryID);//$NON-NLS-1$
			continue;
		}
			
		// Get the input element.
		IAdaptable input = factory.createElement(inputMem);
		if (input == null) {
			WorkbenchPlugin.log("Unable to restore page - cannot instantiate input element: " + factoryID);//$NON-NLS-1$
			continue;
		}

		// Open the perspective.
		WorkbenchPage result = null;
		try {
			result = new WorkbenchPage(this, pageMem, input);
			pageTable.add(result);
			pageListeners.firePageOpened(result);
			addShortcut(result);
		} catch (WorkbenchException e) {
			WorkbenchPlugin.log("Unable to restore perspective - constructor failed.");//$NON-NLS-1$
			continue;
		}

		// Check for focus.
		String strFocus = pageMem.getString(IWorkbenchConstants.TAG_FOCUS);
		if (strFocus != null && strFocus.length() > 0)
			newActivePage = result;
	}

	// If there are no pages create a default.
	if (pageTable.isEmpty()) {
		try {
			IContainer root = WorkbenchPlugin.getPluginWorkspace().getRoot();
			String defPerspID = workbench.getPerspectiveRegistry().getDefaultPerspective();
			WorkbenchPage result = new WorkbenchPage(this, defPerspID, root);
			pageTable.add(result);
			pageListeners.firePageOpened(result);
			addShortcut(result);
		} catch (WorkbenchException e) {
			WorkbenchPlugin.log("Unable to create default perspective - constructor failed.");//$NON-NLS-1$
			getShell().setText(workbench.getProductInfo().getName());
			return;
		}
	}
		
	// Set active page.
	if (newActivePage == null)
		newActivePage = (IWorkbenchPage)pageTable.get(0);
	setActivePage(newActivePage);
}
/**
 * Save all of the pages.  Returns true if the operation succeeded.
 */
private boolean saveAllPages(boolean bConfirm) 
{
	boolean bRet = true;
	Iterator enum = pageTable.iterator();
	while (bRet && enum.hasNext()) {
		WorkbenchPage page = (WorkbenchPage)enum.next();
		bRet = page.saveAllEditors(bConfirm);
	}
	return bRet;
}
/**
 * @see IPersistable
 */
public void saveState(IMemento memento) {
	
	// Save the bounds.
	if(getShell().getMaximized()) {
		memento.putString("maximized","true");//$NON-NLS-2$//$NON-NLS-1$
	} else {
		Rectangle bounds = getShell().getBounds();
		memento.putInteger(IWorkbenchConstants.TAG_X, bounds.x);
		memento.putInteger(IWorkbenchConstants.TAG_Y, bounds.y);
		memento.putInteger(IWorkbenchConstants.TAG_WIDTH, bounds.width);
		memento.putInteger(IWorkbenchConstants.TAG_HEIGHT, bounds.height);
	}

	// Save each page.
	Iterator enum = pageTable.iterator();
	while (enum.hasNext()) 
	{
		WorkbenchPage page = (WorkbenchPage)enum.next();
		
		// Get the input.
		IAdaptable input = page.getInput();
		if (input == null) {
			WorkbenchPlugin.log("Unable to save page input: " + page);//$NON-NLS-1$
			continue;
		}
		IPersistableElement persistable = (IPersistableElement)input.getAdapter(IPersistableElement.class);
		if (persistable == null) {
			WorkbenchPlugin.log("Unable to save page input: " + input);//$NON-NLS-1$
			continue;
		}

		// Save perspective.
		IMemento pageMem = memento.createChild(IWorkbenchConstants.TAG_PAGE);
		page.saveState(pageMem);
		
		if (page == activePage) {
			pageMem.putString(IWorkbenchConstants.TAG_FOCUS, "true");//$NON-NLS-1$
		}
		
		// Save input.
		IMemento inputMem = pageMem.createChild(IWorkbenchConstants.TAG_INPUT);
		inputMem.putString(TAG_FACTORY_ID, persistable.getFactoryId());
		persistable.saveState(inputMem);
	}
}
/**
 * Select the shortcut for a page.
 */
private void selectShortcut(IWorkbenchPage page, boolean selected) {
	IContributionItem item = findShortcut(page);
	if (page != null) {
		IAction action = ((ActionContributionItem)item).getAction();
		action.setChecked(selected);
	}
}
/**
 * Sets the active page within the window.
 *
 * @param page identifies the new active page.
 */
public void setActivePage(final IWorkbenchPage in) {
	// Shortcut.
	if (activePage == in) {
		if (activePage != null)
			selectShortcut(activePage, true);
		return;
	}

	// 1FVGTNR: ITPUI:WINNT - busy cursor for switching perspectives
	BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
		public void run() {
			// Deactivate old persp.
			if (activePage != null) {
				activePage.onDeactivate();
				selectShortcut(activePage, false);
			}

			// Activate new persp.
			if (in == null || pageTable.contains(in)) {
				activePage = (WorkbenchPage)in;
				if (activePage != null) {
					activePage.onActivate();
					firePageActivated(activePage);
					firePerspectiveActivated(activePage, activePage.getPerspective());
					selectShortcut(activePage, true);
				}
			}

			// Update action bars ( implicitly calls updateActionBars() )
			updateTitle();
			updateActionSets();
			shortcutBar.update(false);
		}
	});
}
/**
 * Shows the popup menu for a page item in the shortcut bar.
 */
private void showShortcutBarPopup(MouseEvent e) {
	// Get the tool item under the mouse.
	Point pt = new Point(e.x, e.y);
	ToolBar tb = shortcutBar.getControl();
	ToolItem toolItem = tb.getItem(pt);
	if (toolItem == null)
		return;

	// Get the page for the tool item.
	Object data = toolItem.getData();
	if (!(data instanceof ActionContributionItem))
		return;
	IAction action = ((ActionContributionItem) data).getAction();
	Menu menu;
	if (action instanceof SetPageAction) {
		SetPageAction sp = (SetPageAction) action;
		final IWorkbenchPage page = sp.getPage();

		// Create a popup menu.	
		menu = new Menu(tb);
		MenuItem menuItem = new MenuItem(menu, 0);
		menuItem.setText(WorkbenchMessages.getString("WorkbenchWindow.close")); //$NON-NLS-1$
		menuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				closePage(page, true);
			}
		});
		menuItem = new MenuItem(menu, 0);
		menuItem.setText(WorkbenchMessages.getString("WorkbenchWindow.closeAll")); //$NON-NLS-1$
		menuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (saveAllPages(true))
					closeAllPages();
			}
		});

	} else {
		return;
	}

	// Show popup menu.
	pt = tb.toDisplay(pt);
	menu.setLocation(pt.x, pt.y);
	menu.setVisible(true);
}
/**
 * update the action bars.
 */
public void updateActionBars() {
	// updateAll required in order to enable accelerators on pull-down menus
	getMenuBarManager().updateAll(false);
	getToolBarManager().update(false);
	getStatusLineManager().update(false);
}
/**
 * Update the visible action sets. This method is typically called
 * from a page when the user changes the visible action sets
 * within the prespective.  
 */
public void updateActionSets() {
	if (activePage == null)
		actionPresentation.clearActionSets();
	else
		actionPresentation.setActionSets(activePage.getActionSets());
	updateActionBars();

	// hide the launch menu if it is empty
	String path = IWorkbenchActionConstants.M_WINDOW + IWorkbenchActionConstants.SEP + IWorkbenchActionConstants.M_LAUNCH;
	IMenuManager manager = getMenuBarManager().findMenuUsingPath(path);
	IContributionItem item = getMenuBarManager().findUsingPath(path);
	if (manager == null || item == null)
		return;
	item.setVisible(manager.getItems().length >= 2);  // there is a separator for the additions group thus >= 2
}
/**
 * Updates the content and layout for a tab.
 */
public void updateShortcut(IWorkbenchPage page) {
	IContributionItem item = findShortcut(page);
	if (page != null) {
		SetPageAction action = (SetPageAction)((ActionContributionItem)item).getAction();
		action.update();
		if (page == activePage)
			updateTitle();
	}
}
/**
 * Updates the window title.
 */
private void updateTitle() {
	String title = workbench.getProductInfo().getName();
	if (activePage != null) {
		IPerspectiveDescriptor persp = activePage.getPerspective();
		String label = persp.getLabel();
		IAdaptable input = activePage.getInput();
		if((input != null) && (activePage.getInput() != ResourcesPlugin.getWorkspace().getRoot()))
			label = activePage.getLabel();
		title = WorkbenchMessages.format("WorkbenchWindow.shellTitle", new Object[] {label, title}); //$NON-NLS-1$
	}
	getShell().setText(title);	
}
}
