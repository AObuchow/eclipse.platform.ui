/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.jface.text.source;


import java.text.NumberFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.TextEvent;


/**
 * A vertical ruler column displaying line numbers.
 * Clients may use this class as is.
 * @since 2.0
 */
public final class LineNumberRulerColumn implements IVerticalRulerColumn {
	
	/**
	 * Internal listener class.
	 */
	class InternalListener implements IViewportListener, ITextListener {
		
		/*
		 * @see IViewportListener#viewportChanged(int)
		 */
		public void viewportChanged(int verticalPosition) {
			if (verticalPosition != fScrollPos)
				redraw();
		}
		
		/*
		 * @see ITextListener#textChanged(TextEvent)
		 */
		public void textChanged(TextEvent event) {
			
			if (!event.getViewerRedrawState())
				return;
			
			if (computeNumberOfDigits()) {
				computeIndentations();
				layout();
				return;
			}
				
			if (fSensitiveToTextChanges || event.getDocumentEvent() == null) {
				if (fCanvas != null && !fCanvas.isDisposed()) {
					Display d= fCanvas.getDisplay();
					if (d != null) {
						d.asyncExec(new Runnable() {
							public void run() {
								redraw();
							}
						});
					}	
				}
			}
		}
	};
	
	/** This column's parent ruler */
	private CompositeRuler fParentRuler;
	/** Cached text viewer */
	private ITextViewer fCachedTextViewer;
	/** Cached text widget */
	private StyledText fCachedTextWidget;
	/** The columns canvas */
	private Canvas fCanvas;
	/** Cache for the actual scroll position in pixels */
	private int fScrollPos;
	/** The drawable for double buffering */
	private Image fBuffer;
	/** The internal listener */
	private InternalListener fInternalListener= new InternalListener();
	/** The font of this column */
	private Font fFont;
	/** The indentation cache */
	private int[] fIndentation;
	/** Indicates whether this column reacts on text change events */
	private boolean fSensitiveToTextChanges= false;
	/** The foreground color */
	private Color fForeground;
	/** The background color */
	private Color fBackground;
	/** Cached number of displayed digits */
	private int fCachedNumberOfDigits= -1;
	
	
	/**
	 * Constructs a new vertical ruler column.
	 */
	public LineNumberRulerColumn() {
	}
	
	/**
	 * Sets the foreground color of this column.
	 * 
	 * @param foreground the foreground color
	 */
	public void setForeground(Color foreground) {
		fForeground= foreground;
	}
	
	/**
	 * Sets the background color of this column.
	 * 
	 * @param background the background color
	 */
	public void setBackground(Color background) {
		fBackground= background;			
		if (fCanvas != null && !fCanvas.isDisposed())
			fCanvas.setBackground(getBackground(fCanvas.getDisplay()));
	}
	
	/**
	 * Returns the System background color for list widgets.
	 * 
	 * @param display the display
	 * @return the System background color for list widgets
	 */
	protected Color getBackground(Display display) {
		if (fBackground == null)
			return display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		return fBackground;
	}
	
	/*
	 * @see IVerticalRulerColumn#getControl()
	 */
	public Control getControl() {
		return fCanvas;
	}
	
	/*
	 * @see IVerticalRuleColumnr#getWidth
	 */
	public int getWidth() {
		return fIndentation[0];
	}
	
	/**
	 * Computes the number of digits to be displayed. Returns
	 * <code>true</code> if the number of digits changed compared
	 * to the previous call of this method. If the method is called
	 * for the first time, the return value is also <code>true</code>.
	 * 
	 * @return the number of digits to be displayed
	 */
	protected boolean computeNumberOfDigits() {
		
		IDocument document= fCachedTextViewer.getDocument();
		int lines= document == null ? 0 : document.getNumberOfLines();
		
		int digits= 2;
		while (lines > Math.pow(10, digits) -1) {
			++digits;
		}
		
		if (fCachedNumberOfDigits != digits) {
			fCachedNumberOfDigits= digits;
			return true;
		}
		
		return false;
	}
	
	/**
	 * Layouts the enclosing viewer to adapt the layout to changes of the
	 * size of the individual components.
	 */
	protected void layout() {
		if (fCachedTextViewer instanceof ITextViewerExtension) {
			ITextViewerExtension extension= (ITextViewerExtension) fCachedTextViewer;
			Control control= extension.getControl();
			if (control instanceof Composite && !control.isDisposed()) {
				Composite composite= (Composite) control;
				composite.layout(true);
			}
		}
	}
	
	/**
	 * Computes the indentations for the given font and stores them in
	 * <code>fIndentation</code>.
	 */
	protected void computeIndentations() {
		GC gc= new GC(fCanvas);
		try {
			
			gc.setFont(fCanvas.getFont());
			NumberFormat nf= NumberFormat.getInstance();
			
			fIndentation= new int[fCachedNumberOfDigits + 1];
			
			double number= Math.pow(10, fCachedNumberOfDigits) - 1;
			Point p= gc.stringExtent(nf.format(number));
			fIndentation[0]= p.x;
			
			for (int i= 1; i <= fCachedNumberOfDigits; i++) {
				number= Math.pow(10, i) - 1;
				p= gc.stringExtent(nf.format(number));
				fIndentation[i]= fIndentation[0] - p.x;
			}
		
		} finally {
			gc.dispose();
		}
	}
	
	/*
	 * @see IVerticalRulerColumn#createControl(CompositeRuler, Composite)
	 */
	public Control createControl(CompositeRuler parentRuler, Composite parentControl) {
		
		fParentRuler= parentRuler;
		fCachedTextViewer= parentRuler.getTextViewer();
		fCachedTextWidget= fCachedTextViewer.getTextWidget();
		
		fCanvas= new Canvas(parentControl, SWT.NONE);
		fCanvas.setBackground(getBackground(fCanvas.getDisplay()));
		fCanvas.setForeground(fForeground);
			
		fCanvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent event) {
				if (fCachedTextViewer != null)
					doubleBufferPaint(event.gc);
			}
		});
		
		fCanvas.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				handleDispose();
				fCachedTextViewer= null;
				fCachedTextWidget= null;
			}
		});
		
		fCanvas.addMouseListener(new MouseListener() {
			public void mouseUp(MouseEvent event) {
			}
			
			public void mouseDown(MouseEvent event) {
				fParentRuler.setLocationOfLastMouseButtonActivity(event.x, event.y);
				selectLine(fParentRuler.getLineOfLastMouseButtonActivity());
			}
			
			public void mouseDoubleClick(MouseEvent event) {
				fParentRuler.setLocationOfLastMouseButtonActivity(event.x, event.y);
			}
		});
		
		if (fCachedTextViewer != null) {
			
			fCachedTextViewer.addViewportListener(fInternalListener);
			fCachedTextViewer.addTextListener(fInternalListener);
			
			if (fFont == null) {
				if (fCachedTextWidget != null && !fCachedTextWidget.isDisposed())
					fFont= fCachedTextWidget.getFont();
			}
		}
		
		if (fFont != null)
			fCanvas.setFont(fFont);
			
		computeNumberOfDigits();
		computeIndentations();
		return fCanvas;
	}

	/**
	 * Selects the given line in the text viewer.
	 * 
	 * @param line the number of the line to be selected
	 */
	private void selectLine(int line) {
		try {
			IDocument document= fCachedTextViewer.getDocument();
			int offset= document.getLineOffset(fParentRuler.getLineOfLastMouseButtonActivity());
			fCachedTextViewer.setSelectedRange(offset, 0);
		} catch (BadLocationException x) {
		}
	}
	
	/**
	 * Disposes the column's resources.
	 */
	private void handleDispose() {
		
		if (fCachedTextViewer != null) {
			fCachedTextViewer.removeViewportListener(fInternalListener);
			fCachedTextViewer.removeTextListener(fInternalListener);
		}
		
		if (fBuffer != null) {
			fBuffer.dispose();
			fBuffer= null;
		}
	}
	
	/**
	 * Double buffer drawing.
	 * 
	 * @param dest the gc to draw into
	 */
	private void doubleBufferPaint(GC dest) {
		
		Point size= fCanvas.getSize();
		
		if (size.x <= 0 || size.y <= 0)
			return;
		
		if (fBuffer != null) {
			Rectangle r= fBuffer.getBounds();
			if (r.width != size.x || r.height != size.y) {
				fBuffer.dispose();
				fBuffer= null;
			}
		}
		if (fBuffer == null)
			fBuffer= new Image(fCanvas.getDisplay(), size.x, size.y);
			
		GC gc= new GC(fBuffer);
		gc.setFont(fCanvas.getFont());
		if (fForeground != null)
			gc.setForeground(fForeground);
		
		try {
			gc.setBackground(getBackground(fCanvas.getDisplay()));
			gc.fillRectangle(0, 0, size.x, size.y);
			doPaint(gc);
		} finally {
			gc.dispose();
		}
		
		dest.drawImage(fBuffer, 0, 0);
	}
	
	/**
	 * Returns the viewport height in lines.
	 *
	 * @return the viewport height in lines
	 */
	protected int getVisibleLinesInViewport() {
		Rectangle clArea= fCachedTextWidget.getClientArea();
		if (!clArea.isEmpty())
			return clArea.height / fCachedTextWidget.getLineHeight();
		return -1;
	}
	
	/**
	 * Draws the ruler column.
	 * 
	 * @param gc the gc to draw into
	 */
	private void doPaint(GC gc) {
		
		if (fCachedTextViewer == null)
			return;
			
		
		int firstLine= 0;
			
		int topLine= fCachedTextViewer.getTopIndex() -1;
		int bottomLine= fCachedTextViewer.getBottomIndex() + 1;
		
		try {
			
			IRegion region= fCachedTextViewer.getVisibleRegion();
			IDocument doc= fCachedTextViewer.getDocument();
			
			firstLine= doc.getLineOfOffset(region.getOffset());
			if (firstLine > topLine)
				topLine= firstLine;
					
			int lastLine= doc.getLineOfOffset(region.getOffset() + region.getLength());
			if (lastLine < bottomLine)
				bottomLine= lastLine;
				
		} catch (BadLocationException x) {
			return;
		}
		
		fSensitiveToTextChanges= bottomLine - topLine < getVisibleLinesInViewport();
		
		int lineheight= fCachedTextWidget.getLineHeight();
		fScrollPos= fCachedTextWidget.getTopPixel();
		int canvasheight= fCanvas.getSize().y;

		NumberFormat nf= NumberFormat.getInstance();
		int y= ((topLine - firstLine) * lineheight) - fScrollPos + fCachedTextViewer.getTopInset();
		for (int line= topLine; line <= bottomLine; line++, y+= lineheight) {
			
			if (y >= canvasheight)
				break;
				
			String s= Integer.toString(line + 1);
			int indentation= fIndentation[s.length()];
			gc.drawString(nf.format(line + 1), indentation, y);
		}
	}
	
	/*
	 * @see IVerticalRulerColumn#redraw
	 */
	public void redraw() {
		if (fCanvas != null && !fCanvas.isDisposed()) {
			GC gc= new GC(fCanvas);
			doubleBufferPaint(gc);
			gc.dispose();
		}
	}
	
	/*
	 * @see IVerticalRulerColumn#setModel(IAnnotationModel)
	 */
	public void setModel(IAnnotationModel model) {
	}
	
	/*
	 * @see IVerticalRulerColumn#setFont(Font)
	 */
	public void setFont(Font font) {
		fFont= font;
		if (fCanvas != null && !fCanvas.isDisposed()) {
			fCanvas.setFont(fFont);
			computeNumberOfDigits();
			computeIndentations();
		}
	}
}
