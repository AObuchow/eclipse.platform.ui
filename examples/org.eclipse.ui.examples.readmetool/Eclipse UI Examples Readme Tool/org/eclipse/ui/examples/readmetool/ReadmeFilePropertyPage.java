package org.eclipse.ui.examples.readmetool;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.help.*;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import java.io.*;

/**
 * This page will be added to the property page dialog
 * when the "Properties..." popup menu item is selected
 * for Readme files. 
 */
public class ReadmeFilePropertyPage extends PropertyPage {

/**
 * Utility method that creates a new composite and
 * sets up its layout data.
 *
 * @param parent  the parent of the composite
 * @param numColumns  the number of columns in the new composite
 * @return the newly-created composite
 */
protected Composite createComposite(Composite parent, int numColumns) {
	Composite composite = new Composite(parent, SWT.NULL);
	GridLayout layout = new GridLayout();
	layout.numColumns = numColumns;
	composite.setLayout(layout);
	GridData data = new GridData();
	data.verticalAlignment = GridData.FILL;
	data.horizontalAlignment = GridData.FILL;
	composite.setLayoutData(data);
	return composite;
}
/** (non-Javadoc)
 * Method declared on PreferencePage
 */
public Control createContents(Composite parent) {

	// ensure the page has no special buttons
	noDefaultAndApplyButton();
	Composite panel = createComposite(parent, 2);

	WorkbenchHelp.setHelp(panel, new DialogPageContextComputer(this, IReadmeConstants.PROPERTY_PAGE_CONTEXT));

	// layout the page

	IResource resource = (IResource) getElement();
	IStatus result = null;
	if (resource.getType() == IResource.FILE) {
		Label l = createLabel(panel, "File name:");
		l = createLabel(panel, resource.getName());
		grabExcessSpace(l);

		//
		createLabel(panel, "Path: ");
		l = createLabel(panel, resource.getFullPath().setDevice(null).toString());
		grabExcessSpace(l);

		//
		createLabel(panel, "Size: ");
		InputStream contentStream = null;
		try {
			IFile file = (IFile) resource;
			if (!file.isLocal(IResource.DEPTH_ZERO))
				l = createLabel(panel,"<file contents not local>");
			else {
				contentStream = file.getContents();
				Reader in = new InputStreamReader(contentStream);
				int chunkSize = contentStream.available();
				StringBuffer buffer = new StringBuffer(chunkSize);
				char[] readBuffer = new char[chunkSize];
				int n = in.read(readBuffer);
				
				while (n > 0) {
					buffer.append(readBuffer);
					n = in.read(readBuffer);
				}
				
				contentStream.close();
				l = createLabel(panel, Integer.toString(buffer.length()));
			}
		} catch (CoreException e) {
			result = e.getStatus();
			String message = result.getMessage();
			if (message == null)
				l = createLabel(panel, "<Unknown>");
			else
				l = createLabel(panel, message);
		} catch (IOException e) {
			l = createLabel(panel, "<Unknown>");
		} finally {
			if (contentStream != null) {
				try {
					contentStream.close();
				} catch (IOException e) {
				}
			}
		}
		grabExcessSpace(l);
		createLabel(panel, "Number of sections:");
		// We will get the sections property and simply
		// report number of elements found.
		IAdaptable sections = getSections(resource);
		if (sections instanceof AdaptableList) {
			AdaptableList list = (AdaptableList)sections;
			l = createLabel(panel, String.valueOf(list.size()));
			grabExcessSpace(l);
		}
	}

	//
	Label l = createLabel(panel, "Additional information about the Readme file can go here.");
	grabExcessSpace(l);
	GridData gd = (GridData) l.getLayoutData();
	gd.horizontalSpan = 2;
	return new Canvas(panel, 0);
}
/**
 * Utility method that creates a new label and sets up its layout data.
 *
 * @param parent  the parent of the label
 * @param text  the text of the label
 * @return the newly-created label
 */
protected Label createLabel(Composite parent, String text) {
	Label label = new Label(parent, SWT.LEFT);
	label.setText(text);
	GridData data = new GridData();
	data.horizontalAlignment = GridData.FILL;
	label.setLayoutData(data);
	return label;
}
/**
 * Returns the readme sections for this resource, or null
 * if not applicable (resource is not a readme file).
 */
private AdaptableList getSections(IAdaptable adaptable) {
	if (adaptable instanceof IFile)
		return ReadmeModelFactory.getInstance().getSections((IFile)adaptable);
	else
		return null;
}
/**
 * Sets this control to grab any excess horizontal space
 * left in the window.
 *
 * @param control  the control for which to grab excess space
 */
private void grabExcessSpace(Control control) {
	GridData gd = (GridData) control.getLayoutData();
	if (gd != null) {
		gd.grabExcessHorizontalSpace = true;
	}
}
/** (non-Javadoc)
 * Method declared on PreferencePage
 */
public boolean performOk() {
	// nothing to do - read-only page
	return true;
}
}
