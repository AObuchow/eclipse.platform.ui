package org.eclipse.ui.tests.api;

import junit.framework.TestCase;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.tests.util.*;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;
import org.eclipse.ui.internal.registry.*;

public class IWorkingSetManagerTest extends UITestCase {
	final static String WORKING_SET_NAME_1 = "ws1";
	final static String WORKING_SET_NAME_2 = "ws2";	

	IWorkingSetManager fWorkingSetManager;
	IWorkspace fWorkspace;
	IWorkingSet fWorkingSet;
	String fChangeProperty;
	Object fChangeNewValue;
	Object fChangeOldValue;		

	class TestPropertyChangeListener implements IPropertyChangeListener {
		public void propertyChange(PropertyChangeEvent event) {
			fChangeProperty = event.getProperty();
			fChangeNewValue = event.getNewValue();
			fChangeOldValue = event.getOldValue();
		}
	}

	public IWorkingSetManagerTest(String testName) {
		super(testName);		
	}
	protected void setUp() throws Exception {
		super.setUp();                                                      
		fWorkingSetManager = fWorkbench.getWorkingSetManager();
		fWorkspace = ResourcesPlugin.getWorkspace();
		fWorkingSet = fWorkingSetManager.createWorkingSet(WORKING_SET_NAME_1, new IAdaptable[] {fWorkspace.getRoot()});

		IWorkingSet[] workingSets = fWorkingSetManager.getWorkingSets();
		for (int i = 0; i < workingSets.length; i++) {
			fWorkingSetManager.removeWorkingSet(workingSets[i]);
		}		
	}
	void resetChangeData() {
		fChangeProperty = "";
		fChangeNewValue = null;
		fChangeOldValue = null;		
	}

	public void testAddPropertyChangeListener() throws Throwable {
		IPropertyChangeListener listener = new TestPropertyChangeListener();
		fWorkingSetManager.addPropertyChangeListener(listener);
		
		resetChangeData();
		fWorkingSetManager.removeWorkingSet(fWorkingSet);
/*
		Add back test once 21187 is fixed
		assertEquals("", fChangeProperty);
*/				
		resetChangeData();
		fWorkingSetManager.addWorkingSet(fWorkingSet);
		assertEquals(IWorkingSetManager.CHANGE_WORKING_SET_ADD, fChangeProperty);
		assertEquals(null, fChangeOldValue);				
		assertEquals(fWorkingSet, fChangeNewValue);						

		resetChangeData();
		fWorkingSetManager.removeWorkingSet(fWorkingSet);
		assertEquals(IWorkingSetManager.CHANGE_WORKING_SET_REMOVE, fChangeProperty);
		assertEquals(fWorkingSet, fChangeOldValue);				
		assertEquals(null, fChangeNewValue);						

		resetChangeData();
		fWorkingSet.setName(WORKING_SET_NAME_2);
		assertEquals(IWorkingSetManager.CHANGE_WORKING_SET_NAME_CHANGE, fChangeProperty);
		assertEquals(null, fChangeOldValue);				
		assertEquals(fWorkingSet, fChangeNewValue);						

		resetChangeData();
		fWorkingSet.setElements(new IAdaptable[] {});
		assertEquals(IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE, fChangeProperty);
		assertEquals(null, fChangeOldValue);				
		assertEquals(fWorkingSet, fChangeNewValue);						
	}
	public void testAddRecentWorkingSet() throws Throwable {
		fWorkingSetManager.addRecentWorkingSet(fWorkingSet);
		fWorkingSetManager.addWorkingSet(fWorkingSet);	
		assertTrue(ArrayUtil.equals(new IWorkingSet[] {fWorkingSet}, fWorkingSetManager.getRecentWorkingSets()));
		
		IWorkingSet workingSet2 = fWorkingSetManager.createWorkingSet(WORKING_SET_NAME_2, new IAdaptable[] {fWorkspace.getRoot()});
		fWorkingSetManager.addRecentWorkingSet(workingSet2);
		fWorkingSetManager.addWorkingSet(workingSet2);
		assertTrue(ArrayUtil.equals(new IWorkingSet[] {workingSet2, fWorkingSet}, fWorkingSetManager.getRecentWorkingSets()));
	}
	public void testAddWorkingSet() throws Throwable {
		fWorkingSetManager.addWorkingSet(fWorkingSet);			
		assertTrue(ArrayUtil.equals(new IWorkingSet[] {fWorkingSet}, fWorkingSetManager.getWorkingSets()));

		boolean exceptionThrown = false;
		try {
			fWorkingSetManager.addWorkingSet(fWorkingSet);
		}
		catch (RuntimeException exception) {
			exceptionThrown = true;
		}
		assertTrue(exceptionThrown);
		assertTrue(ArrayUtil.equals(new IWorkingSet[] {fWorkingSet}, fWorkingSetManager.getWorkingSets()));
	}
	public void testCreateWorkingSet() throws Throwable {
		IWorkingSet workingSet2 = fWorkingSetManager.createWorkingSet(WORKING_SET_NAME_2, new IAdaptable[] {fWorkspace.getRoot()});
		assertEquals(WORKING_SET_NAME_2, workingSet2.getName());
		assertTrue(ArrayUtil.equals(new IAdaptable[] {fWorkspace.getRoot()}, workingSet2.getElements()));

		workingSet2 = fWorkingSetManager.createWorkingSet("", new IAdaptable[] {});
		assertEquals("", workingSet2.getName());
		assertTrue(ArrayUtil.equals(new IAdaptable[] {}, workingSet2.getElements()));
	}
	public void testCreateWorkingSetSelectionDialog() throws Throwable {
		IWorkbenchWindow window = openTestWindow();
		IWorkingSetSelectionDialog dialog = fWorkingSetManager.createWorkingSetSelectionDialog(window.getShell(), true);
		
		assertNotNull(dialog);
	}
	public void testGetRecentWorkingSets() throws Throwable {
		assertEquals(0, fWorkingSetManager.getRecentWorkingSets().length);

		fWorkingSetManager.addRecentWorkingSet(fWorkingSet);
		fWorkingSetManager.addWorkingSet(fWorkingSet);
		assertTrue(ArrayUtil.equals(new IWorkingSet[] {fWorkingSet}, fWorkingSetManager.getRecentWorkingSets()));

		IWorkingSet workingSet2 = fWorkingSetManager.createWorkingSet(WORKING_SET_NAME_2, new IAdaptable[] {fWorkspace.getRoot()});
		fWorkingSetManager.addRecentWorkingSet(workingSet2);
		fWorkingSetManager.addWorkingSet(workingSet2);
		assertTrue(ArrayUtil.equals(new IWorkingSet[] {workingSet2, fWorkingSet}, fWorkingSetManager.getRecentWorkingSets()));
		
		fWorkingSetManager.removeWorkingSet(workingSet2);
		assertTrue(ArrayUtil.equals(new IWorkingSet[] {fWorkingSet}, fWorkingSetManager.getRecentWorkingSets()));
	}
	public void testGetWorkingSet() throws Throwable {
		assertNull(fWorkingSetManager.getWorkingSet(WORKING_SET_NAME_1));
		
		fWorkingSetManager.addWorkingSet(fWorkingSet);	
		assertNotNull(fWorkingSetManager.getWorkingSet(fWorkingSet.getName()));

		assertNull(fWorkingSetManager.getWorkingSet(""));
		
		assertNull(fWorkingSetManager.getWorkingSet(null));
	}
	public void testGetWorkingSets() throws Throwable {
		assertTrue(ArrayUtil.equals(new IWorkingSet[] {}, fWorkingSetManager.getWorkingSets()));

		fWorkingSetManager.addWorkingSet(fWorkingSet);	
		assertTrue(ArrayUtil.equals(new IWorkingSet[] {fWorkingSet}, fWorkingSetManager.getWorkingSets()));

		try {
			fWorkingSetManager.addWorkingSet(fWorkingSet);	
		}
		catch (RuntimeException exception) {}
		assertTrue(ArrayUtil.equals(new IWorkingSet[] {fWorkingSet}, fWorkingSetManager.getWorkingSets()));

		IWorkingSet workingSet2 = fWorkingSetManager.createWorkingSet(WORKING_SET_NAME_2, new IAdaptable[] {fWorkspace.getRoot()});
		fWorkingSetManager.addWorkingSet(workingSet2);	
		assertTrue(ArrayUtil.contains(fWorkingSetManager.getWorkingSets(), workingSet2));
		assertTrue(ArrayUtil.contains(fWorkingSetManager.getWorkingSets(), fWorkingSet));		
	}
	public void testRemovePropertyChangeListener() throws Throwable {
		IPropertyChangeListener listener = new TestPropertyChangeListener();
		
		fWorkingSetManager.removePropertyChangeListener(listener);
		
		fWorkingSetManager.addPropertyChangeListener(listener);
		fWorkingSetManager.removePropertyChangeListener(listener);		

		resetChangeData();
		fWorkingSet.setName(WORKING_SET_NAME_1);
		assertEquals("", fChangeProperty);
	}
	public void testRemoveWorkingSet() throws Throwable {
		fWorkingSetManager.removeWorkingSet(fWorkingSet);
		assertTrue(ArrayUtil.equals(new IWorkingSet[] {}, fWorkingSetManager.getWorkingSets()));
				
		fWorkingSetManager.addWorkingSet(fWorkingSet);
		IWorkingSet workingSet2 = fWorkingSetManager.createWorkingSet(WORKING_SET_NAME_2, new IAdaptable[] {fWorkspace.getRoot()});
		fWorkingSetManager.addWorkingSet(workingSet2);	
		fWorkingSetManager.removeWorkingSet(fWorkingSet);
		assertTrue(ArrayUtil.equals(new IWorkingSet[] {workingSet2}, fWorkingSetManager.getWorkingSets()));
	}
}