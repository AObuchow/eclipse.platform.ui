/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.ide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IMarkerHelpRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.registry.MarkerHelpRegistry;
import org.eclipse.ui.internal.ide.registry.MarkerHelpRegistryReader;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Collection of IDE-specific APIs factored out of existing workbench.
 * This class cannot be instantiated; all functionality is provided by 
 * static methods and fields.
 * 
 * @since 3.0
 */
public final class IDE {
	/**
	 * The persistent property key used on IFile resources to contain
	 * the preferred editor ID to use.
	 * <p>
	 * Example of retrieving the persisted editor id:
	 * <pre><code>
	 * IFile file = ...
	 * IEditorDescriptor editorDesc = null;
	 * try {
	 * 	String editorID = file.getPersistentProperty(EDITOR_KEY);
	 * 	if (editorID != null) {
	 * 		editorDesc = editorReg.findEditor(editorID);
	 * 	}
	 * } catch (CoreException e) {
	 * 	// handle problem accessing persistent property here
	 * }
	 * </code></pre>
	 * </p><p>
	 * Example of persisting the editor id:
	 * <pre><code>
	 * IFile file = ...
	 * try {
	 * 	file.setPersistentProperty(EDITOR_KEY, editorDesc.getId());
	 * } catch (CoreException e) {
	 * 	// handle problem setting persistent property here
	 * }
	 * </code></pre>
	 * </p>
	 */
	public static final QualifiedName EDITOR_KEY = new QualifiedName("org.eclipse.ui.internal.registry.ResourceEditorRegistry","EditorProperty");//$NON-NLS-2$//$NON-NLS-1$

	/**
	 * An optional attribute within a workspace marker (<code>IMarker</code>) which
	 * identifies the preferred editor type to be opened.
	 */	
	public static final String EDITOR_ID_ATTR = "org.eclipse.ui.editorID"; //$NON-NLS-1$
	
	/**
	 * The resource based perspective identifier.
	 * @issue should this now be org.eclipse.ui.ide.resourcePerspective?
	 */
	public static final String RESOURCE_PERSPECTIVE_ID = "org.eclipse.ui.resourcePerspective"; //$NON-NLS-1$

	/**
	 * Marker help registry mapping markers to help context ids and resolutions;
	 * lazily initialized on fist access.
	 */
	private static MarkerHelpRegistry markerHelpRegistry = null;

	/**
	 * An empty unmodifiable list. Used to avoid garbage creation.
	 */
	private static final List emptyUnmodifiableList = Collections.unmodifiableList(new ArrayList(0));
	
	/**
	 * Standard shared images defined by the IDE. These are over and about the
	 * standard workbench images declared in {@link org.eclipse.ui.ISharedImages
	 * ISharedImages}.
	 * <p>
	 * This interface is not intended to be implemented by clients.
	 * </p>
	 * 
	 * @see org.eclipse.ui.ISharedImages
	 */
	public interface SharedImages {
		/**
		 * Identifies a project image.
		 */
		public final static String IMG_OBJ_PROJECT = "IMG_OBJ_PROJECT"; //$NON-NLS-1$

		/**
		 * Identifies a closed project image.
		 */
		public final static String IMG_OBJ_PROJECT_CLOSED = "IMG_OBJ_PROJECT_CLOSED"; //$NON-NLS-1$
	
		/**
		 * Identifies the image used for "open marker".
		 */
		public final static String IMG_OPEN_MARKER = "IMG_OPEN_MARKER"; //$NON-NLS-1$
	
		/**
		 * Identifies the default image used to indicate a task.
		 */
		public final static String IMG_OBJS_TASK_TSK = "IMG_OBJS_TASK_TSK"; //$NON-NLS-1$
		/**
		 * Identifies the default image used to indicate a bookmark.
		 */
		public final static String IMG_OBJS_BKMRK_TSK = "IMG_OBJS_BKMRK_TSK"; //$NON-NLS-1$
	}
	
	/**
	 * Preferences defined by the IDE workbench.
	 * <p>
	 * This interface is not intended to be implemented by clients.
	 * </p>
	 */
	public interface Preferences {
		/**
		 * A named preference for how a new perspective should be opened
		 * when a new project is created.
		 * <p>
		 * Value is of type <code>String</code>.  The possible values are defined 
		 * by the constants <code>OPEN_PERSPECTIVE_WINDOW, OPEN_PERSPECTIVE_PAGE, 
		 * OPEN_PERSPECTIVE_REPLACE, and NO_NEW_PERSPECTIVE</code>.
		 * </p>
		 * 
		 * @see org.eclipse.ui.IWorkbenchPreferenceConstants#OPEN_PERSPECTIVE_WINDOW
		 * @see org.eclipse.ui.IWorkbenchPreferenceConstants#OPEN_PERSPECTIVE_PAGE
		 * @see org.eclipse.ui.IWorkbenchPreferenceConstants#OPEN_PERSPECTIVE_REPLACE
		 * @see org.eclipse.ui.IWorkbenchPreferenceConstants#NO_NEW_PERSPECTIVE
		 */
		public static final String PROJECT_OPEN_NEW_PERSPECTIVE =
			"PROJECT_OPEN_NEW_PERSPECTIVE"; //$NON-NLS-1$
	}
	
	/**
	 * Block instantiation.
	 */
	private IDE() {
	}
	
	/**
	 * Returns the marker help registry for the workbench.
	 * 
	 * @return the marker help registry
	 */
	public static IMarkerHelpRegistry getMarkerHelpRegistry() {
		if (markerHelpRegistry == null) {
			markerHelpRegistry = new MarkerHelpRegistry();
			new MarkerHelpRegistryReader().addHelp(markerHelpRegistry);
		}
		return markerHelpRegistry;
	}
	
	
	/**
	 * Sets the cursor and selection state for the given editor to 
	 * reveal the position of the given marker.
	 * This is done on a best effort basis.  If the editor does not
	 * provide an <code>IGotoMarker</code> interface (either directly
     * or via <code>IAdaptable.getAdapter</code>), this has no effect.
	 * 
	 * @param editor the editor
	 * @param marker the marker
	 */
	public static void gotoMarker(IEditorPart editor, IMarker marker) {
		IGotoMarker gotoMarker = null;
		if (editor instanceof IGotoMarker) {
			gotoMarker = (IGotoMarker) editor;
		} else {
			gotoMarker = (IGotoMarker) editor.getAdapter(IGotoMarker.class);
		}
		if (gotoMarker != null) {
			gotoMarker.gotoMarker(marker);
		} 
	}
	
	/**
	 * Opens an editor on the given file resource.  
	 * <p>
	 * If this page already has an editor open on the target file that editor is 
	 * brought to front; otherwise, a new editor is opened. If 
	 * <code>activate == true</code> the editor will be activated. 
	 * </p><p>
	 * An appropriate editor for the file input is determined using a multistep process.
	 * </p>
	 * <ol>
	 *   <li>The file input is consulted for a persistent property named
	 *       <code>IDE.EDITOR_KEY</code> containing the preferred editor id
	 *       to be used.</li>
	 *   <li>The workbench editor registry is consulted to determine if an editor 
	 *			extension has been registered for the file type.  If so, an 
	 *			instance of the editor extension is opened on the file.  
	 *			See <code>IEditorRegistry.getDefaultEditor(String)</code>.</li>
	 *   <li>The operating system is consulted to determine if an in-place
	 *       component editor is available (e.g. OLE editor on Win32 platforms).</li>
	 *   <li>The operating system is consulter to determine if an external
	 * 		editor is available.</li>
	 * </ol>
	 * </p>
	 *
	 * @param page the workbench page to open the editor in
	 * @param input the file to edit
	 * @param activate if <code>true</code> the editor will be activated
	 * @return an open editor or <code>null</code> if external editor open
	 * @exception PartInitException if the editor could not be initialized
	 */
	public static IEditorPart openEditor(IWorkbenchPage page, IFile input, boolean activate) throws PartInitException {
		// sanity checks
		if (page == null || input == null) {
			throw new IllegalArgumentException();
		}
		
		IEditorRegistry editorReg = PlatformUI.getWorkbench().getEditorRegistry();
		IEditorDescriptor editorDesc = null;
		
		// determine the editor id to open the file input
		// first look for the persistent property
		try {
			String editorID = input.getPersistentProperty(EDITOR_KEY);
			if (editorID != null) {
				editorDesc = editorReg.findEditor(editorID);
			}
		} catch (CoreException e) {
			// ignore this
		}

		// next look for editor registered for file name
		if (editorDesc == null) {
			editorDesc = editorReg.getDefaultEditor(input.getName());
		}
		
		// next check the OS for in-place editor (OLE on Win32)
		if (editorDesc == null && editorReg.isSystemInPlaceEditorAvailable(input.getName())) {
			editorDesc = editorReg.findEditor(IEditorRegistry.SYSTEM_INPLACE_EDITOR_ID);
		}
		
		// next check with the OS for an external editor
		if (editorDesc == null && editorReg.isSystemExternalEditorAvailable(input.getName())) {
			editorDesc = editorReg.findEditor(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
		}
		
		// next lookup the default text editor
		if (editorDesc == null) {
			editorDesc = editorReg.findEditor(IDEWorkbenchPlugin.DEFAULT_TEXT_EDITOR_ID);
		}
		
		// if no valid editor found, bail out
		if (editorDesc == null) {
			throw new PartInitException(IDEWorkbenchMessages.getString("IDE.noFileEditorFound")); //$NON-NLS-1$
		}
		
		// open the editor on the file
		return page.openEditor(new FileEditorInput(input), editorDesc.getId(), activate);
	}

	/**
	 * Opens an editor on the file resource of the given marker. 
	 * <p>
	 * If this page already has an editor open on the marker resource file that 
	 * editor is brought to front; otherwise, a new editor is opened. If 
	 * <code>activate == true</code> the editor will be activated.  The cursor and 
	 * selection state of the editor are then updated from information recorded in 
	 * the marker.
	 * </p><p>
	 * If the marker contains an <code>EDITOR_ID_ATTR</code> attribute 
	 * the attribute value will be used to determine the editor type to be opened. 
	 * If not, the registered editor for the marker resource file will be used. 
	 * </p>
	 *
	 * @param page the workbench page to open the editor in
	 * @param marker the marker to open
	 * @param activate if <code>true</code> the editor will be activated
	 * @return an open editor or <code>null</code> not possible
	 * @exception PartInitException if the editor could not be initialized
	 */
	public static IEditorPart openEditor(IWorkbenchPage page, IMarker marker, boolean activate) throws PartInitException {
		// sanity checks
		if (page == null || marker == null) {
			throw new IllegalArgumentException();
		}
		
		// get the marker resource file
		if (!(marker.getResource() instanceof IFile)) {
			IDEWorkbenchPlugin.log("Open editor on marker failed; marker resource not an IFile"); //$NON-NLS-1$
			return null;
		}
		IFile file = (IFile) marker.getResource();

		// get the preferred editor id from the marker
		IEditorRegistry editorReg = PlatformUI.getWorkbench().getEditorRegistry();
		IEditorDescriptor editorDesc = null;
		try {
			String editorID = (String) marker.getAttribute(EDITOR_ID_ATTR);
			if (editorID != null) {
				editorDesc = editorReg.findEditor(editorID);
			}
		}
		catch (CoreException e) {
			// ignore this
		}

		// open the editor on the marker resource file
		IEditorPart editor = null;
		if (editorDesc == null) {
			editor = openEditor(page, file, activate);
		} else {
			editor = page.openEditor(new FileEditorInput(file), editorDesc.getId(), activate);
		}
		
		// get the editor to update its position based on the marker
		if (editor != null) {
			gotoMarker(editor, marker);
		}
		
		return editor;
	}
	
	/**
	 * Sets the default editor id for a given file.  This value will be used
	 * to determine the default editor descriptor for the file in future calls to
	 * <code>getDefaultEditor(IFile)</code>.
	 *
	 * @param file the file
	 * @param editorId the editor id
	 */
	public static void setDefaultEditor(IFile file, String editorID) {
		try {
			file.setPersistentProperty(EDITOR_KEY,editorID);
		} catch (CoreException e) {}
	}
	
	/**
	 * Returns the default editor for a given file.
	 * <p>
	 * A default editor id may be registered for a specific file using
	 * <code>setDefaultEditor</code>.  If the given file has a registered
	 * default editor id the default editor will derived from it.  If not, 
	 * the default editor is determined by taking the file name for the 
	 * file and obtaining the default editor for that name.
	 * </p>
	 *
	 * @param file the file
	 * @return the descriptor of the default editor, or <code>null</code> if not
	 *   found
	 */
	public static IEditorDescriptor getDefaultEditor(IFile file) {
		// Try file specific editor.
		IEditorRegistry editorReg = PlatformUI.getWorkbench().getEditorRegistry();
		try {
			String editorID = file.getPersistentProperty(EDITOR_KEY);
			if (editorID != null) {
				IEditorDescriptor desc = editorReg.findEditor(editorID);
				if (desc != null)
					return desc;
			}
		} catch (CoreException e) {
		}
		
		// Try lookup with filename
		return editorReg.getDefaultEditor(file.getName());
	}

	/**
	 * Extracts and returns the <code>IResource</code>s in the given
	 * selection or the resource objects they adapts to.
	 * 
	 * @param originalSelection the original selection, possibly empty
	 * @return list of resources (element type: <code>IResource</code>), 
	 *    possibly empty
	 */
	public static List computeSelectedResources(IStructuredSelection originalSelection) {
		List resources = null;
		for (Iterator e = originalSelection.iterator(); e.hasNext();) {
			Object next = e.next();
			Object resource = null;
			if (next instanceof IResource) {
				resource = next;
			} else if (next instanceof IAdaptable) {
				resource = ((IAdaptable) next).getAdapter(IResource.class);
			}
			if (resource != null) {
				if (resources == null) {
					// lazy init to avoid creating empty lists
					// assume selection contains mostly resources most times
					resources = new ArrayList(originalSelection.size());
				}
				resources.add(resource);
			}
		}
		if (resources == null ) {
			return emptyUnmodifiableList;
		} else {
			return resources;
		}
	}
	
}
