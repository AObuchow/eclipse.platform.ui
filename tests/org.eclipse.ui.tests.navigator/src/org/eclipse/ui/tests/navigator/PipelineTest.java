/*******************************************************************************
 * Copyright (c) 2003, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.TreeItem;

public class PipelineTest extends NavigatorTestBase {

	public PipelineTest() {
		_navigatorInstanceId = TEST_VIEWER_PIPELINE;
	}

	public void testNavigatorRootContents() throws Exception {

		assertEquals(
				"There should be no visible extensions for the pipeline viewer.",
				0, _contentService.getVisibleExtensionIds().length);

		_contentService.bindExtensions(new String[] {
				COMMON_NAVIGATOR_RESOURCE_EXT, COMMON_NAVIGATOR_JAVA_EXT },
				false);

		// Note this test will fail showing only one if the JDT stuff
		// is not included in the executing bundles (which it normally is)
		assertEquals(
				"There should be two visible extensions for the pipeline viewer.",
				2, _contentService.getVisibleExtensionIds().length);

		_contentService.getActivationService().activateExtensions(
				new String[] { COMMON_NAVIGATOR_RESOURCE_EXT,
						COMMON_NAVIGATOR_JAVA_EXT }, true);

		refreshViewer();

		// we do this to force the rendering of the children of items[0]
		_viewer.setSelection(
				new StructuredSelection(_project.getFile(".project")), true); //$NON-NLS-1$

		TreeItem[] rootItems = _viewer.getTree().getItems();

		assertEquals("There should be one item.", _projectCount, rootItems.length); //$NON-NLS-1$		

		assertTrue(
				"The root object should be an IJavaProject, which is IAdaptable.", rootItems[0].getData() instanceof IAdaptable); //$NON-NLS-1$

		IProject adaptedProject = (IProject) ((IAdaptable) rootItems[_projectInd]
				.getData()).getAdapter(IProject.class);
		assertEquals(_project, adaptedProject);

		IFolder sourceFolder = _project.getFolder(new Path("src"));
		_viewer.add(_project, sourceFolder);

		TreeItem[] projectChildren = rootItems[_projectInd].getItems();

		assertTrue("There should be some items.", projectChildren.length > 0); //$NON-NLS-1$

		for (int i = 0; i < projectChildren.length; i++) {
			if (projectChildren[i].getData() == sourceFolder)
				fail("The src folder should not be added as an IFolder.");
		}

		// a new project without a Java nature should add without an issue.
		IProject newProject = ResourcesPlugin.getWorkspace().getRoot()
				.getProject("New Project");
		_viewer.add(_viewer.getInput(), newProject);

		rootItems = _viewer.getTree().getItems();

		assertEquals("There should be " + (_projectCount + 1) + 
				" items.", _projectCount + 1, rootItems.length);

		boolean found = false;
		for (int i = 0; i < rootItems.length && !found; i++) {
			if (rootItems[i].getData() instanceof IProject) {
				IProject newProjectFromTree = (IProject) rootItems[i].getData();
				if (newProject.equals(newProjectFromTree))
					found = true;
			}
		}
		assertTrue(found);
	}
}
