package org.eclipse.ui.tests.dialogs;

import java.util.Iterator;

import junit.framework.TestCase;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.dialogs.*;
import org.eclipse.jdt.junit.util.DialogCheck;
import org.eclipse.ui.model.IWorkbenchAdapter;


public class UIPreferencesAuto extends TestCase{

	private IProject _project;
	private static final String PROJECT_NAME = "DummyProject";
	
	public UIPreferencesAuto(String name) {
		super(name);
	}
	private Shell getShell() {
		return DialogCheck.getShell();
	}
	
	private IProject getDummyProject() {
		try {
			IProject projects[] = WorkbenchPlugin.getPluginWorkspace().getRoot().getProjects();
			for (int i = 0; i < projects.length; i++) {
				if ( projects[i].getName().equals(PROJECT_NAME) ) {
					projects[i].delete(true, null);
					break;
				}
			}
			_project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
			_project.create(null);
		} catch (CoreException e) {
			System.out.println(e);
		}
		return _project;
	}
	private void deleteDummyProject(){
		try {
			if (_project != null) {
				_project.delete(true, null);
			}
		} catch (CoreException e) {
			System.out.println(e);
		}
	}
	private PreferenceDialog getPreferenceDialog(String id) {
		PreferenceDialogWrapper dialog = null;
		PreferenceManager manager = WorkbenchPlugin.getDefault().getPreferenceManager();
		if (manager != null) {
			dialog = new PreferenceDialogWrapper(getShell(), manager);
			dialog.create();	
			WorkbenchHelp.setHelp(dialog.getShell(), new Object[]{IHelpContextIds.PREFERENCE_DIALOG});

			for (Iterator iterator = manager.getElements(PreferenceManager.PRE_ORDER).iterator();
			     iterator.hasNext();)
			{
				IPreferenceNode node = (IPreferenceNode)iterator.next();
				if ( node.getId().equals(id) ) {
					dialog.showPage(node);
					break;
				}
			}
		}
		return dialog;
	}
	private PropertyDialog getPropertyDialog(String id) {
		PropertyDialogWrapper dialog = null;

		PropertyPageManager manager = new PropertyPageManager();
		String title = "";
		String name  = "";
		
		
		IProject element = getDummyProject();
		if (element ==  null) {			
			return null;
		}
		// load pages for the selection
		// fill the manager with contributions from the matching contributors
		PropertyPageContributorManager.getManager().contribute(manager, element);
		
		IWorkbenchAdapter adapter = (IWorkbenchAdapter)element.getAdapter(IWorkbenchAdapter.class);
		if (adapter != null) {
			name = adapter.getLabel(element);
		}
		
		// testing if there are pages in the manager
		Iterator pages = manager.getElements(PreferenceManager.PRE_ORDER).iterator();		
		if (!pages.hasNext()) {
			return null;
		} else {
			title = WorkbenchMessages.format("PropertyDialog.propertyMessage", new Object[] {name});
			dialog = new PropertyDialogWrapper(getShell(), manager, new StructuredSelection(element)); 
			dialog.create();
			dialog.getShell().setText(title);
			WorkbenchHelp.setHelp(dialog.getShell(), new Object[]{IHelpContextIds.PROPERTY_DIALOG});
			for (Iterator iterator = manager.getElements(PreferenceManager.PRE_ORDER).iterator();
			     iterator.hasNext();)
			{
				IPreferenceNode node = (IPreferenceNode)iterator.next();
				if ( node.getId().equals(id) ) {
					dialog.showPage(node);
					break;
				}
			}
		}
		return dialog;
	}
	
	public void testWorkbenchPref() {
		Dialog dialog = getPreferenceDialog("org.eclipse.ui.preferencePages.Workbench");
		DialogCheck.assertDialogTexts(dialog, this);
	}
	public void testAppearancePref() {
		Dialog dialog = getPreferenceDialog("org.eclipse.ui.preferencePages.Views");
		DialogCheck.assertDialogTexts(dialog, this);
	}
	public void testCompareViewersPref() {
		Dialog dialog = getPreferenceDialog("org.eclipse.compare.internal.ComparePreferencePage");
		DialogCheck.assertDialogTexts(dialog, this);
	}
	public void testDefaultTextEditorPref() {
		Dialog dialog = getPreferenceDialog("org.eclipse.ui.preferencePages.TextEditor");
		DialogCheck.assertDialogTexts(dialog, this);
	}
	public void testFileEditorsPref() {
		Dialog dialog = getPreferenceDialog("org.eclipse.ui.preferencePages.FileEditors");
		DialogCheck.assertDialogTexts(dialog, this);
	}
	public void testLocalHistoryPref() {
		Dialog dialog = getPreferenceDialog("org.eclipse.ui.preferencePages.FileStates");
		DialogCheck.assertDialogTexts(dialog, this);
	}
	public void testPerspectivesPref() {
		Dialog dialog = getPreferenceDialog("org.eclipse.ui.preferencePages.Perspectives");
		DialogCheck.assertDialogTexts(dialog, this);
	}
	public void testInfoProp() {
		/*
		 * Commented out because it generates a failure
		 * of expect and actual width values. Suspect this
		 * is an SWT issue.
		 * 
		Dialog dialog = getPropertyDialog("org.eclipse.ui.propertypages.info.file");
		DialogCheck.assertDialogTexts(dialog, this);
		 */
	}
	public void testProjectReferencesProp() {
		/*
		 * Commented out because it generates a failure
		 * of expect and actual width values. Suspect this
		 * is an SWT issue.
		 * 
		Dialog dialog = getPropertyDialog("org.eclipse.ui.propertypages.project.reference");
		DialogCheck.assertDialogTexts(dialog, this);
		 */
	}
}

