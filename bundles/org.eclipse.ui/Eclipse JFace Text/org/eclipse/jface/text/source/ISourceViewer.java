package org.eclipse.jface.text.source;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;


/**
 * In addition to the text viewer functionality a source viewer supports:
 * <ul>
 * <li> visual annotations based on an annotation model
 * <li> visual range indication
 * <li> management of text viewer add-ons
 * <li> explicit configuration
 * </ul>
 * It is assumed that range indication and visual annotations are shown
 * inside the same presentation area. There are no assumptions about 
 * whether this area is different from the viewer's text widget.<p>
 * As the visibility of visual annotations can dynamically be changed, 
 * it is assumed that the annotation presentation area can dynamically 
 * be hidden if it is different from the text widget.<p>
 * Clients may implement this interface or use the default implementation provided
 * by <code>SourceViewer</code>.
 */
public interface ISourceViewer extends ITextViewer {
	
	/** 
	 * Text operation code for requesting content assist to show completetion
	 * proposals for the current insert position. 
	 */
	static final int CONTENTASSIST_PROPOSALS= ITextOperationTarget.STRIP_PREFIX + 1;
	
	/** 
	 * Text operation code for requesting content assist to show
	 * the content information for the current insert position. 
	 */
	static final int CONTENTASSIST_CONTEXT_INFORMATION=	ITextOperationTarget.STRIP_PREFIX + 2;
	
	/** 
	 * Text operation code for formatting the selected text or complete document
	 * of this viewer if the selection is empty. 
	 */
	static final int FORMAT= ITextOperationTarget.STRIP_PREFIX + 3;
	
	/**
	 * Configures the source viewer using the given configuration.
	 * 
	 * @param configuration the source viewer configuration to be used
	 */
	void configure(SourceViewerConfiguration configuration);
	
	/**
	 * Sets the annotation hover of this source viewer. The annotation hover
	 * provides the information to be displayed in a hover popup window
	 * if requested over the annotation presentation area. The annotation
	 * hover is assumed to be line oriented.
	 *
	 * @param annotationHover the hover to be used, <code>null</code> is a valid argument
	 */
	void setAnnotationHover(IAnnotationHover annotationHover);
	
	/**
	 * Sets the given document as this viewer's text model and the 
	 * given annotation model as the model for this viewer's visual
	 * annotations. The presentation is accordingly updated. An approriate 
	 * <code>TextEvent</code> is issued. This text event does not carry 
	 * a related document event.
	 *
	 * @param document the viewer's new input document
	 * @param annotationModel the model for the viewer's visual annotations
	 *
	 * @see ITextViewer#setDocument(IDocument)
	 */
	void setDocument(IDocument document, IAnnotationModel annotationModel);
	 
	/**
	 * Sets the given document as this viewer's text model and the 
	 * given annotation model as the model for this viewer's visual
	 * annotations. The presentation is accordingly updated whereby 
	 * only the specified region is made visible. An approriate
	 * <code>TextEvent</code> is issued. The text event does not carry a 
	 * related document event. This method is a convenience method for
	 * <code>setDocument(document, annotationModel);setVisibleRegion(offset, length)</code>.
	 *
	 * @param document the new input document
	 * @param annotationModel the model of the viewer's visual annotations
	 * @param visibleRegionOffset the offset of the visible region
	 * @param visibleRegionLength the length of the visible region
	 *
	 * @see ITextViewer#setDocument(IDocument, int, int)
	 */
	void setDocument(IDocument document, IAnnotationModel annotationModel, int visibleRegionOffset, int visibleRegionLength);
	
	/**
	 * Returns this viewer's annotation model.
	 *
	 * @return this viewer's annotation model
	 */
	IAnnotationModel getAnnotationModel();
		
	/**
	 * Sets the annotation used by this viewer as range indicator. The 
	 * range covered by this annotation is referred to as range indication.
	 *
	 * @param rangeIndicator the annotation to be used as this viewer's range indicator
	 */
	void setRangeIndicator(Annotation rangeIndicator);
	
	/**
	 * Sets the viewers's range indication to the specified range. Its is indicated
	 * whether the cursor should also be moved to the beginning of the specified range.
	 *
	 * @param offset the offset of the range
	 * @param length the length of the range
	 * @param moveCursor indicates whether the cursor should be moved to the given offset
	 */
	void setRangeIndication(int offset, int length, boolean moveCursor);
	
	/**
	 * Returns the viewer's range indication.
	 *
	 * @return the viewer's range indication.
	 */
	IRegion getRangeIndication();
		
	/**
	 * Removes the viewer's range indication. There is no visible range indication
	 * after this method completed.
	 *
	 * @return the viewer's range indication
	 */
	void removeRangeIndication();
	
	/**
	 * Controls the visibility of annotations and in the case of separate
	 * presentation areas of text and annotations, the visibility of the 
	 * annotation's presentation area.<p> 
	 * By default, annotations and their presentation area are visible.
	 *
	 * @param show indicates the visibility of annotations
	 */
	void showAnnotations(boolean show);
}