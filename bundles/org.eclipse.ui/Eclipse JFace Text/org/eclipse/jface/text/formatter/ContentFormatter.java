package org.eclipse.jface.text.formatter;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.jface.util.Assert;


/**
 * Standard implementation of <code>IContentFormatter</code>.
 * The formatter supports two operation modi: partition aware and
 * partition unaware. <p>
 * In the partition aware mode, the formatter determines the 
 * partitioning of the document region to be formatted. For each 
 * partition it determines all document positions  which are affected 
 * when text changes are applied to the partition. Those which overlap
 * with the partition are remembered as character positions. These
 * character positions are passed over to the formatting strategy
 * registered for the partition's content type. The formatting strategy
 * returns a string containing the formatted document partition as well
 * as the adapted character positions. The formatted partition replaces
 * the old content of the partition. The remembered document postions 
 * are updated with the adapted character positions. In addition, all
 * other document positions are accordingly adapted to the formatting 
 * changes.<p>
 * In the partition unaware mode, the document's partitioning is ignored
 * and the document is considered consisting of only one partition of 
 * the content type <code>IDocument.DEFAULT_CONTENT_TYPE</code>. The 
 * formatting process is similar to the partition aware mode, with the 
 * exception of having only one partition.<p>
 * Usually, clients instantiate this class and configure it before using it.
 *
 * @see IContentFormatter
 * @see IDocument
 * @see ITypedRegion
 * @see Position
 */
public class ContentFormatter implements IContentFormatter {
		
	/**
	 * Defines a reference to either the offset or the end offset of
	 * a particular position.
	 */
	static class PositionReference implements Comparable {
		
		/** The referenced position */
		protected Position fPosition;
		/** The reference to either the offset or the end offset */
		protected boolean fRefersToOffset;
		/** The original category of the referenced position */
		protected String fCategory;
		
		protected PositionReference(Position position, boolean refersToOffset, String category) {
			fPosition= position;
			fRefersToOffset= refersToOffset;
			fCategory= category;
		}
		
		/**
		 * Returns the offset of the referenced position.
		 */
		protected int getOffset() {
			return fPosition.getOffset();
		}
		
		/**
		 * Manipulates the offset of the referenced position.
		 */
		protected void setOffset(int offset) {
			fPosition.setOffset(offset);
		}
		
		/**
		 * Returns the length of the referenced position.
		 */
		protected int getLength() {
			return fPosition.getLength();
		}
		
		/**
		 * Manipulates the length of the referenced position.
		 */
		protected void setLength(int length) {
			fPosition.setLength(length);
		}
		
		/**
		 * Returns whether this reference points to the offset or endoffset
		 * of the references position.
		 */
		protected boolean refersToOffset() {
			return fRefersToOffset;
		}
		
		/**
		 * Returns the category of the referenced position.
		 */
		protected String getCategory() {
			return fCategory;
		}
		
		/**
		 * Returns the referenced position.
		 */
		protected Position getPosition() {
			return fPosition;
		}
		
		/**
		 * Returns the referenced character position
		 */
		protected int getCharacterPosition() {
			if (fRefersToOffset)
				return getOffset();
			return getOffset() + getLength();
		}
		
		/**
		 * @see Comparable#compareTo(Object)
		 */
		public int compareTo(Object obj) {
			
			if (obj instanceof PositionReference) {
				PositionReference r= (PositionReference) obj;
				return getCharacterPosition() - r.getCharacterPosition();
			}
			
			throw new ClassCastException();
		}
	};
	
	/**
	 * The position updater used to adapt all to update the 
	 * remembered partitions.
	 *
	 * @see IPositionUpdater
	 * @see DefaultPositionUpdater
	 */
	class NonDeletingPositionUpdater extends DefaultPositionUpdater {
		
		protected NonDeletingPositionUpdater(String category) {
			super(category);
		}
		
		/*
		 * @see DefaultPositionUpdater#notDeleted()
		 */
		protected boolean notDeleted() {
			return true;
		}
	};
	
	/**
	 * The position updater which runs as first updater on the document's positions.
	 * Used to remove all affected positions from their categories to avoid them
	 * from being regularily updated.
	 * 
	 * @see IPositionUpdater
	 */
	class RemoveAffectedPositions implements IPositionUpdater {
		/**
		 * @see IPositionUpdater#update(DocumentEvent)
		 */
		public void update(DocumentEvent event) {
			removeAffectedPositions(event.getDocument());
		}	
	};
	
	/**
	 * The position updater which runs as last updater on the document's positions.
	 * Used to update all affected positions and adding them back to their
	 * original categories.
	 * 
	 * @see IPositionUpdater
	 */
	class UpdateAffectedPositions implements IPositionUpdater {
		
		private int[] fPositions;
		private int fOffset;
		
		public UpdateAffectedPositions(int[] positions, int offset) {
			fPositions= positions;
			fOffset= offset;
		}
		
		/**
		 * @see IPositionUpdater#update(DocumentEvent)
		 */
		public void update(DocumentEvent event) {
			updateAffectedPositions(event.getDocument(), fPositions, fOffset);
		}	
	};
	
	
	/** Internal position category used for the formatter partitioning */
	private final static String PARTITIONING= "__formatter_partitioning"; //$NON-NLS-1$
	
	/** The map of <code>IFormattingStrategy</code> objects */
	private Map fStrategies;
	/** The indicator of whether the formatter operates in partition aware mode or not */
	private boolean fIsPartitionAware= true;
	
	/** The partition information managing document position categories */
	private String[] fPartitionManagingCategories;
	/** The list of references to offset and end offset of all overlapping positions */
	private List fOverlappingPositionReferences;
	/** Position updater used for partitioning positions */
	private IPositionUpdater fPartitioningUpdater;
	
	
	
	/**
	 * Creates a new content formatter. The content formatter operates by default
	 * in the partition-aware mode. There are no preconfigured formatting strategies.
	 */
	public ContentFormatter() {
	}
	/**
	 * Installs those updaters which the formatter needs to keep 
	 * track of the partitions.
	 *
	 * @param document the document to be formatted
	 */
	private void addPartitioningUpdater(IDocument document) {
		fPartitioningUpdater= new NonDeletingPositionUpdater(PARTITIONING);
		document.addPositionCategory(PARTITIONING);
		document.addPositionUpdater(fPartitioningUpdater);
	}
	/**
	 * Determines all embracing, overlapping, and follow up positions 
	 * for the given region of the document.
	 *
	 * @param document the document to be formatted
	 * @param offset the offset of the document region to be formatted
	 * @param length the length of the document to be formatted
	 */
	private void determinePositionsToUpdate(IDocument document, int offset, int length) {
		
		String[] categories= document.getPositionCategories();
		if (categories != null) {
			for (int i= 0; i < categories.length; i++) {
				
				if (ignoreCategory(categories[i]))
					continue;
					
				try {
					
					Position[] positions= document.getPositions(categories[i]);
					
					for (int j= 0; j < positions.length; j++) {
						
						Position p= (Position) positions[j];
						if (p.overlapsWith(offset, length)) {
							
							if (offset < p.getOffset())
								fOverlappingPositionReferences.add(new PositionReference(p, true, categories[i]));
							
							if (p.getOffset() + p.getLength() < offset + length)
								fOverlappingPositionReferences.add(new PositionReference(p, false, categories[i]));
						}
					}
					
				} catch (BadPositionCategoryException x) {
					// can not happen
				}
			}
		}
	}
	/**
	 * Sets the formatter's operation mode.
	 * 
	 * @param enable indicates whether the formatting process should be partition ware
	 */
	public void enablePartitionAwareFormatting(boolean enable) {
		fIsPartitionAware= enable;
	}
	/**
	 * Formats one partition after the other using the formatter strategy registered for
	 * the partition's content type.
	 *
	 * @param document to document to be formatted
	 * @param ranges the partitioning of the document region to be formatted
	 */
	private void format(final IDocument document, TypedPosition[] ranges) {
		for (int i= 0; i < ranges.length; i++) {
			IFormattingStrategy s= getFormattingStrategy(ranges[i].getType());
			if (s != null) {
				format(document, s, ranges[i]);
			}
		}
	}
	/**
	 * Formats the given region of the document using the specified formatting
	 * strategy. In order to maintain positions correctly, first all affected 
	 * positions determined, after all document listeners have been informed about
	 * the upcoming change, the affected positions are removed to avoid that they
	 * are regularily updated. After all position updaters have run, the affected
	 * positions are updated with the formatter's information and added back to 
	 * their categories, right before the first document listener is informed about
	 * that a change happend.
	 * 
	 * @param document the document to be formatted
	 * @param strategy the strategy to be used
	 * @param region the region to be formatted
	 */
	private void format(final IDocument document, IFormattingStrategy strategy, TypedPosition region) {
		try {
		
			final int offset= region.getOffset();
			int length= region.getLength();
		
			String content= document.get(offset, length);
			final int[] positions= getAffectedPositions(document, offset, length);
			String formatted= strategy.format(content, isLineStart(document, offset), getIndentation(document, offset), positions);
			
			IPositionUpdater first= new RemoveAffectedPositions();
			document.insertPositionUpdater(first, 0);
			IPositionUpdater last= new UpdateAffectedPositions(positions, offset);
			document.addPositionUpdater(last);
			
			document.replace(offset, length, formatted);
			
			document.removePositionUpdater(first);
			document.removePositionUpdater(last);
					
		} catch (BadLocationException x) {
			// should not happen
		}
	}
	/*
	 * @see IContentFormatter#format
	 */
	public void format(IDocument document, IRegion region) {
		if (fIsPartitionAware)
			formatPartitions(document, region);
		else
			formatRegion(document, region);
	}
	/**
	 * Determines the partitioning of the given region of the document.
	 * Informs for each partition about the start, the process, and the
	 * termination of the formatting session.
	 */
	private void formatPartitions(IDocument document, IRegion region) {
		
		addPartitioningUpdater(document);
		
		try {
			
			TypedPosition[] ranges= getPartitioning(document, region);
			if (ranges != null) {
				start(ranges, getIndentation(document, region.getOffset()));
				format(document, ranges);
				stop(ranges);
			}
			
		} catch (BadLocationException x) {
		}
			
		removePartitioningUpdater(document);
	}
	/**
	 * Informs for the given region about the start, the process, and
	 * the termination of the formatting session.
	 */
	private void formatRegion(IDocument document, IRegion region) {
		
		IFormattingStrategy strategy= getFormattingStrategy(IDocument.DEFAULT_CONTENT_TYPE);
		if (strategy != null) {
			strategy.formatterStarts(getIndentation(document, region.getOffset()));
			format(document, strategy, new TypedPosition(region.getOffset(), region.getLength(), IDocument.DEFAULT_CONTENT_TYPE));
			strategy.formatterStops();
		}
	}
	/**
	 * Returns all offset and the end offset of all positions overlapping with the 
	 * specified document range.
	 *
	 * @param document the document to be formatted
	 * @param offset the offset of the document region to be formatted
	 * @param length the length of the document to be formatted
	 * @return all character positions of the interleaving positions
	 */
	private int[] getAffectedPositions(IDocument document, int offset, int length) {
		
		fOverlappingPositionReferences= new ArrayList();
		
		determinePositionsToUpdate(document, offset, length);
		
		Collections.sort(fOverlappingPositionReferences);
		
		int[] positions= new int[fOverlappingPositionReferences.size()];
		for (int i= 0; i < positions.length; i++) {
			PositionReference r= (PositionReference) fOverlappingPositionReferences.get(i);
			positions[i]= r.getCharacterPosition() - offset;
		}
		
		return positions;
	}
	/*
	 * @see IContentFormatter#getFormattingStrategy
	 */
	public IFormattingStrategy getFormattingStrategy(String contentType) {
		
		Assert.isNotNull(contentType);
		
		if (fStrategies == null)
			return null;
								
		return (IFormattingStrategy) fStrategies.get(contentType);
	}
	/**
	 * Returns the indentation of the line of the given offset.
	 *
	 * @param document the document
	 * @param offset the offset
	 * @return the indentation of the line of the offset
	 */
	private String getIndentation(IDocument document, int offset) {
		
		try {
			int start= document.getLineOfOffset(offset);
			start= document.getLineOffset(start);
			
			int end= start;
			char c= document.getChar(end);
			while ('\t' == c || ' ' == c)
				c= document.getChar(++end);
				
			return document.get(start, end - start);
		} catch (BadLocationException x) {
		}
		
		return ""; //$NON-NLS-1$
	}
	/**
	 * Returns the partitioning of the given region of the specified document.
	 * As one partition after the other will be formatted and formatting will 
	 * probably change the length of the formatted partition, it must be kept 
	 * track of the modifications in order to submit the correct partition to all 
	 * formatting strategies. For this, all partitions are remembered as positions
	 * in a dedicated position category. (As formatting stratgies might rely on each
	 * other, calling them in reversed order is not an option.)
	 *
	 * @param document the document
	 * @param region the region for which the partitioning must be determined
	 * @return the partitioning of the specified region
	 * @exception BadLocationException of region is invalid in the document
	 */
	private TypedPosition[] getPartitioning(IDocument document, IRegion region) throws BadLocationException {
		
		ITypedRegion[] regions= document.computePartitioning(region.getOffset(), region.getLength());
		TypedPosition[] positions= new TypedPosition[regions.length];
		
		for (int i= 0; i < regions.length; i++) {
			positions[i]= new TypedPosition(regions[i]);
			try {
				document.addPosition(PARTITIONING, positions[i]);
			} catch (BadPositionCategoryException x) {
				// should not happen
			}
		}
		
		return positions;
	}
	/**
	 * Determines whether the given document position category should be ignored
	 * by this formatter's position updating.
	 *
	 * @param category the category to check
	 * @return <code>true</code> if the category should be ignored, <code>false</code> otherwise
	 */
	private boolean ignoreCategory(String category) {
		
		if (PARTITIONING.equals(category))
			return true;
						
		if (fPartitionManagingCategories != null) {
			for (int i= 0; i < fPartitionManagingCategories.length; i++) {
				if (fPartitionManagingCategories[i].equals(category))
					return true;
			}
		}
		
		return false;
	}
	/**
	 * Determines whether the offset is the beginning of a line in the given document.
	 *
	 * @param document the document
	 * @param offset the offset
	 * @return <code>true</code> if offset is the beginning of a line
	 * @exception BadLocationException if offset is invalid in document
	 */
	private boolean isLineStart(IDocument document, int offset) throws BadLocationException {
		int start= document.getLineOfOffset(offset);
		start= document.getLineOffset(start);
		return (start == offset);
	}
	/**
	 * Removes the affected positions from their categories to avoid
	 * that they are invalidly updated.
	 * 
	 * @param document the document 
	 */
	private void removeAffectedPositions(IDocument document) {
		int size= fOverlappingPositionReferences.size();
		for (int i= 0; i < size; i++) {
			PositionReference r= (PositionReference) fOverlappingPositionReferences.get(i);
			try {
				document.removePosition(r.getCategory(), r.getPosition());
			} catch (BadPositionCategoryException x) {
				// can not happen
			}
		}	
	}
	/**
	 * Removes the formatter's internal position updater and category.
	 *
	 * @param document the document that has been formatted
	 */
	private void removePartitioningUpdater(IDocument document) {
		
		try {
						
			document.removePositionUpdater(fPartitioningUpdater);
			document.removePositionCategory(PARTITIONING);
			fPartitioningUpdater= null;
			
		} catch (BadPositionCategoryException x) {
			// should not happen
		}
	}
	/**
	 * Registers a strategy for a particular content type. If there is already a strategy
	 * registered for this type, the new strategy is registered instead of the old one.
	 * If the given content type is <code>null</code> the given strategy is registered for
	 * all content types as is called only once per formatting session.
	 *
	 * @param strategy the formatting strategy to register, or <code>null</code> to remove an existing one
	 * @param contentType the content type under which to register, or <code>null</code> for all content types
	 */
	public void setFormattingStrategy(IFormattingStrategy strategy, String contentType) {
		
		Assert.isNotNull(contentType);
			
		if (fStrategies == null)
			fStrategies= new HashMap();
			
		if (strategy == null)
			fStrategies.remove(contentType);
		else
			fStrategies.put(contentType, strategy);
	}
	/**
	 * Informs this content formatter about the names of those position categories
	 * which are used to manage the document's partitioning information and thus should
	 * be ignored when this formatter updates positions.
	 *
	 * @param categories the categories to be ignored
	 */
	public void setPartitionManagingPositionCategories(String[] categories) {
		fPartitionManagingCategories= categories;
	}
	/**
	 * Fires <code>formatterStarts</code> to all formatter strategies
	 * which will be involved in the forthcoming formatting process.
	 * 
	 * @param regions the partitioning of the document to be formatted
	 * @param indentation the initial indentation
	 */
	private void start(TypedPosition[] regions, String indentation) {
		for (int i= 0; i < regions.length; i++) {
			IFormattingStrategy s= getFormattingStrategy(regions[i].getType());
			if (s != null)
				s.formatterStarts(indentation);
		}
	}
	/**
	 * Fires <code>formatterStops</code> to all formatter strategies which were
	 * involved in the formatting process which is about to terminate.
	 *
	 * @param regions the partitioning of the document which has been formatted
	 */
	private void stop(TypedPosition[] regions) {
		for (int i= 0; i < regions.length; i++) {
			IFormattingStrategy s= getFormattingStrategy(regions[i].getType());
			if (s != null)
				s.formatterStops();
		}
	}
	/**
	 * Updates all the overlapping positions. Note, all other positions are
	 * automatically updated by their document position updaters.
	 *
	 * @param document the document to has been formatted
	 * @param positions the adapted character positions to be used to update the document positions
	 * @param offset the offset of the document region that has been formatted
	 */
	private void updateAffectedPositions(IDocument document, int[] positions, int offset) {
		
		if (positions.length == 0)
			return;
		
		Map added= new HashMap(positions.length * 2);
		
		for (int i= 0; i < positions.length; i++) {
			
			PositionReference r= (PositionReference) fOverlappingPositionReferences.get(i);
			
			if (r.refersToOffset())
				r.setOffset(offset + positions[i]);
			else
				r.setLength((offset + positions[i]) - r.getOffset());
			
			if (added.get(r.getPosition()) == null) {
				try {
					document.addPosition(r.getCategory(), r.getPosition());
					added.put(r.getPosition(), r.getPosition());
				} catch (BadPositionCategoryException x) {
					// can not happen
				} catch (BadLocationException x) {
					// should not happen
				}
			}	
			
		}
		
		fOverlappingPositionReferences= null;
	}
}
