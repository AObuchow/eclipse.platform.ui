/*******************************************************************************
 * Copyright (c) 2008, 2009 Oakland Software Incorporated and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Oakland Software Incorporated - initial API and implementation
 *.....IBM Corporation - fixed dead code warning
 *     Fair Issac Corp - bug 287103 - NCSLabelProvider does not properly handle overrides 
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.tests.navigator.extension.TestLabelProvider;
import org.eclipse.ui.tests.navigator.extension.TestLabelProviderBlank;
import org.eclipse.ui.tests.navigator.extension.TestLabelProviderCyan;
import org.eclipse.ui.tests.navigator.extension.TestLabelProviderGreen;
import org.eclipse.ui.tests.navigator.extension.TrackingLabelProvider;

public class LabelProviderTest extends NavigatorTestBase {

	public LabelProviderTest() {
		_navigatorInstanceId = "org.eclipse.ui.tests.navigator.OverrideTestView";
	}

	// Backed out this test because the blank label provider was actually
	// used to signify no label content see bug 268250
	public void XXXtestBlankLabelProvider() throws Exception {

		TestLabelProvider._blankStatic = true;

		_contentService.bindExtensions(new String[] { TEST_CONTENT_OVERRIDDEN1,
				TEST_CONTENT_OVERRIDE1 }, false);
		_contentService.getActivationService()
				.activateExtensions(
						new String[] { TEST_CONTENT_OVERRIDE1,
								TEST_CONTENT_OVERRIDDEN1 }, true);

		refreshViewer();

		TreeItem[] rootItems = _viewer.getTree().getItems();

		if (!rootItems[0].getText().equals(""))
			fail("Wrong text: " + rootItems[0].getText());
	}

	private void checkItemsAll(TreeItem[] rootItems, TestLabelProvider tlp) {
		if (!rootItems[0].getText().startsWith(tlp.getColorName()))
			fail("Wrong text: " + rootItems[0].getText());
		checkItems(rootItems, tlp);
	}
	
	private void checkItems(TreeItem[] rootItems, TestLabelProvider tlp) {
		assertEquals(tlp.backgroundColor, rootItems[0]
				.getBackground(0));
		assertEquals(tlp.backgroundColor, rootItems[0]
				.getForeground(0));
		assertEquals(tlp.font, rootItems[0].getFont(0));
		assertEquals(tlp.image, rootItems[0].getImage(0));
	}
	
	// bug 252293 [CommonNavigator] LabelProviders do not obey override rules
	public void testSimpleResFirst() throws Exception {

		_contentService.bindExtensions(new String[] { TEST_CONTENT_OVERRIDDEN1,
				TEST_CONTENT_OVERRIDE1 }, false);
		_contentService.getActivationService()
				.activateExtensions(
						new String[] { TEST_CONTENT_OVERRIDE1,
								TEST_CONTENT_OVERRIDDEN1 }, true);

		refreshViewer();

		TreeItem[] rootItems = _viewer.getTree().getItems();
		checkItemsAll(rootItems, TestLabelProviderGreen.instance);
	}
	
	/**
	 * E{low} overrides D{low} overrides B{normal} overrides A
	 * F{high} overrides C{low} overrides A
	 * G{normal} overrides C{low}
	 * B branch has higher priority than C's, and G higher than G, so
	 * order should be B branch (E - D - B) then C branches (F - G - C) then A
	 */
	public void testOverrideChain() throws Exception {
		final String[] EXTENSIONS = new String[] { 
				TEST_CONTENT_TRACKING_LABEL + ".A",
				TEST_CONTENT_TRACKING_LABEL + ".B",
				TEST_CONTENT_TRACKING_LABEL + ".C",
				TEST_CONTENT_TRACKING_LABEL + ".D",
				TEST_CONTENT_TRACKING_LABEL + ".E",
				TEST_CONTENT_TRACKING_LABEL + ".F",
				TEST_CONTENT_TRACKING_LABEL + ".G" };
		_contentService.bindExtensions(EXTENSIONS, true);
		_contentService.getActivationService().activateExtensions(EXTENSIONS, true);

		refreshViewer();
		_viewer.getTree().getItems();
		
		TrackingLabelProvider.resetQueries();

		// Time for the decorating label provider to settle down
		DisplayHelper.sleep(200);
		
		refreshViewer();

		// The label provider (sync runs) and then the decorating label provider runs (in something like every 100ms)
		// Give time for both and expect both to have happened.
		DisplayHelper.sleep(200);

		final String EXPECTED = "EDBFGCA";
		if (false)
			System.out.println("Map: " + TrackingLabelProvider.styledTextQueries);
		String queries = (String)TrackingLabelProvider.styledTextQueries.get(_project);
		// This can happen multiple times depending on when the decorating label provider
		// runs, so just make sure the sequence is right
		assertTrue("Wrong query order for text", queries.startsWith(EXPECTED));
	}
	

	// bug 252293 [CommonNavigator] LabelProviders do not obey override rules
	public void testSimpleResLast() throws Exception {
		_contentService.bindExtensions(new String[] { TEST_CONTENT_OVERRIDDEN2,
				TEST_CONTENT_OVERRIDE2 }, false);
		_contentService.getActivationService()
				.activateExtensions(
						new String[] { TEST_CONTENT_OVERRIDDEN2,
								TEST_CONTENT_OVERRIDE2 }, true);

		refreshViewer();

		TreeItem[] rootItems = _viewer.getTree().getItems();
		checkItemsAll(rootItems, TestLabelProviderCyan.instance);
	}

	// Make sure that it finds label providers that are in overridden content
	// extensions
	// if none of the label providers from the desired content extensions return
	// anything
	public void testUsingOverriddenLabelProvider() throws Exception {

		_contentService.bindExtensions(new String[] { TEST_CONTENT_OVERRIDDEN2,
				TEST_CONTENT_OVERRIDE2_BLANK }, true);
		_contentService.getActivationService().activateExtensions(
				new String[] { TEST_CONTENT_OVERRIDDEN2,
						TEST_CONTENT_OVERRIDE2_BLANK }, true);

		refreshViewer();

		TreeItem[] rootItems = _viewer.getTree().getItems();

		// DisplayHelper.sleep(10000000);

		// But we get the text from the overridden label provider
		if (!rootItems[0].getText().startsWith("Blue"))
			fail("Wrong text: " + rootItems[0].getText());

		// We get the everything else from the blank label provider
		checkItems(rootItems, TestLabelProviderBlank.instance);
	}

}
