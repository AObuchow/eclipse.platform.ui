package org.eclipse.jface.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.jface.util.Assert;


/**
 * Implements a gap managing text store. The gap text store 
 * relies on the assumption that subsequent changes of a document are co-located.
 * The start of the gap is always moved to the location of the last change. The
 * size of the gap varies between the low water mark and the high water mark. <p>
 * This class is not intended to be subclassed.
 */
public class GapTextStore implements ITextStore {
	
	/** The store's content */
	private char[] fContent= new char[0];
	/** Starting index of the gap */
	private int fGapStart= -1;
	/** End index of the gap */
	private int fGapEnd= -1;
	
	/** The high water mark. If the gap is larger than this, it will be shrunken */
	private int fHighWatermark;
	/** The low water mark, If this gap is smaller than this, it will be extended */
	private int fLowWatermark;
	
	/**
	 * Creates a new empty text store using the specified low and high watermarks.
	 *
	 * @param lowWatermark if this gap is ever smaller than this, it will automatically be extended
	 * @param highWatermark if the gap is ever larger than this, it will automatically be shrunken
	 */
	public GapTextStore(int lowWatermark, int highWatermark) {
		Assert.isTrue(lowWatermark < highWatermark);
		fLowWatermark= lowWatermark;
		fHighWatermark= highWatermark;
	}
	/**
	 * Adjusts the gap so that is at the right offset and capable of handling
	 * the addition of a specified number of characters without having to be shifted.
	 * The <code>sizeHint</code> represents the range that will be filled afterwards.
	 * If the gap is already at the right offset, it must only be
	 * resized if it will be no longer between the low and high watermark.
	 *
	 * @param offset the offset at which the change happens
	 * @param sizeHint the number of character which will be inserted
	 */
	private void adjustGap(int offset, int sizeHint) {

		if (offset == fGapStart) {
			int size= (fGapEnd - fGapStart) - sizeHint;
			if (fLowWatermark <= size && size <= fHighWatermark)
				return;
		}
		
		moveAndResizeGap(offset, sizeHint);
	}
	/*
	 * @see ITextStore#get
	 */
	public char get(int offset) {
		
		if (offset < fGapStart)
			return fContent[offset];

		int gapLength= fGapEnd - fGapStart;
		return fContent[offset + gapLength];
	}
	/*
	 * @see ITextStore#get
	 */
	public String get(int offset, int length) {

		int end= offset + length;

		if (fContent == null)
			return ""; //$NON-NLS-1$
		
		if (end < fGapStart)
			return new String(fContent, offset, length);

		if (fGapStart < offset) {
			int gapLength= fGapEnd - fGapStart;
			return new String(fContent, offset + gapLength , length);
		}

		StringBuffer buf= new StringBuffer();
		buf.append(fContent, offset, fGapStart - offset);
		buf.append(fContent, fGapEnd, end - fGapStart);
		return buf.toString();
	}
	/**
	 * Returns a copy of the content of this text store.
	 * For internal use only.
	 *
	 * @return a copy of the content of this text store 
	 */
	protected String getContentAsString() {
		return new String(fContent);
	}
	/**
	 * Returns the end index of the gap managed by this text store.
	 * For internal use only.
	 *
	 * @returns the end index of the gap managed by this text store
	 */
	protected int getGapEndIndex() {
		return fGapEnd;
	}
	/**
	 * Returns the start index of the gap managed by this text store.
	 * For internal use only.
	 *
	 * @returns the start index of the gap managed by this text store
	 */
	protected int getGapStartIndex() {
		return fGapStart;
	}
	/*
	 * @see ITextStore#getLength
	 */
	public int getLength() {
		int length= fGapEnd - fGapStart;
		return (fContent.length - length);
	}
	/**
	 * Moves the gap to the specified offset and adjust its size to the
	 * anticipated change size. The given size represents the expected 
	 * range of the gap that will be filled after the gap has been moved.
	 * Thus the gap is resized to actual size + the specified size and
	 * moved to the given offset.
	 *
	 * @param offset the offset where the gap is moved to
	 * @param size the anticipated size of the change
	 */ 
	private void moveAndResizeGap(int offset, int size) {
		
		char[] content= null;
		int oldSize= fGapEnd - fGapStart;
		int newSize= fHighWatermark + size;


		if (newSize < 0) {

			if (oldSize > 0) {
				content= new char[fContent.length - oldSize];
				System.arraycopy(fContent, 0, content, 0, fGapStart);
				System.arraycopy(fContent, fGapEnd, content, fGapStart, content.length - fGapStart);
				fContent= content;
			}
			fGapStart= fGapEnd= offset;
			return;
		}


		content= new char[fContent.length + (newSize - oldSize)];

		int newGapStart= offset;
		int newGapEnd= newGapStart + newSize;

		if (oldSize == 0) {
			
			System.arraycopy(fContent, 0, content, 0, newGapStart);
			System.arraycopy(fContent, newGapStart, content, newGapEnd, content.length - newGapEnd);
		
		} else if (newGapStart < fGapStart) {
			
			int delta= fGapStart - newGapStart;
			System.arraycopy(fContent, 0, content, 0, newGapStart);
			System.arraycopy(fContent, newGapStart, content, newGapEnd, delta);
			System.arraycopy(fContent, fGapEnd, content, newGapEnd + delta, fContent.length - fGapEnd);

		} else {
		
			int delta= newGapStart - fGapStart;
			System.arraycopy(fContent, 0, content, 0, fGapStart);
			System.arraycopy(fContent, fGapEnd, content, fGapStart, delta);
			System.arraycopy(fContent, fGapEnd + delta, content, newGapEnd, content.length - newGapEnd);
		}


		fContent= content;
		fGapStart= newGapStart;
		fGapEnd= newGapEnd;
	}
	/*
	 * @see ITextStore#replace
	 */
	public void replace(int offset, int length, String text) {
		
		if (text == null)
			text= ""; //$NON-NLS-1$

		// move gap
		adjustGap(offset + length, text.length() - length);

		// overwrite
		int min= Math.min(text.length(), length);
		for (int i= offset, j= 0; i < offset + min; i++, j++)
			fContent[i]= text.charAt(j);

		if (length > text.length()) {
			// enlarge the gap
			fGapStart -= (length - text.length());
		} else if (text.length() > length) {
			// shrink gap
			fGapStart += (text.length() - length);
			for (int i= length; i < text.length(); i++)
				fContent[offset + i]= text.charAt(i);
		}	
	}
	/**
	 * Sets the content to <code>text</code> and removes the gap
	 * since there are no sensible predictions about 
	 * where the next change will occur.
	 * @see ITextStore#set
	 */
	public void set(String text) {
		
		if (text == null)
			text= ""; //$NON-NLS-1$

		fContent= text.toCharArray();

		fGapStart= -1;
		fGapEnd=   -1;
	}
}
