/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dialogs;

import org.eclipse.ui.tests.compare.UIComparePreferencesAuto;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Test all areas of the UI.
 */
public class UIAutomatedSuite extends TestSuite {

	/**
	 * Returns the suite.  This is required to
	 * use the JUnit Launcher.
	 */
	public static Test suite() {
		return new UIAutomatedSuite();
	}
	
	/**
	 * Construct the test suite.
	 */
	public UIAutomatedSuite() {
		addTest(new TestSuite(UIDialogsAuto.class));
		addTest(new TestSuite(DeprecatedUIDialogsAuto.class));
		addTest(new TestSuite(UIWizardsAuto.class));
		addTest(new TestSuite(DeprecatedUIWizardsAuto.class));
		addTest(new TestSuite(UIPreferencesAuto.class));
		addTest(new TestSuite(UIComparePreferencesAuto.class));
		addTest(new TestSuite(DeprecatedUIPreferencesAuto.class));
		addTest(new TestSuite(UIMessageDialogsAuto.class));
		addTest(new TestSuite(UINewWorkingSetWizardAuto.class));
		addTest(new TestSuite(UIEditWorkingSetWizardAuto.class));		
	}
}