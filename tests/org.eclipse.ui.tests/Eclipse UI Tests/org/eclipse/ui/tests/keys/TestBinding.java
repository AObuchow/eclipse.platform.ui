package org.eclipse.ui.tests.keys;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.util.Util;

/**
 * A binding that can be used for testing purposes. This guarantees that the
 * properties tested are inherent to all bindings, and not just a specific type
 * of bindings.
 */
final class TestBinding extends Binding {

	/**
	 * A simple trigger sequence for this test.
	 */
	static final class TestTriggerSequence extends TriggerSequence {

		/**
		 * Constructs a new instance of <code>TestTriggerSequence</code>.
		 * 
		 * @param myTriggers
		 *            The triggers to use in constructing this sequence; must
		 *            not be <code>null</code>.
		 */
		public TestTriggerSequence() {
			super(Collections.EMPTY_LIST);
		}

		public final String format() {
			return toString();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.bindings.TriggerSequence#getPrefixes()
		 */
		public List getPrefixes() {
			return Collections.EMPTY_LIST;
		}
	}

	/**
	 * A trigger sequence to be used by all test bindings. This value is never
	 * <code>null</code>.
	 */
	static final TriggerSequence TRIGGER_SEQUENCE = new TestTriggerSequence();

	/**
	 * Constructs a new instance of <code>TestBinding</code>
	 * 
	 * @param commandId
	 *            The command id
	 * @param schemeId
	 *            The scheme id
	 * @param contextId
	 *            The context id
	 * @param locale
	 *            The locale
	 * @param platform
	 *            The platform
	 * @param type
	 *            The type: SYSTEM or USER
	 */
	TestBinding(final String commandId, final String schemeId,
			final String contextId, final String locale, final String platform,
			final int type) {
		super(commandId, schemeId, contextId, locale, platform, null, type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.bindings.Binding#getTriggerSequence()
	 */
	public final TriggerSequence getTriggerSequence() {
		return TRIGGER_SEQUENCE;
	}

	public final String toString() {
		return Util.ZERO_LENGTH_STRING;
	}
}