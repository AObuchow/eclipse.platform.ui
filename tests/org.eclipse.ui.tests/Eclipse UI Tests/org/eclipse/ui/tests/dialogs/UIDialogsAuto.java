package org.eclipse.ui.tests.dialogs;

import junit.framework.TestCase;
import org.eclipse.core.resources.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.dialogs.*;
import org.eclipse.ui.internal.model.AdaptableList;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.ui.junit.util.DialogCheck;
import org.eclipse.ui.model.WorkbenchContentProvider;


public class UIDialogsAuto extends TestCase {
	private static final String PROJECT_SELECTION_MESSAGE = WorkbenchMessages.getString("BuildOrderPreference.selectOtherProjects");
	private static final String FILTER_SELECTION_MESSAGE = ResourceNavigatorMessagesCopy.getString("FilterSelection.message");
	
	public UIDialogsAuto(String name) {
		super(name);
	}
	private Shell getShell() {
		return DialogCheck.getShell();
	}
	private IWorkbench getWorkbench() {
		return WorkbenchPlugin.getDefault().getWorkbench();
	}

	public void testAbout() {
		Dialog dialog = new AboutDialog( getShell() );
		DialogCheck.assertDialogTexts(dialog, this);
	}
	public void testAddProjects() {
		Dialog dialog = new ListSelectionDialog(getShell(),
			null,
			new SimpleListContentProvider(),
			new LabelProvider(),
			PROJECT_SELECTION_MESSAGE
		);
		DialogCheck.assertDialogTexts(dialog, this);
	}
	public void testCopyMoveProject() {
		IProject dummyProject = ResourcesPlugin.getWorkspace().getRoot().getProject("DummyProject");
		Dialog dialog = new ProjectLocationSelectionDialog(getShell(), dummyProject);
		DialogCheck.assertDialogTexts(dialog, this);
	}
	public void testCopyMoveResource() {
		Dialog dialog = new ContainerSelectionDialog( getShell(), null, true, WorkbenchMessages.getString("CopyResourceAction.selectDestination") );
		DialogCheck.assertDialogTexts(dialog, this);
	}
	public void testEditActionSetsDialog() {
		Dialog dialog;
		Perspective persp = null;
		//Test perspective: use current perspective of test case
		try {/*fixme: should try to get current perspective, or default; currently only
			 gets first perspective in the registry.*/
			persp = new Perspective((PerspectiveDescriptor)getWorkbench().getPerspectiveRegistry().getPerspectives()[0],
			                                    (WorkbenchPage)getWorkbench().getActiveWorkbenchWindow().getActivePage()
			);
			dialog = new ActionSetSelectionDialog(getShell(),  persp);
		} catch (WorkbenchException e) {
			dialog = null;
		}
		DialogCheck.assertDialogTexts(dialog, this);
		if (persp != null) {
			persp.dispose();
		}
	}
	public void testEditorSelection() {
		Dialog dialog = new EditorSelectionDialog( getShell() );
		DialogCheck.assertDialogTexts(dialog, this);
	}
/**
 * 1GJWD2E: ITPUI:ALL - Test classes should not be released in public packages.
 * 
	public void testFindReplace() {
		Dialog dialog = TextEditorTestStub.newFindReplaceDialog( getShell() );
		DialogCheck.assertDialogTexts(dialog, this);
	}
	public void testGotoResource() {
		Dialog dialog = NavigatorTestStub.newGotoResourceDialog(getShell(), new IResource[0]);
		DialogCheck.assertDialogTexts(dialog, this);
	}
 */
	public void testNavigatorFilter() {
		Dialog dialog = new ListSelectionDialog(getShell(), null, new SimpleListContentProvider(), new LabelProvider(), FILTER_SELECTION_MESSAGE);
		DialogCheck.assertDialogTexts(dialog, this);
	}
	public void testNewFileType() {
		Dialog dialog = new FileExtensionDialog( getShell() );
		DialogCheck.assertDialogTexts(dialog, this);
	}
	public void testProgressInformation() {
		ProgressMonitorDialog dialog = new ProgressMonitorDialog( getShell() );
		dialog.setBlockOnOpen(true);
		DialogCheck.assertDialogTexts(dialog, this);
	}
	public void testSaveAll() {
		YesNoCancelListSelectionDialog dialog = new YesNoCancelListSelectionDialog(
			getShell(),
			new AdaptableList(),
			new WorkbenchContentProvider(),
			new WorkbenchPartLabelProvider(),
			WorkbenchMessages.getString("EditorManager.saveResourcesMessage")
		);
		dialog.setTitle(WorkbenchMessages.getString("EditorManager.saveResourcesTitle"));
		DialogCheck.assertDialogTexts(dialog, this);
	}
	public void testSaveAs() {
		Dialog dialog = new SaveAsDialog( getShell() );
		DialogCheck.assertDialogTexts(dialog, this);
	}
	public void testSavePerspective() {
		PerspectiveRegistry reg = (PerspectiveRegistry)WorkbenchPlugin.getDefault().getPerspectiveRegistry();
		// Get persp name.
		SavePerspectiveDialog dialog = new SavePerspectiveDialog(getShell(), reg);
		IPerspectiveDescriptor description = reg.findPerspectiveWithId( getWorkbench().getActiveWorkbenchWindow().getActivePage().getPerspective().getId() );
		dialog.setInitialSelection(description);
		DialogCheck.assertDialogTexts(dialog, this);
	}
	public void testSelectPerspective() {
		Dialog dialog = new SelectPerspectiveDialog(getShell(), PlatformUI.getWorkbench().getPerspectiveRegistry() );
		DialogCheck.assertDialogTexts(dialog, this);
	}
	public void testSelectTypes() {
		Dialog dialog = new TypeFilteringDialog(getShell(), null);
		DialogCheck.assertDialogTexts(dialog, this);
	}
	public void testShowView() {
		Dialog dialog = new ShowViewDialog( getShell(), WorkbenchPlugin.getDefault().getViewRegistry() );
		DialogCheck.assertDialogTexts(dialog, this);
	}
/**
 * 1GJWD2E: ITPUI:ALL - Test classes should not be released in public packages.
 * 
	public void testTaskFilters() {
		Dialog dialog = TaskListTestStub.newFiltersDialog( getShell() );
		DialogCheck.assertDialogTexts(dialog, this);
	}
 */
}



