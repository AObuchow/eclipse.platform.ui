package org.eclipse.jface.text.source;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;


/**
 * SWT based implementation of <code>ISourceViewer</code>. The same rules apply 
 * as for <code>TextViewer</code>. A source viewer uses an <code>IVerticalRuler</code>
 * as its annotation presentation area. The vertical ruler is a small strip shown left
 * of the viewer's text widget.<p>
 * Clients are supposed to instantiate a source viewer and subsequently to communicate
 * with it exclusively using the <code>ISourceViewer</code> interface. Clients should not
 * subclass this class as it is rather likely that subclasses will be broken by future releases. 
 */
public class SourceViewer extends TextViewer implements ISourceViewer {


	/**
	 * Layout of a source viewer. Vertical ruler and text widget are shown side by side.
	 */
	class RulerLayout extends Layout {
		
		protected int fGap;
		
		protected RulerLayout(int gap) {
			fGap= gap;
		}
		
		protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
			Control[] children= composite.getChildren();
			Point s= children[children.length - 1].computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
			if (fVerticalRuler != null && fIsVerticalRulerVisible)
				s.x += fVerticalRuler.getWidth() + fGap;
			return s;
		}
		
		protected void layout(Composite composite, boolean flushCache) {
			Rectangle clArea= composite.getClientArea();
			if (fVerticalRuler != null && fIsVerticalRulerVisible) {
				
				Rectangle trim= getTextWidget().computeTrim(0, 0, 0, 0);
				int scrollbarHeight= trim.height;
				
				int rulerWidth= fVerticalRuler.getWidth();
				fVerticalRuler.getControl().setBounds(0, 0, rulerWidth, clArea.height - scrollbarHeight);
				getTextWidget().setBounds(rulerWidth + fGap, 0, clArea.width - rulerWidth - fGap, clArea.height);
			
			} else
				getTextWidget().setBounds(0, 0, clArea.width, clArea.height);
		}
	};
	
	
	/** The viewer's content assistant */
	protected IContentAssistant fContentAssistant;
	/** The viewer's content formatter */
	protected IContentFormatter fContentFormatter;
	/** The viewer's model reconciler */
	protected IReconciler fReconciler;
	/** The viewer's presentation reconciler */
	protected IPresentationReconciler fPresentationReconciler;
	/** The viewer's annotation hover */
	protected IAnnotationHover fAnnotationHover;
	
	/** Visual vertical ruler */
	private IVerticalRuler fVerticalRuler;
	/** Visibility of vertical ruler */
	private boolean fIsVerticalRulerVisible;
	/** The SWT widget used when supporting a vertical ruler */
	private Composite fComposite;
	/** The vertical ruler's annotation model */
	private VisualAnnotationModel fVisualAnnotationModel;
	/** The viewer's range indicator to be shown in the vertical ruler */
	private Annotation fRangeIndicator;
	/** The viewer's vertical ruler hovering controller */
	private VerticalRulerHoveringController fVerticalRulerHoveringController;
	
	
	
	/** The size of the gap between the vertical ruler and the text widget */
	protected final static int GAP_SIZE= 0;
	
	/**
	 * Constructs a new source viewer. The vertical ruler is initially visible.
	 * The viewer has not yet been initialized with a source viewer configuration.
	 *
	 * @param parent the parent of the viewer's control
	 * @param ruler the vertical ruler used by this source viewer
	 * @patam styles the SWT style bits
	 */
	public SourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		super();
		
		fIsVerticalRulerVisible= true;
		fVerticalRuler= ruler;
		createControl(parent, styles);
	}
	
	/*
	 * @see TextViewer#createControl
	 */
	protected void createControl(Composite parent, int styles) {
		
		if (fVerticalRuler != null) {
			styles= (styles & ~SWT.BORDER);
			fComposite= new Canvas(parent, SWT.NONE);
			fComposite.setLayout(new RulerLayout(GAP_SIZE));
			parent= fComposite;
		}
		
		super.createControl(parent, styles);
					
		if (fComposite != null)
			fVerticalRuler.createControl(fComposite, this);
	}
	
	/*
	 * @see TextViewer#getControl
	 */
	public Control getControl() {
		if (fComposite != null)
			return fComposite;
		return super.getControl();
	}
	
	/*
	 * @see ISourceViewer#setAnnotationHover
	 */
	public void setAnnotationHover(IAnnotationHover annotationHover) {
		fAnnotationHover= annotationHover;
	}
	
	/*
	 * @see ISourceViewer#configure
	 */
	public void configure(SourceViewerConfiguration configuration) {
		
		if (getTextWidget() == null)
			return;
		
		// install content type independent plugins
		fPresentationReconciler= configuration.getPresentationReconciler(this);
		if (fPresentationReconciler != null)
			fPresentationReconciler.install(this);
								
		fReconciler= configuration.getReconciler(this);
		if (fReconciler != null)
			fReconciler.install(this);
			
		fContentAssistant= configuration.getContentAssistant(this);
		if (fContentAssistant != null)
			fContentAssistant.install(this);
			
		fContentFormatter= configuration.getContentFormatter(this);
		
		setUndoManager(configuration.getUndoManager(this));
		
		getTextWidget().setTabs(configuration.getTabWidth(this));
		
		setAnnotationHover(configuration.getAnnotationHover(this));
				 
		// install content type specific plugins
		String[] types= configuration.getConfiguredContentTypes(this);
		for (int i= 0; i < types.length; i++) {
			
			String t= types[i];
				
			setAutoIndentStrategy(configuration.getAutoIndentStrategy(this, t), t);
			setTextDoubleClickStrategy(configuration.getDoubleClickStrategy(this, t), t);
			setTextHover(configuration.getTextHover(this, t), t);
			
			String[] prefixes= configuration.getIndentPrefixes(this, t);
			if (prefixes != null && prefixes.length > 0)
				setIndentPrefixes(prefixes, t);
			
			prefixes= configuration.getDefaultPrefixes(this, t);
			if (prefixes != null && prefixes.length > 0)
				setDefaultPrefixes(prefixes, t);
		}
		
		activatePlugins();
	}
	
	/*
	 * @see TextViewer#activatePlugins
	 */
	public void activatePlugins() {
		
		if (fVerticalRuler != null && fAnnotationHover != null && fVerticalRulerHoveringController == null) {
			fVerticalRulerHoveringController= new VerticalRulerHoveringController(this, fVerticalRuler, fAnnotationHover);
			fVerticalRulerHoveringController.install();
		}
		
		super.activatePlugins();
	}
	
	/*
	 * @see ISourceViewer#setDocument(IDocument, IAnnotationModel)
	 */
	public void setDocument(IDocument document) {
		setDocument(document, null, -1, -1);		
	}
	
	/*
	 * @see ISourceViewer#setDocument(IDocument, IAnnotationModel, int, int)
	 */
	public void setDocument(IDocument document, int visibleRegionOffset, int visibleRegionLength) {
		setDocument(document, null, visibleRegionOffset, visibleRegionLength);
	}
	
	/*
	 * @see ISourceViewer#setDocument(IDocument, IAnnotationModel)
	 */
	public void setDocument(IDocument document, IAnnotationModel annotationModel) {
		setDocument(document, annotationModel, -1, -1);		
	}
	
	/*
	 * @see ISourceViewer#setDocument(IDocument, IAnnotationModel, int, int)
	 */
	public void setDocument(IDocument document, IAnnotationModel annotationModel, int visibleRegionOffset, int visibleRegionLength) {
		
		if (fVerticalRuler == null) {
			
			if (visibleRegionOffset == -1 && visibleRegionLength == -1)
				super.setDocument(document);
			else
				super.setDocument(document, visibleRegionOffset, visibleRegionLength);
		
		} else {
			
			if (fVisualAnnotationModel != null && getDocument() != null)
				fVisualAnnotationModel.disconnect(getDocument());
			
			if (visibleRegionOffset == -1 && visibleRegionLength == -1)
				super.setDocument(document);
			else
				super.setDocument(document, visibleRegionOffset, visibleRegionLength);
			
			if (annotationModel != null && document != null) {
				fVisualAnnotationModel= new VisualAnnotationModel(annotationModel);
				fVisualAnnotationModel.connect(document);
			} else {
				fVisualAnnotationModel= null;
			}
			
			fVerticalRuler.setModel(fVisualAnnotationModel);
		}
	}
	
	/*
	 * @see ISourceViewer#getAnnotationModel
	 */
	public IAnnotationModel getAnnotationModel() {
		if (fVisualAnnotationModel != null)
			return fVisualAnnotationModel.getModelAnnotationModel();
		return null;
	}
	
	/*
	 * @see TextViewer#handleDispose
	 */
	protected void handleDispose() {
		
		if (fPresentationReconciler != null) {
			fPresentationReconciler.uninstall();
			fPresentationReconciler= null;
		}
		
		if (fReconciler != null) {
			fReconciler.uninstall();
			fReconciler= null;
		}
		
		if (fContentAssistant != null) {
			fContentAssistant.uninstall();
			fContentAssistant= null;
		}
		
		fContentFormatter= null;
		
		if (fVisualAnnotationModel != null && getDocument() != null) {
			fVisualAnnotationModel.disconnect(getDocument());
			fVisualAnnotationModel= null;
		}
		
		fVerticalRuler= null;
				
		if (fVerticalRulerHoveringController != null) {
			fVerticalRulerHoveringController.dispose();
			fVerticalRulerHoveringController= null;
		}
		
		super.handleDispose();
	}
	
	/*
	 * @see ITextOperationTarget#canDoOperation
	 */
	public boolean canDoOperation(int operation) {
		
		if (getTextWidget() == null)
			return false;
		
		if (operation == CONTENTASSIST_PROPOSALS || operation == CONTENTASSIST_CONTEXT_INFORMATION)
			return fContentAssistant != null;
		
		if (operation == FORMAT) {
			Point p= getSelectedRange();
			int length= (p == null ? -1 : p.y);
			return (fContentFormatter != null && (length == 0 || isBlockSelected()));
		}
		
		return super.canDoOperation(operation);
	}
	
	/*
	 * @see ITextOperationTarget#doOperation
	 */
	public void doOperation(int operation) {
		
		if (getTextWidget() == null)
			return;
		
		switch (operation) {
			case CONTENTASSIST_PROPOSALS:
				fContentAssistant.showPossibleCompletions();
				return;
			case CONTENTASSIST_CONTEXT_INFORMATION:
				fContentAssistant.showContextInformation();
				return;
			case FORMAT: {
				Point s= getSelectedRange();
				IRegion r= (s.y == 0 ? getVisibleRegion() : new Region(s.x, s.y));
				fContentFormatter.format(getDocument(), r);
				return;
			}
			default:
				super.doOperation(operation);
		}
	}		
		
	/*
	 * @see ISourceViewer#setRangeIndicator
	 */
	public void setRangeIndicator(Annotation rangeIndicator) {
		fRangeIndicator= rangeIndicator;
	}
		
	/*
	 * @see ISourceViewer#setRangeIndication 	 
	 */
	public void setRangeIndication(int start, int length, boolean moveCursor) {
		
		if (moveCursor) {
			setSelectedRange(start, 0);
			revealRange(start, length);
		}
		
		if (fRangeIndicator != null && fVisualAnnotationModel != null)
			fVisualAnnotationModel.modifyAnnotation(fRangeIndicator, new Position(start, length));
	}
	
	/*
	 * @see ISourceViewer#getRangeIndication
	 */
	public IRegion getRangeIndication() {
		if (fRangeIndicator != null && fVisualAnnotationModel != null) {
			Position position= fVisualAnnotationModel.getPosition(fRangeIndicator);
			if (position != null)
				return new Region(position.getOffset(), position.getLength());
		}
		
		return null;
	}
	
	/*
	 * @see ISourceViewer#removeRangeIndication
	 */
	public void removeRangeIndication() {
		if (fRangeIndicator != null && fVisualAnnotationModel != null)
			fVisualAnnotationModel.modifyAnnotation(fRangeIndicator, null);
	}
	
	/*
	 * @see ISourceViewer#showAnnotations
	 */
	public void showAnnotations(boolean show) {
		boolean old= fIsVerticalRulerVisible;
		fIsVerticalRulerVisible= show;
		if (old != fIsVerticalRulerVisible)
			fComposite.layout();
	}		
}