/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.workbench.modeling.ExpressionContext;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.ISourceProviderListener;
import org.eclipse.ui.ISources;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.services.IEvaluationReference;
import org.eclipse.ui.services.IEvaluationService;

/**
 * @since 3.3
 * 
 */
public final class EvaluationService implements IEvaluationService {
	private ExpressionContext legacyContext;
	private int notifying = 0;

	private ListenerList serviceListeners = new ListenerList(ListenerList.IDENTITY);
	ArrayList<ISourceProvider> sourceProviders = new ArrayList<ISourceProvider>();
	private IEclipseContext context;
	LinkedList<EvaluationReference> refs = new LinkedList<EvaluationReference>();
	private ISourceProviderListener contextUpdater;

	private HashSet<String> variableFilter = new HashSet<String>();

	public EvaluationService(IEclipseContext c) {
		context = c;
		legacyContext = new ExpressionContext(c);
		contextUpdater = new ISourceProviderListener() {

			public void sourceChanged(int sourcePriority, String sourceName, Object sourceValue) {
				changeVariable(sourceName, sourceValue);
			}

			public void sourceChanged(int sourcePriority, Map sourceValuesByName) {
				Iterator i = sourceValuesByName.entrySet().iterator();
				while (i.hasNext()) {
					final Map.Entry entry = (Entry) i.next();
					changeVariable((String) entry.getKey(), entry.getValue());
				}
			}
		};
		variableFilter.addAll(Arrays.asList(new String[] { ISources.ACTIVE_WORKBENCH_WINDOW_NAME,
				ISources.ACTIVE_WORKBENCH_WINDOW_SHELL_NAME, ISources.ACTIVE_EDITOR_ID_NAME,
				ISources.ACTIVE_EDITOR_INPUT_NAME, ISources.SHOW_IN_INPUT,
				ISources.SHOW_IN_SELECTION, ISources.ACTIVE_PART_NAME,
				ISources.ACTIVE_PART_ID_NAME, ISources.ACTIVE_SITE_NAME,
				ISources.ACTIVE_CONTEXT_NAME, ISources.ACTIVE_CURRENT_SELECTION_NAME }));
	}

	protected final void changeVariable(final String name, final Object value) {
		if (name == null || variableFilter.contains(name)) {
			return;
		}
		if (value == null) {
			legacyContext.removeVariable(name);
		} else {
			legacyContext.addVariable(name, value);
		}
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.services.IServiceWithSources#addSourceProvider(org.eclipse
	 * .ui.ISourceProvider)
	 */
	public void addSourceProvider(ISourceProvider provider) {
		sourceProviders.add(provider);
		provider.addSourceProviderListener(contextUpdater);
		final Map currentState = provider.getCurrentState();
		final Iterator variableItr = currentState.entrySet().iterator();
		while (variableItr.hasNext()) {
			final Map.Entry entry = (Map.Entry) variableItr.next();
			final String variableName = (String) entry.getKey();
			final Object variableValue = entry.getValue();

			/*
			 * Bug 84056. If we update the active workbench window, then we risk
			 * falling back to that shell when the active shell has registered
			 * as "none".
			 */
			if ((variableName != null)
					&& (!ISources.ACTIVE_WORKBENCH_WINDOW_SHELL_NAME.equals(variableName))) {
				changeVariable(variableName, variableValue);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.services.IServiceWithSources#removeSourceProvider(org.
	 * eclipse.ui.ISourceProvider)
	 */
	public void removeSourceProvider(ISourceProvider provider) {
		provider.removeSourceProviderListener(contextUpdater);
		sourceProviders.remove(provider);

		final Map currentState = provider.getCurrentState();
		final Iterator variableItr = currentState.entrySet().iterator();
		while (variableItr.hasNext()) {
			final Map.Entry entry = (Map.Entry) variableItr.next();
			final String variableName = (String) entry.getKey();
			changeVariable(variableName, null);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.services.IDisposable#dispose()
	 */
	public void dispose() {
		for (EvaluationReference ref : refs) {
			invalidate(ref, false);
		}
		refs.clear();
		serviceListeners.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.services.IEvaluationService#addServiceListener(org.eclipse
	 * .jface.util.IPropertyChangeListener)
	 */
	public void addServiceListener(IPropertyChangeListener listener) {
		serviceListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.services.IEvaluationService#removeServiceListener(org.
	 * eclipse.jface.util.IPropertyChangeListener)
	 */
	public void removeServiceListener(IPropertyChangeListener listener) {
		serviceListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.services.IEvaluationService#addEvaluationListener(org.
	 * eclipse.core.expressions.Expression,
	 * org.eclipse.jface.util.IPropertyChangeListener, java.lang.String)
	 */
	public IEvaluationReference addEvaluationListener(Expression expression,
			IPropertyChangeListener listener, String property) {
		EvaluationReference ref = new EvaluationReference(context, expression, listener, property);
		addEvaluationReference(ref);
		return ref;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.services.IEvaluationService#addEvaluationReference(org
	 * .eclipse.ui.services.IEvaluationReference)
	 */
	public void addEvaluationReference(IEvaluationReference ref) {
		EvaluationReference eref = (EvaluationReference) ref;
		refs.add(eref);
		eref.participating = true;
		context.runAndTrack(eref);
	}

	private void invalidate(IEvaluationReference ref, boolean remove) {
		if (remove) {
			refs.remove(ref);
		}
		EvaluationReference eref = (EvaluationReference) ref;
		eref.participating = false;
		eref.evaluate();
		eref.hasRun = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.services.IEvaluationService#removeEvaluationListener(org
	 * .eclipse.ui.services.IEvaluationReference)
	 */
	public void removeEvaluationListener(IEvaluationReference ref) {
		invalidate(ref, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.services.IEvaluationService#getCurrentState()
	 */
	public IEvaluationContext getCurrentState() {
		return legacyContext;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.services.IEvaluationService#requestEvaluation(java.lang.String)
	 */
	public void requestEvaluation(String propertyName) {
		String[] sourceNames = new String[] { propertyName };
		startSourceChange(sourceNames);
		for (EvaluationReference ref : refs) {
			Expression expr = ref.getExpression();
			if (expr != null) {
				ExpressionInfo info = expr.computeExpressionInfo();
				String[] names = info.getAccessedPropertyNames();
				for (String name : names) {
					if (propertyName.equals(name)) {
						ref.evaluate();
						break;
					}
				}
			}
		}
		endSourceChange(sourceNames);
	}

	/**
	 * @param sourceNames
	 */
	private void startSourceChange(final String[] sourceNames) {
		notifying++;
		if (notifying == 1) {
			fireServiceChange(IEvaluationService.PROP_NOTIFYING, Boolean.FALSE, Boolean.TRUE);
		}
	}

	/**
	 * @param sourceNames
	 */
	private void endSourceChange(final String[] sourceNames) {
		if (notifying == 1) {
			fireServiceChange(IEvaluationService.PROP_NOTIFYING, Boolean.TRUE, Boolean.FALSE);
		}
		notifying--;
	}

	private void fireServiceChange(final String property, final Object oldValue,
			final Object newValue) {
		Object[] listeners = serviceListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			final IPropertyChangeListener listener = (IPropertyChangeListener) listeners[i];
			SafeRunner.run(new ISafeRunnable() {
				public void handleException(Throwable exception) {
					WorkbenchPlugin.log(exception);
				}

				public void run() throws Exception {
					listener.propertyChange(new PropertyChangeEvent(EvaluationService.this,
							property, oldValue, newValue));
				}
			});
		}
	}
}
