package org.eclipse.ui.texteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

import org.eclipse.core.resources.IMarker;


/**
 * A marker updater is responsible for saving changes to markers.
 * Marker updaters either update markers of a specific types or 
 * any type. Also they either assume update responsibility for a 
 * specific set of marker attributes or any marker attribute.
 * Marker updater must be registered with an <code>AbstractMarkerAnnotationModel</code>.
 */
public interface IMarkerUpdater {
	
	/**
	 * Returns the marker type for which this updater is responsible. If
	 * the result is <code>null</code>, the updater assumes responsibility
	 * for any marker type.
	 * 
	 * @return the marker type or <code>null</code> for any marker type
	 */
	String getMarkerType();
	
	/**
	 * Returns the attributes for which this updater is responsible. If the
	 * result is <code>null</code>, the updater assumes responsibility for
	 * any attributes.
	 *
	 * @return the attributes or <code>null</code> for any attribute
	 */
	String[] getAttribute();
	
	/**
	 * Updates the given marker according to the position of the given document.
	 * If the given position is <code>null</code>, the marker is assumed to
	 * carry the correct positional information. If the updater recognizes that
	 * the marker should be deleted, it returns <code>false</code>.
	 *
	 * @param marker the marker to be updated
	 * @param document the document into which the given position points
	 * @param position the current position of the marker inside the given document
	 */
	boolean updateMarker(IMarker marker, IDocument document, Position position);
}