/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.images;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;

import junit.framework.TestCase;


/**
 * @since 3.0
 */
public class ImageRegistryTest extends TestCase {
	public ImageRegistryTest(String name) {
		super(name);
	}

	public static void main(String args[]) {
		junit.textui.TestRunner.run(ImageRegistryTest.class);
	}
	
	public void testGetNull() {
		ImageRegistry reg = JFaceResources.getImageRegistry();
		
		Image result = reg.get((String)null);
		assertTrue("Registry should handle null", result == null);
	}
	
	public void testGetString() {
		
		// note, we must touch the class to ensure the static initialer runs
		// so the image registry is up to date
		Class clazz = Dialog.class;
		
		String[] imageNames = new String[] {
				Dialog.DLG_IMG_ERROR,
				Dialog.DLG_IMG_INFO,
				Dialog.DLG_IMG_QUESTION,
				Dialog.DLG_IMG_WARNING,
				Dialog.DLG_IMG_MESSAGE_ERROR,
				Dialog.DLG_IMG_MESSAGE_INFO,
				Dialog.DLG_IMG_MESSAGE_WARNING
				};

		ImageRegistry reg = JFaceResources.getImageRegistry();
		
		for (int i = 0; i < imageNames.length; i++) {
			String imageName = imageNames[i];
			Image image1 = reg.get(imageName);
			assertTrue("Returned null image", image1 != null);
		}
		
	}
}
