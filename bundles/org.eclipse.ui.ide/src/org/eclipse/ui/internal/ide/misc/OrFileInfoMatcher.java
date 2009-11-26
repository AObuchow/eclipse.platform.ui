/*******************************************************************************
 * Copyright (c) 2008, 2009 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Serge Beauchamp (Freescale Semiconductor) - [252996] initial API and implementation
 *     IBM Corporation - ongoing implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.misc;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.resources.filtermatchers.CompoundFileInfoMatcher;

/**
 * A Resource Filter Type Factory for supporting the OR logical preposition
 */
public class OrFileInfoMatcher extends CompoundFileInfoMatcher {

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.AbstractFileInfoMatcher#matches(org.eclipse.core.filesystem.IFileInfo)
	 */
	public boolean matches(IFileInfo fileInfo) {
		if (filterTypes.length > 0) {
			for (int i = 0; i < filterTypes.length; i++) {
				if (filterTypes[i].matches(fileInfo))
					return true;
			}
			return false;
		}
		return true;
	}
}
