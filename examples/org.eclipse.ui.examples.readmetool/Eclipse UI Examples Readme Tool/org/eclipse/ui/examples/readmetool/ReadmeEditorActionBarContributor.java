package org.eclipse.ui.examples.readmetool;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.texteditor.BasicTextEditorActionContributor;

/**
 * This class demonstrates action contribution for the readme editor.
 * A number of menu, toolbar, and status line contributions are defined
 * in the workbench window.  These actions are shared among all 
 * readme editors, and are only visible when a readme editor is 
 * active.  Otherwise, they are invisible.
 */
public class ReadmeEditorActionBarContributor extends BasicTextEditorActionContributor 
{
	private EditorAction action1;
	private EditorAction action2;
	private EditorAction action3;
	private DirtyStateContribution dirtyStateContribution;
	
	class EditorAction extends Action {
		private Shell shell;
		private IEditorPart activeEditor;
		public EditorAction(String label) {
			super(label);
		}
		public void setShell(Shell shell) {
			this.shell = shell;
		}
		public void run() {
			String editorName = "Empty";
			if (activeEditor != null)
				editorName = activeEditor.getTitle();
			MessageDialog.openInformation(shell,
				"Readme Editor", 
				"Readme Editor Action executed: " + getText() +
				" for: " + editorName);
		}
		public void setActiveEditor(IEditorPart part) {
			activeEditor = part;
		}
	}
/**
 * Creates a new ReadmeEditorActionBarContributor.
 */
public ReadmeEditorActionBarContributor() {
	ImageRegistry registry = ReadmePlugin.getDefault().getImageRegistry();
	action1 = new EditorAction("&Editor Action1");
	action1.setToolTipText("Readme Editor Action1");
	action1.setDisabledImageDescriptor(ReadmeImages.EDITOR_ACTION1_IMAGE_DISABLE);
	action1.setImageDescriptor(ReadmeImages.EDITOR_ACTION1_IMAGE_ENABLE);
	action1.setHoverImageDescriptor(ReadmeImages.EDITOR_ACTION1_IMAGE);
	WorkbenchHelp.setHelp(action1, new String[] {IReadmeConstants.EDITOR_ACTION1_CONTEXT});
	
	action2 = new EditorAction("&Editor Action2");
	action2.setToolTipText("Readme Editor Action2");
	action2.setDisabledImageDescriptor(ReadmeImages.EDITOR_ACTION2_IMAGE_DISABLE);
	action2.setImageDescriptor(ReadmeImages.EDITOR_ACTION2_IMAGE_ENABLE);
	action2.setHoverImageDescriptor(ReadmeImages.EDITOR_ACTION2_IMAGE);
	WorkbenchHelp.setHelp(action2, new String[] {IReadmeConstants.EDITOR_ACTION2_CONTEXT});
	
	action3 = new EditorAction("&Editor Action3");
	action3.setToolTipText("Readme Editor Action3");
	action3.setDisabledImageDescriptor(ReadmeImages.EDITOR_ACTION3_IMAGE_DISABLE);
	action3.setImageDescriptor(ReadmeImages.EDITOR_ACTION3_IMAGE_ENABLE);
	action3.setHoverImageDescriptor(ReadmeImages.EDITOR_ACTION3_IMAGE);
	WorkbenchHelp.setHelp(action3, new String[] {IReadmeConstants.EDITOR_ACTION3_CONTEXT});

	dirtyStateContribution = new DirtyStateContribution();
}
/** (non-Javadoc)
 * Method declared on EditorActionBarContributor
 */
public void contributeToMenu(IMenuManager menuManager) {
	// Run super.
	super.contributeToMenu(menuManager);
	
	// Editor-specitic menu
	MenuManager readmeMenu = new MenuManager("&Readme");
	// It is important to append the menu to the
	// group "additions". This group is created
	// between "Project" and "Tools" menus
	// for this purpose.
	menuManager.insertAfter("additions", readmeMenu);
	readmeMenu.add(action1);
	readmeMenu.add(action2);
	readmeMenu.add(action3);
}
/** (non-Javadoc)
 * Method declared on EditorActionBarContributor
 */
public void contributeToStatusLine(IStatusLineManager statusLineManager) {
	// Run super.
	super.contributeToStatusLine(statusLineManager);

	// Test status line.	
	statusLineManager.setMessage("Editor is active");
	statusLineManager.add(dirtyStateContribution);
}
/** (non-Javadoc)
 * Method declared on EditorActionBarContributor
 */
public void contributeToToolBar(IToolBarManager toolBarManager) {
	// Run super.
	super.contributeToToolBar(toolBarManager);
	
	// Add toolbar stuff.
	toolBarManager.add(new Separator("ReadmeEditor"));
	toolBarManager.add(action1);
	toolBarManager.add(action2);
	toolBarManager.add(action3);
}
/** (non-Javadoc)
 * Method declared on IEditorActionBarContributor
 */
public void setActiveEditor(IEditorPart editor) {
	// Run super.
	super.setActiveEditor(editor);

	// Retarget shared actions to new editor.
	action1.setActiveEditor(editor);
	action2.setActiveEditor(editor);
	action3.setActiveEditor(editor);
	dirtyStateContribution.editorChanged(editor);
}
}
