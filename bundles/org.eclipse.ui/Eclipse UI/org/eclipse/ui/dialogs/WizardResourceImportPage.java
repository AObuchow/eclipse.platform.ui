package org.eclipse.ui.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.dialogs.WizardDataTransferPage;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.ResourceTreeAndListGroup;
import org.eclipse.ui.internal.dialogs.TypeFilteringDialog;
import org.eclipse.ui.model.*;
import java.util.*;

/**
 * The abstract superclass for a typical import wizard's main page.
 * <p>
 * Clients may subclass this page to inherit its common destination resource
 * selection facilities.
 * </p>
 * <p>
 * Subclasses must implement 
 * <ul>
 *   <li><code>createSourceGroup</code></li>
 * </ul>
 * </p>
 * <p>
 * Subclasses may override
 * <ul>
 *   <li><code>allowNewContainerName</code></li>
 * </ul>
 * </p>
 * <p>
 * Subclasses may extend
 * <ul>
 *   <li><code>handleEvent</code></li>
 * </ul>
 * </p>
 */
public abstract class WizardResourceImportPage extends WizardDataTransferPage {
	private IResource currentResourceSelection;

	// initial value stores
	private String initialContainerFieldValue;
	protected java.util.List selectedTypes = new ArrayList();

	// widgets
	private Text containerNameField;
	private Button containerBrowseButton;
	protected ResourceTreeAndListGroup selectionGroup;

	private final static int SIZING_SELECTION_WIDGET_WIDTH = 400;
	private final static int SIZING_SELECTION_WIDGET_HEIGHT = 150;


	//messages
	private static final String EMPTY_FOLDER_MESSAGE = WorkbenchMessages.getString("WizardImportPage.specifyFolder"); //$NON-NLS-1$
	private static final String INACCESSABLE_FOLDER_MESSAGE = WorkbenchMessages.getString("WizardImportPage.folderMustExist"); //$NON-NLS-1$

/**
 * Creates an import wizard page. If the initial resource selection 
 * contains exactly one container resource then it will be used as the default
 * import destination.
 *
 * @param pageName the name of the page
 * @param selection the current resource selection
 */
protected WizardResourceImportPage(String name, IStructuredSelection selection) {
	super(name);

	if (selection.size() == 1)
		currentResourceSelection = (IResource) selection.getFirstElement();
	else
		currentResourceSelection = null;

	if (currentResourceSelection != null) {
		if (currentResourceSelection.getType() == IResource.FILE)
			currentResourceSelection = currentResourceSelection.getParent();

		if (!currentResourceSelection.isAccessible())
			currentResourceSelection = null;
	}

}
/**
 * The <code>WizardResourceImportPage</code> implementation of this 
 * <code>WizardDataTransferPage</code> method returns <code>true</code>. 
 * Subclasses may override this method.
 */
protected boolean allowNewContainerName() {
	return true;
}
/** (non-Javadoc)
 * Method declared on IDialogPage.
 */
public void createControl(Composite parent) {

	initializeDialogUnits(parent);
	
	Composite composite = new Composite(parent, SWT.FLAT);
	composite.setLayout(new GridLayout());
	composite.setLayoutData(new GridData(
		GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
	composite.setSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

	createSourceGroup(composite);

	createSpacer(composite);

	createPlainLabel(composite, WorkbenchMessages.getString("WizardImportPage.destinationLabel")); //$NON-NLS-1$
	createDestinationGroup(composite);

	createSpacer(composite);

	createPlainLabel(composite, WorkbenchMessages.getString("WizardExportPage.options")); //$NON-NLS-1$
	createOptionsGroup(composite);

	restoreWidgetValues();
	updateWidgetEnablements();
	setPageComplete(determinePageCompletion());

	setControl(composite);
}
/**
 * Creates the import destination specification controls.
 *
 * @param parent the parent control
 */
protected final void createDestinationGroup(Composite parent) {
	// container specification group
	Composite containerGroup = new Composite(parent,SWT.FLAT);
	GridLayout layout = new GridLayout();
	layout.numColumns = 3;
	containerGroup.setLayout(layout);
	containerGroup.setLayoutData(new GridData(
		GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));

	// container label
	Label resourcesLabel = new Label(containerGroup,SWT.FLAT);
	resourcesLabel.setText(WorkbenchMessages.getString("WizardExportPage.folder")); //$NON-NLS-1$

	// container name entry field
	containerNameField = new Text(containerGroup,SWT.SINGLE | SWT.FLAT);
	containerNameField.addListener(SWT.Modify,this);
	GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
	data.widthHint = SIZING_TEXT_FIELD_WIDTH;
	containerNameField.setLayoutData(data);

	// container browse button
	containerBrowseButton = new Button(containerGroup,SWT.PUSH | SWT.FLAT);
	containerBrowseButton.setText(WorkbenchMessages.getString("WizardImportPage.browse2")); //$NON-NLS-1$
	containerBrowseButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
	containerBrowseButton.addListener(SWT.Selection,this);

	initialPopulateContainerField();
}
/**
 *	Create the import source selection widget
 */
protected void createFileSelectionGroup(Composite parent) {

	//Just create with a dummy root.
	this.selectionGroup =
		new ResourceTreeAndListGroup(
			parent,
			new FileSystemElement("Dummy", null, true),//$NON-NLS-1$
			getFolderProvider(),
			new WorkbenchLabelProvider(),
			getFileProvider(),
			new WorkbenchLabelProvider(),
			SWT.FLAT,
			SIZING_SELECTION_WIDGET_WIDTH,
			SIZING_SELECTION_WIDGET_HEIGHT);

	ICheckStateListener listener = new ICheckStateListener() {
		public void checkStateChanged(CheckStateChangedEvent event) {
			updateWidgetEnablements();
		}
	};

	WorkbenchViewerSorter sorter = new WorkbenchViewerSorter();
	this.selectionGroup.setTreeSorter(sorter);
	this.selectionGroup.setListSorter(sorter);
	this.selectionGroup.addCheckStateListener(listener);

}
/**
 * Creates the import source specification controls.
 * <p>
 * Subclasses must implement this method.
 * </p>
 *
 * @param parent the parent control
 */
protected abstract void createSourceGroup(Composite parent);
/**
 * Display an error dialog with the specified message.
 *
 * @param message the error message
 */
protected void displayErrorDialog(String message) {
	MessageDialog.openError(getContainer().getShell(), WorkbenchMessages.getString("WizardImportPage.errorDialogTitle"), message); //$NON-NLS-1$
}
/**
 * Returns the path of the container resource specified in the container
 * name entry field, or <code>null</code> if no name has been typed in.
 * <p>
 * The container specified by the full path might not exist and would need to
 * be created.
 * </p>
 *
 * @return the full path of the container resource specified in
 *   the container name entry field, or <code>null</code>
 */
protected IPath getContainerFullPath() {
	IWorkspace workspace = WorkbenchPlugin.getPluginWorkspace();

	//make the path absolute to allow for optional leading slash
	IPath testPath = getResourcePath();

	IStatus result =
		workspace.validatePath(
			testPath.toString(),
			IResource.PROJECT | IResource.FOLDER);
	if (result.isOK()) {
		return testPath;
	}

	return null;
}
/**
 * Returns a content provider for <code>FileSystemElement</code>s that returns 
 * only files as children.
 */
protected abstract ITreeContentProvider getFileProvider();
/**
 * Returns a content provider for <code>FileSystemElement</code>s that returns 
 * only folders as children.
 */
protected abstract ITreeContentProvider getFolderProvider();
/**
 * Return the path for the resource field.
 * @return IPath
 */
protected IPath getResourcePath() {
	return getPathFromText(this.containerNameField);
}
/**
 * Returns this page's list of currently-specified resources to be 
 * imported. This is the primary resource selection facility accessor for 
 * subclasses.
 *
 * @return a list of resources currently selected 
 * for export (element type: <code>IResource</code>)
 */
protected java.util.List getSelectedResources() {
	return this.selectionGroup.getAllCheckedListItems();
}
/**
 * Returns the container resource specified in the container name entry field,
 * or <code>null</code> if such a container does not exist in the workbench.
 *
 * @return the container resource specified in the container name entry field,
 *   or <code>null</code>
 */
protected IContainer getSpecifiedContainer() {
	IWorkspace workspace = WorkbenchPlugin.getPluginWorkspace();
	IPath path = getContainerFullPath();
	if (workspace.getRoot().exists(path))
		return (IContainer) workspace.getRoot().findMember(path);

	return null;
}
/**
 * Returns a collection of the currently-specified resource types for
 * use by the type selection dialog.
 */
protected java.util.List getTypesToImport() {

	return selectedTypes;
}
/**
 * Opens a container selection dialog and displays the user's subsequent
 * container resource selection in this page's container name field.
 */
protected void handleContainerBrowseButtonPressed() {
	// see if the user wishes to modify this container selection
	IPath containerPath =
		queryForContainer(getSpecifiedContainer(), WorkbenchMessages.getString("WizardImportPage.selectFolderLabel")); //$NON-NLS-1$

	// if a container was selected then put its name in the container name field
	if (containerPath != null) { // null means user cancelled
		setErrorMessage(null);
		containerNameField.setText(containerPath.makeRelative().toString());
	}
}
/**
 * The <code>WizardResourceImportPage</code> implementation of this 
 * <code>Listener</code> method handles all events and enablements for controls
 * on this page. Subclasses may extend.
 * @param event Event
 */
public void handleEvent(Event event) {
	Widget source = event.widget;

	if (source == containerBrowseButton)
		handleContainerBrowseButtonPressed();

	updateWidgetEnablements();
}
/**
 *	Open a registered type selection dialog and note the selections
 *	in the receivers types-to-export field
 */
protected void handleTypesEditButtonPressed() {

	TypeFilteringDialog dialog =
		new TypeFilteringDialog(getContainer().getShell(), getTypesToImport());

	dialog.open();

	Object[] newSelectedTypes = dialog.getResult();
	if (newSelectedTypes != null) { // ie.- did not press Cancel
		this.selectedTypes = new ArrayList(newSelectedTypes.length);
		for (int i = 0; i < newSelectedTypes.length; i++)
			this.selectedTypes.add(newSelectedTypes[i]);

		setupSelectionsBasedOnSelectedTypes();
	}

}
/**
 * Sets the initial contents of the container name field.
 */
protected final void initialPopulateContainerField() {
	if (initialContainerFieldValue != null)
		containerNameField.setText(initialContainerFieldValue);
	else if (currentResourceSelection != null)
		containerNameField.setText(currentResourceSelection.getFullPath().makeRelative().toString());
}
/**
 * Set all of the selections in the selection group to value
 * @param value boolean
 */
protected void setAllSelections(boolean value) {
	selectionGroup.setAllSelections(value);
}
/**
 * Sets the value of this page's container resource field, or stores
 * it for future use if this page's controls do not exist yet.
 *
 * @param value String
 */
public void setContainerFieldValue(String value) {
	if (containerNameField == null)
		initialContainerFieldValue = value;
	else
		containerNameField.setText(value);
}
/**
 * Update the tree to only select those elements that match the selected types.
 * Do nothing by default.
 */
protected void setupSelectionsBasedOnSelectedTypes() {
}
/**
 * Update the selections with those in map .
 * @param map Map - key tree elements, values Lists of list elements
 */
protected void updateSelections(Map map) {
	selectionGroup.updateSelections(map);
}
/**
 * Check if widgets are enabled or disabled by a change in the dialog.
 * @param event Event
 */
protected void updateWidgetEnablements() {

	boolean pageComplete = determinePageCompletion();
	setPageComplete(pageComplete);
	if (pageComplete)
		setMessage(null);
	super.updateWidgetEnablements();
}
/* (non-Javadoc)
 * Method declared on WizardDataTransferPage.
 */
protected final boolean validateDestinationGroup() {

	IPath containerPath = getContainerFullPath();
	if (containerPath == null) {
		setMessage(EMPTY_FOLDER_MESSAGE);
		return false;
	}

	// If the container exist, validate it
	IContainer container = getSpecifiedContainer();
	if (container == null) {
		//if it is does not exist be sure the project does
		IWorkspace workspace = WorkbenchPlugin.getPluginWorkspace();
		IPath projectPath =
			containerPath.removeLastSegments(containerPath.segmentCount() - 1);

		if (workspace.getRoot().exists(projectPath))
			return true;
		else {
			setErrorMessage(WorkbenchMessages.getString("WizardImportPage.projectNotExist")); //$NON-NLS-1$
			return false;
		}
	} else {
		if (!container.isAccessible()) {
			setErrorMessage(INACCESSABLE_FOLDER_MESSAGE);
			return false;
		}
	}

	return true;

}
}
