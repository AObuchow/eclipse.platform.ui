package org.eclipse.ui.texteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.help.WorkbenchHelp;


/**
 * An action which configures its label, image, tooltip, and description from
 * a resource bundle using known keys.
 * <p>
 * Clients may subclass this abstract class to define new kinds of actions. As
 * with <code>Action</code>, subclasses must implement the 
 * <code>IAction.run</code> method to carry out the action's semantics.
 * </p>
 */
public abstract class ResourceAction extends Action {
	
	/**
	 * Creates a new action that configures itself from the given resource
	 * bundle.
	 * <p>
	 * The following keys, prepended by the given option prefix,
	 * are used for retrieving resources from the given bundle:
	 * <ul>
	 *   <li><code>"label"</code> - <code>setText</code></li>
	 *   <li><code>"tooltip"</code> - <code>setToolTipText</code></li>
	 *   <li><code>"image"</code> - <code>setImageDescriptor</code></li>
	 *   <li><code>"description"</code> - <code>setDescription</code></li>
	 * </ul>
	 * </p>
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys, or 
	 *   <code>null</code> if none
	 */
	public ResourceAction(ResourceBundle bundle, String prefix) {
		super();
		
		String labelKey= "label";
		String tooltipKey= "tooltip";
		String imageKey= "image";
		String descriptionKey= "description";
		
		if (prefix != null && prefix.length() > 0) {
			labelKey= prefix + labelKey;
			tooltipKey= prefix + tooltipKey;
			imageKey= prefix + imageKey;
			descriptionKey= prefix + descriptionKey;
		}
		
		setText(getString(bundle, labelKey, labelKey));
		setToolTipText(getString(bundle, tooltipKey, null));
		setDescription(getString(bundle, descriptionKey, null));
		
		String file= getString(bundle, imageKey, null);
		if (file != null && file.trim().length() > 0)
			setImageDescriptor(ImageDescriptor.createFromFile(getClass(), file));
	}
	/**
	 * Retrieves and returns the value with the given key from the given resource 
	 * bundle, or returns the given default value if there is no such resource.
	 * Convenience method for dealing gracefully with missing resources.
	 *
	 * @param bundle the resource bundle
	 * @param key the resource key
	 * @param defaultValue the default value, or <code>null</code>
	 * @return the resource value, or the given default value (which may be
	 *   <code>null</code>)
	 */
	protected static String getString(ResourceBundle bundle, String key, String defaultValue) {
		
		String value= defaultValue;
		try {
			value= bundle.getString(key);
		} catch (MissingResourceException x) {
		}
		
		return value;
	}
	/**
	 * Sets the action's help context id.
	 * 
	 * @param contextId the help context id
	 */
	public final void setHelpContextId(String contextId) {
		WorkbenchHelp.setHelp(this, new Object[] { contextId });
	}
}
