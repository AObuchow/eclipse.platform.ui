package org.eclipse.ui.internal.registry;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.*;
import org.eclipse.swt.program.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.misc.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.swt.widgets.*;

import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Provides access to the collection of defined editors for
 * resource types.
 */
public class EditorRegistry implements IEditorRegistry {

	/* Cached images - these include images from registered editors (via plugins) and others
	 * hence this table is not one to one with the mappings table. It is in fact a superset
	 * of the keys one would find in typeEditorMappings
	 */
	private Map extensionImages = new HashMap();
	
	/* Vector of EditorDescriptor - all the editors loaded from plugin files.
	 * The list is kept in order to be able to show in the editor selection dialog of the resource associations page.
	 */
	private List sortedEditorsFromPlugins = new ArrayList();

	// Hashtable of EditorDescriptor - map editor id to editor.
	private Map mapIDtoEditor = initialIdToEditorMap(10);

	// Vector of FileEditorMapping (extension to collection of EditorDescriptor)
	private Map typeEditorMappings;

	// Vector for prop changed listeners.
	private ListenerList propChangeListeners = new ListenerList();

	// Key for the EditorID save as a IFile persistent property
	private static final QualifiedName EDITOR_KEY = new QualifiedName("org.eclipse.ui.internal.registry.ResourceEditorRegistry","EditorProperty");//$NON-NLS-2$//$NON-NLS-1$
/**
 * Return an instance of the receiver.
 */
public EditorRegistry() {
	super();
	initializeFromStorage();
}
/**
 * Add an editor for the given extensions with the specified (possibly null)
 * extended type. The editor is being registered from a plugin
 *
 * @param editor        The description of the editor (as obtained 
 *                      from the plugin file and built by the registry reader)
 * @param extensions    Collection of file extensions the editor applies to
 * @param filenames     Collection of filenames the editor applies to
 * @param bDefault      Indicates whether the editor should be made the default editor
 *                      and hence appear first inside a FileEditorMapping
 *
 * This method is not API and should not be called outside the workbench code.
 */
public void addEditorFromPlugin(EditorDescriptor editor, 
	List extensions, List filenames, boolean bDefault) {

	// record it in our quick reference list
	sortedEditorsFromPlugins.add(editor);

	// add it to the table of mappings
	Iterator enum = extensions.iterator();
	while(enum.hasNext()) {
		String fileExtension = (String)enum.next();
		
		if (fileExtension != null && fileExtension.length()>0) {
			FileEditorMapping mapping = getMappingFor("*." + fileExtension);//$NON-NLS-1$
			if (mapping == null) {  // no mapping for that extension
				mapping = new FileEditorMapping(fileExtension);
				typeEditorMappings.put(mappingKeyFor(mapping), mapping);
			}
			mapping.addEditor(editor);
			if (bDefault)
				mapping.setDefaultEditor(editor);
		}
	}

	// add it to the table of mappings
	enum = filenames.iterator();
	while(enum.hasNext()) {
		String filename = (String)enum.next();
		
		if (filename != null && filename.length() > 0) {
			FileEditorMapping mapping = getMappingFor(filename);
			if (mapping == null) {  // no mapping for that extension
				String name;
				String extension;
				int index = filename.indexOf('.');
				if (index < 0) {
					name = filename;
					extension = "";//$NON-NLS-1$
				}
				else {
					name = filename.substring(0, index);
					extension = filename.substring(index + 1);
				}
				mapping = new FileEditorMapping(name, extension);
				typeEditorMappings.put(mappingKeyFor(mapping), mapping);
			}
			mapping.addEditor(editor);
			if (bDefault)
				mapping.setDefaultEditor(editor);
		}
	}

	// Update editor map.
	mapIDtoEditor.put(editor.getId(), editor);
}
/**
 * Add external editors to the editor mapping.
 */
private void addExternalEditorsToEditorMap() {
	Iterator enum = null;
	IEditorDescriptor desc = null;
	
	// Add registered editors (may include external editors).
	enum = typeEditorMappings.values().iterator();
	while (enum.hasNext()) {
		FileEditorMapping map = (FileEditorMapping)enum.next();
		IEditorDescriptor [] descArray = map.getEditors();
		for (int n = 0; n < descArray.length; n++) {
			desc = descArray[n];
			mapIDtoEditor.put(desc.getId(), desc);
		}
	}
}
/* (non-Javadoc)
 * Method declared on IEditorRegistry.
 */
public void addPropertyListener(IPropertyListener l) {
	propChangeListeners.add(l);
}
/* (non-Javadoc)
 * Method declared on IEditorRegistry.
 */
public IEditorDescriptor findEditor(String id) {
	return (IEditorDescriptor)mapIDtoEditor.get(id);
}
/**
 * Fires a property changed event.
 */
private void firePropertyChange(final int type) {
	Object [] array = propChangeListeners.getListeners();
	for (int nX = 0; nX < array.length; nX ++) {
		final IPropertyListener l = (IPropertyListener)array[nX];
		Platform.run(new SafeRunnableAdapter() {
			public void run() {
				l.propertyChanged(EditorRegistry.this, type);
			}
			public void handleException(Throwable e) {
				super.handleException(e);
				//If and unexpected exception happens, remove it
				//to make sure the workbench keeps running.
				propChangeListeners.remove(l);
			}
		});
	}
}
/* (non-Javadoc)
 * Method declared on IEditorRegistry.
 */
public IEditorDescriptor getDefaultEditor() {
	// Find the default editor descriptor and return it.
	IEditorDescriptor desc = findEditor(IWorkbenchConstants.DEFAULT_EDITOR_ID);
	if (desc != null)
		return desc;

	// Panic: the default editor cannot be found.
	MessageDialog.openError((Shell)null, 
		"Editor Problems", //$NON-NLS-1$
		"Unable to find default editor.");//$NON-NLS-1$
	return null;
}
/* (non-Javadoc)
 * Method declared on IEditorRegistry.
 */
public IEditorDescriptor getDefaultEditor(String filename) {
	FileEditorMapping[] mapping = getMappingForFilename(filename);
	if (mapping[0] != null)
		return mapping[0].getDefaultEditor();
	if (mapping[1] != null)
		return mapping[1].getDefaultEditor();
	return null;
}
/* (non-Javadoc)
 * Method declared on IEditorRegistry.
 */
public IEditorDescriptor getDefaultEditor(IFile file) {
	// Try file specific editor.
	try {
		String editorID = file.getPersistentProperty(EDITOR_KEY);
		if (editorID != null) {
			IEditorDescriptor desc = findEditor(editorID);
			if (desc != null)
				return desc;
		}
	} catch (CoreException e) {
	}
	
	// Try lookup with filename
	return getDefaultEditor(file.getName());
}
/**
 * Returns the default file image.
 */
private ImageDescriptor getDefaultImage() {
	return WorkbenchImages.getImageDescriptor(ISharedImages.IMG_OBJ_FILE);
}
/* (non-Javadoc)
 * Method declared on IEditorRegistry.
 */
public IEditorDescriptor[] getEditors(String filename) {
	IEditorDescriptor[] editors = new IEditorDescriptor[0];
	IEditorDescriptor[] filenameEditors = editors;
	IEditorDescriptor[] extensionEditors = editors;

	FileEditorMapping mapping[] = getMappingForFilename(filename);
	if (mapping[0] != null) {
		editors = mapping[0].getEditors();
		if (editors != null)
			filenameEditors = editors;
	}
	if (mapping[1] != null) {
		editors = mapping[1].getEditors();
		if (editors != null)
			extensionEditors = editors;
	}

	editors = new IEditorDescriptor[filenameEditors.length + extensionEditors.length];
	System.arraycopy(filenameEditors, 0, editors, 0, filenameEditors.length);
	System.arraycopy(extensionEditors, 0, editors, filenameEditors.length, extensionEditors.length);
	return editors;
}
/* (non-Javadoc)
 * Method declared on IEditorRegistry.
 */
public IEditorDescriptor[] getEditors(IFile element) {
	return getEditors(element.getName());
}
/* (non-Javadoc)
 * Method declared on IEditorRegistry.
 */
public IFileEditorMapping[] getFileEditorMappings() {
	List sortedMappings = sortedTypeEditorMappings(); // sort hash table elements
	IFileEditorMapping[] array = new IFileEditorMapping[sortedMappings.size()];
	sortedMappings.toArray(array);
	return array;
}
/* (non-Javadoc)
 * Method declared on IEditorRegistry.
 */
public ImageDescriptor getImageDescriptor(String filename) {
	if (filename == null)
		return getDefaultImage();

	// Lookup in the cache first... 
	String key = mappingKeyFor(filename);
	ImageDescriptor anImage = (ImageDescriptor)extensionImages.get(key);
	if (anImage != null)
		return anImage;

	// See if we have a mapping for the filename or extension
	FileEditorMapping[] mapping = getMappingForFilename(filename);
	for (int i = 0; i < 2; i++) {
		if (mapping[i] != null) {
			// Lookup in the cache first...
			String mappingKey = mappingKeyFor(mapping[i]);
			ImageDescriptor mappingImage = (ImageDescriptor)extensionImages.get(key);
			if (mappingImage != null)
				return mappingImage;
			// Create it and cache it
			IEditorDescriptor editor = mapping[i].getDefaultEditor();
			if (editor != null) {
				mappingImage = editor.getImageDescriptor();
				extensionImages.put(mappingKey, mappingImage);
				return mappingImage;
			}
		}
	}   

	// Nothing - time to look externally for the icon
	anImage = getSystemEditorImageDescriptor(filename);
	if (anImage == null)
		anImage = getDefaultImage();
	extensionImages.put(key, anImage);
	return anImage;
}
/* (non-Javadoc)
 * Method declared on IEditorRegistry.
 */
public ImageDescriptor getImageDescriptor(IFile element) {
	return getImageDescriptor(element.getName());
}
/**
 * Find the file editor mapping for the type. Returns
 * null if not found.
 */
private FileEditorMapping getMappingFor(String type) {
	if (type == null)
		return null;
	String key = mappingKeyFor(type);
	return (FileEditorMapping) typeEditorMappings.get(key);
}
/**
 * Find the file editor mappings for the given filename.
 *
 * Return an array of two FileEditorMapping items, where
 * the first mapping is for the entire filename, and the
 * second mapping is for the filename's extension only.
 * These items can be null if no mapping exist on the
 * filename and/or filename's extension.
 */
private FileEditorMapping[] getMappingForFilename(String filename) {
	FileEditorMapping[] mapping = new FileEditorMapping[2];
	
	// Lookup on entire filename
	mapping[0] = getMappingFor(filename);
	
	// Lookup on filename's extension
	int index = filename.lastIndexOf('.');
	if (index > -1) {
		String extension = filename.substring(index);
		mapping[1] = getMappingFor("*" + extension);//$NON-NLS-1$
	}
	
	return mapping;
}
/* 
 * WARNING!
 * The image described by each editor descriptor is *not* known by
 * the workbench's graphic registry.
 * Therefore clients must take care to ensure that if they access
 * any of the images held by these editors that they also dispose them
 */
 
public IEditorDescriptor[] getSortedEditorsFromOS() {
	List externalEditors = new ArrayList();
	Program [] programs = Program.getPrograms();

	List localNames = new ArrayList();
	for (int i = 0; i < programs.length; i++){
		//1FPLRL2: ITPUI:WINNT - NOTEPAD editor cannot be launched
		//Some entries start with %SystemRoot%
		//For such cases just use the file name as they are generally
		//in directories which are on the path
		/*if (fileName.charAt(0) == '%') {
			fileName = name + ".exe";
		}   */
		
		EditorDescriptor editor = new EditorDescriptor();
		editor.setProgram(programs[i]);

		// determine the program icon this editor would need (do not let it be cached in the workbench registry)
		ImageDescriptor desc = new ExternalProgramImageDescriptor(programs[i]);
		editor.setImageDescriptor(desc);
		externalEditors.add(editor);
	}

	Object[] tempArray = sortEditors(externalEditors);
	IEditorDescriptor[] array = new IEditorDescriptor[externalEditors.size()];
	for (int i = 0; i < tempArray.length; i++){
		array[i] = (IEditorDescriptor)tempArray[i];
	}
	return array;
}
/**
 *
 */
public IEditorDescriptor[] getSortedEditorsFromPlugins() {
	IEditorDescriptor[] array = new IEditorDescriptor[sortedEditorsFromPlugins.size()];
	sortedEditorsFromPlugins.toArray(array);
	return array;   
		
}
/*
 * Return the image descriptor of the system editor
 * that is registered with the OS to edit files of
 * this type. Null if none can be found.
 */
public ImageDescriptor getSystemEditorImageDescriptor(String filename) {
	int extensionIndex = filename.indexOf('.');
	Program externalProgram = null;
	if (extensionIndex >= 0)
		externalProgram = Program.findProgram(filename.substring(extensionIndex));
	if (externalProgram == null)
		return null;
	else
		return new ExternalProgramImageDescriptor(externalProgram);
}
/*
 * Return the image descriptor of the system editor
 * that is registered with the OS to edit files of
 * this type. Null if none can be found.
 */
public ImageDescriptor getSystemEditorImageDescriptor(IFile element) {
	return getSystemEditorImageDescriptor(element.getName());
}
/**
 * Answer an intial id to editor map.
 */
private HashMap initialIdToEditorMap(int initialSize) {
	HashMap map = new HashMap(initialSize);
	map.put(IWorkbenchConstants.SYSTEM_EDITOR_ID, EditorDescriptor.getSystemEditorDescriptor());
	return map;
}
private void initializeFromStorage () {
	typeEditorMappings = new HashMap();
	extensionImages = new HashMap();
	
	//Get editors from the registry
	EditorRegistryReader registryReader = new EditorRegistryReader();
	registryReader.addEditors(true, this);
	sortInternalEditors();
	rebuildInternalEditorMap();

	loadAssociations(); //get saved earlier state
	addExternalEditorsToEditorMap();
}
/**
 * Load the serialized resource associations
 * Return true if the operation was successful, false otherwise
 */
private boolean loadAssociations() {

	//Get the workbench plugin's working directory
	WorkbenchPlugin workbenchPlugin = WorkbenchPlugin.getDefault();
	IPluginDescriptor workbenchPluginDescriptor = workbenchPlugin.getDescriptor();
	IPath workbenchStatePath = workbenchPlugin.getStateLocation();

	//Get the editors and validate each one
	Map editorTable = new HashMap();
	InputStreamReader reader = null;

	try {
		FileInputStream stream = new FileInputStream(workbenchStatePath.append(IWorkbenchConstants.EDITOR_FILE_NAME).toOSString());
	 	reader = new InputStreamReader(stream, "utf-8");
		XMLMemento memento = XMLMemento.createReadRoot(reader);
		EditorDescriptor editor;
		IMemento[] edMementos = memento.getChildren(IWorkbenchConstants.TAG_DESCRIPTOR);
		for (int i = 0; i < edMementos.length; i++) {
			editor = new EditorDescriptor();
			editor.loadValues(edMementos[i]);

			if (editor.getPluginID() != null) {
				//If the editor is prom a plugin we use its ID to look it up in the mapping of editors we
				//have obtained from plugins. This allows us to verify that the editor is still valid
				//and allows us to get the editor description from the mapping table which has
				//a valid config element field.
				EditorDescriptor validEditorDescritor =
					(EditorDescriptor) mapIDtoEditor.get(editor.getId());
				if (validEditorDescritor != null) {
					editorTable.put(validEditorDescritor.getId(), validEditorDescritor);
				}
			} else { //This is either from a program or a user defined editor
				ImageDescriptor descriptor;
				if (editor.getProgram() == null)
					descriptor = new ProgramImageDescriptor(editor.getFileName(), 0);
				else
					descriptor = new ExternalProgramImageDescriptor(editor.getProgram());
				editor.setImageDescriptor(descriptor);
				editorTable.put(editor.getId(), editor);
			}
		}
	} catch (IOException e) {
		try {
			if (reader != null)
				reader.close();
		} catch (IOException ex) {
		}
		//Ignore this as the workbench may not yet have saved any state
		return false;
	}

	//Get the resource types
	reader = null;
	try {
		FileInputStream stream = new FileInputStream(
			workbenchStatePath
				.append(IWorkbenchConstants.RESOURCE_TYPE_FILE_NAME)
				.toOSString());
	 	reader = new InputStreamReader(stream, "utf-8");
		XMLMemento memento = XMLMemento.createReadRoot(reader);
		IMemento[] extMementos = memento.getChildren(IWorkbenchConstants.TAG_INFO);
		for (int i = 0; i < extMementos.length; i++) {
			String name = extMementos[i].getString(IWorkbenchConstants.TAG_NAME);
			if (name == null)
				name = "*";//$NON-NLS-1$
			String extension = extMementos[i].getString(IWorkbenchConstants.TAG_EXTENSION);
			IMemento[] idMementos =
				extMementos[i].getChildren(IWorkbenchConstants.TAG_EDITOR);
			String[] editorIDs = new String[idMementos.length];
			for (int j = 0; j < idMementos.length; j++) {
				editorIDs[j] = idMementos[j].getString(IWorkbenchConstants.TAG_ID);
			}
			FileEditorMapping mapping = getMappingFor(name + "." + extension);//$NON-NLS-1$
			if (mapping == null) {
				mapping = new FileEditorMapping(name, extension);
			}
			List editors = new ArrayList();
			for (int j = 0; j < editorIDs.length; j++) {
				if (editorIDs[j] != null) {
					EditorDescriptor editor = (EditorDescriptor) editorTable.get(editorIDs[j]);
					if (editor != null) {
						editors.add(editor);
					}
				}
			}

			//Add any new editors that have already been resd from the registry
			IEditorDescriptor[] editorsArray = mapping.getEditors();
			for (int j = 0; j < editorsArray.length; j++) {
				if (!editors.contains(editorsArray[j])) {
					editors.add(editorsArray[j]);
				}
			}

			mapping.setEditorsVector(editors);
			typeEditorMappings.put(mappingKeyFor(mapping), mapping);
		}
	} catch (IOException e) {
		try {
			if (reader != null)
				reader.close();
		} catch (IOException ex) {
		}
		MessageDialog.openError(
			(Shell) null,
			"Error",//$NON-NLS-1$
			"Unable to load resource associations.");//$NON-NLS-1$
		return false;
	}
	return true;
}
/*
 * 
 */
private String mappingKeyFor(String type) {
	//Unix issue
	return type.toLowerCase();
}
/**
 * Return a key that combines the file's name and extension
 * of the given mapping
 */
private String mappingKeyFor(FileEditorMapping mapping) {
	return mappingKeyFor(mapping.getName() + "." + mapping.getExtension());//$NON-NLS-1$
}
/**
 * Rebuild the editor map
 */
private void rebuildEditorMap() {
	rebuildInternalEditorMap();
	addExternalEditorsToEditorMap();
}
/**
 * Rebuild the internal editor mapping.
 */
private void rebuildInternalEditorMap() 
{
	Iterator enum = null;
	IEditorDescriptor desc = null;
	
	// Allocate a new map.
	mapIDtoEditor = initialIdToEditorMap(mapIDtoEditor.size());

	// Add plugin editors.
	enum = sortedEditorsFromPlugins.iterator();
	while (enum.hasNext()) {
		desc = (IEditorDescriptor)enum.next();
		mapIDtoEditor.put(desc.getId(), desc);
	}
}
/* (non-Javadoc)
 * Method declared on IEditorRegistry.
 */
public void removePropertyListener(IPropertyListener l) {
	propChangeListeners.remove(l);
}
/**
 * Save the registry to the filesystem by serializing
 * the current resource associations.
 */
public void saveAssociations () {
	//Get the workbench plugin's working directory
	IPath workbenchStatePath = WorkbenchPlugin.getDefault().getStateLocation();
	//Save the resource type descriptions
	List editors = new ArrayList();
	
	XMLMemento memento = XMLMemento.createWriteRoot(IWorkbenchConstants.TAG_EDITORS);
	Iterator enum = typeEditorMappings.values().iterator();

	while (enum.hasNext()) {
		FileEditorMapping type = (FileEditorMapping)enum.next();
		IMemento editorMemento = memento.createChild(IWorkbenchConstants.TAG_INFO);
		editorMemento.putString(IWorkbenchConstants.TAG_NAME,type.getName());
		editorMemento.putString(IWorkbenchConstants.TAG_EXTENSION,type.getExtension());
		IEditorDescriptor[] editorArray = type.getEditors();
		for (int i = 0; i < editorArray.length; i++){
			EditorDescriptor editor = (EditorDescriptor)editorArray[i];
			if (!editors.contains(editor)) {
				editors.add(editor); 
			}
			IMemento idMemento = editorMemento.createChild(IWorkbenchConstants.TAG_EDITOR);
			idMemento.putString(IWorkbenchConstants.TAG_ID,editorArray[i].getId());
		}
	}
	OutputStreamWriter writer = null;
	try {
		FileOutputStream stream = new FileOutputStream(workbenchStatePath.append(IWorkbenchConstants.RESOURCE_TYPE_FILE_NAME).toOSString());
	 	writer = new OutputStreamWriter(stream, "utf-8");
		memento.save(writer);
		writer.close();
	} catch(IOException e) {
		try{
			if(writer != null) writer.close();
		} catch(IOException ex) {}
		MessageDialog.openError((Shell)null, 
			"Saving Problems", //$NON-NLS-1$
			"Unable to save resource associations.");//$NON-NLS-1$
		return;
	}   
	

	memento = XMLMemento.createWriteRoot(IWorkbenchConstants.TAG_EDITORS);
	enum = editors.iterator();
	while (enum.hasNext()) {
		EditorDescriptor editor = (EditorDescriptor)enum.next();
		IMemento editorMemento = memento.createChild(IWorkbenchConstants.TAG_DESCRIPTOR);
		editor.saveValues(editorMemento);
	}
	writer = null;
	try {
		FileOutputStream stream = new FileOutputStream(workbenchStatePath.append(IWorkbenchConstants.EDITOR_FILE_NAME).toOSString());
	 	writer = new OutputStreamWriter(stream, "utf-8");
		memento.save(writer);
		writer.close();
	} catch(IOException e) {
		try{
			if(writer != null) writer.close();
		} catch(IOException ex) {}
		MessageDialog.openError((Shell)null,
			"Error",//$NON-NLS-1$
			"Unable to save resource associations.");//$NON-NLS-1$
		return;
	}       
}
/* (non-Javadoc)
 * Method declared on IEditorRegistry.
 */
public void setDefaultEditor(IFile file, String editorID) {
	try {
		file.setPersistentProperty(EDITOR_KEY,editorID);
	} catch (CoreException e) {}
}
/**
 * Set the collection of FileEditorMappings. 
 * The given collection is converted into the internal hash table for faster lookup
 * Each mapping goes from an extension to the collection of editors that work on it.
 */

public void setFileEditorMappings(FileEditorMapping[] newResourceTypes) {
	typeEditorMappings = new HashMap();
	for (int i = 0;i < newResourceTypes.length;i++) {
		FileEditorMapping mapping = newResourceTypes[i];
		typeEditorMappings.put(mappingKeyFor(mapping), mapping);
	}
	extensionImages = new HashMap();
	rebuildEditorMap();
	firePropertyChange(PROP_CONTENTS);
}
/**
 * Alphabetically sort the internal editors
 */
private Object[] sortEditors(List unsortedList) {
	Object[] array = new Object[unsortedList.size()];
	unsortedList.toArray(array);
	
	Sorter s = new Sorter() {
		public boolean compare(Object o1, Object o2) {
			String s1 = ((IEditorDescriptor)o1).getLabel().toUpperCase();
			String s2 = ((IEditorDescriptor)o2).getLabel().toUpperCase();
			//Return true if elementTwo is 'greater than' elementOne
			return s2.compareTo(s1) > 0;
		}
	};
	return s.sort(array);

}
/**
 * Convenience Method
 * Alphabetically sort the FileEditorMappings.
 * The mappings are kept in a hash table for fast lookup. Sorting is
 * typically only needed for certain dialogs/choices etc.
 */
private List sortedTypeEditorMappings() {
	
	Object[] array = new Object[typeEditorMappings.size()];
	Iterator enum = typeEditorMappings.values().iterator();  // enumeration of FileEditorMapping
	int j = 0;
	while (enum.hasNext()) {
		array[j++] = enum.next();
	}
	
	Sorter s = new Sorter() {
		public boolean compare(Object o1, Object o2) {
			String s1 = ((FileEditorMapping)o1).getLabel().toUpperCase();
			String s2 = ((FileEditorMapping)o2).getLabel().toUpperCase();
			//Return true if elementTwo is 'greater than' elementOne
			return s2.compareTo(s1) > 0;
		}
	};
	array = s.sort(array);
	List result = new ArrayList();  // vector of FileEditorMapping
	for (int i = 0; i < array.length; i++) {
		result.add(array[i]);
	}
	return result;
}
/**
 * Alphabetically sort the internal editors
 */
private void sortInternalEditors() {
	Object[] array = sortEditors(sortedEditorsFromPlugins);
	sortedEditorsFromPlugins = new ArrayList();
	for (int i = 0; i < array.length; i++) {
		sortedEditorsFromPlugins.add(array[i]);
	}
}
}
