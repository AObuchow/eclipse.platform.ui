package org.eclipse.ui.tests.api;

import junit.framework.*;
import org.eclipse.core.resources.*;
import org.eclipse.ui.*;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.internal.*;

/**
 * Tests the IPageService class.
 */
public class IPageServiceTest extends AbstractTestCase 
	implements IPageListener, org.eclipse.ui.IPerspectiveListener
{
	private IWorkbenchWindow fWindow;
	private IWorkspace fWorkspace;
	
	private boolean pageEventReceived;
	private boolean perspEventReceived;
	
	public IPageServiceTest(String testName) {
		super(testName);
	}

	protected void setUp() {
		fWindow = openTestWindow();
		fWorkspace = ResourcesPlugin.getWorkspace();
	}
	
	/**
	 * Tests the addPageListener method.
	 */	
	public void testAddPageListener() throws Throwable {
		// From Javadoc: "Adds the given listener for page lifecycle events.
		// Has no effect if an identical listener is already registered."
		
		// Add listener.
		fWindow.addPageListener(this);
		
		// Open and close page.
		// Verify events are received.
		pageEventReceived = false;
		IWorkbenchPage page = fWindow.openPage(EmptyPerspective.PERSP_ID,
			fWorkspace);
		page.close();
		assert(pageEventReceived);
		
		// Remove listener.	
		fWindow.removePageListener(this);		
	}
	
	/**
	 * Tests the removePageListener method.
	 */
	public void testRemovePageListener() throws Throwable {
		// From Javadoc: "Removes the given page listener.
		// Has no affect if an identical listener is not registered."
		
		// Add and remove listener.
		fWindow.addPageListener(this);
		fWindow.removePageListener(this);		
		
		// Open and close page.
		// Verify no events are received.
		pageEventReceived = false;
		IWorkbenchPage page = fWindow.openPage(EmptyPerspective.PERSP_ID,
			fWorkspace);
		page.close();
		assert(!pageEventReceived);
	}
	
	/**
	 * Tests getActivePage.
	 */
	public void testGetActivePage() throws Throwable {
		// From Javadoc: "return the active page, or null if no page 
		// is currently active"
		
		// Add page.
		IWorkbenchPage page1 = fWindow.openPage(EmptyPerspective.PERSP_ID,
			fWorkspace);
		assertEquals(fWindow.getActivePage(), page1);
		
		// Add second page.
		IWorkbenchPage page2 = fWindow.openPage(EmptyPerspective.PERSP_ID,
			fWorkspace);
		assertEquals(fWindow.getActivePage(), page2);
		
		// Set active page.
		fWindow.setActivePage(page1);
		assertEquals(fWindow.getActivePage(), page1);
		fWindow.setActivePage(page2);
		assertEquals(fWindow.getActivePage(), page2);
		
		// Cleanup.
		page1.close();
		page2.close();
	}
	
	/**
	 * Tests the addPerspectiveListener method.
	 */	
	public void testAddPerspectiveListener() throws Throwable {
		// From Javadoc: "Adds the given listener for a page's perspective lifecycle events.
		// Has no effect if an identical listener is already registered."
		
		// Add listener.
		fWindow.addPerspectiveListener(this);
		
		// Open page and change persp feature.
		// Verify events are received.
		perspEventReceived = false;
		IWorkbenchPage page = fWindow.openPage(IWorkbenchConstants.DEFAULT_LAYOUT_ID,
			fWorkspace);
		page.setEditorAreaVisible(false);
		page.setEditorAreaVisible(true);
		page.close();
		assert(perspEventReceived);
		
		// Remove listener.	
		fWindow.removePerspectiveListener(this);		
	}
	
	/**
	 * Tests the removePerspectiveListener method.
	 */	
	public void testRemovePerspectiveListener() throws Throwable {
		// From Javadoc: "Removes the given page's perspective listener.
		// Has no affect if an identical listener is not registered."
		
		// Add and remove listener.
		fWindow.addPerspectiveListener(this);
		fWindow.removePerspectiveListener(this);		
		
		// Open page and change persp feature.
		// Verify no events are received.
		perspEventReceived = false;
		IWorkbenchPage page = fWindow.openPage(IWorkbenchConstants.DEFAULT_LAYOUT_ID,
			fWorkspace);
		page.setEditorAreaVisible(false);
		page.setEditorAreaVisible(true);
		page.close();
		assert(!perspEventReceived);
	}
	
	/**
	 * @see IPageListener#pageActivated(IWorkbenchPage)
	 */
	public void pageActivated(IWorkbenchPage page) {
		pageEventReceived = true;
	}

	/**
	 * @see IPageListener#pageClosed(IWorkbenchPage)
	 */
	public void pageClosed(IWorkbenchPage page) {
		pageEventReceived = true;
	}

	/**
	 * @see IPageListener#pageOpened(IWorkbenchPage)
	 */
	public void pageOpened(IWorkbenchPage page) {
		pageEventReceived = true;
	}

	/**
	 * @see IPerspectiveListener#perspectiveActivated(IWorkbenchPage, IPerspectiveDescriptor)
	 */
	public void perspectiveActivated(IWorkbenchPage page,
		IPerspectiveDescriptor perspective) 
	{
		perspEventReceived = true;
	}

	/**
	 * @see IPerspectiveListener#perspectiveChanged(IWorkbenchPage, IPerspectiveDescriptor, String)
	 */
	public void perspectiveChanged(IWorkbenchPage page,
		IPerspectiveDescriptor perspective, String changeId) 
	{
		perspEventReceived = true;
	}

}