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

package org.eclipse.ui.tests.keys;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.ui.commands.ICommandManager;
import org.eclipse.ui.commands.IKeySequenceBinding;
import org.eclipse.ui.commands.NotDefinedException;
import org.eclipse.ui.tests.util.UITestCase;

/**
 * Tests Bug 36420
 * 
 * @since 3.0
 */
public class Bug36420Test extends UITestCase {

	/**
	 * Constructor for Bug36420Test.
	 * 
	 * @param name
	 *           The name of the test
	 */
	public Bug36420Test(String name) {
		super(name);
	}

	/**
	 * Tests that importing key preferences actually has an effect.
	 * 
	 * @throws CoreException
	 *            If the preferences can't be imported.
	 * @throws FileNotFoundException
	 *            If the temporary file is removed after it is created, but
	 *            before it is opened. (Wow)
	 * @throws IOException
	 *            If something fails during output of the preferences file.
	 * @throws NotDefinedException
	 *            If the command for which the preference is stored is not
	 *            defined.
	 */
	public void testImportKeyPreferences() throws CoreException, FileNotFoundException, IOException, NotDefinedException {
		String commandId = "org.eclipse.ui.window.nextView"; //$NON-NLS-1$
		String keySequenceText = "F S C K"; //$NON-NLS-1$

		/*
		 * DO NOT USE PreferenceMutator for this section. This test case must
		 * use these exact steps, while PreferenceMutator might use something
		 * else in the future.
		 */
		// Set up the preferences.
		Properties preferences = new Properties();
		String key = "org.eclipse.ui.workbench/org.eclipse.ui.commands"; //$NON-NLS-1$
		String value = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<org.eclipse.ui.commands><activeKeyConfiguration/><keyBinding commandId=\"" + commandId + "\" keySequence=\"" + keySequenceText + "\"/></org.eclipse.ui.commands>"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		preferences.put(key, value);
		IPluginDescriptor[] descriptors = Platform.getPluginRegistry().getPluginDescriptors();
		for (int i = 0; i < descriptors.length; i++) {
			preferences.put(descriptors[i].getUniqueIdentifier(), descriptors[i].getVersionIdentifier().toString());
		}

		// Export the preferences.
		File file = File.createTempFile("preferences", ".txt"); //$NON-NLS-1$//$NON-NLS-2$
		file.deleteOnExit();
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
		preferences.store(bos, null);
		bos.close();

		// Attempt to import the key binding.
		Preferences.importPreferences(new Path(file.getAbsolutePath()));
		/*
		 * END SECTION
		 */

		// Check to see that the key binding for the given command matches.
		ICommandManager manager = fWorkbench.getCommandManager();
		List keyBindings = manager.getCommand(commandId).getKeySequenceBindings();
		Iterator keyBindingItr = keyBindings.iterator();
		boolean found = false;
		while (keyBindingItr.hasNext()) {
			IKeySequenceBinding keyBinding = (IKeySequenceBinding) keyBindingItr.next();
			String currentText = keyBinding.getKeySequence().toString();
			if (keySequenceText.equals(currentText)) {
				found = true;
			}
		}

		assertTrue("Key binding not imported.", found); //$NON-NLS-1$
	}
}
