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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.jface.wizard.*;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;

import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.WorkingSetDescriptor;
import org.eclipse.ui.internal.registry.WorkingSetRegistry;
import org.eclipse.ui.tests.util.DialogCheck;
import org.eclipse.ui.tests.util.FileUtil;
import org.eclipse.ui.tests.util.UITestCase;

/**
 * Abstract test class for the working set wizard tests.
 */
public abstract class UIWorkingSetWizardsAuto extends UITestCase {
	protected static final int SIZING_WIZARD_WIDTH    = 470;
	protected static final int SIZING_WIZARD_HEIGHT   = 550;
	protected static final int SIZING_WIZARD_WIDTH_2  = 500;
	protected static final int SIZING_WIZARD_HEIGHT_2 = 500;
	protected static final String WORKING_SET_NAME_1 = "ws1";
	protected static final String WORKING_SET_NAME_2 = "ws2";
	
	protected WizardDialog fWizardDialog;
	protected Wizard fWizard;
	protected WorkingSetDescriptor[] fWorkingSetDescriptors;
	protected IProject p1;
	protected IProject p2;
	protected IFile f1;
	protected IFile f2;
	
	public UIWorkingSetWizardsAuto(String name) {
		super(name);
	}
	protected void checkTreeItems() {
		List widgets = getWidgets(fWizardDialog.getShell(), Tree.class);
		Tree tree = (Tree) widgets.get(0);
		TreeItem[] treeItems = tree.getItems();
		for (int i = 0; i < treeItems.length; i++) {
			treeItems[i].setChecked(true);
			Event event = new Event();
			event.detail = SWT.CHECK;
			event.item = treeItems[i];
			tree.notifyListeners(SWT.Selection, event);
		}
	}
	private void deleteResources() throws CoreException {
		if (p1 != null) {
			FileUtil.deleteProject(p1);
		}
		if (p2 != null) {
			FileUtil.deleteProject(p2);
		}
	}
	private Shell getShell() {
		return DialogCheck.getShell();
	}
	protected List getWidgets(Composite composite, Class clazz) {
		Widget[] children = composite.getChildren();
		List selectedChildren = new ArrayList();
		
		for (int i = 0; i < children.length; i++) {
			Widget child = children[i];
			if (child.getClass() == clazz) {
				selectedChildren.add(child);
			}
			if (child instanceof Composite) {
				selectedChildren.addAll(getWidgets((Composite) child, clazz));
			}
		}
		return selectedChildren;
	}
	/**
	 * <code>fWizard</code> must be initialized by subclasses prior to calling setUp.
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	
		fWizardDialog = new WizardDialog(getShell(), fWizard);
		fWizardDialog.create();
		Shell dialogShell = fWizardDialog.getShell();
		dialogShell.setSize(Math.max(SIZING_WIZARD_WIDTH_2, dialogShell.getSize().x), SIZING_WIZARD_HEIGHT_2);
		WorkbenchHelp.setHelp(fWizardDialog.getShell(), IHelpContextIds.WORKING_SET_NEW_WIZARD);
		
		WorkingSetRegistry registry = WorkbenchPlugin.getDefault().getWorkingSetRegistry();
		fWorkingSetDescriptors = registry.getWorkingSetDescriptors();
		
		IWorkingSetManager workingSetManager = fWorkbench.getWorkingSetManager();
		IWorkingSet[] workingSets = workingSetManager.getWorkingSets();
		for (int i = 0; i < workingSets.length; i++) {
			workingSetManager.removeWorkingSet(workingSets[i]);
		}		
		setupResources();
	}
	private void setupResources() throws CoreException {
		p1 = FileUtil.createProject("TP1");
		p2 = FileUtil.createProject("TP2");
		f1 = FileUtil.createFile("f1.txt", p1);
		f2 = FileUtil.createFile("f2.txt", p2);
	}
	protected void setTextWidgetText(String text,IWizardPage page) {
		List widgets = getWidgets((Composite) page.getControl(), Text.class);
		Text textWidget = (Text) widgets.get(0);
		textWidget.setText(text);
		textWidget.notifyListeners(SWT.Modify, new Event());
	}
	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		deleteResources();
		super.tearDown();
	}

}

