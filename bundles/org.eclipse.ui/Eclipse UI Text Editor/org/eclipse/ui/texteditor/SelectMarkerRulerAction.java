package org.eclipse.ui.texteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.viewers.StructuredSelection;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.tasklist.TaskList;



/**
 * A ruler action which can select the range covered by markers 
 * which have a visual  representation in the ruler.
 * <p>
 * This class may be instantiated but is not intended for subclassing.
 * </p>
 */
public class SelectMarkerRulerAction extends ResourceAction implements IUpdate {

	private IVerticalRuler fRuler;
	private ITextEditor fTextEditor;
	private List fMarkers;

	private ResourceBundle fBundle;
	private String fPrefix;

	/**
	 * Creates a new action for the given ruler and editor. The action configures
	 * its visual representation from the given resource bundle.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or 
	 *   <code>null</code> if none
	 * @param ruler the ruler
	 * @param editor the editor
	 * 
	 * @see ResourceAction#ResourceAction
	 */
	public SelectMarkerRulerAction(ResourceBundle bundle, String prefix, IVerticalRuler ruler, ITextEditor editor) {
		super(bundle, prefix);
		fRuler= ruler;
		fTextEditor= editor;

		fBundle= bundle;
		fPrefix= prefix;
	}
			
	/*
	 * @see IUpdate#update()
	 */
	public void update() {
		fMarkers= getMarkers();
		setEnabled(!fMarkers.isEmpty());
	}

	/*
	 * @see Action#run()
	 */
	public void run() {
		
		IMarker marker= chooseMarker(fMarkers);
		fTextEditor.gotoMarker(marker);
		
		IWorkbenchPage page= fTextEditor.getSite().getPage();
		IViewPart view= view= page.findView("org.eclipse.ui.views.TaskList"); //$NON-NLS-1$
		if (view instanceof TaskList) {
			StructuredSelection ss= new StructuredSelection(marker);
			((TaskList) view).setSelection(ss, true);
		}
	}

	/**
	 * Chooses the marker with the highest layer. If there are multiple
	 * markers at the found layer, the first marker is taken.
	 * 
	 * @param markers the list of markers to choose from
	 * @return the chosen marker
	 */
	protected IMarker chooseMarker(List markers) {
		
		AbstractMarkerAnnotationModel model= getAnnotationModel();
		
		IMarker marker= null;
		int maxLayer= 0;
		
		Iterator iter= markers.iterator();
		while (iter.hasNext()) {
			IMarker m= (IMarker) iter.next();
			Annotation a= model.getMarkerAnnotation(m);
			int l= a.getLayer();
			if (l == maxLayer) {
				if (marker == null)
					marker= m;
			} else if (l > maxLayer) {
				maxLayer= l;
				marker= m;
			}
		}
		
		return marker;
	}
	
	/** 
	 * Returns the resource for which to create the marker, 
	 * or <code>null</code> if there is no applicable resource.
	 *
	 * @return the resource for which to create the marker or <code>null</code>
	 */
	protected IResource getResource() {
		IEditorInput input= fTextEditor.getEditorInput();
		
		IResource resource= (IResource) input.getAdapter(IFile.class);
		
		if (resource == null)
			resource= (IResource) input.getAdapter(IResource.class);
			
		return resource;
	}

	/**
	 * Returns the <code>AbstractMarkerAnnotationModel</code> of the editor's input.
	 *
	 * @return the marker annotation model
	 */
	protected AbstractMarkerAnnotationModel getAnnotationModel() {
		IDocumentProvider provider= fTextEditor.getDocumentProvider();
		IAnnotationModel model= provider.getAnnotationModel(fTextEditor.getEditorInput());
		if (model instanceof AbstractMarkerAnnotationModel)
			return (AbstractMarkerAnnotationModel) model;
		return null;
	}

	/**
	 * Returns the <code>IDocument</code> of the editor's input.
	 *
	 * @return the document of the editor's input
	 */
	protected IDocument getDocument() {
		IDocumentProvider provider= fTextEditor.getDocumentProvider();
		return provider.getDocument(fTextEditor.getEditorInput());
	}

	/**
	 * Checks whether a position includes the ruler's line of activity.
	 *
	 * @param position the position to be checked
	 * @param document the document the position refers to
	 * @return <code>true</code> if the line is included by the given position
	 */
	protected boolean includesRulerLine(Position position, IDocument document) {

		if (position != null) {
			try {
				int markerLine= document.getLineOfOffset(position.getOffset());
				int line= fRuler.getLineOfLastMouseButtonActivity();
				if (line == markerLine)
					return true;
				// commented because of "1GEUOZ9: ITPJUI:ALL - Confusing UI for multiline Bookmarks and Tasks"
				// return (markerLine <= line && line <= document.getLineOfOffset(position.getOffset() + position.getLength()));
			} catch (BadLocationException x) {
			}
		}
		
		return false;
	}

	/**
	 * Handles core exceptions. This implementation logs the exceptions
	 * with the workbech plugin.
	 *
	 * @param exception the exception to be handled
	 * @param message the message to be logged with the given exception
	 */
	protected void handleCoreException(CoreException exception, String message) {
		ILog log= Platform.getPlugin(PlatformUI.PLUGIN_ID).getLog();
		
		if (message != null)
			log.log(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, message, null));
		
		log.log(exception.getStatus());
		
		
		Shell shell= fTextEditor.getSite().getShell();
		String title= getString(fBundle, fPrefix + "error.dialog.title", fPrefix + "error.dialog.title"); //$NON-NLS-2$ //$NON-NLS-1$
		String msg= getString(fBundle, fPrefix + "error.dialog.message", fPrefix + "error.dialog.message"); //$NON-NLS-2$ //$NON-NLS-1$
		
		ErrorDialog.openError(shell, title, msg, exception.getStatus());		
	}

	/**
	 * Returns all markers which include the ruler's line of activity.
	 *
	 * @returns all markers which include the ruler's line of activity
	 */
	protected List getMarkers() {

		List markers= new ArrayList();

		IResource resource= getResource();
		IDocument document= getDocument();
		AbstractMarkerAnnotationModel model= getAnnotationModel();

		if (resource != null && model != null) {
			try {
				IMarker[] allMarkers= resource.findMarkers(null, true, IResource.DEPTH_ZERO);
				if (allMarkers != null) {
					for (int i= 0; i < allMarkers.length; i++) {
						if (includesRulerLine(model.getMarkerPosition(allMarkers[i]), document)) {
							markers.add(allMarkers[i]);
						}
					}
				}
			} catch (CoreException x) {
				handleCoreException(x, "SelectMarkerRulerAction.getMarker");
			}
		}

		return markers;
	}
}
