package org.eclipse.jface.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.util.Assert;

/**
 * A child document represent a range of its parent document. 
 * The child document is always in sync with its parent document
 * by utilizing the parent document as its <code>ITextStore</code>.
 * This class is for internal use only.
 *
 * @see ITextStore
 */
public final class ChildDocument extends AbstractDocument {
	
	
	/**
	 * Implements ITextStore based on IDocument.
	 */
	class TextStore implements ITextStore {
		
		/*
		 * @see ITextStore#set
		 */
		public void set(String txt) {
			update(true);
			try {
				fParentDocument.replace(fRange.getOffset(), fRange.getLength(), txt);
			} catch (BadLocationException x) {
				// cannot happen
			} finally {
				update(false);
			}
		}
		
		/*
		 * @see ITextStore#replace
		 */
		public void replace(int offset, int length, String txt) {
			update(true);
			try {
				fParentDocument.replace(fRange.getOffset() + offset, length, txt);
			} catch (BadLocationException x) {
				// ignored as surrounding document should have handled this
			} finally {
				update(false);
			}
		}
		
		/*
		 * @see ITextStore#getLength
		 */
		public int getLength() {
			return fRange.getLength();
		}
		
		/*
		 * @see ITextStore#get
		 */
		public String get(int offset, int length) {
			try {
				return fParentDocument.get(fRange.getOffset() + offset, length);
			} catch (BadLocationException x) {
			}
			
			return null;
		}
		
		/*
		 * @see ITextStore#get
		 */
		public char get(int offset) {
			try {
				return fParentDocument.getChar(fRange.getOffset() + offset);
			} catch (BadLocationException x) {
			}
			
			return (char) 0;
		}
	};
	
	
	
	/** The parent document */
	private IDocument fParentDocument;
	/** The section inside the parent document */
	private Position fRange;
	/** The document event issued by the parent document */
	private DocumentEvent fParentEvent;
	/** The document event issued and to be issued by the child document */
	private DocumentEvent fEvent;
	/** Indicates whether the child document initiated a parent document update or not */
	private boolean fIsUpdating= false;
	
	/**
	 * Creates a child document for the given range of the given parent document.
	 *
	 * @param parentDocument the parent Document
	 * @param range the parent document range covered by the child document
	 */
	public ChildDocument(IDocument parentDocument, Position range) {
		super();
		
		fParentDocument= parentDocument;
		fRange= range;
		
		ITextStore s= new TextStore();
		ILineTracker tracker= new DefaultLineTracker();
		tracker.set(s.get(0, fRange.getLength()));
		
		setTextStore(s);
		setLineTracker(tracker);
		
		completeInitialization();
	}
	
	/**
	 * Sets the child document's parent document range.
	 *
	 * @param offset the offset of the parent document range
	 * @param length the length of the parent document range
	 */
	public void setParentDocumentRange(int offset, int length) throws BadLocationException {
		
		if (offset < 0 || length < 0 || offset + length > fParentDocument.getLength())
			throw new BadLocationException();
								
		fRange.setOffset(offset);
		fRange.setLength(length);
		
		getTracker().set(fParentDocument.get(offset, length));
	}
	
	/**
	 * Returns parent document
	 *
	 * @return the parent document
	 */
	public IDocument getParentDocument() {
		return fParentDocument;
	}
	
	/**
	 * Returns the range of the parent document covered by this child document.
	 *
	 * @return the child document's parent document range
	 */
	public Position getParentDocumentRange() {
		return fRange;
	}
	
	/**
	 * Marks whether a parent document update has been initialized
	 * by this child document or not.
	 *
	 * @param updating the mark
	 */
	private void update(boolean updating) {
		fIsUpdating= updating;
	}
	
	/**
	 * Transforms a document event of the parent document into a child document
	 * based document event.
	 *
	 * @param e the parent document event
	 * @return the child document event
	 */
	private DocumentEvent normalize(DocumentEvent e) {
		
		int delta= e.getOffset() - fRange.getOffset();
		int offset= delta < 0 ? 0 : delta;
		int length= delta < 0 ? e.fLength + delta : e.fLength;
		if (offset + length > fRange.getLength())
			length= fRange.getLength() - offset;
			
		return new ChildDocumentEvent(this, offset, length, e.fText, e); 
	}
	
	/**
	 * When called this child document is informed about a forthcoming change
	 * of its parent document. This child document checks whether the parent
	 * document changed affects it and if so informs all document listeners.
	 *
	 * @param event the parent document event
	 */
	public void parentDocumentAboutToBeChanged(DocumentEvent event) {
		
		fParentEvent= event;
				
		if (fRange.overlapsWith(event.fOffset, event.fLength)) {			
			fEvent= normalize(event);
			delayedFireDocumentAboutToBeChanged();
		} else
			fEvent= null;
	}
		
	/**
	 * When called this child document is informed about a change of its parent document.
	 * If this child document is affected it informs all of its document listeners.
	 *
	 * @param event the parent document event
	 */
	public void parentDocumentChanged(DocumentEvent event) {
		if ( !fIsUpdating && event == fParentEvent && fEvent != null) {
			try {
				getTracker().replace(fEvent.fOffset, fEvent.fLength, fEvent.fText);
				fireDocumentChanged(fEvent);
			} catch (BadLocationException x) {
				Assert.isLegal(false);
			}
		}
	}
	
	/*
	 * @see AbstractDocument#fireDocumentAboutToBeChanged
	 */
	protected void fireDocumentAboutToBeChanged(DocumentEvent event) {
		// delay it until there is a notification from the parent document
		// otherwise there it is expensive to construct the parent document information
	}
	
	/**
	 * Fires the child document event as about-to-be-changed event to all
	 * registed listeners.
	 */
	private void delayedFireDocumentAboutToBeChanged() {
		super.fireDocumentAboutToBeChanged(fEvent);
	}
	
	/**
	 * Ignores the given event and sends the similar child document event instead.
	 *
	 * @param event the event to be ignored
	 */
	protected void fireDocumentChanged(DocumentEvent event) {
		super.fireDocumentChanged(fEvent);
	}
}