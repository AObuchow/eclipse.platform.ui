/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.examples.readmetool;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;

/**
 * Creates resolutions for readme markers.
 */
public class ReadmeMarkerResolutionGenerator implements IMarkerResolutionGenerator2 {
	
	/* (non-Javadoc)
	 * Method declared on IMarkerResolutionGenerator.
	 */
	public IMarkerResolution[] getResolutions(IMarker marker) {
		return new IMarkerResolution[] {new AddSentenceResolution()};
	}
	
	/* (non-Javadoc)
	 * Method declared on IMarkerResolutionGenerator2.
	 */
	public boolean hasResolutions(IMarker marker) {
		return true;
	}

}
