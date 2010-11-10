/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.navigator.dnd;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.internal.navigator.CustomAndExpression;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.internal.navigator.extensions.INavigatorContentExtPtConstants;
import org.eclipse.ui.navigator.CommonDropAdapterAssistant;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;

/**
 * @since 3.2
 * 
 */
public final class CommonDropAdapterDescriptor implements
		INavigatorContentExtPtConstants {

	private final IConfigurationElement element;

	private final INavigatorContentDescriptor contentDescriptor;

	private Expression dropExpr;

	/* package */CommonDropAdapterDescriptor(
			IConfigurationElement aConfigElement,
			INavigatorContentDescriptor aContentDescriptor) {
		element = aConfigElement;
		contentDescriptor = aContentDescriptor;
		init();
	}

	private void init() {
		IConfigurationElement[] children = element.getChildren(TAG_POSSIBLE_DROP_TARGETS);
		if (children.length == 1) {
			dropExpr = new CustomAndExpression(children[0]);
		}
	}

	/**
	 * 
	 * @param anElement
	 *            The element from the set of elements being dragged.
	 * @return True if the element matches the drag expression from the
	 *         extension.
	 */
	public boolean isDragElementSupported(Object anElement) { 
		return contentDescriptor.isPossibleChild(anElement); 
	}

	/**
	 * 
	 * @param aSelection
	 *            The set of elements being dragged.
	 * @return True if the element matches the drag expression from the
	 *         extension.
	 */
	public boolean areDragElementsSupported(IStructuredSelection aSelection) {
		if (aSelection.isEmpty()) {
			return false;
		}
		return contentDescriptor.arePossibleChildren(aSelection);
	}

	/**
	 * 
	 * @param anElement
	 *            The element from the set of elements benig dropped.
	 * @return True if the element matches the drop expression from the
	 *         extension.
	 */
	public boolean isDropElementSupported(Object anElement) {
		if (dropExpr != null && anElement != null) {
			try {
				IEvaluationContext context = NavigatorPlugin.getEvalContext(anElement);
				return dropExpr
						.evaluate(context) == EvaluationResult.TRUE;
			} catch (CoreException e) {
				NavigatorPlugin.logError(0, e.getMessage(), e);
			}
		}
		return false;
	}

	/**
	 * 
	 * @return An instance of {@link CommonDropAdapterAssistant} from the
	 *         descriptor or {@link SkeletonCommonDropAssistant}.
	 */
	public CommonDropAdapterAssistant createDropAssistant() {

		try {
			return (CommonDropAdapterAssistant) element
					.createExecutableExtension(ATT_CLASS);
		} catch (CoreException e) {
			NavigatorPlugin.logError(0, e.getMessage(), e);
		} catch (RuntimeException re) {
			NavigatorPlugin.logError(0, re.getMessage(), re);
		}
		return SkeletonCommonDropAssistant.INSTANCE;
	}

	/**
	 * 
	 * @return The content descriptor that contains this drop descriptor.
	 */
	public INavigatorContentDescriptor getContentDescriptor() {
		return contentDescriptor;
	}

}
