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
package org.eclipse.ui.views.markers.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Represents a list of ConcreteMarkers.
 */
public class MarkerList {
	//private static final String SEARCHING_FOR_MARKERS = Messages.getString("MarkerList.0"); //$NON-NLS-1$
	//private Collection markers;
	private int[] markerCounts = null;
	private ConcreteMarker[] markers;
	
	/**
	 * Creates an initially empty marker list
	 */
	public MarkerList() {
		this(new ConcreteMarker[0]);
	}
	
	public MarkerList(Collection markers) {
		this((ConcreteMarker[])markers.toArray(new ConcreteMarker[markers.size()]));
	}
	
	/**
	 * Creates a list containing the given set of markers
	 * 
	 * @param markerList
	 */
	public MarkerList(ConcreteMarker[] markers) {
		this.markers = markers;
	}
	
	public static ConcreteMarker createMarker(IMarker marker) throws CoreException {
		if (marker.isSubtypeOf(IMarker.TASK)) {
			return new TaskMarker(marker);
		} else if (marker.isSubtypeOf(IMarker.BOOKMARK)) {
			return new BookmarkMarker(marker);
		} else if (marker.isSubtypeOf(IMarker.PROBLEM)) {
			return new ProblemMarker(marker);
		} else return new ConcreteMarker(marker);
	}
	
	public static ConcreteMarker[] createMarkers(Collection ofIMarker) throws CoreException {
		return createMarkers((IMarker[])ofIMarker.toArray(new IMarker[ofIMarker.size()]));
	}
	
	public static Collection createMarkersIgnoringErrors(Collection ofIMarker) {
		List result = new ArrayList(ofIMarker.size());
		
		Iterator iter = ofIMarker.iterator();
		while (iter.hasNext()) {
			IMarker next = (IMarker)iter.next();
			
			try {
				result.add(createMarker(next));
			} catch (CoreException e) {
				// Ignore errors
			}
		}
				
		return result;
	}
	
	public static ConcreteMarker[] createMarkers(IMarker[] source) throws CoreException {
		ConcreteMarker[] result = new ConcreteMarker[source.length];
		
		for (int idx = 0; idx < source.length; idx++) {
			result[idx] = createMarker(source[idx]);
		}
		
		return result;
	}
	
	/**
	 * Computes the set of markers that match the given filter
	 * 
	 * @param filter
	 * @param mon
	 */
	public static MarkerList compute(MarkerFilter filter, IProgressMonitor mon, 
									 boolean ignoreExceptions) throws CoreException {
		return new MarkerList(filter.findMarkers(mon, ignoreExceptions));
	}
	
	/**
	 * Returns a new MarkerList containing all markers in the workspace of the specified types
	 * 
	 * @param types
	 * @param mon
	 * @return
	 */
	public static IMarker[] compute(String[] types) throws CoreException {
		
		ArrayList result = new ArrayList();
		IResource input = ResourcesPlugin.getWorkspace().getRoot();

		for (int i = 0; i < types.length; i++) {
			IMarker[] newMarkers = input.findMarkers(types[i], true, IResource.DEPTH_INFINITE);
			result.addAll(Arrays.asList(newMarkers));
		}

		return (IMarker[])result.toArray(new IMarker[result.size()]);
	}
	
	/**
	 * Returns the markers in the list. Read-only.
	 * 
	 * @return an array of markers inthe list
	 */
	public ConcreteMarker[] toArray() {
		return markers;
	}
	
	/**
	 * Returns the markers in this list. Read-only.
	 * 
	 * @return the markers in the list
	 */
	//public Collection getMarkers() {
	//	return markers;
	//}
	
	/**
	 * Returns the number of items in the list
	 *  
	 * @return
	 */
	public int getItemCount() {
		return markers.length;
	}
	
	/**
	 * Returns the number of error markers in the list
	 * 
	 * @return
	 */
	public int getErrors() {
		return getMarkerCounts()[IMarker.SEVERITY_ERROR];
	}
	
	/**
	 * Returns the number of info markers in the list
	 * 
	 * @return
	 */
	public int getInfos() {
		return getMarkerCounts()[IMarker.SEVERITY_INFO];
	}
	
	/**
	 * Returns the number of warning markers in the list 
	 * 
	 * @return
	 */
	public int getWarnings() {
		return getMarkerCounts()[IMarker.SEVERITY_WARNING];
	}
	
	/**
	 * Returns an array of marker counts where getMarkerCounts()[severity] is the number
	 * of markers in the list with the given severity. 
	 * 
	 * @return
	 */
	private int[] getMarkerCounts() {
		if (markerCounts == null) {
			markerCounts = new int[] {0,0,0};

			for (int idx = 0; idx < markers.length; idx++) {
				ConcreteMarker marker = markers[idx];
				
				if (marker instanceof ProblemMarker) {
					int severity = ((ProblemMarker)markers[idx]).getSeverity();
					if (severity >= 0 && severity <= 2) {
						markerCounts[severity]++;					
					}
				}

			}
		}
		return markerCounts;
	}
}
