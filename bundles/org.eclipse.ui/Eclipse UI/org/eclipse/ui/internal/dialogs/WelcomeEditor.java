package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.*;
import org.eclipse.ui.part.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.events.*;
import java.io.*;
import java.util.Enumeration;
import java.util.Iterator;
import java.net.*;

/**
 * A "fake" editor to show a welcome page
 * The contents of this page are supplied in the product configuration
 *
 * @private
 *		This class is internal to the workbench and must not be called outside the workbench
 */
public class WelcomeEditor extends EditorPart {
	private final String TAG_TITLE = "title";//$NON-NLS-1$
	private final String TAG_INTRO = "intro";//$NON-NLS-1$
	private final String TAG_ITEM = "item";//$NON-NLS-1$
	private final String TAG_CONTENTS = "contents";//$NON-NLS-1$
	
	private final static int HORZ_SCROLL_INCREMENT = 20;
	private final static int VERT_SCROLL_INCREMENT = 20;
	
	private Composite editorComposite;

	private Cursor handCursor;
	private Cursor busyCursor;
	
	private IWorkbench workbench;
	private WelcomeParser parser;
	private Image image;
/**
 * Create a new instance of the welcome editor
 */
public WelcomeEditor() {
	super();
	setTitle(WorkbenchMessages.getString("WelcomeEditor.title")); //$NON-NLS-1$	
}
/**
 * Adds listeners to the given styled text
 */
private void addListeners(StyledText styledText) {
	styledText.addMouseListener(new MouseAdapter() {
		public void mouseUp(MouseEvent e) {
			StyledText text = (StyledText)e.widget;
			WelcomeItem item = (WelcomeItem)e.widget.getData();
			int offset = text.getCaretOffset();
			if (item.isLinkAt(offset)) {	
				text.setCursor(busyCursor);
				item.triggerLinkAt(offset);
				text.setCursor(null);
			}
		}
	});
	styledText.addMouseMoveListener(new MouseMoveListener() {
		public void mouseMove(MouseEvent e) {
			StyledText text = (StyledText)e.widget;
			WelcomeItem item = (WelcomeItem)e.widget.getData();
			int offset = getOffsetAtLocation(text, e.x, e.y);
			if (offset == -1)
				text.setCursor(null);
			else if (item.isLinkAt(offset)) 
				text.setCursor(handCursor);
			else 
				text.setCursor(null);
		}
	});
}
/**
 * Creates the wizard's title area.
 *
 * @param parent the SWT parent for the title area composite
 * @return the created info area composite
 */
private Composite createInfoArea(Composite parent) {
	// Create the title area which will contain
	// a title, message, and image.
	ScrolledComposite sc = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
	sc.setLayoutData(new GridData(GridData.FILL_BOTH));
	Composite infoArea = new Composite(sc, SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.marginHeight = 10;
	layout.verticalSpacing = 5;
	layout.numColumns = 2;
	infoArea.setLayout(layout);
	GridData data = new GridData(GridData.FILL_BOTH);
	infoArea.setLayoutData(data);

	// Get the background color for the title area
	Display display = parent.getDisplay();
	Color bg = display.getSystemColor(SWT.COLOR_WHITE);
	infoArea.setBackground(bg);

	StyledText sampleStyledText = null;
	// Create the intro item
	WelcomeItem item = getIntroItem();
	if (item != null) {
		StyledText styledText = new StyledText(infoArea, SWT.MULTI | SWT.READ_ONLY);
		sampleStyledText = styledText;
		styledText.setCursor(null);
		styledText.getCaret().setVisible(false);
		styledText.setBackground(bg);
		styledText.setText(getIntroItem().getText());
		setBoldRanges(styledText, item.getBoldRanges());
		setLinkRanges(styledText, item.getActionRanges());
		setLinkRanges(styledText, item.getHelpRanges());
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		gd.horizontalIndent = 20;
		styledText.setLayoutData(gd);
		styledText.setData(item);
		addListeners(styledText);
	
		Label spacer = new Label(infoArea, SWT.NONE);
		spacer.setBackground(bg);
		gd = new GridData(); 
		gd.horizontalSpan = 2;
		spacer.setLayoutData(gd);
	}

	// Create the welcome items
	WelcomeItem[] items = getItems();
	for (int i = 0; i < items.length; i++) {
		Label image = new Label(infoArea, SWT.NONE);
		image.setBackground(bg);
		image.setImage(WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_OBJS_WELCOME_ITEM));
		GridData gd = new GridData(); 
		gd.horizontalIndent = 20;
		gd.verticalAlignment = gd.VERTICAL_ALIGN_BEGINNING;
		image.setLayoutData(gd);

		StyledText styledText = new StyledText(infoArea, SWT.MULTI | SWT.READ_ONLY);
		sampleStyledText = styledText;
		styledText.setCursor(null);
		styledText.getCaret().setVisible(false);
		styledText.setBackground(bg);
		styledText.setText(items[i].getText());
		setBoldRanges(styledText, items[i].getBoldRanges());
		setLinkRanges(styledText, items[i].getActionRanges());
		setLinkRanges(styledText, items[i].getHelpRanges());
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalSpan = 2;
		styledText.setLayoutData(gd);
		styledText.setData(items[i]);
		addListeners(styledText);
			
		Label spacer = new Label(infoArea, SWT.NONE);
		spacer.setBackground(bg);

		spacer = new Label(infoArea, SWT.NONE);
		spacer.setBackground(bg);
		gd = new GridData(); 
		gd.horizontalSpan = 2;
		spacer.setLayoutData(gd);
	}
	sc.setContent(infoArea);
	Point p = infoArea.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
	sc.setMinHeight(p.y);
	sc.setMinWidth(p.x);
	sc.setExpandHorizontal(true);
	sc.setExpandVertical(true);

	// Adjust the scrollbar increments
	if (sampleStyledText == null) {
		sc.getHorizontalBar().setIncrement(HORZ_SCROLL_INCREMENT);
		sc.getVerticalBar().setIncrement(VERT_SCROLL_INCREMENT);
	} else {
		GC gc = new GC(sampleStyledText);
		int width = gc.getFontMetrics().getAverageCharWidth();
		gc.dispose();
		sc.getHorizontalBar().setIncrement(width);
		sc.getVerticalBar().setIncrement(sampleStyledText.getLineHeight());
	}
	sc.addControlListener(new ControlAdapter() {
		public void controlResized(ControlEvent e) {
			ScrolledComposite localSC = (ScrolledComposite)e.widget;
			ScrollBar horizontal = localSC.getHorizontalBar();
			ScrollBar vertical = localSC.getVerticalBar();
			Rectangle clientArea = localSC.getClientArea(); 

			horizontal.setPageIncrement(clientArea.width - horizontal.getIncrement());
			vertical.setPageIncrement(clientArea.height - vertical.getIncrement());
		}
	});

	return infoArea;
}
/**
 * Creates the SWT controls for this workbench part.
 * <p>
 * Clients should not call this method (the workbench calls this method at
 * appropriate times).
 * </p>
 * <p>
 * For implementors this is a multi-step process:
 * <ol>
 *   <li>Create one or more controls within the parent.</li>
 *   <li>Set the parent layout as needed.</li>
 *   <li>Register any global actions with the <code>IActionService</code>.</li>
 *   <li>Register any popup menus with the <code>IActionService</code>.</li>
 *   <li>Register a selection provider with the <code>ISelectionService</code>
 *     (optional). </li>
 * </ol>
 * </p>
 *
 * @param parent the parent control
 */
public void createPartControl(Composite parent) {
	// read our contents
	readFile();
	if (parser == null)
		return;

	handCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_HAND);
	busyCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_WAIT);
	
	editorComposite = new Composite(parent, SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.marginHeight = 0;
	layout.marginWidth = 0;
	layout.verticalSpacing = 0;
	layout.horizontalSpacing = 0;
	editorComposite.setLayout(layout);

	createTitleArea(editorComposite);

	Label titleBarSeparator = new Label(editorComposite, SWT.HORIZONTAL | SWT.SEPARATOR);
	GridData gd = new GridData(GridData.FILL_HORIZONTAL);
	titleBarSeparator.setLayoutData(gd);

	createInfoArea(editorComposite);

	WorkbenchHelp.setHelp(editorComposite, new String[] {IHelpContextIds.WELCOME_EDITOR});
}
/**
 * Creates the wizard's title area.
 *
 * @param parent the SWT parent for the title area composite
 * @return the created title area composite
 */
private Composite createTitleArea(Composite parent) {
	// Get the background color for the title area
	Display display = parent.getDisplay();
	Color bg = display.getSystemColor(SWT.COLOR_WHITE);

	// Create the title area which will contain
	// a title, message, and image.
	Composite titleArea = new Composite(parent, SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.marginHeight = 0;
	layout.marginWidth = 0;
	layout.verticalSpacing = 0;
	layout.horizontalSpacing = 0;
	layout.numColumns = 2;
	titleArea.setLayout(layout);
	titleArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	// Message label
	CLabel messageLabel = new CLabel(titleArea, SWT.LEFT);
	messageLabel.setBackground(bg);
	messageLabel.setText(getBannerTitle());
	messageLabel.setFont(JFaceResources.getBannerFont());
	GridData gd = new GridData(GridData.FILL_BOTH);
	messageLabel.setLayoutData(gd);

	// Title image
	Label titleImage = new Label(titleArea, SWT.LEFT);
	titleImage.setBackground(bg);
	titleImage.setImage(WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_OBJS_WELCOME_BANNER));
	gd = new GridData(); 
	gd.horizontalAlignment = gd.END;
	titleImage.setLayoutData(gd);

	return titleArea;
}
/**
 * The <code>WorkbenchPart</code> implementation of this 
 * <code>IWorkbenchPart</code> method disposes the title image
 * loaded by <code>setInitializationData</code>. Subclasses may extend.
 */
public void dispose() {
	super.dispose();
	if (busyCursor != null)
		busyCursor.dispose();
	if (handCursor != null)
		handCursor.dispose();
}
/* (non-Javadoc)
 * Saves the contents of this editor.
 * <p>
 * Subclasses must override this method to implement the open-save-close lifecycle
 * for an editor.  For greater details, see <code>IEditorPart</code>
 * </p>
 *
 * @see IEditorPart
 */
public void doSave(IProgressMonitor monitor) {
	// do nothing
}
/* (non-Javadoc)
 * Saves the contents of this editor to another object.
 * <p>
 * Subclasses must override this method to implement the open-save-close lifecycle
 * for an editor.  For greater details, see <code>IEditorPart</code>
 * </p>
 *
 * @see IEditorPart
 */
public void doSaveAs() {
	// do nothing	
}
/**
 * Returns the title obtained from the parser
 */
private String getBannerTitle() {
	if (parser.getTitle() == null)
		return ""; //$NON-NLS-1$
	return parser.getTitle();
}
/**
 * Returns the intro item or <code>null</code>
 */
private WelcomeItem getIntroItem() {
	return parser.getIntroItem();
}
/**
 * Returns the welcome items
 */
private WelcomeItem[] getItems() {
	return parser.getItems();
}
/**
 * Returns the text offset at the given pixwl location
 * Returns -1 if there is no offset at the location
 */
private int getOffsetAtLocation(StyledText styledText, int x, int y) {
		int charCount = styledText.getCharCount();
	if (charCount == 0 || x < 0 || y < 0)
		return -1;
	
	// get the line at the y coordinate
	int line = (y + styledText.getTopPixel()) / styledText.getLineHeight();

	// find an offset in the line
	int low = -1;
	int high = charCount;
	int offset = 0;
	int currentLine;
	
	while (high - low > 1) {
		offset = (high + low) / 2;
		currentLine = styledText.getLineAtOffset(offset);
		if (currentLine == line)
			break;
		if (currentLine > line)
			high = offset;			
		else 
			low = offset;
	}
	currentLine = styledText.getLineAtOffset(offset);

	// find the offset at x
	int delta;
	Point loc = styledText.getLocationAtOffset(offset);
	if (loc.x == x)
		return offset;
	else if (loc.x < x)
		delta = 1;
	else
		delta = -1;
	int newOffset = offset + delta;
	Point newLoc = styledText.getLocationAtOffset(newOffset);	
	while (currentLine == styledText.getLineAtOffset(newOffset)) {
		if (delta == 1) {
			if (newLoc.x > x)
				return offset;
		} else { 
			if (newLoc.x < x)
				return offset;
		}
		offset = newOffset;
		loc = newLoc;
		newOffset = offset + delta;
		if (newOffset < 0 || newOffset > charCount)
			return -1;
		newLoc = styledText.getLocationAtOffset(newOffset);
	}
	return -1;
}
/* (non-Javadoc)
 * Sets the cursor and selection state for this editor to the passage defined
 * by the given marker.
 * <p>
 * Subclasses may override.  For greater details, see <code>IEditorPart</code>
 * </p>
 *
 * @see IEditorPart
 */
public void gotoMarker(IMarker marker) {
	// do nothing
}
/* (non-Javadoc)
 * Initializes the editor part with a site and input.
 * <p>
 * Subclasses of <code>EditorPart</code> must implement this method.  Within
 * the implementation subclasses should verify that the input type is acceptable
 * and then save the site and input.  Here is sample code:
 * </p>
 * <pre>
 *		if (!(input instanceof IFileEditorInput))
 *			throw new PartInitException("Invalid Input: Must be IFileEditorInput");
 *		setSite(site);
 *		setInput(editorInput);
 * </pre>
 */
public void init(IEditorSite site, IEditorInput input) throws PartInitException {
	setSite(site);
	setInput(new WelcomeEditorInput());
}
/* (non-Javadoc)
 * Returns whether the contents of this editor have changed since the last save
 * operation.
 * <p>
 * Subclasses must override this method to implement the open-save-close lifecycle
 * for an editor.  For greater details, see <code>IEditorPart</code>
 * </p>
 *
 * @see IEditorPart
 */
public boolean isDirty() {
	return false;
}
/* (non-Javadoc)
 * Returns whether the "save as" operation is supported by this editor.
 * <p>
 * Subclasses must override this method to implement the open-save-close lifecycle
 * for an editor.  For greater details, see <code>IEditorPart</code>
 * </p>
 *
 * @see IEditorPart
 */
public boolean isSaveAsAllowed() {
	return false;
}
/**
 * Read the contents of the welcome page
 */
public void read(InputStream is) {
	parser = new WelcomeParser();
	parser.parse(is);
}
/**
 * Reads the welcome file
 */
public void readFile() {
	ProductInfo info = ((Workbench)PlatformUI.getWorkbench()).getProductInfo();
	URL url = info.getWelcomePageURL();

	if (url == null)
		// should not happen since we disable if none specified
		return;
		
	InputStream is = null;
	try {
		is = url.openStream();
		read(is);
	}
	catch (IOException e) {
		IStatus status = new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, 1, WorkbenchMessages.getString("WelcomeEditor.accessException"), e); //$NON-NLS-1$
		WorkbenchPlugin.log(WorkbenchMessages.getString("WelcomeEditor.readFileError"), status); //$NON-NLS-1$
	}
	finally {
		try { 
			if (is != null)
				is.close(); 
		} catch (IOException e) {}
	}
}
/**
 * Sets the styled text's bold ranges
 */
private void setBoldRanges(StyledText styledText, int[][] boldRanges) {
	for (int i = 0; i < boldRanges.length; i++) {
		StyleRange r = new StyleRange(boldRanges[i][0], boldRanges[i][1], null, null, SWT.BOLD);
		styledText.setStyleRange(r);
	}
}
/**
 * Asks this part to take focus within the workbench.
 * <p>
 * Clients should not call this method (the workbench calls this method at
 * appropriate times).
 * </p>
 */
public void setFocus() {
	if (editorComposite != null) {
		editorComposite.setFocus();
	}
}
/**
 * Sets the styled text's link (blue) ranges
 */
private void setLinkRanges(StyledText styledText, int[][] linkRanges) {
	Color fg = styledText.getDisplay().getSystemColor(SWT.COLOR_BLUE);
	for (int i = 0; i < linkRanges.length; i++) {
		StyleRange r = new StyleRange(linkRanges[i][0], linkRanges[i][1], fg, null);
		styledText.setStyleRange(r);
	}
}
}
