package org.eclipse.ui.tests.api;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.*;
import org.eclipse.ui.junit.util.*;

public class MockEditorPart extends MockWorkbenchPart implements IEditorPart {

	private static final String BASE = "org.eclipse.ui.tests.api.MockEditorPart";
	public static final String ID1 = BASE + "1";
	public static final String ID2 = BASE + "2";
	public static final String NAME = "Mock Editor 1";

	private IEditorInput input;
	private boolean dirty = false;
	private boolean saveNeeded = false;
	
	public MockEditorPart() {
		super();
	}
	
	/**
	 * @see IEditorPart#doSave(IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) {
		dirty = false;
		callTrace.add( "doSave" );
	}

	/**
	 * @see IEditorPart#doSaveAs()
	 */
	public void doSaveAs() {
	}

	/**
	 * @see IEditorPart#getEditorInput()
	 */
	public IEditorInput getEditorInput() {
		return input;
	}

	/**
	 * @see IEditorPart#getEditorSite()
	 */
	public IEditorSite getEditorSite() {
		return (IEditorSite)getSite();
	}

	/**
	 * @see IEditorPart#gotoMarker(IMarker)
	 */
	public void gotoMarker(IMarker marker) {
		callTrace.add( "gotoMarker" );	
	}

	/**
	 * @see IEditorPart#init(IEditorSite, IEditorInput)
	 */
	public void init(IEditorSite site, IEditorInput input)
		throws PartInitException {
		this.input = input;
		setSite(site);
		callTrace.add( "init" );
	}

	/**
	 * @see IEditorPart#isDirty()
	 */
	public boolean isDirty() {
		callTrace.add( "isDirty" );
		return dirty;
	}

	public void setDirty( boolean value )
	{
		dirty = value;	
	}

	/**
	 * @see IEditorPart#isSaveAsAllowed()
	 */
	public boolean isSaveAsAllowed() {
		return false;
	}

	/**
	 * @see IEditorPart#isSaveOnCloseNeeded()
	 */
	public boolean isSaveOnCloseNeeded() {
		callTrace.add( "isSaveOnCloseNeeded" );
		return saveNeeded;
	}
	
	public void setSaveNeeded( boolean value )
	{
		saveNeeded = value;
	}
}

