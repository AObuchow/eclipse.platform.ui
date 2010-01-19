/*******************************************************************************
 * Copyright (c) 2003, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.extensions;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.osgi.util.NLS;

import org.eclipse.core.expressions.ElementHandler;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.IEvaluationContext;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;

import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.navigator.CommonNavigatorMessages;
import org.eclipse.ui.internal.navigator.CustomAndExpression;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.navigator.ICommonContentProvider;
import org.eclipse.ui.navigator.ICommonLabelProvider;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.OverridePolicy;
import org.eclipse.ui.navigator.Priority;

/**
 * Encapsulates the <code>org.eclipse.ui.navigator.navigatorContent</code>
 * extension point.
 * 
 * @since 3.2
 */
public final class NavigatorContentDescriptor implements
		INavigatorContentDescriptor, INavigatorContentExtPtConstants {

	private static final int HASH_CODE_NOT_COMPUTED = -1;
	private String id;

	private String name;

	private IConfigurationElement configElement;

	private int priority = Priority.NORMAL_PRIORITY_VALUE;

	private Expression enablement;

	private Expression possibleChildren;

	private String icon;

	private boolean activeByDefault;

	private IPluginContribution contribution;

	private Set overridingExtensions;
	private LinkedList overridingExtensionsReverseOrder2; // FIXME: will replace 'overridingExtensions' in 3.6

	private OverridePolicy overridePolicy;

	private String suppressedExtensionId;

	private INavigatorContentDescriptor overriddenDescriptor;

	private int hashCode = HASH_CODE_NOT_COMPUTED;

	private boolean providesSaveables;

	/**
	 * Creates a new content descriptor from a configuration element.
	 * 
	 * @param configElement
	 *            configuration element to create a descriptor from
	 * 
	 * @throws WorkbenchException
	 *             if the configuration element could not be parsed. Reasons
	 *             include:
	 *             <ul>
	 *             <li>A required attribute is missing.</li>
	 *             <li>More elements are define than is allowed.</li>
	 *             </ul>
	 */
	/* package */ NavigatorContentDescriptor(IConfigurationElement configElement)
			throws WorkbenchException {
		super();
		this.configElement = configElement;
		init();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.navigator.extensions.INavigatorContentDescriptor#getId()
	 */
	public String getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.navigator.extensions.INavigatorContentDescriptor#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.navigator.extensions.INavigatorContentDescriptor#getPriority()
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * Parses the configuration element.
	 * 
	 * @throws WorkbenchException
	 *             if the configuration element could not be parsed. Reasons
	 *             include:
	 *             <ul>
	 *             <li>A required attribute is missing.</li>
	 *             <li>More elements are define than is allowed.</li>
	 *             </ul>
	 */
	private void init() throws WorkbenchException {
		id = configElement.getAttribute(ATT_ID);
		name = configElement.getAttribute(ATT_NAME);
		String priorityString = configElement.getAttribute(ATT_PRIORITY);
		icon = configElement.getAttribute(ATT_ICON);

		String activeByDefaultString = configElement
				.getAttribute(ATT_ACTIVE_BY_DEFAULT);
		activeByDefault = (activeByDefaultString != null && activeByDefaultString
				.length() > 0) ? Boolean.valueOf(
				configElement.getAttribute(ATT_ACTIVE_BY_DEFAULT))
				.booleanValue() : true;

		String providesSaveablesString = configElement
			.getAttribute(ATT_PROVIDES_SAVEABLES);
		providesSaveables = (providesSaveablesString != null && providesSaveablesString
				.length() > 0) ? Boolean.valueOf(providesSaveablesString)
						.booleanValue() : false;

		if (priorityString != null) {
			try {
				Priority p = Priority.get(priorityString);
				priority = p != null ? p.getValue()
						: Priority.NORMAL_PRIORITY_VALUE;
			} catch (NumberFormatException exception) {
				priority = Priority.NORMAL_PRIORITY_VALUE;
			}
		}
		if (id == null) {
			throw new WorkbenchException(NLS.bind(
					CommonNavigatorMessages.Attribute_Missing_Warning,
					new Object[] {
							ATT_ID,
							id,
							configElement.getDeclaringExtension()
									.getNamespaceIdentifier() }));
		}

		IConfigurationElement[] children = configElement
				.getChildren(TAG_ENABLEMENT);
		if (children.length == 0) {

			children = configElement.getChildren(TAG_TRIGGER_POINTS);
			if (children.length == 1) {
				enablement = new CustomAndExpression(children[0]);
			} else {
				throw new WorkbenchException(NLS.bind(
						CommonNavigatorMessages.Attribute_Missing_Warning,
						new Object[] {
								TAG_TRIGGER_POINTS,
								id,
								configElement.getDeclaringExtension()
										.getNamespaceIdentifier() }));
			}

			children = configElement.getChildren(TAG_POSSIBLE_CHILDREN);
			if (children.length == 1) {
				possibleChildren = new CustomAndExpression(children[0]);
			} else if(children.length > 1){
				throw new WorkbenchException(NLS.bind(
						CommonNavigatorMessages.Attribute_Missing_Warning,
						new Object[] {
								TAG_POSSIBLE_CHILDREN,
								id,
								configElement.getDeclaringExtension()
										.getNamespaceIdentifier() }));
			}
		} else if (children.length == 1) {
			try {
				enablement = ElementHandler.getDefault().create(
						ExpressionConverter.getDefault(), children[0]);
			} catch (CoreException e) {
				NavigatorPlugin.log(IStatus.ERROR, 0, e.getMessage(), e);
			}
		} else if (children.length > 1) {
			throw new WorkbenchException(NLS.bind(
					CommonNavigatorMessages.Attribute_Missing_Warning,
					new Object[] {
							TAG_ENABLEMENT,
							id,
							configElement.getDeclaringExtension()
									.getNamespaceIdentifier() }));
		}

		contribution = new IPluginContribution() {

			public String getLocalId() {
				return getId();
			}

			public String getPluginId() {
				return configElement.getDeclaringExtension().getNamespaceIdentifier();
			}

		};

		children = configElement.getChildren(TAG_OVERRIDE);
		if (children.length == 0) {
			overridePolicy = OverridePolicy.get(OverridePolicy.InvokeAlwaysRegardlessOfSuppressedExt_LITERAL);
		} else if (children.length == 1) {
			suppressedExtensionId = children[0]
					.getAttribute(ATT_SUPPRESSED_EXT_ID);
			overridePolicy = OverridePolicy.get(children[0]
					.getAttribute(ATT_POLICY));
		} else if (children.length > 1) {
			throw new WorkbenchException(NLS.bind(
					CommonNavigatorMessages.Too_many_elements_Warning,
					new Object[] {
							TAG_OVERRIDE,
							id,configElement.getDeclaringExtension()
							.getNamespaceIdentifier() }));
		}
	}

	/**
	 * @return Returns the icon.
	 */
	public String getIcon() {
		return icon;
	}

	/**
	 * @return Returns the suppressedExtensionId or null if none specified.
	 */
	public String getSuppressedExtensionId() {
		return suppressedExtensionId;
	}

	/**
	 * @return Returns the overridePolicy or null if this extension does not
	 *         override another extension.
	 */
	public OverridePolicy getOverridePolicy() {
		return overridePolicy;
	}

	/**
	 * @return Returns the contribution.
	 */
	public IPluginContribution getContribution() {
		return contribution;
	}

	/**
	 * The content provider could be an instance of
	 * {@link ICommonContentProvider}, but only {@link ITreeContentProvider} is
	 * required.
	 * 
	 * 
	 * @return An instance of the Content provider defined for this extension.
	 * @throws CoreException
	 *             if an instance of the executable extension could not be
	 *             created for any reason
	 * 
	 */
	public ITreeContentProvider createContentProvider() throws CoreException {
		return (ITreeContentProvider) configElement
				.createExecutableExtension(ATT_CONTENT_PROVIDER);
	}

	/**
	 * 
	 * The content provider could be an instance of {@link ICommonLabelProvider},
	 * but only {@link ILabelProvider} is required.
	 * 
	 * @return An instance of the Label provider defined for this extension
	 * @throws CoreException
	 *             if an instance of the executable extension could not be
	 *             created for any reason
	 */
	public ILabelProvider createLabelProvider() throws CoreException {
		return (ILabelProvider) configElement
				.createExecutableExtension(ATT_LABEL_PROVIDER);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.navigator.extensions.INavigatorContentDescriptor#isEnabledByDefault()
	 */
	public boolean isActiveByDefault() {
		return activeByDefault;
	}

	/**
	 * Determine if this content extension would be able to provide children for
	 * the given element.
	 * 
	 * @param anElement
	 *            The element that should be used for the evaluation.
	 * @return True if and only if the extension is enabled for the element.
	 */
	public boolean isTriggerPoint(Object anElement) {

		if (enablement == null || anElement == null) {
			return false;
		}

		try {
			IEvaluationContext context = NavigatorPlugin.getEvalContext(anElement);
			return (enablement.evaluate(context) == EvaluationResult.TRUE);
		} catch (CoreException e) {
			NavigatorPlugin.logError(0, e.getMessage(), e);
		}
		return false;
	}

	/**
	 * Determine if this content extension could provide the given element as a
	 * child.
	 * 
	 * <p>
	 * This method is used to determine what the parent of an element could be
	 * for Link with Editor support.
	 * </p>
	 * 
	 * @param anElement
	 *            The element that should be used for the evaluation.
	 * @return True if and only if the extension might provide an object of this
	 *         type as a child.
	 */
	public boolean isPossibleChild(Object anElement) {

		if ((enablement == null && possibleChildren == null)
				|| anElement == null) {
			return false;
		} else if(anElement instanceof IStructuredSelection) {
			return arePossibleChildren((IStructuredSelection) anElement);
		}

		try {
			IEvaluationContext context = NavigatorPlugin.getEvalContext(anElement);
			if (possibleChildren != null) {
				return (possibleChildren.evaluate(context) == EvaluationResult.TRUE);
			} else if (enablement != null) {
				return (enablement.evaluate(context) == EvaluationResult.TRUE);
			}
		} catch (CoreException e) {
			NavigatorPlugin.logError(0, e.getMessage(), e);
		}
		return false;
	}
	
	/**
	 * A convenience method to check all elements in a selection.
	 * 
	 * @param aSelection A non-null selection
	 * @return True if and only if every element in the selection is a possible child.
	 */
	public boolean arePossibleChildren(IStructuredSelection aSelection) {
		if(aSelection.isEmpty()) {
			return false;
		}
		for (Iterator iter = aSelection.iterator(); iter.hasNext();) {
			Object element = iter.next();
			if(!isPossibleChild(element)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * Does not force the creation of the set of overriding extensions.
	 * 
	 * @return True if this extension has overridding extensions.
	 */
	public boolean hasOverridingExtensions() {
		return overridingExtensions != null && overridingExtensions.size() > 0;
	}

	/**
	 * @return The set of overriding extensions (of type
	 *         {@link INavigatorContentDescriptor}
	 */
	public Set getOverriddingExtensions() {
		if (overridingExtensions == null) {
			overridingExtensions = new TreeSet(ExtensionPriorityComparator.DESCENDING);
		}
		return overridingExtensions;
	}

	/**
	 * @param fromStart
	 *            <code>true</code> if list iterator starts at the beginning and
	 *            <code>false</code> if it starts at the end of the list
	 * @return a list iterator over the overriding extensions which are ordered
	 *         by ExtensionPriorityComparator.DESCENDING
	 * @since 3.5.1
	 */
	public ListIterator getOverriddingExtensionsListIterator(boolean fromStart) {
		if (overridingExtensions == null)
			return Collections.EMPTY_LIST.listIterator();

		if (overridingExtensionsReverseOrder2 == null) {
			overridingExtensionsReverseOrder2 = new LinkedList();
			Iterator iter = overridingExtensions.iterator();
			while (iter.hasNext())
				overridingExtensionsReverseOrder2.addLast(iter.next());
		}
		return overridingExtensionsReverseOrder2.listIterator(fromStart ? 0 : overridingExtensionsReverseOrder2.size());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Content[" + id + ", \"" + name + "\"]"; //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (hashCode == HASH_CODE_NOT_COMPUTED) {
			String hashCodeString = configElement.getNamespaceIdentifier() + getId();
			hashCode = hashCodeString.hashCode();
			if (hashCode == HASH_CODE_NOT_COMPUTED)
				hashCode++;
		}
		return hashCode;
	}

	/**
	 * @return The descriptor of the <code>suppressedExtensionId</code> if
	 *         non-null.
	 */
	public INavigatorContentDescriptor getOverriddenDescriptor() {
		return overriddenDescriptor;
	}

	/**
	 * @param theOverriddenDescriptor
	 *            The overriddenDescriptor to set.
	 */
	/* package */void setOverriddenDescriptor(
			INavigatorContentDescriptor theOverriddenDescriptor) {
		overriddenDescriptor = theOverriddenDescriptor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.INavigatorContentDescriptor#hasSaveablesProvider()
	 */
	public boolean hasSaveablesProvider() {
		return providesSaveables;
	}

}
