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

package org.eclipse.ui.views.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.views.markers.internal.ActionProblemProperties;
import org.eclipse.ui.views.markers.internal.ActionResolveMarker;
import org.eclipse.ui.views.markers.internal.ActionShowOnBuild;
import org.eclipse.ui.views.markers.internal.DialogProblemFilter;
import org.eclipse.ui.views.markers.internal.FieldCreationTime;
import org.eclipse.ui.views.markers.internal.FieldFolder;
import org.eclipse.ui.views.markers.internal.FieldLineNumber;
import org.eclipse.ui.views.markers.internal.FieldMessage;
import org.eclipse.ui.views.markers.internal.FieldResource;
import org.eclipse.ui.views.markers.internal.FieldSeverity;
import org.eclipse.ui.views.markers.internal.IField;
import org.eclipse.ui.views.markers.internal.IFilter;
import org.eclipse.ui.views.markers.internal.MarkerRegistry;
import org.eclipse.ui.views.markers.internal.MarkerView;
import org.eclipse.ui.views.markers.internal.ProblemFilter;

public class ProblemView extends MarkerView {
	
	private final static ColumnLayoutData[] DEFAULT_COLUMN_LAYOUTS = { 
		new ColumnPixelData(19, false), 
		new ColumnWeightData(200), 
		new ColumnWeightData(75), 
		new ColumnWeightData(150), 
		new ColumnWeightData(60) 
	};
		
	private final static IField[] HIDDEN_FIELDS = { 
		new FieldCreationTime() 
	};
	
	private final static String[] ROOT_TYPES = { 
		IMarker.PROBLEM 
	};
	
	private final static String TAG_DIALOG_SECTION = "org.eclipse.ui.views.problem"; //$NON-NLS-1$
	
	private final static IField[] VISIBLE_FIELDS = { 
		new FieldSeverity(), 
		new FieldMessage(), 
		new FieldResource(), 
		new FieldFolder(), 
		new FieldLineNumber() 
	};
	
	private MarkerRegistry markerRegistry;
	private ProblemFilter problemFilter;
	private ActionResolveMarker resolveMarkerAction;

	public void dispose() {
		if (resolveMarkerAction != null)
			resolveMarkerAction.dispose();
		
		super.dispose();
	}

	public void init(IViewSite viewSite, IMemento memento) throws PartInitException {
		super.init(viewSite, memento);
		problemFilter = new ProblemFilter();
		IDialogSettings dialogSettings = getDialogSettings();
		
		if (problemFilter != null)
			problemFilter.restoreState(dialogSettings);
			
		markerRegistry = new MarkerRegistry();
		markerRegistry.setType(IMarker.PROBLEM); 		
		markerRegistry.setFilter(problemFilter);
		markerRegistry.setInput((IResource) getViewerInput());
	}

	public void saveState(IMemento memento) {
		IDialogSettings dialogSettings = getDialogSettings();
		
		if (problemFilter != null)
			problemFilter.saveState(dialogSettings);
		
		super.saveState(memento);	
	}

	protected ColumnLayoutData[] getDefaultColumnLayouts() {
		return DEFAULT_COLUMN_LAYOUTS;
	}

	protected IDialogSettings getDialogSettings() {
		AbstractUIPlugin plugin = (AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
		IDialogSettings workbenchSettings = plugin.getDialogSettings();
		IDialogSettings settings = workbenchSettings.getSection(TAG_DIALOG_SECTION);
		
		if (settings == null)
			settings = workbenchSettings.addNewSection(TAG_DIALOG_SECTION);

		return settings;
	}

	protected void createActions() {
		super.createActions();
		propertiesAction = new ActionProblemProperties(this, getViewer());
		resolveMarkerAction = new ActionResolveMarker(this, getViewer());
	}
	
	protected void createColumns(Table table) {
		super.createColumns(table);
		TableColumn[] columns = table.getColumns();
		
		if (columns != null && columns.length >= 1)
			columns[0].setResizable(false);
	}

	protected void fillContextMenuAdditions(IMenuManager manager) {
		manager.add(new Separator());
		manager.add(resolveMarkerAction);
	}

	protected IFilter getFilter() {
		return problemFilter;
	}
	
	protected Dialog getFiltersDialog() {
		return new DialogProblemFilter(getSite().getShell(), problemFilter);
	}
	
	protected IField[] getHiddenFields() {
		return HIDDEN_FIELDS;
	}

	protected MarkerRegistry getRegistry() {
		return markerRegistry;
	}

	protected String[] getRootTypes() {
		return ROOT_TYPES;
	}

	protected Object getViewerInput() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
	
	protected IField[] getVisibleFields() {
		return VISIBLE_FIELDS;
	}
	
	protected void initActionBars(IActionBars actionBars) {
		super.initActionBars(actionBars);
		IMenuManager menu = actionBars.getMenuManager();
		menu.add(new Separator());
		menu.add(new ActionShowOnBuild());
	}
	
	public IStructuredSelection getSelection() {
		// TODO: added because nick doesn't like public API inherited from internal classes
		return super.getSelection();
	}

	public void setSelection(IStructuredSelection structuredSelection, boolean reveal) {
		// TODO: added because nick doesn't like public API inherited from internal classes
		super.setSelection(structuredSelection, reveal);
	}
}
