package org.eclipse.ui.examples.readmetool;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.*;
import org.eclipse.ui.help.*;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.events.*;

/**
 * This class implements a sample preference page that is 
 * added to the preference dialog based on the registration.
 */
public class ReadmePreferencePage extends PreferencePage implements IWorkbenchPreferencePage, SelectionListener, ModifyListener {
	private Button radioButton1;
	private Button radioButton2;
	private Button radioButton3;

	private Button checkBox1;
	private Button checkBox2;
	private Button checkBox3;

	private Text textField;
	
	private Button pushButton_textField;
/**
 * Creates an new checkbox instance and sets the default
 * layout data.
 *
 * @param group  the composite in which to create the checkbox
 * @param label  the string to set into the checkbox
 * @return the new checkbox
 */ 
private Button createCheckBox(Composite group, String label) {
	Button button = new Button(group, SWT.CHECK | SWT.LEFT);
	button.setText(label);
	button.addSelectionListener(this);
	GridData data = new GridData();
	button.setLayoutData(data);
	return button;
}
/**
 * Creates composite control and sets the default layout data.
 *
 * @param parent  the parent of the new composite
 * @param numColumns  the number of columns for the new composite
 * @return the newly-created coposite
 */
private Composite createComposite(Composite parent, int numColumns) {
	Composite composite = new Composite(parent, SWT.NULL);

	//GridLayout
	GridLayout layout = new GridLayout();
	layout.numColumns = numColumns;
	composite.setLayout(layout);

	//GridData
	GridData data = new GridData();
	data.verticalAlignment = GridData.FILL;
	data.horizontalAlignment = GridData.FILL;
	composite.setLayoutData(data);
	return composite;
}
/** (non-Javadoc)
 * Method declared on PreferencePage
 */
protected Control createContents(Composite parent)
{
	WorkbenchHelp.setHelp(parent, new DialogPageContextComputer(this, IReadmeConstants.PREFERENCE_PAGE_CONTEXT));
	
	//composite_textField << parent
	Composite composite_textField = createComposite(parent, 2);
	Label label_textField = createLabel(composite_textField, "Text Field");	
	textField = createTextField(composite_textField);
	pushButton_textField = createPushButton(composite_textField, "Change");

	//composite_tab << parent
	Composite composite_tab = createComposite(parent, 2);
	Label label1 = createLabel(composite_tab, "Radio Button Options");

	//
	tabForward(composite_tab);
	//radio button composite << tab composite
	Composite composite_radioButton = createComposite(composite_tab, 1);
	radioButton1 = createRadioButton(composite_radioButton, "Radio button 1");
	radioButton2 = createRadioButton(composite_radioButton, "Radio button 2");
	radioButton3 = createRadioButton(composite_radioButton, "Radio button 3");


	//composite_tab2 << parent
	Composite composite_tab2 = createComposite(parent, 2);
	Label label2 = createLabel(composite_tab2, "Check Box Options");

	//
	tabForward(composite_tab2);
	//composite_checkBox << composite_tab2
	Composite composite_checkBox = createComposite(composite_tab2, 1);
	checkBox1 = createCheckBox(composite_checkBox, "Check box 1");
	checkBox2 = createCheckBox(composite_checkBox, "Check box 2");
	checkBox3 = createCheckBox(composite_checkBox, "Check box 3");

	initializeValues();

	//font = null;
	return new Composite(parent, SWT.NULL);
}
/**
 * Utility method that creates a label instance
 * and sets the default layout data.
 *
 * @param parent  the parent for the new label
 * @param text  the text for the new label
 * @return the new label
 */
private Label createLabel(Composite parent, String text) {
	Label label = new Label(parent, SWT.LEFT);
	label.setText(text);
	GridData data = new GridData();
	data.horizontalSpan = 2;
	data.horizontalAlignment = GridData.FILL;
	label.setLayoutData(data);
	return label;
}
/**
 * Utility method that creates a push button instance
 * and sets the default layout data.
 *
 * @param parent  the parent for the new button
 * @param label  the label for the new button
 * @return the newly-created button
 */
private Button createPushButton(Composite parent, String label) {
	Button button = new Button(parent, SWT.PUSH);
	button.setText(label);
	button.addSelectionListener(this);
	GridData data = new GridData();
	data.horizontalAlignment = GridData.FILL;
	button.setLayoutData(data);
	return button;
}
/**
 * Utility method that creates a radio button instance
 * and sets the default layout data.
 *
 * @param parent  the parent for the new button
 * @param label  the label for the new button
 * @return the newly-created button
 */
private Button createRadioButton(Composite parent, String label) {
	Button button = new Button(parent, SWT.RADIO | SWT.LEFT);
	button.setText(label);
	button.addSelectionListener(this);
	GridData data = new GridData();
	button.setLayoutData(data);
	return button;
}
/**
 * Create a text field specific for this application
 *
 * @param parent  the parent of the new text field
 * @return the new text field
 */
private Text createTextField(Composite parent) {
	Text text = new Text(parent, SWT.SINGLE | SWT.BORDER);
	text.addModifyListener(this);
	GridData data = new GridData();
	data.horizontalAlignment = GridData.FILL;
	data.grabExcessHorizontalSpace = true;
	data.verticalAlignment = GridData.CENTER;
	data.grabExcessVerticalSpace = false;
	text.setLayoutData(data);
	return text;
}
/** 
 * The <code>ReadmePreferencePage</code> implementation of this
 * <code>PreferencePage</code> method 
 * returns preference store that belongs to the our plugin.
 * This is important because we want to store
 * our preferences separately from the desktop.
 */
protected IPreferenceStore doGetPreferenceStore() {
	return ReadmePlugin.getDefault().getPreferenceStore();
}
/* (non-Javadoc)
 * Method declared on IWorkbenchPreferencePage
 */
public void init(IWorkbench workbench){
}
/**
 * Initializes states of the controls using default values
 * in the preference store.
 */
private void initializeDefaults() {
	IPreferenceStore store = getPreferenceStore();
	checkBox1.setSelection(store.getDefaultBoolean(IReadmeConstants.PRE_CHECK1));
	checkBox2.setSelection(store.getDefaultBoolean(IReadmeConstants.PRE_CHECK2));
	checkBox3.setSelection(store.getDefaultBoolean(IReadmeConstants.PRE_CHECK3));

	radioButton1.setSelection(false);
	radioButton2.setSelection(false);
	radioButton3.setSelection(false);
	int choice = store.getDefaultInt(IReadmeConstants.PRE_RADIO_CHOICE);
	switch (choice) {
		case 1:
		   radioButton1.setSelection(true);
		   break;
		case 2:
		   radioButton2.setSelection(true);
		   break;
		case 3:
		   radioButton3.setSelection(true);
		   break;
	}
	textField.setText(store.getDefaultString(IReadmeConstants.PRE_TEXT));
}
/**
 * Initializes states of the controls from the preference store.
 */
private void initializeValues() {
	IPreferenceStore store = getPreferenceStore();
	checkBox1.setSelection(store.getBoolean(IReadmeConstants.PRE_CHECK1));
	checkBox2.setSelection(store.getBoolean(IReadmeConstants.PRE_CHECK2));
	checkBox3.setSelection(store.getBoolean(IReadmeConstants.PRE_CHECK3));

	int choice = store.getInt(IReadmeConstants.PRE_RADIO_CHOICE);
	switch (choice) {
		case 1:
		   radioButton1.setSelection(true);
		   break;
		case 2:
		   radioButton2.setSelection(true);
		   break;
		case 3:
		   radioButton3.setSelection(true);
		   break;
	}
	textField.setText(store.getString(IReadmeConstants.PRE_TEXT));
}
/** (non-Javadoc)
 * Method declared on ModifyListener
 */
public void modifyText(ModifyEvent event) {
	//Do nothing on a modification in this example
}
/* (non-Javadoc)
 * Method declared on PreferencePage
 */
protected void performDefaults() {
	super.performDefaults();
	initializeDefaults();
}
/* (non-Javadoc)
 * Method declared on PreferencePage
 */
public boolean performOk() {
	storeValues();
	return true;
}
/**
 * Stores the values of the controls back to the preference store.
 */
private void storeValues() {
	IPreferenceStore store = getPreferenceStore();
	store.setValue(IReadmeConstants.PRE_CHECK1, checkBox1.getSelection());
	store.setValue(IReadmeConstants.PRE_CHECK2, checkBox2.getSelection());
	store.setValue(IReadmeConstants.PRE_CHECK3, checkBox3.getSelection());

	int choice = 1;

	if (radioButton2.getSelection()) choice = 2;
	else if (radioButton3.getSelection()) choice = 3;

	store.setValue(IReadmeConstants.PRE_RADIO_CHOICE, choice);
	store.setValue(IReadmeConstants.PRE_TEXT, textField.getText());
}
/**
 * Creates a tab of one horizontal spans.
 *
 * @param parent  the parent in which the tab should be created
 */
private void tabForward(Composite parent) {
	Label vfiller = new Label(parent, SWT.LEFT);
	GridData gridData = new GridData();
	gridData = new GridData();
	gridData.horizontalAlignment = GridData.BEGINNING;
	gridData.grabExcessHorizontalSpace = false;
	gridData.verticalAlignment = GridData.CENTER;
	gridData.grabExcessVerticalSpace = false;
	vfiller.setLayoutData(gridData);
}
/** (non-Javadoc)
 * Method declared on SelectionListener
 */
public void widgetDefaultSelected(SelectionEvent event) {
	//Handle a default selection. Do nothing in this example
}
/** (non-Javadoc)
 * Method declared on SelectionListener
 */
public void widgetSelected(SelectionEvent event) {
	//Do nothing on selection in this example;
}
}
