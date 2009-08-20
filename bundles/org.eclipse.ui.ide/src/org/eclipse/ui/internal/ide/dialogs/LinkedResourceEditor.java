/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Serge Beauchamp (Freescale Semiconductor) - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.ide.dialogs;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * UI to edit the location of the linked resources contained in a project.
 * @since 3.5
 * 
 */
public class LinkedResourceEditor {

	
	private static int NAME_COLUMN = 0;
	private static int PATH_COLUMN = -1;
	private static int LOCATION_COLUMN = 1;
	
	/**
	 * 
	 */
	public LinkedResourceEditor() {
		absoluteImg = IDEWorkbenchPlugin.getIDEImageDescriptor(
				"obj16/warning.gif").createImage(); //$NON-NLS-1$
		brokenImg = IDEWorkbenchPlugin.getIDEImageDescriptor(
				"obj16/error_tsk.gif").createImage(); //$NON-NLS-1$
		fixedImg = IDEWorkbenchPlugin
				.getIDEImageDescriptor("obj16/folder.gif").createImage(); //$NON-NLS-1$

		FIXED = IDEWorkbenchMessages.LinkedResourceEditor_fixed;
		BROKEN = IDEWorkbenchMessages.LinkedResourceEditor_broken;
		ABSOLUTE = IDEWorkbenchMessages.LinkedResourceEditor_absolute;
	}

	/**
	 * @param project
	 */
	public void setProject(IProject project) {
		fProject = project;
	}

	protected void createButtons(Composite parent) {
		Composite buttonParent = new Composite(parent, 0);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 4;
		layout.marginWidth = 0;
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		buttonParent.setLayout(layout);
		buttonParent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		fEditResourceButton = createButton(buttonParent,
				IDEWorkbenchMessages.LinkedResourceEditor_editLinkedLocation);
		fEditResourceButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				editLocation();
			}
		});

		fConvertAbsoluteButton = createButton(buttonParent,
				IDEWorkbenchMessages.LinkedResourceEditor_convertToVariableLocation);
		fConvertAbsoluteButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				convertLocation();
			}
		});

		Label l = new Label(buttonParent, SWT.NONE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;

		l.setLayoutData(data);
		layout = (GridLayout) parent.getLayout();
		layout.numColumns++;
		layout.makeColumnsEqualWidth = false;

		updateSelection();
	}

	/**
	 * @param parent
	 * @param text
	 * @return the new button
	 */
	private Button createButton(Composite parent, String text) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(text);
		return button;
	}

	/**
	 * Creates the widget group. Callers must call <code>dispose</code> when the
	 * group is no longer needed.
	 * 
	 * @param composite
	 *            the widget parent
	 * @return container of the widgets
	 */
	public Control createContents(Composite composite) {
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 1;
		layout.verticalSpacing = 9;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		fTree = new TreeViewer(composite, SWT.MULTI | SWT.FULL_SELECTION);

		fTree.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateSelection();
			}
		});

		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = fTree.getTree().getItemHeight() * 10;
		data.horizontalSpan = 5;
		fTree.getTree().setLayoutData(data);
		fTree.getTree().setLinesVisible(true);

		fTree.setContentProvider(new ContentProvider());
		fTree.setLabelProvider(new LabelProvider());
		fTree.setInput(this);

		TreeColumn column = new TreeColumn(fTree.getTree(), SWT.LEFT, NAME_COLUMN);
		column.setText(IDEWorkbenchMessages.LinkedResourceEditor_resourceName);
		column.setResizable(true);
		column.setMoveable(false);
		column.setWidth(200);

		column = new TreeColumn(fTree.getTree(), SWT.LEFT, LOCATION_COLUMN);
		column.setText(IDEWorkbenchMessages.LinkedResourceEditor_location);
		column.setResizable(true);
		column.setMoveable(false);
		column.setWidth(300);

		fTree.getTree().setHeaderVisible(true);
		createButtons(composite);

		return composite;
	}

	/**
 * 
 */
	public void dispose() {
		fixedImg.dispose();
		brokenImg.dispose();
		absoluteImg.dispose();
	}

	class LabelProvider implements ILabelProvider,
			ITableLabelProvider {

		WorkbenchLabelProvider stockProvider = new WorkbenchLabelProvider();
		public LabelProvider() {
			super();
		}

		public String getColumnText(Object obj, int index) {
			if (obj instanceof IResource) {
				if (index == NAME_COLUMN)
					return ((IResource) obj).getName();
				else if (index == PATH_COLUMN)
					return ((IResource) obj).getParent()
							.getProjectRelativePath().toPortableString();
				else {
					IPath rawLocation = ((IResource) obj).getRawLocation();
					if (rawLocation != null)
						return rawLocation.toPortableString();
				}
			} else if ((obj instanceof String) && index == 0)
				return (String) obj;
			return null;
		}

		public Image getColumnImage(Object obj, int index) {
			if (index == NAME_COLUMN) {
				if (obj instanceof String) {
					if (obj.equals(BROKEN))
						return brokenImg;
					if (obj.equals(ABSOLUTE))
						return absoluteImg;
					return fixedImg;
				}
				return stockProvider.getImage(obj);
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
		 */
		public void addListener(ILabelProviderListener listener) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
		 */
		public void dispose() {
			stockProvider.dispose();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
		 */
		public boolean isLabelProperty(Object element, String property) {
			return stockProvider.isLabelProperty(element, property);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
		 */
		public void removeListener(ILabelProviderListener listener) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
		 */
		public Image getImage(Object element) {
			return getColumnImage(element, 0);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
		 */
		public String getText(Object element) {
			return getColumnText(element, 0);
		}
	}

	class ContentProvider implements IContentProvider, ITreeContentProvider {

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof LinkedResourceEditor) {
				return new Object[] { BROKEN, ABSOLUTE, FIXED };
			} else if (parentElement instanceof String) {
				if (((String) parentElement).equals(BROKEN))
					return fBrokenResources.values().toArray();
				if (((String) parentElement).equals(ABSOLUTE))
					return fAbsoluteResources.values().toArray();
				return fFixedResources.values().toArray();
			}
			return null;
		}

		public Object getParent(Object element) {
			if (element instanceof IResource) {
				String fullPath = ((IResource) element).getFullPath()
						.toPortableString();
				if (fBrokenResources.containsKey(fullPath))
					return BROKEN;
				if (fAbsoluteResources.containsKey(fullPath))
					return ABSOLUTE;
				return FIXED;
			} else if (element instanceof String)
				return this;
			return null;
		}

		public boolean hasChildren(Object element) {
			if (element instanceof LinkedResourceEditor) {
				return true;
			} else if (element instanceof String) {
				if (((String) element).equals(BROKEN))
					return !fBrokenResources.isEmpty();
				if (((String) element).equals(ABSOLUTE))
					return !fAbsoluteResources.isEmpty();
				return !fFixedResources.isEmpty();
			}
			return false;
		}

		public Object[] getElements(Object parentElement) {
			if (parentElement instanceof LinkedResourceEditor) {
				return new Object[] { BROKEN, ABSOLUTE, FIXED };
			} else if (parentElement instanceof String) {
				if (((String) parentElement).equals(BROKEN))
					return fBrokenResources.values().toArray();
				if (((String) parentElement).equals(ABSOLUTE))
					return fAbsoluteResources.values().toArray();
				return fFixedResources.values().toArray();
			}
			return null;
		}
	}

	void refreshContent() {
		if (fProjectFiles == null) {
			final LinkedList/* <IResource> */resources = new LinkedList/*
																		 * <IResource
																		 * >
																		 */();
			try {
				fProject.accept(new IResourceVisitor() {
					/**
					 * @throws CoreException
					 */
					public boolean visit(IResource resource)
							throws CoreException {
						if (resource.isLinked() && !resource.isGroup())
							resources.add(resource);
						return true;
					}
				});
			} catch (CoreException e) {
			}
			fProjectFiles = (IResource[]) resources.toArray(new IResource[0]);
		}
		fBrokenResources = new TreeMap/* <String, IResource> */();
		fFixedResources = new TreeMap/* <String, IResource> */();
		fAbsoluteResources = new TreeMap/* <String, IResource> */();
		for (int i = 0; i < fProjectFiles.length; i++) {
			IResource resource = fProjectFiles[i];
			String fullPath = resource.getFullPath().toPortableString();
			try {
				if (exists(resource)) {
					if (isAbsolute(resource))
						fAbsoluteResources.put(fullPath, resource);
					else
						fFixedResources.put(fullPath, resource);
				} else
					fBrokenResources.put(fullPath, resource);
			} catch (CoreException e) {
				fBrokenResources.put(fullPath, resource);
			}
		}
	}

	boolean isAbsolute(IResource res) {
		IPath path = res.getRawLocation();
		return path != null && path.isAbsolute();
	}

	boolean areAbsolute(IResource[] res) {
		for (int i = 0; i < res.length; i++) {
			if (!isAbsolute(res[i]))
				return false;
		}
		return true;
	}

	boolean exists(IResource res) throws CoreException {
		URI uri = res.getLocationURI();
		if (uri != null) {
			IFileStore fileStore = EFS.getStore(uri);
			return (fileStore != null) && fileStore.fetchInfo().exists();
		}
		return false;
	}

	void updateSelection() {
		fEditResourceButton.setEnabled(getSelectedResource().length == 1);
		fConvertAbsoluteButton.setEnabled((getSelectedResource().length > 0)
				&& (areAbsolute(getSelectedResource())
				|| areFixed(getSelectedResource())));
	}

	boolean areFixed(IResource[] res) {
		for (int i = 0; i < res.length; i++) {
			String fullPath = res[i].getFullPath().toPortableString();
			if (!fFixedResources.containsKey(fullPath))
				return false;
		}
		return true;
	}

	IResource[] getSelectedResource() {
		IStructuredSelection selection = (IStructuredSelection) fTree
				.getSelection();
		Object[] array = selection.toArray();
		if (array.length > 0) {
			for (int i = 0; i < array.length; i++) {
				if (!(array[i] instanceof IResource))
					return new IResource[0];
			}
			IResource[] result = new IResource[array.length];
			System.arraycopy(array, 0, result, 0, array.length);
			return result;
		}
		return new IResource[0];
	}

	private void convertLocation() {
		ArrayList/* <IResource> */resources = new ArrayList/* <IResource> */();
		IResource[] selectedResources = getSelectedResource();
		resources.addAll(Arrays.asList(selectedResources));
		if (areFixed(selectedResources))
			convertToAbsolute(resources, selectedResources);
		else
			convertToRelative(resources, selectedResources);
	}

	private void convertToAbsolute(ArrayList/* <IResource> */resources,
			IResource[] selectedResources) {
		ArrayList/* <String> */report = new ArrayList/* <String> */();

		Iterator/* <IResource> */it = resources.iterator();
		while (it.hasNext()) {
			IResource res = (IResource) it.next();
			IPath location = res.getLocation();

			try {
				res.setLinkLocation(location, IResource.NONE,
						new NullProgressMonitor());
				report
						.add(NLS
								.bind(
										IDEWorkbenchMessages.LinkedResourceEditor_changedTo,
										new Object[] {
												res.getProjectRelativePath()
														.toPortableString(),
												res.getRawLocation()
														.toPortableString(),
												location.toPortableString() }));
			} catch (CoreException e) {
				report
						.add(NLS
								.bind(
										IDEWorkbenchMessages.LinkedResourceEditor_unableToSetLinkLocationForResource,
										res.getProjectRelativePath()
												.toPortableString()));
			}
		}

		reportResult(
				selectedResources,
				report,
				IDEWorkbenchMessages.LinkedResourceEditor_convertRelativePathLocations);
	}

	/**
	 * @param selectedResources
	 * @param report
	 */
	private void reportResult(IResource[] selectedResources,
			ArrayList/* <String> */report, String title) {
		StringBuffer message = new StringBuffer();
		Iterator/* <String> */stringIt = report.iterator();
		while (stringIt.hasNext()) {
			message.append(stringIt.next());
			if (stringIt.hasNext())
				message.append("\n"); //$NON-NLS-1$
		}
		final String resultMessage = message.toString();
		MessageDialog dialog = new MessageDialog(fConvertAbsoluteButton
				.getShell(), title, null,
				IDEWorkbenchMessages.LinkedResourceEditor_convertionResults,
				MessageDialog.INFORMATION,
				new String[] { IDEWorkbenchMessages.linkedResourceEditor_OK },
				0) {

			/* (non-Javadoc)
			 * @see org.eclipse.jface.dialogs.Dialog#isResizable()
			 */
			protected boolean isResizable() {
				return true;
			}

			protected Control createCustomArea(Composite comp) {
				setShellStyle(getShellStyle() | SWT.RESIZE);
				Composite parent = new Composite(comp, 0);
				GridLayout layout = new GridLayout();
				layout.marginHeight = 0;
				layout.marginWidth = 0;
				layout.marginLeft = 2;
				layout.numColumns = 1;
				layout.verticalSpacing = 9;
				parent.setLayout(layout);
				parent.setLayoutData(new GridData(GridData.FILL_BOTH));

				Text text = new Text(parent, SWT.BORDER | SWT.MULTI
						| SWT.V_SCROLL | SWT.H_SCROLL);
				text.setText(resultMessage.toString());
				GridData data = new GridData(640, 300);
				text.setLayoutData(data);
				return parent;
			}

		};
		dialog.open();
		reparent(selectedResources);
	}

	private IPath convertToProperCase(IPath path) {
		if (Platform.getOS().equals(Platform.OS_WIN32))
			return Path.fromPortableString(path.toPortableString()
					.toLowerCase());
		return path;
	}

	/**
	 * @param resources
	 * @param selectedResources
	 */
	private void convertToRelative(ArrayList/* <IResource> */resources,
			IResource[] selectedResources) {
		ArrayList/* <String> */report = new ArrayList/* <String> */();
		
		// first, try to use the automatic converter
		ArrayList/* <IResource> */remaining = new ArrayList/* <IResource> */();
		Iterator/* <IResource> */it = resources.iterator();
		while (it.hasNext()) {
			IResource res = (IResource) it.next();
			IPath location = res.getLocation();
			try {
				IPath newLocation = fProject.getPathVariableManager().convertToRelative(location, true, null);
				if (newLocation == null || newLocation.equals(location))
					remaining.add(res);
				else {
					res.setLinkLocation(newLocation, IResource.NONE,
							new NullProgressMonitor());
					report
							.add(NLS
									.bind(
											IDEWorkbenchMessages.LinkedResourceEditor_changedTo,
											new Object[] {
													res
															.getProjectRelativePath()
															.toPortableString(),
													location.toPortableString(),
													newLocation
															.toPortableString() }));
				}
			} catch (CoreException e) {
				remaining.add(res);
			}
		}
		resources = remaining;
		remaining = new ArrayList();
		// try for each to match with an existing variable
		String[] variables = fProject.getPathVariableManager()
				.getPathVariableNames();

		IPath[] resolvedVariables = new IPath[variables.length];
		for (int i = 0; i < variables.length; i++) {
			resolvedVariables[i] = convertToProperCase(fProject
					.getPathVariableManager().resolvePath(
							Path.fromPortableString(variables[i])));
		}

		it = resources.iterator();
		int amountLeft = 0;
		while (it.hasNext()) {
			IResource res = (IResource) it.next();
			IPath location = res.getLocation();

			int maxCount = 0;
			int variable = -1;
			for (int i = 0; i < variables.length; i++) {
				if (resolvedVariables[i]
						.isPrefixOf(convertToProperCase(location))) {
					int count = location
							.matchingFirstSegments(resolvedVariables[i]);
					if (count > maxCount) {
						maxCount = count;
						variable = i;
					}
				}
			}
			if (variable != -1) {
				IPath newLocation = Path.fromOSString(variables[variable])
						.append(location.removeFirstSegments(maxCount));
				try {
					res.setLinkLocation(newLocation, IResource.NONE,
							new NullProgressMonitor());
					report
							.add(NLS
									.bind(
											IDEWorkbenchMessages.LinkedResourceEditor_changedTo,
											new Object[] {
													res
															.getProjectRelativePath()
															.toPortableString(),
													location.toPortableString(),
													newLocation
															.toPortableString() }));
				} catch (CoreException e) {
					variable = -1;
				}
			}

			if (variable == -1) {
				amountLeft++;
				remaining.add(res);
			}
		}
		resources = remaining;

		if (amountLeft > 1) {
			// try to generate a generic variable
			it = resources.iterator();
			IPath commonPath = null;
			while (it.hasNext()) {
				IResource res = (IResource) it.next();
				IPath location = res.getLocation();

				if (commonPath == null)
					commonPath = location;
				else {
					int count = commonPath.matchingFirstSegments(location);
					if (count < commonPath.segmentCount())
						commonPath = commonPath.removeLastSegments(commonPath
								.segmentCount()
								- count);
				}
				if (commonPath.segmentCount() == 0)
					break;
			}
			if (commonPath.segmentCount() > 1) {
				String variableName = getSuitablePathVariable(commonPath);
				try {
					fProject.getPathVariableManager().setValue(variableName,
							commonPath);
				} catch (CoreException e) {
					report
							.add(NLS
									.bind(
											IDEWorkbenchMessages.LinkedResourceEditor_unableToCreateVariable,
											variableName, commonPath
													.toPortableString()));
				}
				it = resources.iterator();
				while (it.hasNext()) {
					IResource res = (IResource) it.next();
					IPath location = res.getLocation();
					int commonCount = location
							.matchingFirstSegments(commonPath);
					IPath newLocation = Path.fromOSString(variableName).append(
							location.removeFirstSegments(commonCount));
					try {
						res.setLinkLocation(newLocation, IResource.NONE,
								new NullProgressMonitor());
						report
								.add(NLS
										.bind(
												IDEWorkbenchMessages.LinkedResourceEditor_changedTo,
												new Object[] {
														res
																.getProjectRelativePath()
																.toPortableString(),
														location
																.toPortableString(),
														newLocation
																.toPortableString() }));
					} catch (CoreException e) {
						report
								.add(NLS
										.bind(
												IDEWorkbenchMessages.LinkedResourceEditor_unableToSetLinkLocationForResource,
												res.getProjectRelativePath()
														.toPortableString()));
					}
				}
			} else {
				report
						.add(IDEWorkbenchMessages.LinkedResourceEditor_unableToFindCommonPathSegments);
				it = resources.iterator();
				while (it.hasNext()) {
					IResource res = (IResource) it.next();
					report.add(res.getProjectRelativePath().toPortableString());
				}
			}
		} else if (!resources.isEmpty()) {
			IResource res = (IResource) resources.get(0);
			IPath resLocation = res.getLocation();
			IPath commonPath = resLocation.removeLastSegments(1);
			String variableName = getSuitablePathVariable(commonPath);
			try {
				fProject.getPathVariableManager().setValue(variableName,
						commonPath);
			} catch (CoreException e) {
				report
						.add(NLS
								.bind(
										IDEWorkbenchMessages.LinkedResourceEditor_unableToCreateVariable,
										variableName, commonPath
												.toPortableString()));
			}
			IPath location = res.getLocation();
			int commonCount = location.matchingFirstSegments(commonPath);
			IPath newLocation = Path.fromOSString(variableName).append(
					location.removeFirstSegments(commonCount));
			try {
				res.setLinkLocation(newLocation, IResource.NONE,
						new NullProgressMonitor());
				report
						.add(NLS
								.bind(
										IDEWorkbenchMessages.LinkedResourceEditor_changedTo,
										new Object[] {
												res.getProjectRelativePath()
														.toPortableString(),
												location.toPortableString(),
												newLocation.toPortableString() }));
			} catch (CoreException e) {
				report
						.add(NLS
								.bind(
										IDEWorkbenchMessages.LinkedResourceEditor_unableToSetLinkLocationForResource,
										res.getProjectRelativePath()
												.toPortableString()));
			}
		}
		reportResult(
				selectedResources,
				report,
				IDEWorkbenchMessages.LinkedResourceEditor_convertAbsolutePathLocations);
	}

	private String getSuitablePathVariable(IPath commonPath) {
		String variableName = commonPath.lastSegment();
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < variableName.length(); i++) {
			char c = variableName.charAt(i);
			if (Character.isLetterOrDigit(c) || (c == '_'))
				buf.append(c);
			else
				buf.append('_');

		}
		variableName = buf.toString();
		int index = 1;
		while (fProject.getPathVariableManager().isDefined(variableName)) {
			variableName += index;
			index++;
		}
		return variableName;
	}

	void editLocation() {
		IResource resource = getSelectedResource()[0];

		IPath location = resource.getRawLocation();

		PathVariableDialog dialog = new PathVariableDialog(
				fConvertAbsoluteButton.getShell(),
				PathVariableDialog.EDIT_LINK_LOCATION, resource.getType(),
				resource.getProject().getPathVariableManager(), null);
		if (location != null)
			dialog.setLinkLocation(location);
		dialog.setProject(resource.getProject());
		if (dialog.open() == Window.CANCEL) {
			return;
		}
		location = Path.fromOSString(dialog.getVariableValue());
		try {
			resource.setLinkLocation(location, 0, new NullProgressMonitor());
		} catch (Exception e) {
			e.printStackTrace();
		}
		reparent(new IResource[] { resource });
	}

	void reparent(IResource[] resources) {
		boolean changed = false;

		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			boolean isBroken;
			try {
				isBroken = !exists(resource);
			} catch (CoreException e) {
				isBroken = true;
			}
			TreeMap/* <String, IResource> */container = null;
			if (isBroken)
				container = fBrokenResources;
			else {
				if (isAbsolute(resource))
					container = fAbsoluteResources;
				else
					container = fFixedResources;
			}
			String fullPath = resource.getFullPath().toPortableString();

			if (!container.containsKey(fullPath)) {
				fBrokenResources.remove(fullPath);
				fAbsoluteResources.remove(fullPath);
				fFixedResources.remove(fullPath);

				container.put(fullPath, resource);
				changed = true;
			}
		}
		if (changed)
			fTree.refresh();
	}

	IResource fProjectFiles[] = null;
	TreeMap/* <String, IResource> */fBrokenResources = new TreeMap/*
																	 * <String,
																	 * IResource
																	 * >
																	 */();
	TreeMap/* <String, IResource> */fAbsoluteResources = new TreeMap/*
																	 * <String,
																	 * IResource
																	 * >
																	 */();
	TreeMap/* <String, IResource> */fFixedResources = new TreeMap/*
																 * <String,
																 * IResource>
																 */();

	IProject fProject;
	TreeViewer fTree;
	Button fEditResourceButton;
	Button fConvertAbsoluteButton;

	Image fixedImg = null;
	Image brokenImg = null;
	Image absoluteImg = null;

	String FIXED;
	String BROKEN;
	String ABSOLUTE;

	/**
	 * @return true
	 */
	public boolean performOk() {
		return true;
	}

	/**
	 * @param enableLinking
	 */
	public void setEnabled(boolean enableLinking) {
	}

	/**
	 * 
	 */
	public void reloadContent() {
		refreshContent();
		fTree.refresh();
		updateSelection();
		fTree.expandAll();
	}
}
