package org.eclipse.ui.internal.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IResourceActionFilter;
import org.eclipse.ui.actions.SimpleWildcardTester;
import java.util.Iterator;

/**
 * An IWorkbenchAdapter that represents IResources.
 */
public abstract class WorkbenchResource extends WorkbenchAdapter
	implements IResourceActionFilter
{
/**
 *	Answer the appropriate base image to use for the resource.
 */
protected abstract ImageDescriptor getBaseImage(IResource resource);
/**
 * Returns an image descriptor for this object.
 */
public ImageDescriptor getImageDescriptor(Object o) {
	IResource resource = getResource(o);
	return resource == null ? null : getBaseImage(resource);
}
/**
 * getLabel method comment.
 */
public String getLabel(Object o) {
	IResource resource = getResource(o);
	return resource == null ? null : resource.getName();
}
/**
 * Returns the parent of the given object.  Returns null if the
 * parent is not available.
 */
public Object getParent(Object o) {
	IResource resource = getResource(o);
	return resource == null ? null : resource.getParent();
}
/**
 * Returns the resource corresponding to this object,
 * or null if there is none.
 */
protected IResource getResource(Object o) {
	if (o instanceof IResource) {
		return (IResource)o;
	}
	if (o instanceof IAdaptable) {
		return (IResource)((IAdaptable)o).getAdapter(IResource.class);
	}
	return null;
}
/**
 * Returns whether the specific attribute matches the state of the target
 * object.
 *
 * @param target the target object
 * @param name the attribute name
 * @param value the attriute value
 * @return <code>true</code> if the attribute matches; <code>false</code> otherwise
 */
public boolean testAttribute(Object target, String name, String value) {
	IResource res;
	if ((target instanceof IResource) == true) {
		res = (IResource) target;
	}
	else {
		res = (IResource) ((IAdaptable) target).getAdapter(IResource.class);
	}
	if (name.equals(NAME)) {
		return SimpleWildcardTester.testWildcardIgnoreCase(value, 
			res.getName());
	} else if (name.equals(PATH)) {
		return SimpleWildcardTester.testWildcardIgnoreCase(value, 
			res.getFullPath().toString());
	} else if (name.equals(EXTENSION)) {
		return SimpleWildcardTester.testWildcardIgnoreCase(value, 
			res.getFileExtension());
	} else if (name.equals(READ_ONLY)) {
		value = value.toLowerCase();
		return (res.isReadOnly() == value.equals("true"));//$NON-NLS-1$
	} else if (name.equals(PROJECT_NATURE)) {
		try {
			return res.getProject().hasNature(value);
		} catch (CoreException e) {
			return false;		
		}
	}
	return false;
}
}
