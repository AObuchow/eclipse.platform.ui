package org.eclipse.ui.texteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.util.ResourceBundle;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelectionProvider;

import org.eclipse.ui.IWorkbenchPage;


/**
 * Action for jumping to a particular line if the editor's text viewer. 
 * The user is requested to enter the line number into an input dialog.
 * The action is initially associated with a text editor via the constructor,
 * but that can be subsequently changed using <code>setEditor</code>.
 * <p>
 * The following keys, prepended by the given option prefix,
 * are used for retrieving resources from the given bundle:
 * <ul>
 *   <li><code>"dialog.invalid_range"</code> - to indicate an invalid line number</li>
 *   <li><code>"dialog.invalid_input"</code> - to indicate an invalid line number format</li>
 *   <li><code>"dialog.title"</code> - the input dialog's title</li>
 *   <li><code>"dialog.message"</code> - the input dialog's message</li>
 * </ul>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class GotoLineAction extends TextEditorAction {

	/**
	 * Validates whether the text found in the input field of the
	 * dialog forms a valid line number, i.e. one to which can be 
	 * jumped.
	 */
	class NumberValidator implements IInputValidator {
		
		public String isValid(String input) {
			try {
				int i= Integer.parseInt(input);
				if (i <= 0 || fLastLine < i)
					return fBundle.getString(fPrefix + "dialog.invalid_range"); //$NON-NLS-1$
					
			} catch (NumberFormatException x) {
				return fBundle.getString(fPrefix + "dialog.invalid_input"); //$NON-NLS-1$
			}
			
			return ""; //$NON-NLS-1$
		}
	};
	
	/**
	 * Standard input dialog which additionally sets the focus to the
	 * text input field. Workaround for <code>InputDialog</code> issue.
	 * 1GIJZOO: ITPSRCEDIT:ALL - Gotodialog's edit field has no initial focus
	 */
	class GotoLineDialog extends InputDialog {
		
		/*
		 * @see InputDialog#InputDialog
		 */
		public GotoLineDialog(Shell parent, String title, String message, String initialValue, IInputValidator validator) {
			super(parent, title, message, initialValue, validator);
		}
		
		/*
		 * @see InputDialog#createDialogArea(Composite)
		 */
		protected Control createDialogArea(Composite parent) {
			Control result= super.createDialogArea(parent);
			getText().setFocus();
			return result;
		}
	};
	
	/** The biggest valid line number of the presented document */
	private int fLastLine;
	/** This action's resource bundle */
	private ResourceBundle fBundle;
	/** This action's prefix used for accessing the resource bundle */
	private String fPrefix;
	
	/**
	 * Creates a new action for the given text editor. The action configures its
	 * visual representation from the given resource bundle.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or 
	 *   <code>null</code> if none
	 * @param editor the text editor
	 * @see ResourceAction#ResourceAction
	 */
	public GotoLineAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
		super(bundle, prefix, editor);
		fBundle= bundle;
		fPrefix= prefix;
	}
	
	/**
	 * Jumps to the given line.
	 *
	 * @param line the line to jump to
	 */
	private void gotoLine(int line) {
		
		ITextEditor editor= getTextEditor();
		
		IDocumentProvider provider= editor.getDocumentProvider();
		IDocument document= provider.getDocument(editor.getEditorInput());
		try {
			
			int start= document.getLineOffset(line);			
			editor.selectAndReveal(start, 0);
			
			IWorkbenchPage page= editor.getSite().getPage();
			page.activate(editor);
			
		} catch (BadLocationException x) {
			// ignore
		}
	}
	
	/*
	 * @see Action#run()
	 */
	public void run() {
		try {
			
			ITextEditor editor= getTextEditor();

			
			IDocumentProvider docProvider= editor.getDocumentProvider();
			ISelectionProvider selProvider= editor.getSelectionProvider();
			ITextSelection selection= (ITextSelection) selProvider.getSelection();		
			
			IDocument document= docProvider.getDocument(editor.getEditorInput());
			fLastLine= document.getLineOfOffset(document.getLength()) + 1;
		
			String title= fBundle.getString(fPrefix + "dialog.title"); //$NON-NLS-1$
			String message= fBundle.getString(fPrefix + "dialog.message"); //$NON-NLS-1$
			String value= Integer.toString(selection.getStartLine() + 1);
			
			/*
			 * 1GIJZOO: ITPSRCEDIT:ALL - Gotodialog's edit field has no initial focus
			 */
			GotoLineDialog d= new GotoLineDialog(editor.getSite().getShell(), title, message, value, new NumberValidator());
			d.open();
			
			try {
				int line= Integer.parseInt(d.getValue());
				gotoLine(line - 1);
			} catch (NumberFormatException x) {
			}
		} catch (BadLocationException x) {
			return;
		}
	}
}