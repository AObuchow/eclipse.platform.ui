package org.eclipse.ui.texteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ResourceBundle;

import org.eclipse.jface.text.ITextOperationTarget;


/**
 * An action which gets a text operation target from its text editor.
 * <p>
 * The action is initially associated with a text editor via the constructor,
 * but can subsequently be changed using <code>setEditor</code>.
 * </p>
 * <p>
 * If this class is used as is, it works by asking the text editor for its
 * text operation target adapter (using <code>getAdapter(ITextOperationTarget.class)</code>. 
 * The action runs this operation with the pre-configured opcode.
 * </p>
 */
public final class TextOperationAction extends TextEditorAction {
	
	/** The text operation code */
	private int fOperationCode= -1;
	/** The text operation target */
	private ITextOperationTarget fOperationTarget;
	
	/**
	 * Creates and initializes the action for the given text editor and operation 
	 * code. The action configures its visual representation from the given resource
	 * bundle. The action works by asking the text editor at the time for its 
	 * text operation target adapter (using
	 * <code>getAdapter(ITextOperationTarget.class)</code>. The action runs that
	 * operation with the given opcode.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or 
	 *   <code>null</code> if none
	 * @param editor the text editor
	 * @param operationCode the operation code
	 * @see ResourceAction#ResourceAction
	 */
	public TextOperationAction(ResourceBundle bundle, String prefix, ITextEditor editor, int operationCode) {
		super(bundle, prefix, editor);
		fOperationCode= operationCode;
		update();
	}
	
	/**
	 * The <code>TextOperationAction</code> implementation of this 
	 * <code>IAction</code> method runs the operation with the current
	 * operation code.
	 */
	public void run() {
		if (fOperationCode != -1 && fOperationTarget != null)
			fOperationTarget.doOperation(fOperationCode);
	}
	
	/**
	 * The <code>TextOperationAction</code> implementation of this 
	 * <code>IUpdate</code> method discovers the operation through the current
	 * editor's <code>ITextOperationTarget</code> adapter, and sets the
	 * enabled state accordingly.
	 */
	public void update() {
		
		ITextEditor editor= getTextEditor();
		if (editor instanceof ITextEditorExtension) {
			ITextEditorExtension extension= (ITextEditorExtension) editor;
			if (extension.isEditorInputReadOnly()) {
				setEnabled(false);
				return;
			}
		}
		
		if (fOperationTarget == null && editor!= null && fOperationCode != -1)
			fOperationTarget= (ITextOperationTarget) editor.getAdapter(ITextOperationTarget.class);
			
		boolean isEnabled= (fOperationTarget != null && fOperationTarget.canDoOperation(fOperationCode));
		setEnabled(isEnabled);
	}
	
	/*
	 * @see TextEditorAction#setEditor(ITextEditor)
	 */
	public void setEditor(ITextEditor editor) {
		super.setEditor(editor);
		fOperationTarget= null;
	}
}
