package org.eclipse.ui.tests.api;

import org.eclipse.ui.*;

public class IViewSiteTest extends IWorkbenchPartSiteTest {

	/**
	 * Constructor for IViewPartSiteTest
	 */
	public IViewSiteTest(String testName) {
		super(testName);
	}

	/**
	 * @see IWorkbenchPartSiteTest#getTestPartName()
	 */
	protected String getTestPartName() throws Throwable {
		return MockViewPart.NAME;
	}

	/**
	 * @see IWorkbenchPartSiteTest#getTestPartId()
	 */
	protected String getTestPartId() throws Throwable {
		return MockViewPart.ID;
	}

	/**
	 * @see IWorkbenchPartSiteTest#createTestPart(IWorkbenchPage)
	 */
	protected IWorkbenchPart createTestPart(IWorkbenchPage page) throws Throwable {
		return page.showView(MockViewPart.ID);
	}
	
	public void testGetActionBars() throws Throwable {
		// From Javadoc: "Returns the action bars for this part site."
		
		IViewPart view = (IViewPart) createTestPart(fPage);
		IViewSite site = view.getViewSite();
		assertNotNull(site.getActionBars());
	}

}

