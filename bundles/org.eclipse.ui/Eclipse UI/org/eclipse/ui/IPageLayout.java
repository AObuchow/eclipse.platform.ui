package org.eclipse.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * A page layout defines the initial layout for a page in a workbench window.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * <p>
 * When the workbench needs to create a new page, it uses a perspective to
 * creates a new page layout with a single editor area. This layout is then
 * passed to client code (implementation of
 * <code>IPerspectiveFactory.createInitialLayout</code>) where 
 * additional views can be added, using the existing editor area as the initial
 * point of reference.
 * </p>
 * <p>
 * Example of populating a layout with standard workbench views:
 * <pre>
 * IPageLayout layout = ...
 * // Get the editor area.
 * String editorArea = layout.getEditorArea();
 *
 * // Top left: Resource Navigator view and Bookmarks view placeholder
 * IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, 0.25f,
 *    editorArea);
 * topLeft.addView(IPageLayout.ID_RES_NAV);
 * topLeft.addPlaceholder(IPageLayout.ID_BOOKMARKS);
 *
 * // Bottom left: Outline view and Property Sheet view
 * IFolderLayout bottomLeft = layout.createFolder("bottomLeft", IPageLayout.BOTTOM, 0.50f,
 * 	   "topLeft");
 * bottomLeft.addView(IPageLayout.ID_OUTLINE);
 * bottomLeft.addView(IPageLayout.ID_PROP_SHEET);
 *
 * // Bottom right: Task List view
 * layout.addView(IPageLayout.ID_TASK_LIST, IPageLayout.BOTTOM, 0.66f, editorArea);
 * </pre>
 * </p>
 */
public interface IPageLayout {

	/**
	 * The part id for the workbench's editor area.  This may only be used
	 * as a reference part for view addition.
	 */
	public static String ID_EDITOR_AREA = "org.eclipse.ui.editorss"; //$NON-NLS-1$
	
	/**
	 * The view id for the workbench's Resource Navigator standard component.
	 */
	public static String ID_RES_NAV = "org.eclipse.ui.views.ResourceNavigator"; //$NON-NLS-1$

	/**
	 * The view id for the workbench's Property Sheet standard component.
	 */
	public static String ID_PROP_SHEET = "org.eclipse.ui.views.PropertySheet"; //$NON-NLS-1$

	/**
	 * The view id for the workbench's Content Outline standard component.
	 */
	public static String ID_OUTLINE = "org.eclipse.ui.views.ContentOutline"; //$NON-NLS-1$


	/**
	 * The view id for the workbench's Task List standard component.
	 */
	public static String ID_TASK_LIST = "org.eclipse.ui.views.TaskList"; //$NON-NLS-1$

	/**
	 * The view id for the workbench's Bookmark Navigator standard component.
	 */
	public static String ID_BOOKMARKS = "org.eclipse.ui.views.BookmarkNavigator"; //$NON-NLS-1$

	/**
	 * Relationship constant indicating a part should be placed to the left of
	 * its relative.
	 */
	public static final int LEFT = 1;
	
	/**
	 * Relationship constant indicating a part should be placed to the right of
	 * its relative.
	 */
	public static final int RIGHT = 2;
	
	/**
	 * Relationship constant indicating a part should be placed above its 
	 * relative.
	 */
	public static final int TOP = 3;
	
	/**
	 * Relationship constant indicating a part should be placed below its 
	 * relative.
	 */
	public static final int BOTTOM = 4;
/**
 * Adds an action set with the given id to this page layout.
 * The id must name an action set contributed to the workbench's extension 
 * point (named <code>"org.eclipse.ui.actionSet"</code>).
 *
 * @param actionSetId the action set id
 */
public void addActionSet(String actionSetId);
/**
 * Adds a creation wizard to the File New menu.
 * The id must name a new wizard extension contributed to the 
 * workbench's extension point (named <code>"org.eclipse.ui.newWizards"</code>).
 *
 * @param id the wizard id
 */
public void addNewWizardShortcut(String id);
/**
 * Adds a perspective shortcut to the Perspective menu.
 * The id must name a perspective extension contributed to the 
 * workbench's extension point (named <code>"org.eclipse.ui.perspectives"</code>).
 *
 * @param id the perspective id
 */
public void addPerspectiveShortcut(String id);
/**
 * Adds a placeholder for a view with the given id to this page layout.
 * A view placeholder is used to define the position of a view before the view
 * appears.  Initially, it is invisible; however, if the user ever opens a view
 * with the same id as a placeholder, the view will replace the placeholder
 * as it is being made visible.
 * The id must name a view contributed to the workbench's view extension point 
 * (named <code>"org.eclipse.ui.views"</code>).
 *
 * @param viewId the view id
 * @param relationship the position relative to the reference part;
 *  one of <code>TOP</code>, <code>BOTTOM</code>, <code>LEFT</code>,
 *  or <code>RIGHT</code>
 * @param ratio the amount of space donated to the new part, in the range
 *	<code>0.05f</code> to <code>0.95f</code>; values outside this range
 *  will be clipped to facilitate direct manipulation
 * @param refId the id of the reference part; either a view id, a folder id,
 *   or the special editor area id returned by <code>getEditorArea</code>
 */
public void addPlaceholder(String viewId, int relationship, float ratio, String refId);
/**
 * Adds a view to the Show View menu.
 * The id must name a view extension contributed to the 
 * workbench's extension point (named <code>"org.eclipse.ui.views"</code>).
 *
 * @param id the view id
 */
public void addShowViewShortcut(String id);
/**
 * Adds a view with the given id to this page layout.
 * The id must name a view contributed to the workbench's view extension point 
 * (named <code>"org.eclipse.ui.views"</code>).
 *
 * @param viewId the view id
 * @param relationship the position relative to the reference part;
 *  one of <code>TOP</code>, <code>BOTTOM</code>, <code>LEFT</code>,
 *  or <code>RIGHT</code>
 * @param ratio the amount of space donated to the new part, in the range
 *	<code>0.05f</code> to <code>0.95f</code>; values outside this range
 *  will be clipped to facilitate direct manipulation
 * @param refId the id of the reference part; either a view id, a folder id,
 *   or the special editor area id returned by <code>getEditorArea</code>
 */
public void addView(String viewId, int relationship, float ratio, String refId);
/**
 * Creates and adds a new folder with the given id to this page layout.
 * The position and relative size of the folder is expressed relative to
 * a reference part.
 *
 * @param folderId the id for the new folder.  This must be unique within
 *  the layout to avoid collision with other parts.
 * @param relationship the position relative to the reference part;
 *  one of <code>TOP</code>, <code>BOTTOM</code>, <code>LEFT</code>,
 *  or <code>RIGHT</code>
 * @param ratio the amount of space donated to the new part, in the range
 *	<code>0.05f</code> to <code>0.95f</code>; values outside this range
 *  will be clipped to facilitate direct manipulation
 * @param refId the id of the reference part; either a view id, a folder id,
 *   or the special editor area id returned by <code>getEditorArea</code>
 * @return the new folder
 */
public IFolderLayout createFolder(String folderId, int relationship, float ratio, String refId);
/**
 * Returns the special identifier for the editor area in this page 
 * layout.  The identifier for the editor area is also stored in
 * <code>ID_EDITOR_AREA</code>.
 * <p>
 * The editor area is automatically added to each layout before anything else.
 * It should be used as the point of reference when adding views to a layout.
 * </p>
 *
 * @return the special id of the editor area
 */
public String getEditorArea();
/**
 * Returns whether the page's layout will show
 * the editor area.
 *
 * @return <code>true</code> when editor area visible, <code>false</code> otherwise
 */
public boolean isEditorAreaVisible();
/**
 * Show or hide the editor area for the page's layout.
 *
 * @param showEditorArea <code>true</code> to show the editor area, <code>false</code> to hide the editor area
 */
public void setEditorAreaVisible(boolean showEditorArea);
}
