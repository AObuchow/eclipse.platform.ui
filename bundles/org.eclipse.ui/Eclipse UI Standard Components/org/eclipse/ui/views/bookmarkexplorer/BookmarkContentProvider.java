package org.eclipse.ui.views.bookmarkexplorer;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import java.util.*;

/**
 * Task list content provider returns elements that should be
 * in the task list depending on the selection mode.
 * It goes directly to the marker manager and retreives
 * tasks and problems.
 */
/* package */ class BookmarkContentProvider implements ITreeContentProvider,
	IResourceChangeListener, IBasicPropertyConstants
{
	private BookmarkNavigator bookmarksView;
	private IResource input;
	private Viewer viewer;
/**
 * The constructor.
 */
public BookmarkContentProvider(BookmarkNavigator bookmarksView) {
	this.bookmarksView = bookmarksView;
}
/**
 * The visual part that is using this content provider is about
 * to be disposed. Deallocate all allocated SWT resources.
 */
public void dispose() {
	IResource resource = (IResource) viewer.getInput();
	if (resource != null) {
		resource.getWorkspace().removeResourceChangeListener(this);
	}
}
/**
 * Returns all the bookmarks that should be shown for
 * the current settings.
 */
Object[] getBookmarks(IResource resource) {
	try {
		return resource.findMarkers(IMarker.BOOKMARK, true, IResource.DEPTH_INFINITE);
	} catch (CoreException e) {
		return new Object[0];
	}
}
public Object[] getChildren(Object element) {
	// If the input element is a workbench return a list
	// of the existing bookmarks.  Otherwise, return an empty list.
	if (element instanceof IResource)
		return getBookmarks((IResource) element);
	else
		return new Object[0];
}
public Object[] getElements(Object element) {
	return getChildren(element);
}
/**
 * Recursively walks over the resource delta and gathers all marker deltas.  Marker
 * deltas are placed into one of the three given vectors depending on
 * the type of delta (add, remove, or change).
 */
void getMarkerDeltas(IResourceDelta delta, List additions, List removals, List changes) {
	IMarkerDelta[] markerDeltas = delta.getMarkerDeltas();
	for (int i = 0; i < markerDeltas.length; i++) {
		IMarkerDelta markerDelta = markerDeltas[i];
		IMarker marker = markerDelta.getMarker();
		switch (markerDelta.getKind()) {
			case IResourceDelta.ADDED:
				if (markerDelta.isSubtypeOf(IMarker.BOOKMARK)) {
					additions.add(marker);
				}
				break;
			case IResourceDelta.REMOVED:
				if (markerDelta.isSubtypeOf(IMarker.BOOKMARK)) {
					removals.add(marker);
				}
				break;
			case IResourceDelta.CHANGED:
				if (markerDelta.isSubtypeOf(IMarker.BOOKMARK)) {
					changes.add(marker);
				}
				break;
		}
	}

	//recurse on child deltas
	IResourceDelta[] children = delta.getAffectedChildren();
	for (int i = 0; i < children.length; i++) {
		getMarkerDeltas(children[i], additions, removals, changes);
	}
}
/* (non-Javadoc)
 * Method declared on ITreeContentProvider,
 */
public Object getParent(Object element) {
	return input;
}
/**
 * hasChildren method comment.
 */
public boolean hasChildren(Object element) {
	if (element instanceof IWorkspace)
		return true;
	else
		return false;
}
public void inputChanged(Viewer newViewer, Object oldInput, Object newInput) {
	if (oldInput == null) {
		IResource resource = (IResource) newInput;
		resource.getWorkspace().addResourceChangeListener(this);
	}
	this.viewer = newViewer;
	this.input = (IResource) newInput;
}
/**
 * The workbench has changed.  Process the delta and issue updates to the viewer,
 * inside the UI thread.
 *
 * @see IResourceChangeListener#resourceChanged
 */
public void resourceChanged(final IResourceChangeEvent event) {

	// gather all marker changes from the delta.
	// be sure to do this in the calling thread, 
	// as the delta is destroyed when this method returns
	final List additions = new ArrayList();
	final List removals = new ArrayList();
	final List changes = new ArrayList();
	
	IResourceDelta delta = event.getDelta();
	if(delta == null) return;
	getMarkerDeltas(delta, additions, removals, changes);

	// update the viewer based on the marker changes, in the UI thread
	if (additions.size() + removals.size() + changes.size() > 0) {
		viewer.getControl().getDisplay().asyncExec(new Runnable() {
			public void run() {
				// This method runs inside an asyncExec.  The widget may have been destroyed
				// by the time this is run.  Check for this and do nothing if so.
				Control ctrl = viewer.getControl();
				if (ctrl == null || ctrl.isDisposed())
					return;
				
				// Be conservative for now, and refresh once if there are any changes.
				// Can improve once the following PRs are addressed:
				//   1G8OUTD: ITPCORE:ALL - Marker deltas should be composed
				//   1G8OU65: ITPCORE:ALL - IMarkerDelta.getType() doesn't allow subtype comparisons
				viewer.refresh();
			}
		});
	}
}
}
