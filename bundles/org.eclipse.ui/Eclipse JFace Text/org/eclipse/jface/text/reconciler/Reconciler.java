package org.eclipse.jface.text.reconciler;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TypedRegion;
import org.eclipse.jface.util.Assert;

/**
 * Standard implementation of <code>IReconciler</code>. The reconciler
 * is configured with a set of reconciling strategies each of which is
 * responsible for a particular content type. <p>
 * Usually, clients instantiate this class and configure it before using it.
 *
 * @see IReconciler
 * @see IDocumentListener
 * @see ITextInputListener
 * @see DirtyRegion
 */
public class Reconciler extends AbstractReconciler {
	
	/** The map of reconciling strategies */
	private Map fStrategies;
	
	/**
	 * Creates a new reconciler with the following configuration: it is
	 * an incremental reconciler which kicks in every 500 ms. There are no
	 * predefined reconciling strategies.
	 */ 
	public Reconciler() {
		super();
	}
	
	/**
	 * Registers a given reconciling strategy for a particular content type.
	 * If there is already a strategy registered for this type, the new strategy 
	 * is registered instead of the old one.
	 *
	 * @param strategy the reconciling strategy to register, or <code>null</code> to remove an existing one
	 * @param contentType the content type under which to register
	 */
	public void setReconcilingStrategy(IReconcilingStrategy strategy, String contentType) {
		
		Assert.isNotNull(contentType);
					
		if (fStrategies == null)
			fStrategies= new HashMap();
		
		if (strategy == null)
			fStrategies.remove(contentType);
		else
			fStrategies.put(contentType, strategy);
	}
	
	/*
	 * @see IReconciler#getReconcilingStrategy
	 */
	public IReconcilingStrategy getReconcilingStrategy(String contentType) {
		
		Assert.isNotNull(contentType);
		
		if (fStrategies == null)
			return null;
						
		return (IReconcilingStrategy) fStrategies.get(contentType);
	}
	
	/**
	 * Processes a dirty region. If the dirty region is <code>null</code> the whole
	 * document is consider being dirty. The dirty region is partitioned by the
	 * document and each partition is handed over to a reconciling strategy registered
	 * for the partition's content type.
	 *
	 * @param dirtyRegion the dirty region to be processed
	 * 
	 * @see AbstractReconciler#process(DirtyRegion)
	 */
	protected void process(DirtyRegion dirtyRegion) {
		
		IRegion region= dirtyRegion;
		
		if (region == null)
			region= new Region(0, getDocument().getLength());
			
		ITypedRegion[] regions= null;
		try {
			regions= getDocument().computePartitioning(region.getOffset(), region.getLength());
		} catch (BadLocationException x) {
			regions= new TypedRegion[0];
		}
		
		for (int i= 0; i < regions.length; i++) {
			ITypedRegion r= regions[i];
			IReconcilingStrategy s= getReconcilingStrategy(r.getType());
			if (s == null)
				continue;
				
			if(dirtyRegion != null)
				s.reconcile(dirtyRegion, r);
			else
				s.reconcile(r);
		}
	}
	
	/**
	 * @see AbstractReconciler#reconcilerDocumentChanged(IDocument)
	 */
	protected void reconcilerDocumentChanged(IDocument document) {
		if (fStrategies != null) {
			Iterator e= fStrategies.values().iterator();
			while (e.hasNext()) {
				IReconcilingStrategy strategy= (IReconcilingStrategy) e.next();
				strategy.setDocument(document);
			}
		}
	}	
}
