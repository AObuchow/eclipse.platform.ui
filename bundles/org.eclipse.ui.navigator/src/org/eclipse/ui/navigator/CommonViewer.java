/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.navigator.NavigatorContentService;

/**
 * 
 * Provides the Tree Viewer for the Common Navigator. Content and labels are
 * provided by an instance of {@link INavigatorContentService}&nbsp; which uses
 * the ID supplied in the constructor
 * {@link CommonViewer#CommonViewer(String, Composite, int)} or through
 * {@link NavigatorContentServiceFactory#createContentService(String, org.eclipse.jface.viewers.StructuredViewer)}.
 * 
 * <p>
 * Clients may extend this class.
 * </p>
 * 
 * 
 * @since 3.2
 */
public class CommonViewer extends TreeViewer {

	private final NavigatorContentService contentService;

	/**
	 * <p>
	 * Initializes the content provider, label provider, and drag and drop
	 * support. Should not be called by clients -- this method is invoked when
	 * the constructor is invoked.
	 * </p>
	 */
	protected void init() {
		setUseHashlookup(true);
		setContentProvider(contentService.createCommonContentProvider());
		DecoratingLabelProvider decoratingProvider = new DecoratingLabelProvider(
				contentService.createCommonLabelProvider(), PlatformUI
						.getWorkbench().getDecoratorManager()
						.getLabelDecorator());
		setLabelProvider(decoratingProvider);
		initDragAndDrop();

	}

	protected void removeWithoutRefresh(Object[] elements) {
		super.remove(elements);
	}
 

	/**
	 * <p>
	 * Adds DND support to the Navigator. Uses hooks into the extensible
	 * framework for DND.
	 * </p>
	 * <p>
	 * By default, the following Transfer types are supported:
	 * <ul>
	 * <li>LocalSelectionTransfer.getInstance(),
	 * <li>PluginTransfer.getInstance()
	 * </ul>
	 * </p>
	 * 
	 * @see CommonDragAdapter
	 * @see CommonDropAdapter
	 */
	protected void initDragAndDrop() {

		/* Handle Drag and Drop */
		int operations = DND.DROP_COPY | DND.DROP_MOVE;

		CommonDragAdapter dragAdapter = new CommonDragAdapter(contentService,
				this);
		addDragSupport(operations, dragAdapter.getSupportedDragTransfers(),
				dragAdapter);

		CommonDropAdapter dropAdapter = new CommonDropAdapter(contentService,
				this);
		addDropSupport(operations, dropAdapter.getSupportedDropTransfers(),
				dropAdapter);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#createTreeItem(org.eclipse.swt.widgets.Widget,
	 *      java.lang.Object, int)
	 */
	protected void createTreeItem(Widget parent, final Object element, int index) {
		try {
			super.createTreeItem(parent, element, index);
		} catch (Exception ex) {
			ex.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
		}

	}

	/*
	 * @see ContentViewer#handleLabelProviderChanged(LabelProviderChangedEvent)
	 */
	protected void handleLabelProviderChanged(LabelProviderChangedEvent event) {

		Object[] changed = event.getElements();
		if (changed != null) {
			ArrayList others = new ArrayList();
			for (int i = 0; i < changed.length; i++) {
				Object curr = changed[i];
				others.add(curr);
			}
			if (others.isEmpty()) {
				return;
			}
			event = new LabelProviderChangedEvent((IBaseLabelProvider) event
					.getSource(), others.toArray());
		}
		super.handleLabelProviderChanged(event);
	}

	protected void handleDispose(DisposeEvent event) {
		super.handleDispose(event);
		dispose();
	}

	/* (non-Javadoc) Method declared on StructuredViewer. */
	protected void setSelectionToWidget(List v, boolean reveal) {
		if (v == null) {
			setSelection(new ArrayList(0));
			return;
		}
		int size = v.size();
		List newSelection = new ArrayList(size);
		for (int i = 0; i < size; ++i) {
			// Use internalExpand since item may not yet be created. See
			// 1G6B1AR.
			Widget w = internalExpand(v.get(i), reveal);
			if (w instanceof Item) {
				newSelection.add(w);
			}
		}
		setSelection(newSelection);

		// // Although setting the selection in the control should reveal it,
		// // setSelection may be a no-op if the selection is unchanged,
		// // so explicitly reveal the first item in the selection here.
		// // See bug 100565 for more details.
		// if (reveal && newSelection.size() > 0) {
		// showItem((Item) newSelection.get(0));
		// }
	}

	/**
	 * <p>
	 * Constructs the Tree Viewer for the Common Navigator and the corresponding
	 * NavigatorContentService. The NavigatorContentService will provide the
	 * Content Provider and Label Provider -- these need not be supplied by
	 * clients.
	 * <p>
	 * For the valid bits to supply in the style mask (aStyle), see
	 * documentation provided by {@link TreeViewer}.
	 * </p>
	 * 
	 * @param aViewerId
	 *            An id tied to the extensions that is used to focus specific
	 *            content to a particular instance of the Common Navigator
	 * @param aParent
	 *            A Composite parent to contain the actual SWT widget
	 * @param aStyle
	 *            A style mask that will be used to create the TreeViewer
	 *            Composite.
	 */
	public CommonViewer(String aViewerId, Composite aParent, int aStyle) {
		super(aParent, aStyle);
		contentService = new NavigatorContentService(aViewerId, this);
		init();
	}

	/**
	 * <p>
	 * Disposes of the NavigatorContentService, which will dispose the Content
	 * and Label providers.
	 * </p>
	 */
	public void dispose() {
		if (contentService != null) {
			contentService.dispose();
		}
	}

	/**
	 * Sets this viewer's sorter and triggers refiltering and resorting of this
	 * viewer's element. Passing <code>null</code> turns sorting off.
	 * 
	 * @param sorter
	 *            a viewer sorter, or <code>null</code> if none
	 */
	public void setSorter(ViewerSorter sorter) {
		if (sorter != null && sorter instanceof CommonViewerSorter) {
			((CommonViewerSorter) sorter).setContentService(contentService);
		}

		super.setSorter(sorter);
	}

	/**
	 * <p>
	 * The {@link INavigatorContentService}provides the hook into the framework
	 * to provide content from the various extensions.
	 * </p>
	 * 
	 * @return The {@link INavigatorContentService}that was created when the
	 *         viewer was created.
	 */
	public INavigatorContentService getNavigatorContentService() {
		return contentService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#add(java.lang.Object,
	 *      java.lang.Object[])
	 */
	public void add(Object parentElement, Object[] childElements) {
		// TODO Intercept ADD for the pipeline service.
		super.add(parentElement, childElements);
	}

	/**
	 * <p>
	 * Removals are handled by refreshing the parents of each of the given
	 * elements. The parents are determined via calls ot the contentProvider.
	 * </p>
	 * 
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#remove(java.lang.Object[])
	 */
	public void remove(Object[] elements) {

		// TODO Intercept REMOVE for the pipeline service.

		super.remove(elements);
		ITreeContentProvider contentProvider = (ITreeContentProvider) getContentProvider();
		for (int i = 0; i < elements.length; i++) {
			super.internalRefresh(contentProvider.getParent(elements[i]));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#refresh(java.lang.Object,
	 *      boolean)
	 */
	public void refresh(Object element, boolean updateLabels) {

		INavigatorPipelineService pipeDream = contentService
				.getPipelineService();

		PipelinedViewerUpdate update = new PipelinedViewerUpdate();
		update.getRefreshTargets().add(element);
		update.setUpdateLabels(updateLabels);
		/* if the update is modified */
		if (pipeDream.interceptRefresh(update)) {
			/* intercept and apply the update */
			boolean toUpdateLabels = update.isUpdateLabels();
			for (Iterator iter = update.getRefreshTargets().iterator(); iter
					.hasNext();) {
				super.refresh(iter.next(), toUpdateLabels);
			}
		} else {
			super.refresh(element, updateLabels);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#refresh(java.lang.Object)
	 */
	public void refresh(Object element) {
		refresh(element, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#update(java.lang.Object,
	 *      java.lang.String[])
	 */
	public void update(Object element, String[] properties) {
		// TODO Intercept UPDATE for the pipeline service.
		super.update(element, properties);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return contentService.toString() + " Viewer"; //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#internalRefresh(java.lang.Object, boolean)
	 */
	protected void internalRefresh(Object element, boolean updateLabels) {
		if(element == null && getRoot() == null) {
			return;
		}
		super.internalRefresh(element, updateLabels);
	}

}
