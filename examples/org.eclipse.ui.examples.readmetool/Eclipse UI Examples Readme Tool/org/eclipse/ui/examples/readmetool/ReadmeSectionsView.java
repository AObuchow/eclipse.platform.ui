package org.eclipse.ui.examples.readmetool;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.*;
import org.eclipse.ui.help.*;
import org.eclipse.ui.part.*;
import org.eclipse.ui.model.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.Composite;

/**
 * This class demonstrates a simple view containing a single viewer.
 */
public class ReadmeSectionsView extends ViewPart implements ISelectionListener {
	ListViewer viewer;
/**
 * Creates a new ReadmeSectionsView .
 */
public ReadmeSectionsView() {
	super();
}
/* (non-Javadoc)
 * Method declared on IWorkbenchPart
 */
public void createPartControl(Composite parent) {
	viewer = new ListViewer(parent);

	WorkbenchHelp.setHelp(viewer.getControl(), new ViewContextComputer(this, IReadmeConstants.SECTIONS_VIEW_CONTEXT));

	// if the objects in the viewer implement the IDesktopElement adapter,
	// these generic content and label providers can be used.
	viewer.setContentProvider(new WorkbenchContentProvider());
	viewer.setLabelProvider(new WorkbenchLabelProvider());

	// add myself as a global selection listener
	getSite().getPage().addSelectionListener(this);
	
	// prime the selection
	selectionChanged(null, getSite().getPage().getSelection());
}
/**
 * The <code>ReadmeSectionView</code> implementation of this 
 * <code>IWorkbenchPart</code> method runs super
 * and removes itself from the global selection listener. 
 */
public void dispose() {
	super.dispose();
	getSite().getPage().removeSelectionListener(this);
}
/* (non-Javadoc)
 * Method declared on ISelectionListener
 */
public void selectionChanged(IWorkbenchPart part, ISelection sel) {
	//if the selection is a readme file, get its sections.
	AdaptableList input = ReadmeModelFactory.getInstance().getSections(sel);
	viewer.setInput(input);
}
/* (non-Javadoc)
 * Method declared on IWorkbenchPart
 */
public void setFocus() {
	viewer.getControl().setFocus();
}
}
