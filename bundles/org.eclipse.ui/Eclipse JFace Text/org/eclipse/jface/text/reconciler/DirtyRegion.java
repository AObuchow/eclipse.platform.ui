package org.eclipse.jface.text.reconciler;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.text.ITypedRegion;

/**
 * A dirty region describes a document range which has been changed.
 */
public class DirtyRegion implements ITypedRegion {
	
	/** Definitions of edit types */
	final static public String INSERT= "__insert"; //$NON-NLS-1$
	final static public String REMOVE= "__remove"; //$NON-NLS-1$
	
	/** The region's offset */
	private int fOffset;
	/** The region's length */
	private int fLength;
	/** Indicates the type of the applied change */
	private String fType;
	/** The text which has been inserted */
	private String fText;

	/**
	 * Creates a new dirty region.
	 *
	 * 
	 * @param offset the offset within the document where the change occurred
	 * @param length the length of the text within the document that changed
	 * @param type the type of change that this region represents: <code>INSERT</code> or <code>REMOVE</code>
	 * @param text the substitution text
	 */
	public DirtyRegion(int offset, int length, String type, String text) {
		fOffset= offset;
		fLength= length;
		fType= type;
		fText= text;
	}
	/*
	 * @see ITypedRegion#getLength()
	 */
	public int getLength() {
		return fLength;
	}
	/*
	 * @see ITypedRegion#getOffset()
	 */
	public int getOffset() {
		return fOffset;
	}
	/**
	 * Returns the text that changed as part of the region change.
	 * 
	 * @return the changed text
	 */
	public String getText() {
		return fText;
	}
	/*
	 * @see ITypedRegion#getType
	 */
	public String getType() {
		return fType;
	}
	/**
	 * Modify the receiver so that it encompasses the region specified by the dirty region.
	 * 
	 * @param dr the dirty region with which to merge
	 */
	void mergeWith(DirtyRegion dr) {
		int start= Math.min(fOffset, dr.fOffset);
		int end= Math.max(fOffset + fLength, dr.fOffset + dr.fLength);
		fOffset= start;
		fLength= end - start;
		fText= (dr.fText == null ? fText : (fText == null) ? dr.fText : fText + dr.fText);
	}
}
