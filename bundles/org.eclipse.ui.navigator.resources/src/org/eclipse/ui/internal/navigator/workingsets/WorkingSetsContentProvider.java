package org.eclipse.ui.internal.navigator.workingsets;

import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.internal.AggregateWorkingSet;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonContentProvider;
import org.eclipse.ui.navigator.IExtensionStateModel;

/**
 * Provides children and parents for IWorkingSets.
 * 
 * @since 3.2.1
 * 
 */
public class WorkingSetsContentProvider implements ICommonContentProvider {

	/**
	 * The extension id for the WorkingSet extension.
	 */
	public static final String EXTENSION_ID = "org.eclipse.ui.navigator.resources.workingSets"; //$NON-NLS-1$

	/**
	 * A key used by the Extension State Model to keep track of whether top level Working Sets or
	 * Projects should be shown in the viewer.
	 */
	public static final String SHOW_TOP_LEVEL_WORKING_SETS = EXTENSION_ID + ".showTopLevelWorkingSets"; //$NON-NLS-1$


	private static final Object[] NO_CHILDREN = new Object[0];
	private static final int WORKING_SETS = 0;
	private static final int PROJECTS = 1;

	private WorkingSetHelper helper;
	private int rootMode = WORKING_SETS;
	private IExtensionStateModel extensionStateModel;	

	private IPropertyChangeListener rootModeListener = new IPropertyChangeListener() {
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent event) {
			if(SHOW_TOP_LEVEL_WORKING_SETS.equals(event.getProperty())) {
				updateRootMode();
			}
			
		} 

	};
	

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.ICommonContentProvider#init(org.eclipse.ui.navigator.ICommonContentExtensionSite)
	 */  
	public void init(ICommonContentExtensionSite aConfig) {
		extensionStateModel = aConfig.getExtensionStateModel(); 
		extensionStateModel.addPropertyChangeListener(rootModeListener);
		updateRootMode();
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IMementoAware#restoreState(org.eclipse.ui.IMemento)
	 */
	public void restoreState(IMemento aMemento) { 
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IMementoAware#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento aMemento) { 
		
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IWorkingSet) {
			IWorkingSet workingSet = (IWorkingSet) parentElement;
			if (workingSet.isAggregateWorkingSet()) {
				switch (rootMode) {
					case WORKING_SETS :
						return ((AggregateWorkingSet) workingSet).getComponents();
					case PROJECTS :
						return workingSet.getElements();
				}
			}
			return workingSet.getElements();
		}
		return NO_CHILDREN;
	}

	public Object getParent(Object element) {
		if (helper != null)
			return helper.getParent(element);
		return null;
	}

	public boolean hasChildren(Object element) {
		return true;
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void dispose() {
		helper = null;

		extensionStateModel.removePropertyChangeListener(rootModeListener);
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof IWorkingSet) {
			IWorkingSet rootSet = (IWorkingSet) newInput;
			helper = new WorkingSetHelper(rootSet);
		}

	} 
 
	private void updateRootMode() {
		if( extensionStateModel.getBooleanProperty(SHOW_TOP_LEVEL_WORKING_SETS) )
			rootMode = WORKING_SETS;
		else 
			rootMode = PROJECTS;
	}

	protected class WorkingSetHelper {

		private final IWorkingSet workingSet;
		private final Map parents = new WeakHashMap();

		/**
		 * Create a Helper class for the given working set
		 * 
		 * @param set
		 *            The set to use to build the item to parent map.
		 */
		public WorkingSetHelper(IWorkingSet set) {
			workingSet = set;

			if (workingSet.isAggregateWorkingSet()) {
				AggregateWorkingSet aggregateSet = (AggregateWorkingSet) workingSet;

				IWorkingSet[] components = aggregateSet.getComponents();

				for (int componentIndex = 0; componentIndex < components.length; componentIndex++) {
					IAdaptable[] elements = components[componentIndex].getElements();
					for (int elementsIndex = 0; elementsIndex < elements.length; elementsIndex++) {
						parents.put(elements[elementsIndex], components[componentIndex]);
					}
					parents.put(components[componentIndex], aggregateSet);

				}
			} else {
				IAdaptable[] elements = workingSet.getElements();
				for (int elementsIndex = 0; elementsIndex < elements.length; elementsIndex++) {
					parents.put(elements[elementsIndex], workingSet);
				}
			}
		}

		/**
		 * 
		 * @param element
		 *            An element from the viewer
		 * @return The parent associated with the element, if any.
		 */
		public Object getParent(Object element) {
			return parents.get(element);
		}
	}
	


}
