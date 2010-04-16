/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Nikolay Botev - bug 240651
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.internal.provisional.action.ICoolBarManager2;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.registry.EditorDescriptor;

public class EditorReference extends WorkbenchPartReference implements IEditorReference {

	private static String MEMENTO_KEY = "memento"; //$NON-NLS-1$

	private IEditorInput input;
	private EditorDescriptor descriptor;
	private String factoryId;
	private EditorSite editorSite;

	EditorReference(IEclipseContext windowContext, IWorkbenchPage page, MPart part,
			IEditorInput input, EditorDescriptor descriptor) {
		super(windowContext, page, part);
		this.input = input;
		this.descriptor = descriptor;

		if (input != null) {
			IPersistableElement persistable = input.getPersistable();
			if (persistable != null) {
				factoryId = persistable.getFactoryId();
				XMLMemento root = XMLMemento.createWriteRoot("editor"); //$NON-NLS-1$
				root.putString(IWorkbenchConstants.TAG_ID, descriptor.getId());
				IMemento inputMem = root.createChild(IWorkbenchConstants.TAG_INPUT);
				inputMem.putString(IWorkbenchConstants.TAG_FACTORY_ID, persistable.getFactoryId());
				persistable.saveState(inputMem);
				StringWriter writer = new StringWriter();
				try {
					root.save(writer);
					part.getPersistedState().put(MEMENTO_KEY, writer.toString());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		if (descriptor == null) {
			try {
				XMLMemento createReadRoot = XMLMemento.createReadRoot(new StringReader(getModel()
						.getPersistedState().get(MEMENTO_KEY)));
				IEditorRegistry registry = getPage().getWorkbenchWindow().getWorkbench()
						.getEditorRegistry();
				this.descriptor = (EditorDescriptor) registry.findEditor(createReadRoot
						.getString(IWorkbenchConstants.TAG_ID));
				setImageDescriptor(this.descriptor.getImageDescriptor());
			} catch (WorkbenchException e) {
				WorkbenchPlugin.log(e);
			}
		} else {
			setImageDescriptor(this.descriptor.getImageDescriptor());
		}
	}

	public EditorDescriptor getDescriptor() {
		return descriptor;
	}

	public String getFactoryId() {
		IEditorPart editor = getEditor(false);
		if (editor != null) {
			IPersistableElement persistable = editor.getEditorInput().getPersistable();
			if (persistable != null) {
				return persistable.getFactoryId();
			}
			return null;
		}
		return factoryId;
	}

	public String getName() {
		return input.getName();
	}

	private IEditorInput restoreInput(IMemento editorMem) throws PartInitException {
		IMemento inputMem = editorMem.getChild(IWorkbenchConstants.TAG_INPUT);
		String factoryID = null;
		if (inputMem != null) {
			factoryID = inputMem.getString(IWorkbenchConstants.TAG_FACTORY_ID);
		}
		if (factoryID == null) {
			throw new PartInitException(NLS.bind(
					WorkbenchMessages.EditorManager_no_input_factory_ID, getId(), getName()));
		}
		IAdaptable input = null;
		IElementFactory factory = PlatformUI.getWorkbench().getElementFactory(factoryID);
		if (factory == null) {
			throw new PartInitException(NLS.bind(
					WorkbenchMessages.EditorManager_bad_element_factory, new Object[] { factoryID,
							getId(), getName() }));
		}

		// Get the input element.
		input = factory.createElement(inputMem);
		if (input == null) {
			throw new PartInitException(NLS.bind(
					WorkbenchMessages.EditorManager_create_element_returned_null, new Object[] {
							factoryID, getId(), getName() }));
		}
		if (!(input instanceof IEditorInput)) {
			throw new PartInitException(NLS.bind(
					WorkbenchMessages.EditorManager_wrong_createElement_result, new Object[] {
							factoryID, getId(), getName() }));
		}
		return (IEditorInput) input;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorReference#getEditor(boolean)
	 */
	public IEditorPart getEditor(boolean restore) {
		return (IEditorPart) getPart(restore);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorReference#isPinned()
	 */
	public boolean isPinned() {
		// FIXME compat implement pinning
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorReference#getEditorInput()
	 */
	public IEditorInput getEditorInput() throws PartInitException {
		if (input == null) {
			XMLMemento createReadRoot;
			try {
				createReadRoot = XMLMemento.createReadRoot(new StringReader(getModel()
						.getPersistedState().get(MEMENTO_KEY)));
				input = restoreInput(createReadRoot);
			} catch (WorkbenchException e) {
				throw new PartInitException(e.getStatus());
			}
		}
		return input;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.e4.compatibility.WorkbenchPartReference#createPart
	 * ()
	 */
	@Override
	public IWorkbenchPart createPart() throws PartInitException {
		try {
			return descriptor.createEditor();
		} catch (CoreException e) {
			IStatus status = e.getStatus();
			throw new PartInitException(new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH,
					status.getCode(), status.getMessage(), status.getException()));
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.e4.compatibility.WorkbenchPartReference#initialize
	 * (org.eclipse.ui.IWorkbenchPart)
	 */
	@Override
	public void initialize(IWorkbenchPart part) throws PartInitException {
		editorSite = new EditorSite(getModel(), part, descriptor
				.getConfigurationElement());
		ContextInjectionFactory.inject(editorSite, getModel().getContext());
		editorSite.setActionBars(createEditorActionBars((WorkbenchPage) getPage(), descriptor,
				editorSite));
		((IEditorPart) part).init(editorSite, getEditorInput());
	}

	@Override
	public PartSite getSite() {
		return editorSite;
	}

	private static HashMap<String, EditorActionBars> actionCache = new HashMap<String, EditorActionBars>();

	/*
	 * Creates the action bars for an editor. Editors of the same type should
	 * share a single editor action bar, so this implementation may return an
	 * existing action bar vector.
	 */
	private static EditorActionBars createEditorActionBars(WorkbenchPage page,
			EditorDescriptor desc, final IEditorSite site) {
		// Get the editor type.
		String type = desc.getId();

		// If an action bar already exists for this editor type return it.
		EditorActionBars actionBars = actionCache.get(type);
		if (actionBars != null) {
			actionBars.addRef();
			return actionBars;
		}

		// Create a new action bar set.
		actionBars = new EditorActionBars(page, site.getWorkbenchWindow(), type);
		actionBars.addRef();
		actionCache.put(type, actionBars);

		// Read base contributor.
		IEditorActionBarContributor contr = desc.createActionBarContributor();
		if (contr != null) {
			actionBars.setEditorContributor(contr);
			contr.init(actionBars, page);
		}

		// Read action extensions.
		EditorActionBuilder builder = new EditorActionBuilder();
		contr = builder.readActionExtensions(desc);
		if (contr != null) {
			actionBars.setExtensionContributor(contr);
			contr.init(actionBars, page);
		}

		// Return action bars.
		return actionBars;
	}

	public static void disposeEditorActionBars(EditorActionBars actionBars) {
		actionBars.removeRef();
		if (actionBars.getRef() <= 0) {
			String type = actionBars.getEditorType();
			actionCache.remove(type);
			// refresh the cool bar manager before disposing of a cool item
			ICoolBarManager2 coolBar = (ICoolBarManager2) ((WorkbenchWindow) actionBars.getPage()
					.getWorkbenchWindow()).getCoolBarManager2();
			if (coolBar != null) {
				coolBar.refresh();
			}
			actionBars.dispose();
		}
	}
}
