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
package org.eclipse.ui.tests.compare;

import java.util.Iterator;

import junit.framework.TestCase;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;

import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.tests.dialogs.PreferenceDialogWrapper;
import org.eclipse.ui.tests.util.DialogCheck;

public class UIComparePreferencesAuto extends TestCase {

	public UIComparePreferencesAuto(String name) {
		super(name);
	}
	protected Shell getShell() {
		return DialogCheck.getShell();
	}

	private PreferenceDialog getPreferenceDialog(String id) {
		PreferenceDialogWrapper dialog = null;
		PreferenceManager manager = WorkbenchPlugin.getDefault().getPreferenceManager();
		if (manager != null) {
			dialog = new PreferenceDialogWrapper(getShell(), manager);
			dialog.create();
			WorkbenchHelp.setHelp(dialog.getShell(), IHelpContextIds.PREFERENCE_DIALOG);

			for (Iterator iterator = manager.getElements(PreferenceManager.PRE_ORDER).iterator(); iterator.hasNext();) {
				IPreferenceNode node = (IPreferenceNode) iterator.next();
				if (node.getId().equals(id)) {
					dialog.showPage(node);
					break;
				}
			}
		}
		return dialog;
	}

	public void testCompareViewersPref() {
		Dialog dialog = getPreferenceDialog("org.eclipse.compare.internal.ComparePreferencePage");
		DialogCheck.assertDialogTexts(dialog, this);
	}

}
