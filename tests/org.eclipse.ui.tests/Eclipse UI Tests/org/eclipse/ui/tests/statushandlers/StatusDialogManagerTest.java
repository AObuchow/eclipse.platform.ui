/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.statushandlers;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.util.Policy;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.ui.statushandlers.AbstractStatusAreaProvider;
import org.eclipse.ui.statushandlers.IStatusAdapterConstants;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.WorkbenchStatusDialogManager;

public class StatusDialogManagerTest extends TestCase {

	private static final String ACTION_NAME = "actionname";
	private static final String JOB_NAME = "jobname";
	private static final String THROWABLE = "throwable";
	private final static String MESSAGE_1 = "TEST_MESSAGE_1";
	private final static String MESSAGE_2 = "TEST_MESSAGE_2";
	private final static String TITLE = "TEST_TITLE";
	private final static NullPointerException NPE = new NullPointerException();
	private final static NullPointerException NPE_WITH_MESSAGE = new NullPointerException(
			THROWABLE);
	private final static String NPE_NAME = NPE.getClass().getName();

	private boolean automatedMode;
	WorkbenchStatusDialogManager wsdm;
	
	protected void setUp() throws Exception {
		automatedMode = ErrorDialog.AUTOMATED_MODE;
		wsdm = new WorkbenchStatusDialogManager(null, null);
		ErrorDialog.AUTOMATED_MODE = false;
		super.setUp();
	}

	public void testBlockingAppearance() {
		wsdm.addStatusAdapter(createStatusAdapter(MESSAGE_1), true);
		Shell shell = StatusDialogUtil.getStatusShell();
		assertNotNull(shell);
		assertTrue((shell.getStyle() & SWT.APPLICATION_MODAL) == SWT.APPLICATION_MODAL);
	}

	public void testNonBlockingAppearance() {
		wsdm.addStatusAdapter(createStatusAdapter(MESSAGE_1), false);
		Shell shell = StatusDialogUtil.getStatusShell();
		assertNotNull(shell);
		assertFalse((shell.getStyle() & SWT.APPLICATION_MODAL) == SWT.APPLICATION_MODAL);
	}

	public void testModalitySwitch1() {
		wsdm.addStatusAdapter(createStatusAdapter(MESSAGE_1), false);
		Shell shell = StatusDialogUtil.getStatusShell();
		assertNotNull(shell);
		assertFalse((shell.getStyle() & SWT.APPLICATION_MODAL) == SWT.APPLICATION_MODAL);

		wsdm.addStatusAdapter(createStatusAdapter(MESSAGE_1), true);
		shell = StatusDialogUtil.getStatusShell();
		assertNotNull(shell);
		assertTrue((shell.getStyle() & SWT.APPLICATION_MODAL) == SWT.APPLICATION_MODAL);
	}

	/**
	 * Preserving details selection and state
	 */
	public void _testModalitySwitch2() {
		final StatusAdapter[] passed = new StatusAdapter[] { null };
		final Composite[] details = new Composite[] { null };
		setupDetails(passed, details);
		StatusAdapter sa = createStatusAdapter(MESSAGE_1);
		wsdm.addStatusAdapter(sa, false);

		// open details
		selectWidget(StatusDialogUtil.getDetailsButton());
		wsdm.addStatusAdapter(createStatusAdapter(MESSAGE_2), true);

		assertNotNull(details[0]);
		assertFalse(details[0].isDisposed());
		assertEquals(sa, passed[0]);
	}

	/**
	 * Preserving support selection and state
	 */
	public void _testModalitySwitch3() {
		final StatusAdapter[] passed = new StatusAdapter[] { null };
		final Composite[] support = new Composite[] { null };
		setupSupportArea(passed, support);
		StatusAdapter sa = createStatusAdapter(MESSAGE_1);
		wsdm.addStatusAdapter(sa, false);

		// open support
		selectWidget(StatusDialogUtil.getSupportToolItem());
		wsdm.addStatusAdapter(createStatusAdapter(MESSAGE_2), true);

		assertNotNull(support[0]);
		assertFalse(support[0].isDisposed());
		assertEquals(sa, passed[0]);
	}

	/**
	 * Simple status without exception Check primary and secondary message.
	 * Verify invisible action button.
	 */
	public void testWithStatusAdapter1() {
		wsdm.addStatusAdapter(createStatusAdapter(MESSAGE_1), false);
		Label titleLabel = StatusDialogUtil.getTitleLabel();
		assertNotNull(titleLabel);
		assertEquals(MESSAGE_1, titleLabel.getText());

		Label secondaryLabel = StatusDialogUtil.getSingleStatusLabel();
		assertNotNull(secondaryLabel);
		assertEquals(WorkbenchMessages.WorkbenchStatusDialog_SeeDetails,
				secondaryLabel.getText());

		// check invisible action button
		Button actionButton = StatusDialogUtil.getActionButton();
		assertNotNull(actionButton);
		assertFalse(actionButton.isVisible());
		Object layoutData = actionButton.getLayoutData();
		assertTrue(layoutData instanceof GridData);
		assertTrue(((GridData) layoutData).exclude);
	}

	/**
	 * Simple status with title. Check primary and secondary message. Verify
	 * closing.
	 */
	public void testWithStatusAdapter2() {
		wsdm.addStatusAdapter(createStatusAdapter(MESSAGE_1, TITLE),
				false);
		Label titleLabel = StatusDialogUtil.getTitleLabel();
		assertNotNull(titleLabel);
		assertEquals(TITLE, titleLabel.getText());

		Label secondaryLabel = StatusDialogUtil.getSingleStatusLabel();
		assertNotNull(secondaryLabel);
		assertEquals(MESSAGE_1, secondaryLabel.getText());

		selectWidget(StatusDialogUtil.getOkButton());
		// dialog closed
		assertNull(StatusDialogUtil.getStatusShell());

		// list cleared
		assertEquals(0, wsdm.getStatusAdapters().size());
	}

	/**
	 * Simple status with exception with message
	 */
	public void _testWithStatusAdapter3() {
		wsdm.addStatusAdapter(createStatusAdapter(MESSAGE_1,
				NPE_WITH_MESSAGE), false);
		Label titleLabel = StatusDialogUtil.getTitleLabel();
		assertNotNull(titleLabel);
		assertEquals(MESSAGE_1, titleLabel.getText());

		Label secondaryLabel = StatusDialogUtil.getSingleStatusLabel();
		assertNotNull(secondaryLabel);
		assertEquals(THROWABLE, secondaryLabel.getText());
	}

	/**
	 * Simple status with exception without message
	 */
	public void testWithStatusAdapter4() {
		wsdm.addStatusAdapter(createStatusAdapter(MESSAGE_1, NPE), false);

		Label titleLabel = StatusDialogUtil.getTitleLabel();
		assertNotNull(titleLabel);
		assertEquals(MESSAGE_1, titleLabel.getText());

		Label secondaryLabel = StatusDialogUtil.getSingleStatusLabel();
		assertNotNull(secondaryLabel);
		assertEquals(NPE_NAME, secondaryLabel.getText());
	}

	/**
	 * Simple status from job
	 */
	public void testWithStatusAdapter5() {
		String message = "testmessage";
		StatusAdapter statusAdapter = new StatusAdapter(new Status(
				IStatus.ERROR, "testplugin", message));
		Job job = new Job("job") {
			protected IStatus run(IProgressMonitor monitor) {
				return null;
			}
		};
		statusAdapter.addAdapter(Job.class, job);
		wsdm.addStatusAdapter(statusAdapter, false);
		Label titleLabel = StatusDialogUtil.getTitleLabel();
		assertNotNull(titleLabel);
		assertEquals(titleLabel.getText(), NLS.bind(
				WorkbenchMessages.WorkbenchStatusDialog_ProblemOccurredInJob,
				job.getName()));
		Label secondaryLabel = StatusDialogUtil.getSingleStatusLabel();
		assertNotNull(secondaryLabel);
		assertEquals(secondaryLabel.getText(), message);
	}

	/**
	 * Simple status from job with action
	 */
	public void testWithStatusAdapter6() {
		wsdm.addStatusAdapter(createStatusAdapter(MESSAGE_1, JOB_NAME,
				ACTION_NAME), false);

		Label titleLabel = StatusDialogUtil.getTitleLabel();
		assertNotNull(titleLabel);
		assertEquals(NLS.bind(
				WorkbenchMessages.WorkbenchStatusDialog_ProblemOccurredInJob,
				JOB_NAME), titleLabel.getText());

		Label secondaryLabel = StatusDialogUtil.getSingleStatusLabel();
		assertNotNull(secondaryLabel);
		assertEquals(MESSAGE_1, secondaryLabel.getText());

		// check visible action button
		Button actionButton = StatusDialogUtil.getActionButton();
		assertNotNull(actionButton);
		assertTrue(actionButton.isVisible());

		Object layoutData = actionButton.getLayoutData();
		assertTrue(layoutData instanceof GridData);
		assertFalse(((GridData) layoutData).exclude);
		assertEquals(ACTION_NAME, actionButton.getText());

		// be sure that support button is not created
		ToolItem supportItem = StatusDialogUtil.getSupportToolItem();
		assertNull(supportItem);
	}

	/**
	 * Tests if status dialog passes status adapter to the support provider
	 * tests if status dialog extends its height & width
	 */
	public void testSupport1() {
		StatusAdapter statusAdapter = createStatusAdapter(MESSAGE_1);
		final StatusAdapter[] passed = new StatusAdapter[] { null };
		Composite[] support = new Composite[] { null };
		setupSupportArea(passed, support);
		wsdm.addStatusAdapter(statusAdapter, false);
		openSupportArea(statusAdapter, passed);
	}

	/**
	 * Tests if details can be closed and opened 2 times tests if correct status
	 * adapter is passed to details
	 */
	public void testDetails1() {
		StatusAdapter statusAdapter = createStatusAdapter(MESSAGE_1);
		final StatusAdapter[] passed = new StatusAdapter[] { null };
		final Composite[] details = new Composite[] { null };
		setupDetails(passed, details);
		wsdm.addStatusAdapter(statusAdapter, false);
		for (int i = 0; i < 2; i++) {
			passed[0] = null;
			Point sizeBefore = StatusDialogUtil.getStatusShell().getSize();
			Button detailsButton = StatusDialogUtil.getDetailsButton();
			assertNotNull(detailsButton);
			assertTrue(detailsButton.isEnabled());
			assertEquals(IDialogConstants.SHOW_DETAILS_LABEL, detailsButton
					.getText());

			selectWidget(detailsButton);

			Point sizeAfter = StatusDialogUtil.getStatusShell().getSize();
			assertEquals(statusAdapter, passed[0]);
			assertTrue(sizeAfter.y > sizeBefore.y);
			assertEquals(IDialogConstants.HIDE_DETAILS_LABEL, detailsButton
					.getText());
			assertNotNull(details[0]);
			assertFalse(details[0].isDisposed());

			selectWidget(detailsButton);

			Point sizeAfterAfter = StatusDialogUtil.getStatusShell().getSize();
			assertTrue(sizeAfterAfter.y < sizeAfter.y);
			assertEquals(IDialogConstants.SHOW_DETAILS_LABEL, detailsButton
					.getText());
			assertTrue(details[0].isDisposed());
		}
	}

	/**
	 * Verifies that correct status adapter is passed to the support area
	 */
	public void testList1() {
		StatusAdapter statusAdapter1 = createStatusAdapter(MESSAGE_1);
		StatusAdapter statusAdapter2 = createStatusAdapter(MESSAGE_2);

		StatusAdapter[] passed = new StatusAdapter[] { null };
		Composite[] support = new Composite[] { null };
		setupSupportArea(passed, support);

		wsdm.addStatusAdapter(statusAdapter1, false);
		wsdm.addStatusAdapter(statusAdapter2, false);

		Table table = StatusDialogUtil.getTable();
		assertNotNull(table);
		assertEquals(0, table.getSelectionIndex());
		assertEquals(MESSAGE_1, table.getItem(0).getText());
		assertEquals(MESSAGE_2, table.getItem(1).getText());

		// this verifies if support is opened for correct statusAdapter
		openSupportArea(statusAdapter1, passed);
		selectTable(table, 1);
		assertEquals(statusAdapter2, passed[0]);
	}

	/**
	 * Verifies that correct status adapter is passed to details
	 */
	public void testList2() {
		StatusAdapter statusAdapter1 = createStatusAdapter(MESSAGE_1);
		StatusAdapter statusAdapter2 = createStatusAdapter(MESSAGE_2);

		Composite[] details = new Composite[] { null };
		StatusAdapter[] passed = new StatusAdapter[] { null };
		setupDetails(passed, details);

		wsdm.addStatusAdapter(statusAdapter1, false);
		wsdm.addStatusAdapter(statusAdapter2, false);

		selectWidget(StatusDialogUtil.getDetailsButton());
		assertNotNull(details[0]);
		assertFalse(details[0].isDisposed());
		assertEquals(statusAdapter1, passed[0]);

		Table table = StatusDialogUtil.getTable();
		selectTable(table, 1);

		assertNotNull(details[0]);
		assertFalse(details[0].isDisposed());
		assertEquals(statusAdapter2, passed[0]);
	}

	/**
	 * Tests secondary message and the list element for normal and job status
	 * adapter
	 */
	public void testList3() {
		StatusAdapter sa1 = createStatusAdapter(MESSAGE_1);
		StatusAdapter sa2 = createStatusAdapter(MESSAGE_2, JOB_NAME,
				ACTION_NAME);

		wsdm.addStatusAdapter(sa1, false);
		wsdm.addStatusAdapter(sa2, false);

		Table table = StatusDialogUtil.getTable();
		Label titleLabel = StatusDialogUtil.getTitleLabel();

		assertEquals(WorkbenchMessages.WorkbenchStatusDialog_SeeDetails,
				titleLabel.getText());
		assertEquals(MESSAGE_1, table.getItem(0).getText());

		selectTable(table, 1);

		assertEquals(MESSAGE_2, titleLabel.getText());
		assertEquals(JOB_NAME, table.getItem(1).getText());
	}
	
	public void testBug260937(){
		WorkbenchStatusDialogManager wsdm = new WorkbenchStatusDialogManager(
				IStatus.CANCEL, null);
		StatusAdapter sa = createStatusAdapter(MESSAGE_1);
		try {
			wsdm.addStatusAdapter(sa, false);
			assertTrue(true);
		} catch (NullPointerException npe){
			fail();
		}
	}

	/**
	 * Delivers custom support area.
	 * 
	 * @param passed -
	 *            status adapter passed to the support will be set as first
	 *            element of this array.
	 * @param support -
	 *            a main support composite will be set as first element of this
	 *            array.
	 */
	private void setupSupportArea(final StatusAdapter[] passed,
			final Composite[] support) {
		Policy.setErrorSupportProvider(new AbstractStatusAreaProvider() {
			public Control createSupportArea(Composite parent,
					StatusAdapter statusAdapter) {
				passed[0] = statusAdapter;
				Composite c = new Composite(parent, SWT.NONE);
				GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true,
						true);
				layoutData.minimumHeight = 400;
				c.setLayoutData(layoutData);
				support[0] = c;
				return c;
			}
		});
	}

	/**
	 * Opens support area.
	 * 
	 * @param statusAdapter -
	 *            a statusAdapter (for verification that support area uses
	 *            correct one)
	 * @param passed -
	 *            a statusAdapter used by support area will be set as first
	 *            element of this array.
	 */
	private void openSupportArea(StatusAdapter statusAdapter,
			final StatusAdapter[] passed) {
		Point sizeBefore = StatusDialogUtil.getStatusShell().getSize();
		// be sure that support button is enabled
		ToolItem supportItem = StatusDialogUtil.getSupportToolItem();
		assertNotNull(supportItem);
		assertTrue(supportItem.isEnabled());

		selectWidget(supportItem);
		Point sizeAfter = StatusDialogUtil.getStatusShell().getSize();
		assertEquals(statusAdapter, passed[0]);
		assertTrue(sizeAfter.x > sizeBefore.x);
		assertTrue(sizeAfter.y > sizeBefore.y);
	}

	/**
	 * This method creates custom details area.
	 * 
	 * @param passed -
	 *            status adapter passed to the details will be set as first
	 *            element of this array.
	 * @param details -
	 *            a main details composite will be set as first element of this
	 *            array.
	 */
	private void setupDetails(final StatusAdapter[] passed,
			final Composite[] details) {
		wsdm.setDetailsAreaProvider(new AbstractStatusAreaProvider() {
			public Control createSupportArea(Composite parent,
					StatusAdapter statusAdapter) {
				passed[0] = statusAdapter;
				Composite c = new Composite(parent, SWT.NONE);
				GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true,
						true);
				layoutData.minimumHeight = 400;
				c.setLayoutData(layoutData);
				details[0] = c;
				return c;
			}
		});
	}

	/**
	 * This method simulates mouse selection on Table (selects TableItem).
	 * 
	 * @param table
	 *            a Table to be selected.
	 * @param i
	 *            a number of tableItem to be selected.
	 */
	private void selectTable(Table table, int i) {
		table.setSelection(i);
		Event event = new Event();
		event.item = table.getItem(i);
		table.notifyListeners(SWT.Selection, event);
	}

	/**
	 * This method simulates mouse selection on particular Control.
	 * 
	 * @param control
	 *            a Control to be selected.
	 */
	private void selectWidget(Widget control) {
		Event event = new Event();
		event.item = control;
		control.notifyListeners(SWT.Selection, event);
	}

	/**
	 * Creates StatusAdapter from passed parameters.
	 * 
	 * @param message
	 *            a message to be used in StatusAdapter
	 * @return created StatusAdapter
	 */
	private StatusAdapter createStatusAdapter(String message) {
		return new StatusAdapter(new Status(IStatus.ERROR,
				"org.eclipse.ui.tests", message));
	}

	/**
	 * Creates StatusAdapter from passed parameters.
	 * 
	 * @param message
	 *            a message to be used in StatusAdapter
	 * @param throwable
	 *            a Throwable to be used in StatusAdapter
	 * @return created StatusAdapter
	 */
	private StatusAdapter createStatusAdapter(String message,
			Throwable throwable) {
		return new StatusAdapter(new Status(IStatus.ERROR,
				"org.eclipse.ui.tests", message, throwable));
	}

	/**
	 * Creates StatusAdapter from passed parameters. StatusAdapter will look
	 * like it is coming from job.
	 * 
	 * @param message
	 *            a message to be used in StatusAdapter
	 * @param jobname
	 *            a String that will be used as job name
	 * @param actionName
	 *            a String that will be used as a name of the action available
	 *            to the user
	 * @return created StatusAdapter
	 */
	private StatusAdapter createStatusAdapter(String message, String jobname,
			String actionName) {
		StatusAdapter sa = createStatusAdapter(message);
		if (jobname == null) {
			return sa;
		}
		Job job = new Job(jobname) {
			protected IStatus run(IProgressMonitor monitor) {
				return null;
			}
		};
		sa.addAdapter(Job.class, job);
		if (actionName == null) {
			return sa;
		}
		Action action = new Action(actionName) {
		};
		job.setProperty(IProgressConstants.ACTION_PROPERTY, action);
		return sa;
	}

	/**
	 * Creates StatusAdapter from passed parameters.
	 * 
	 * @param message
	 *            a message to be used in StatusAdapter
	 * @param title
	 *            a String to be passed as StatusAdapter title
	 * @return status adapter with title and message
	 */
	private StatusAdapter createStatusAdapter(String message, String title) {
		StatusAdapter sa = createStatusAdapter(message);
		sa.setProperty(IStatusAdapterConstants.TITLE_PROPERTY, title);
		return sa;
	}

	protected void tearDown() throws Exception {
		wsdm = null;
		Shell shell = StatusDialogUtil.getStatusShell();
		if (shell != null) {
			shell.dispose();
		}
		ErrorDialog.AUTOMATED_MODE = automatedMode;
		Policy.setErrorSupportProvider(null);
		super.tearDown();
	}

}
