package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.registry.*;
import java.util.*;

/**
 * The ViewFactory is used to control the creation and disposal of views.  
 * It implements a reference counting strategy so that one view can be shared
 * by more than one client.
 */
public class ViewFactory
{
	static boolean DEBUG = false;
	private WorkbenchPage page;
	private IViewRegistry viewReg;
	private ReferenceCounter counter;

/**
 * ViewManager constructor comment.
 */
public ViewFactory(WorkbenchPage page, IViewRegistry reg) {
	super();
	this.page = page;
	this.viewReg = reg;
	counter = new ReferenceCounter();
}
/**
 * Creates an instance of a view defined by id.
 * 
 * This factory implements reference counting.  The first call to this
 * method will return a new view.  Subsequent calls will return the
 * first view with an additional reference count.  The view is
 * disposed when releaseView is called an equal number of times
 * to getView.
 */
public IViewReference createView(String id) throws PartInitException {
	return createView(id,null);
}
/**
 * Creates an instance of a view defined by id.
 * 
 * This factory implements reference counting.  The first call to this
 * method will return a new view.  Subsequent calls will return the
 * first view with an additional reference count.  The view is
 * disposed when releaseView is called an equal number of times
 * to getView.
 */
public IViewReference createView(String id,IMemento memento) 
	throws PartInitException 
{
	IViewDescriptor desc = viewReg.find(id);
	if(desc == null)
		throw new PartInitException(WorkbenchMessages.format("ViewFactory.couldNotCreate", new Object[] {id})); //$NON-NLS-1$
	IViewReference ref = (IViewReference)counter.get(desc);
	if (ref == null) {
		ref = createView(desc,memento);
	} else {
		counter.addRef(desc);
	}
	return ref;
}
/**
 * Create a view rec with the given type and parent. 
 */
private ViewReference createView(IViewDescriptor desc,IMemento memento)
	throws PartInitException
{
	// Debugging
	if (DEBUG)
		System.out.println("Create " + desc.getLabel());//$NON-NLS-1$

	// Create the view.
	IViewPart view = null;
	try {
		view = desc.createView();
	} catch (CoreException e) {
		throw new PartInitException(WorkbenchMessages.format("ViewFactory.initException", new Object[] {desc.getID()}),e); //$NON-NLS-1$
	}
	
	// Create site
	ViewSite site = new ViewSite(view, page, desc);
	view.init(site,memento);
	if (view.getSite() != site)
		throw new PartInitException(WorkbenchMessages.format("ViewFactory.siteException", new Object[] {desc.getID()})); //$NON-NLS-1$


	// Create pane, etc.
	ViewReference ref = new ViewReference(view);
	ViewPane pane = new ViewPane(ref, page);
	site.setPane(pane);
	site.setActionBars(new ViewActionBars(page.getActionBars(), pane));
	
	// Add ref to view.
	counter.put(desc, ref);
	
	// Return view.
	return ref;
}
/**
 * Remove a view rec from the manager.
 *
 * The IViewPart.dispose method must be called at a higher level.
 */
private void destroyView(IViewDescriptor desc, IViewPart view) 
{
	// Debugging
	if (DEBUG)
		System.out.println("Dispose " + desc.getLabel());//$NON-NLS-1$

	// Free action bars, pane, etc.
	PartSite site = (PartSite)view.getSite();
	ViewActionBars actionBars = (ViewActionBars)site.getActionBars();
	actionBars.dispose();
	PartPane pane = site.getPane();
	pane.dispose();

	// Free the site.
	site.dispose();
}
/**
 * Returns the view with the given id, or <code>null</code> if not found.
 */
public IViewReference getView(String id) {
	IViewDescriptor desc = viewReg.find(id);
	return (IViewReference) counter.get(desc);
}
/**
 * Returns a list of views which are open.
 */
public IViewReference[] getViews() {
	List list = counter.values();
	IViewReference [] array = new IViewReference[list.size()];
	for (int i = 0; i < array.length; i++) {
		array[i] = (IViewReference)list.get(i);
	}
	return array;
}
/**
 * Returns whether a view with the given id exists.
 */
public boolean hasView(String id) {
	IViewDescriptor desc = viewReg.find(id);
	Object view = counter.get(desc);
	return (view != null);
}
/**
 * Releases an instance of a view defined by id.
 *
 * This factory does reference counting.  For more info see
 * getView.
 */
public void releaseView(String id) {
	IViewDescriptor desc = viewReg.find(id);
	IViewReference ref = (IViewReference)counter.get(desc);
	if (ref == null)
		return;
	int count = counter.removeRef(desc);
	if (count <= 0) {
		IViewPart view = (IViewPart)ref.getPart(false);
		if(view != null)
			destroyView(desc, view);
	}
}

private class ViewReference extends WorkbenchPartReference implements IViewReference {

	private IViewPart part;

	public ViewReference(IViewPart part) {
		this.part = part;
		super.setPart(part);
	}
	/**
	 * @see IViewReference#isFastView()
	 */
	public boolean isFastView() {
		return ((WorkbenchPage)part.getSite().getPage()).isFastView(part);
	}
	/**
	 * @see IWorkbenchPartReference#getPart(boolean)
	 */
	public IWorkbenchPart getPart(boolean restore) {
		return part;
	}
	/**
	 * @see IViewReference#getView(boolean)
	 */
	public IViewPart getView(boolean restore) {
		return (IViewPart)getPart(restore);
	}
	/**
	 * @see IWorkbenchPartReference#getTitle()
	 */
	public String getTitle() {
		return part.getTitle();
	}
	/**
	 * @see IWorkbenchPartReference#getTitleImage()
	 */
	public Image getTitleImage() {
		return part.getTitleImage();
	}
	/**
	 * @see IWorkbenchPartReference#getId()
	 */	
	public String getId() {
		return part.getSite().getId();
	}
	public void setPane(PartPane pane) {
		((PartSite)part.getSite()).setPane(pane);
	}
	public PartPane getPane() {
		return ((PartSite)part.getSite()).getPane();
	}
	public String getTitleToolTip() {
		return part.getTitleToolTip();
	}
}

}
