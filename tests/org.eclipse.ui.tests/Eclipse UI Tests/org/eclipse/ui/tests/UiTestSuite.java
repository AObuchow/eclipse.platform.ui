/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.ui.tests.activities.ActivitiesTestSuite;
import org.eclipse.ui.tests.adaptable.AdaptableTestSuite;
import org.eclipse.ui.tests.api.ApiTestSuite;
import org.eclipse.ui.tests.commands.CommandsTestSuite;
import org.eclipse.ui.tests.contexts.ContextsTestSuite;
import org.eclipse.ui.tests.datatransfer.DataTransferTestSuite;
import org.eclipse.ui.tests.deadlock.NestedSyncExecDeadlockTest;
import org.eclipse.ui.tests.dialogs.UIAutomatedSuite;
import org.eclipse.ui.tests.dnd.DragTestSuite;
import org.eclipse.ui.tests.dynamicplugins.DynamicPluginsTestSuite;
import org.eclipse.ui.tests.internal.InternalTestSuite;
import org.eclipse.ui.tests.intro.IntroTestSuite;
import org.eclipse.ui.tests.keys.KeysTestSuite;
import org.eclipse.ui.tests.menus.MenusTestSuite;
import org.eclipse.ui.tests.multipageeditor.MultiPageEditorTestSuite;
import org.eclipse.ui.tests.navigator.NavigatorTestSuite;
import org.eclipse.ui.tests.preferences.PreferencesTestSuite;
import org.eclipse.ui.tests.propertysheet.PropertySheetTestSuite;
import org.eclipse.ui.tests.themes.ThemesTestSuite;
import org.eclipse.ui.tests.util.PlatformUtil;
import org.eclipse.ui.tests.zoom.ZoomTestSuite;

/**
 * Test all areas of the UI.
 */
public class UiTestSuite extends TestSuite {

    /**
     * Returns the suite. This is required to use the JUnit Launcher.
     */
    public static Test suite() {
        return new UiTestSuite();
    }

    /**
     * Construct the test suite.
     */
    public UiTestSuite() {
        addTest(new ApiTestSuite());

        if (!PlatformUtil.onLinux()) {
            addTest(new UIAutomatedSuite());
        }

        addTest(new PropertySheetTestSuite());
        addTest(new InternalTestSuite());
        addTest(new NavigatorTestSuite());
        addTest(new AdaptableTestSuite());
        addTest(new ZoomTestSuite());
        addTest(new DataTransferTestSuite());
        addTest(new PreferencesTestSuite());
        addTest(new KeysTestSuite());
        addTest(new MultiPageEditorTestSuite());
        addTest(new DynamicPluginsTestSuite());
        addTest(new ActivitiesTestSuite());
        addTest(new CommandsTestSuite());
        addTest(new ContextsTestSuite());
        addTest(new DragTestSuite());
        addTest(new ThemesTestSuite());
        addTest(new IntroTestSuite());
        addTest(new MenusTestSuite());
        addTest(new TestSuite(NestedSyncExecDeadlockTest.class));
    }
}
