/*******************************************************************************
 * Copyright (c) 2008 Oakland Software Incorporated and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Oakland Software Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.navigator.filters.CommonFilterSelectionDialog;
import org.eclipse.ui.navigator.CommonViewer;

public class ExtensionsTest extends NavigatorTestBase {

	public ExtensionsTest() {
		_navigatorInstanceId = "org.eclipse.ui.tests.navigator.HideAvailableExtensionsTestView";
	}

	class CFDialog extends CommonFilterSelectionDialog {
		public CFDialog(CommonViewer aCommonViewer) {
			super(aCommonViewer);
		}

		public void finish() {
			okPressed();
			close();
		}

	}

	// Bug 185561 when hideAvailableExtensionsTab is true, everything gone
	public void testHideAvailableExtensions() throws Exception {
		assertEquals(1, _commonNavigator.getCommonViewer().getTree()
				.getItemCount());

		// Just showing the filters dialog upsets the apple cart
		CFDialog cfDialog = new CFDialog(_commonNavigator.getCommonViewer());
		cfDialog.create();
		cfDialog.finish();

		assertEquals(1, _commonNavigator.getCommonViewer().getTree()
				.getItemCount());

		if (false)
			DisplayHelper.sleep(Display.getCurrent(), 10000000);

	}

}
