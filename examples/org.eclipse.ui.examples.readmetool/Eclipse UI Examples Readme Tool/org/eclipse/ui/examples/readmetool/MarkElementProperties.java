package org.eclipse.ui.examples.readmetool;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.views.properties.*;
import org.eclipse.ui.views.properties.*;
import java.util.List;
import java.util.Vector;

/**
 * This class encapsulates property sheet properties
 * for MarkElement.  This will display properties for
 * the MarkElement when selected in the readme editor's
 * content outline.
 */
public class MarkElementProperties implements IPropertySource {
	protected MarkElement element;

	protected static final String PROPERTY_LINECOUNT = "lineno";
	protected static final String PROPERTY_START = "start";
	protected static final String PROPERTY_LENGTH = "length";
/**
 * Creates a new MarkElementProperties.
 *
 * @param element  the element whose properties this instance represents
 */
public MarkElementProperties(MarkElement element) {
	super();
	this.element = element;
}
/* (non-Javadoc)
 * Method declared on IPropertySource
 */
public Object getEditableValue() {
	return this;
}
/* (non-Javadoc)
 * Method declared on IPropertySource
 */
public IPropertyDescriptor[] getPropertyDescriptors() {
	// Create the property vector.
	IPropertyDescriptor[] propertyDescriptors = new IPropertyDescriptor[3];

	// Add each property supported.
	PropertyDescriptor descriptor;

	descriptor = new PropertyDescriptor(PROPERTY_LINECOUNT, "Line count");
	propertyDescriptors[0] = descriptor;
	descriptor = new PropertyDescriptor(PROPERTY_START, "Title start");
	propertyDescriptors[1] = descriptor;
	descriptor = new PropertyDescriptor(PROPERTY_LENGTH, "Title length");
	propertyDescriptors[2] = descriptor;

	// Return it.
	return propertyDescriptors;
}
/* (non-Javadoc)
 * Method declared on IPropertySource
 */
public Object getPropertyValue(Object name) {
	if (name.equals(PROPERTY_LINECOUNT))
		return new Integer(element.getNumberOfLines());
	if (name.equals(PROPERTY_START))
		return new Integer(element.getStart());
	if (name.equals(PROPERTY_LENGTH))
		return new Integer(element.getLength());
	return null;
}
/* (non-Javadoc)
 * Method declared on IPropertySource
 */
public boolean isPropertySet(Object property) {
	return false;
}
/* (non-Javadoc)
 * Method declared on IPropertySource
 */
public void resetPropertyValue(Object property) {
}
/* (non-Javadoc)
 * Method declared on IPropertySource
 */
public void setPropertyValue(Object name, Object value) {
}
}
