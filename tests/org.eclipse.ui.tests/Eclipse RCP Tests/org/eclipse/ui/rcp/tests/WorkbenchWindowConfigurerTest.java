/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.rcp.tests;

import junit.framework.TestCase;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.rcp.tests.util.WorkbenchAdvisorObserver;

public class WorkbenchWindowConfigurerTest extends TestCase {

    public WorkbenchWindowConfigurerTest(String name) {
        super(name);
    }

    private Display display = null;

    protected void setUp() throws Exception {
        super.setUp();

        assertNull(display);
        display = PlatformUI.createDisplay();
        assertNotNull(display);
    }

    protected void tearDown() throws Exception {
        assertNotNull(display);
        display.dispose();
        assertTrue(display.isDisposed());

        super.tearDown();
    }

    public void testDefaults() {
        WorkbenchAdvisor wa = new WorkbenchAdvisorObserver(1) {

            public void fillActionBars(IWorkbenchWindow window,
                    IActionBarConfigurer actionBarConfig, int flags) {
                super.fillActionBars(window, actionBarConfig, flags);

                String tempTitle = "title"; //$NON-NLS-1$

                IWorkbenchWindowConfigurer windowConfig = workbenchConfig
                        .getWindowConfigurer(window);
                assertNotNull(windowConfig);

                assertEquals(window, windowConfig.getWindow());
                assertEquals(workbenchConfig, windowConfig
                        .getWorkbenchConfigurer());
                assertEquals(actionBarConfig, windowConfig
                        .getActionBarConfigurer());
                assertNull(windowConfig.getTitle());
                assertTrue(windowConfig.getShowCoolBar());
                assertTrue(windowConfig.getShowMenuBar());
                assertFalse(windowConfig.getShowPerspectiveBar());
                assertTrue(windowConfig.getShowStatusLine());

                windowConfig.setTitle(tempTitle);
                windowConfig.setShowCoolBar(false);
                windowConfig.setShowMenuBar(false);
                windowConfig.setShowPerspectiveBar(true);
                windowConfig.setShowStatusLine(false);
                assertEquals(tempTitle, windowConfig.getTitle());
                assertFalse(windowConfig.getShowCoolBar());
                assertFalse(windowConfig.getShowMenuBar());
                assertTrue(windowConfig.getShowPerspectiveBar());
                assertFalse(windowConfig.getShowStatusLine());

                // *** title is orginally null, but cannot set it back to null,
                // should that
                //     check be allowed?
                windowConfig.setTitle("");//$NON-NLS-1$
                windowConfig.setShowCoolBar(true);
                windowConfig.setShowMenuBar(true);
                windowConfig.setShowPerspectiveBar(false);
                windowConfig.setShowStatusLine(true);
            }
        };

        int code = PlatformUI.createAndRunWorkbench(display, wa);
        assertEquals(PlatformUI.RETURN_OK, code);
    }
}
