package org.eclipse.ui.tests.api;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.junit.util.*;

public abstract class MockWorkbenchPart implements IWorkbenchPart,
	IExecutableExtension
{	
	protected CallHistory callTrace;
		
	private IPropertyListener myListener;
	private Composite myParent;		
	private IWorkbenchPartSite site;
	private String title;
	private MockSelectionProvider selectionProvider;
	
	public MockWorkbenchPart() {		
		callTrace = new CallHistory(this);
		selectionProvider = new MockSelectionProvider();
	}
	
	public CallHistory getCallHistory()
	{
		return callTrace;
	}	

	public ISelectionProvider getSelectionProvider() {
		return selectionProvider;
	}
	
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		title = (String)config.getAttribute("name");
	}
	
	public void setSite(IWorkbenchPartSite site) {
		this.site = site;
		site.setSelectionProvider(selectionProvider);
	}
	
	public IWorkbenchPartSite getSite() {
		return site;
	}
	
	/**
	 * @see IWorkbenchPart#addPropertyListener(IPropertyListener)
	 */
	public void addPropertyListener(IPropertyListener listener) {
		myListener = listener;	
	}

	/**
	 * @see IWorkbenchPart#createPartControl(Composite)
	 */
	public void createPartControl(Composite parent) {
		myParent = parent;
		callTrace.add("createPartControl" );
		Label label = new Label(parent, SWT.NONE);
		label.setText(title);
	}

	/**
	 * @see IWorkbenchPart#dispose()
	 */
	public void dispose() {
		callTrace.add("dispose" );
	}

	/**
	 * @see IWorkbenchPart#getTitle()
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @see IWorkbenchPart#getTitleImage()
	 */
	public Image getTitleImage() {
		return null;
	}

	/**
	 * @see IWorkbenchPart#getTitleToolTip()
	 */
	public String getTitleToolTip() {
		return title;
	}

	/**
	 * @see IWorkbenchPart#removePropertyListener(IPropertyListener)
	 */
	public void removePropertyListener(IPropertyListener listener) {
		myListener = null;
	}

	/**
	 * @see IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		callTrace.add("setFocus" );
	}

	/**
	 * @see IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class arg0) {
		return null;
	}
	
	/**
	 * Fires a selection out.
	 */
	public void fireSelection() {
		selectionProvider.fireSelection();
	}
}