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
package org.eclipse.ui.internal.ide.dialogs;

import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.dialogs.WorkbenchPreferencePage;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IHelpContextIds;

/**
 * The IDE workbench main preference page.
 * 
 * @issue want IDE settings to appear in main Workbench preference page (via subclassing),
 *   however the superclass, WorkbenchPreferencePage, is internal
 */
public class IDEWorkbenchPreferencePage extends WorkbenchPreferencePage implements IWorkbenchPreferencePage {
	private Button autoBuildButton;
	private Button autoSaveAllButton;
	private Button refreshButton;
	private Button showTasks;
	private Button exitPromptButton;
	private IntegerFieldEditor saveInterval;

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage
	 */
	protected Control createContents(Composite parent) {

		WorkbenchHelp.setHelp(parent, IHelpContextIds.WORKBENCH_PREFERENCE_PAGE);

		Composite composite = createComposite(parent);

		createAutoBuildPref(composite);
		createSaveAllBeforeBuildPref(composite);
		createRefreshWorkspaceOnStartupPref(composite);
		createExitPromptPref(composite);
		createShowTasksOnAutoBuildPref(composite);
		createStickyCyclePref(composite);
		
		createSpace(composite);
		createSaveIntervalGroup(composite);
		
		createSpace(composite);
		createOpenModeGroup(composite);
		
		return composite;
	}

	protected void createShowTasksOnAutoBuildPref(Composite composite) {
		showTasks = new Button(composite, SWT.CHECK);
		showTasks.setText(IDEWorkbenchMessages.getString("WorkbenchPreference.showTasks")); //$NON-NLS-1$
		showTasks.setFont(composite.getFont());
		showTasks.setSelection(getIDEPreferenceStore().getBoolean(IDEInternalPreferences.SHOW_TASKS_ON_BUILD));
	}

	protected void createExitPromptPref(Composite composite) {
		exitPromptButton = new Button(composite, SWT.CHECK);
		exitPromptButton.setText(IDEWorkbenchMessages.getString("WorkbenchPreference.exitPromptButton")); //$NON-NLS-1$
		exitPromptButton.setFont(composite.getFont());
		exitPromptButton.setSelection(getIDEPreferenceStore().getBoolean(IDEInternalPreferences.EXIT_PROMPT_ON_CLOSE_LAST_WINDOW));
		
	}

	protected void createRefreshWorkspaceOnStartupPref(Composite composite) {
		refreshButton = new Button(composite, SWT.CHECK);
		refreshButton.setText(IDEWorkbenchMessages.getString("WorkbenchPreference.refreshButton")); //$NON-NLS-1$
		refreshButton.setFont(composite.getFont());
		refreshButton.setSelection(getIDEPreferenceStore().getBoolean(IDEInternalPreferences.REFRESH_WORKSPACE_ON_STARTUP));
	}

	protected void createSaveAllBeforeBuildPref(Composite composite) {
		autoSaveAllButton = new Button(composite, SWT.CHECK);
		autoSaveAllButton.setText(IDEWorkbenchMessages.getString("WorkbenchPreference.savePriorToBuilding")); //$NON-NLS-1$
		autoSaveAllButton.setFont(composite.getFont());
		autoSaveAllButton.setSelection(getIDEPreferenceStore().getBoolean(IDEInternalPreferences.SAVE_ALL_BEFORE_BUILD));
	}

	private void createAutoBuildPref(Composite composite) {
		autoBuildButton = new Button(composite, SWT.CHECK);
		autoBuildButton.setText(IDEWorkbenchMessages.getString("WorkbenchPreference.autobuild")); //$NON-NLS-1$
		autoBuildButton.setFont(composite.getFont());
		autoBuildButton.setSelection(ResourcesPlugin.getWorkspace().isAutoBuilding());
	}

	/**
	 * Create a composite that contains entry fields specifying save interval preference.
	 */
	private void createSaveIntervalGroup(Composite composite) {
		Composite groupComposite = new Composite(composite, SWT.LEFT);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		groupComposite.setLayout(layout);
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		groupComposite.setLayoutData(gd);	
		groupComposite.setFont(composite.getFont());
		
		saveInterval = new IntegerFieldEditor(IDEInternalPreferences.SAVE_INTERVAL, IDEWorkbenchMessages.getString("WorkbenchPreference.saveInterval"), groupComposite); //$NON-NLS-1$

		// @issue we should drop our preference constant and let clients use core's pref. ours is not up-to-date anyway if someone changes this interval directly thru core api.
		saveInterval.setPreferenceStore(getIDEPreferenceStore());
		saveInterval.setPreferencePage(this);
		saveInterval.setTextLimit(Integer.toString(IDEInternalPreferences.MAX_SAVE_INTERVAL).length());
		saveInterval.setErrorMessage(IDEWorkbenchMessages.format("WorkbenchPreference.saveIntervalError", new Object[] { new Integer(IDEInternalPreferences.MAX_SAVE_INTERVAL)})); //$NON-NLS-1$
		saveInterval.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
		saveInterval.setValidRange(1, IDEInternalPreferences.MAX_SAVE_INTERVAL);

		IWorkspaceDescription description = ResourcesPlugin.getWorkspace().getDescription();
		long interval = description.getSnapshotInterval() / 60000;
		saveInterval.setStringValue(Long.toString(interval));

		saveInterval.setPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(FieldEditor.IS_VALID)) 
					setValid(saveInterval.isValid());
			}
		});
		
	}	

	/**
	 * Returns the IDE preference store.
	 */
	protected IPreferenceStore getIDEPreferenceStore() {
		return IDEWorkbenchPlugin.getDefault().getPreferenceStore();
	}
	
	/**
	 * The default button has been pressed. 
	 */
	protected void performDefaults() {

		// core holds onto this preference.
		boolean autoBuild = ResourcesPlugin.getPlugin().getPluginPreferences().getDefaultBoolean(ResourcesPlugin.PREF_AUTO_BUILDING);
		autoBuildButton.setSelection(autoBuild);
		
		IPreferenceStore store = getIDEPreferenceStore();
		autoSaveAllButton.setSelection(store.getDefaultBoolean(IDEInternalPreferences.SAVE_ALL_BEFORE_BUILD));
		refreshButton.setSelection(store.getDefaultBoolean(IDEInternalPreferences.REFRESH_WORKSPACE_ON_STARTUP));
		exitPromptButton.setSelection(store.getDefaultBoolean(IDEInternalPreferences.EXIT_PROMPT_ON_CLOSE_LAST_WINDOW));
		showTasks.setSelection(store.getBoolean(IDEInternalPreferences.SHOW_TASKS_ON_BUILD));
		saveInterval.loadDefault();

		super.performDefaults();
	}

	/**
	 *	The user has pressed Ok.  Store/apply this page's values appropriately.
	 */
	public boolean performOk() {
		// set the workspace auto-build flag
		IWorkspaceDescription description = ResourcesPlugin.getWorkspace().getDescription();
		if (autoBuildButton.getSelection() != ResourcesPlugin.getWorkspace().isAutoBuilding()) {
			try {
				description.setAutoBuilding(autoBuildButton.getSelection());
				ResourcesPlugin.getWorkspace().setDescription(description);
			} catch (CoreException e) {
				IDEWorkbenchPlugin.log("Error changing auto build workspace setting.", e.getStatus()); //$NON-NLS-1$
			}
		}

		IPreferenceStore store = getIDEPreferenceStore();

		// store the save all prior to build setting
		store.setValue(IDEInternalPreferences.SAVE_ALL_BEFORE_BUILD, autoSaveAllButton.getSelection());

		// store the refresh workspace on startup setting
		store.setValue(IDEInternalPreferences.REFRESH_WORKSPACE_ON_STARTUP, refreshButton.getSelection());

		// store the exit prompt on last window close setting
		store.setValue(IDEInternalPreferences.EXIT_PROMPT_ON_CLOSE_LAST_WINDOW, exitPromptButton.getSelection());

		// store the preference for bringing task view to front on build
		store.setValue(IDEInternalPreferences.SHOW_TASKS_ON_BUILD, showTasks.getSelection());

		// store the workspace save interval
		// @issue we should drop our preference constant and let clients use core's pref. ours is not up-to-date anyway if someone changes this interval directly thru core api.
		long oldSaveInterval = description.getSnapshotInterval() / 60000;
		long newSaveInterval = new Long(saveInterval.getStringValue()).longValue();
		if (oldSaveInterval != newSaveInterval) {
			try {
				description.setSnapshotInterval(newSaveInterval * 60000);
				ResourcesPlugin.getWorkspace().setDescription(description);
				store.firePropertyChangeEvent(IDEInternalPreferences.SAVE_INTERVAL, new Integer((int)oldSaveInterval), new Integer((int)newSaveInterval));
			} catch (CoreException e) {
				IDEWorkbenchPlugin.log("Error changing save interval preference", e.getStatus()); //$NON-NLS-1$
			}
		}

		return super.performOk();
	}
}
