package org.eclipse.jface.text.source;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.jface.text.DefaultAutoIndentStrategy;
import org.eclipse.jface.text.DefaultTextDoubleClickStrategy;
import org.eclipse.jface.text.DefaultUndoManager;
import org.eclipse.jface.text.IAutoIndentStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;


/**
 * This class bundles the whole configuration space of a source viewer.
 * Instances of this class are passed to the <code>configure</code> method of
 * <code>ISourceViewer</code>.<p>
 * Each method in this class get as argument the source viewer for which it should
 * provide a particular configurational setting such as a presentation reconciler.
 * Based on its specific knowlegde about the returned object, the configuration 
 * might share such objects or compute them according to some rules.<p>
 * Clients should subclass and override just those methods which must be specific to
 * their needs.
 *
 * @see ISourceViewer
 */


public class SourceViewerConfiguration {
	
	
	/**
	 * Creates a new source viewer configuration that behaves according to
	 * specification of this class' methods.
	 */
	public SourceViewerConfiguration() {
		super();
	}
		
	/**
	 * Returns the visual width of the tab character. This implementation always
	 * returns 4.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @return the tab width
	 */
	public int getTabWidth(ISourceViewer sourceViewer) {
		return 4;
	}
	
	/**
	 * Returns the undo manager for the given source viewer. This implementation 
	 * always returns a new instance of <code>DefaultUndoManager</code> whose
	 * history length is set to 25.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @return an undo manager or <code>null</code< if no undo/redo should not be supported
	 */
	public IUndoManager getUndoManager(ISourceViewer sourceViewer) {
		return new DefaultUndoManager(25);
	}
		
	/**
	 * Returns the reconciler ready to be used with the given source viewer.
	 * This implementation always returns <code>null</code>.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @return a reconciler or <code>null</code> if reconciling should not be supported
	 */
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		return null;
	}
		
	/**
	 * Returns the presentation reconciler ready to be used with the given source viewer. 
	 * This implementation always returns <code>null</code>.
	 *
	 * @return the presentation reconciler or <code>null</code> if presentation reconciling should not be supported
	 */
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		return null;
	}
	
	/**
	 * Returns the content formatter ready to be used with the given source viewer.
	 * This implementation always returns <code>null</code>.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @return a content formatter or <code>null</code> if formatting should not be supported
	 */
	public IContentFormatter getContentFormatter(ISourceViewer sourceViewer) {
		return null;
	}
		
	/**
	 * Returns the content assistant ready to be used with the given source viewer.
	 * This implementation always returns <code>null</code>.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @return a content assistant or <code>null</code> if content assist should not be supported
	 */
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		return null;
	}
	
	/**
	 * Returns the auto indentation strategy ready to be used with the given source viewer
	 * when manipulating text of the given content type. This implementation always 
	 * returns an new instance of <code>DefaultAutoIndentStrategy</code>.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @param contentType the content type for which the strategy is applicable
	 * @return the auto indent strategy or <code>null</code> if automatic indentation is not to be enabled
	 */
	public IAutoIndentStrategy getAutoIndentStrategy(ISourceViewer sourceViewer, String contentType) {
		return new DefaultAutoIndentStrategy();
	}
	/**
	 * Returns the default prefix to be used by the line-prefix operation
	 * in the given source viewer for text of the given content type. This implementation always
	 * returns <code>null</code>.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @param contentType the content type for which the prefix is applicable
	 * @return the default prefix or <code>null</code> if the prefix operation should not be supported
	 */
	public String getDefaultPrefix(ISourceViewer sourceViewer, String contentType) {
		return null;
	}

	/**
	 * Returns the double-click strategy ready to be used in this viewer when double clicking
	 * onto text of the given content type. This implementation always returns a new instance of
	 * <code>DefaultTextDoubleClickStrategy</code>.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @param contentType the content type for which the strategy is applicable
	 * @return a double-click strategy or <code>null</code> if double clicking should not be supported
	 */
	public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType) {
		return new DefaultTextDoubleClickStrategy();
	}

	/**
	 * Returns the prefixes to be used by the line-shift operation. This implementation
	 * always returns <code>new String[] { "\t", "    " }</code>.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @param contentType the content type for which the prefix is applicable
	 * @return a prefix or <code>null</code> if the prefix operation should not be supported
	 */
	public String[] getIndentPrefixes(ISourceViewer sourceViewer, String contentType) {
		return new String[] { "\t", "    ", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
		
	/**
	 * Returns the annotation hover which will provide the information to be
	 * shown in a hover popup window when requested for the given
	 * source viewer.This implementation always returns <code>null</code>.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @return an annotation hover or <code>null</code> if no hover support should be installed
	 */
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		return null;
	}
	
	/**
	 * Returns the text hover which will provide the information to be shown
	 * in a text hover popup window when requested for the given source viewer and
	 * the given content type. This implementation always returns <code>
	 * null</code>.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @param contentType the content type
	 * @return a text hover or <code>null</code> if no hover support should be installed
	 */
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		return null;
	}
	
	/**
	 * Returns all configured content types for the given source viewer. This list
	 * tells the caller which content types must be configured for the given source 
	 * viewer, i.e. for which content types the given source viewer's functionalities
	 * must be specified. This implementation always returns <code>
	 * new String[] { IDocument.DEFAULT_CONTENT_TYPE }</code>.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @return the configured content types for the given viewer
	 */
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] { IDocument.DEFAULT_CONTENT_TYPE };
	}
}
