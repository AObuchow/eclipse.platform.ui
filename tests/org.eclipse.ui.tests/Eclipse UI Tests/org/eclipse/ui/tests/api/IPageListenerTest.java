package org.eclipse.ui.tests.api;

import junit.framework.*;
import org.eclipse.core.resources.*;
import org.eclipse.ui.*;

/**
 * Tests the IPageListener class.
 */
public class IPageListenerTest extends AbstractTestCase 
	implements IPageListener
{
	private IWorkbenchWindow fWindow;
	private IWorkspace fWorkspace;
	
	private int eventsReceived = 0;
	final private int OPEN = 0x01;
	final private int CLOSE = 0x02;
	final private int ACTIVATE = 0x04;
	private IWorkbenchPage pageMask;
	
	public IPageListenerTest(String testName) {
		super(testName);
	}

	public void setUp() {
		fWindow = openTestWindow();
		fWorkspace = ResourcesPlugin.getWorkspace();
		fWindow.addPageListener(this);
	}
	
	public void tearDown() {
		fWindow.removePageListener(this);
		super.tearDown();
	}

	/**
	 * Tests the pageOpened method.
	 */	
	public void testPageOpened() throws Throwable {
		// From Javadoc: "Notifies this listener that the given page has been opened."
		
		// Test open page.
		eventsReceived = 0;
		IWorkbenchPage page = fWindow.openPage(EmptyPerspective.PERSP_ID,
			fWorkspace);
		assertEquals(eventsReceived, OPEN|ACTIVATE);
		
		// Close page.
		page.close();
	}
	
	/**
	 * Tests the pageClosed method.
	 */	
	public void testPageClosed() throws Throwable {
		// From Javadoc: "Notifies this listener that the given page has been closed."
		
		// Open page.
		IWorkbenchPage page = fWindow.openPage(EmptyPerspective.PERSP_ID,
			fWorkspace);
			
		// Test close page.
		eventsReceived = 0;
		pageMask = page;
		page.close();
		assertEquals(eventsReceived, CLOSE);
	}
	
	/**
	 * Tests the pageActivated method.
	 */	
	public void testPageActivate() throws Throwable {
		// From Javadoc: "Notifies this listener that the given page has been activated."
		
		// Add pages.
		IWorkbenchPage page1 = fWindow.openPage(EmptyPerspective.PERSP_ID,
			fWorkspace);
		IWorkbenchPage page2 = fWindow.openPage(EmptyPerspective.PERSP_ID,
			fWorkspace);
		
		// Test activation of page 1.
		eventsReceived = 0;
		pageMask = page1;
		fWindow.setActivePage(page1);
		assertEquals(eventsReceived, ACTIVATE);

		// Test activation of page 2.
		eventsReceived = 0;		
		pageMask = page2;
		fWindow.setActivePage(page2);
		assertEquals(eventsReceived, ACTIVATE);
		
		// Cleanup.
		page1.close();
		page2.close();
	}
	
	/**
	 * @see IPageListener#pageActivated(IWorkbenchPage)
	 */
	public void pageActivated(IWorkbenchPage page) {
		if (pageMask == null || page == pageMask)
			eventsReceived |= ACTIVATE;
	}

	/**
	 * @see IPageListener#pageClosed(IWorkbenchPage)
	 */
	public void pageClosed(IWorkbenchPage page) {
		if (pageMask == null || page == pageMask)
			eventsReceived |= CLOSE;
	}

	/**
	 * @see IPageListener#pageOpened(IWorkbenchPage)
	 */
	public void pageOpened(IWorkbenchPage page) {
		if (pageMask == null || page == pageMask)
			eventsReceived |= OPEN;
	}

}