package org.eclipse.ui.examples.readmetool;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import java.net.MalformedURLException;
import java.net.URL;
/**
 * Convenience class for storing references to image descriptors
 * used by the readme tool.
 */
public class ReadmeImages {
	static final URL BASE_URL = ReadmePlugin.getDefault().getDescriptor().getInstallURL();
	static final ImageDescriptor EDITOR_ACTION1_IMAGE;
	static final ImageDescriptor EDITOR_ACTION2_IMAGE;
	static final ImageDescriptor EDITOR_ACTION3_IMAGE;
	static final ImageDescriptor EDITOR_ACTION1_IMAGE_DISABLE;
	static final ImageDescriptor EDITOR_ACTION2_IMAGE_DISABLE;
	static final ImageDescriptor EDITOR_ACTION3_IMAGE_DISABLE;
	static final ImageDescriptor EDITOR_ACTION1_IMAGE_ENABLE;
	static final ImageDescriptor EDITOR_ACTION2_IMAGE_ENABLE;
	static final ImageDescriptor EDITOR_ACTION3_IMAGE_ENABLE;
	static final ImageDescriptor README_WIZARD_BANNER;

	static {
		String iconPath;
		if(Display.getCurrent().getIconDepth() > 4)
			iconPath = "icons/full/";//$NON-NLS-1$
		else
			iconPath = "icons/basic/";//$NON-NLS-1$
		
		String prefix = iconPath + "ctool16/";
		EDITOR_ACTION1_IMAGE = createImageDescriptor(prefix + "action1.gif");
		EDITOR_ACTION2_IMAGE = createImageDescriptor(prefix + "action2.gif");
		EDITOR_ACTION3_IMAGE = createImageDescriptor(prefix + "action3.gif");

		prefix = iconPath + "dtool16/";
		EDITOR_ACTION1_IMAGE_DISABLE = createImageDescriptor(prefix + "action1.gif");
		EDITOR_ACTION2_IMAGE_DISABLE = createImageDescriptor(prefix + "action2.gif");
		EDITOR_ACTION3_IMAGE_DISABLE = createImageDescriptor(prefix + "action3.gif");
		
		prefix = iconPath + "etool16/";
		EDITOR_ACTION1_IMAGE_ENABLE = createImageDescriptor(prefix + "action1.gif");
		EDITOR_ACTION2_IMAGE_ENABLE = createImageDescriptor(prefix + "action2.gif");
		EDITOR_ACTION3_IMAGE_ENABLE = createImageDescriptor(prefix + "action3.gif");
		
		prefix = iconPath + "wizban/";
		README_WIZARD_BANNER = createImageDescriptor(prefix + "newreadme_wiz.gif");
	}
/**
 * Utility method to create an <code>ImageDescriptor</code>
 * from a path to a file.
 */
private static ImageDescriptor createImageDescriptor(String path) {
	try {
		URL url = new URL(BASE_URL, path);
		return ImageDescriptor.createFromURL(url);
	} catch (MalformedURLException e) {
	}
	return ImageDescriptor.getMissingImageDescriptor();
}
}
