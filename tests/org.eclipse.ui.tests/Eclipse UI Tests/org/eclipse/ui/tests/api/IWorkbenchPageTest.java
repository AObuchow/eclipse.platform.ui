package org.eclipse.ui.tests.api;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.ui.*;
import org.eclipse.ui.test.harness.util.*;
import org.eclipse.ui.part.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.registry.*;

public class IWorkbenchPageTest extends AbstractTestCase {

	private IWorkbenchPage fActivePage;
	private IWorkbenchWindow fWin;
	private IWorkbenchPart partMask;
	private IProject proj;

	public IWorkbenchPageTest(String testName) {
		super(testName);
	}

	public void setUp() {
		fWin = openTestWindow();
		fActivePage = fWin.getActivePage();
	}

	public void tearDown() {
		super.tearDown();
		if (proj != null) {
			try {
				FileUtil.deleteProject(proj);
			} catch (Throwable e) {
				fail();
			}
			proj = null;
		}
	}

	/**
	 *	tests both of the following:	
	 *	setEditorAreaVisible()
	 *	isEditorAreaVisible()
	 */
	public void testGet_SetEditorAreaVisible() throws Throwable {
		fActivePage.setEditorAreaVisible(true);
		assert(fActivePage.isEditorAreaVisible() == true);

		fActivePage.setEditorAreaVisible(false);
		assert(fActivePage.isEditorAreaVisible() == false);
	}

	public void testGetPerspective() throws Throwable {
		assertNotNull(fActivePage.getPerspective());

		IWorkbenchPage page =
			fWin.openPage(EmptyPerspective.PERSP_ID, ResourcesPlugin.getWorkspace());
		assertEquals(EmptyPerspective.PERSP_ID, page.getPerspective().getId());
	}

	public void testSetPerspective() throws Throwable {
		IPerspectiveDescriptor per =
			PlatformUI.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(
				EmptyPerspective.PERSP_ID);
		fActivePage.setPerspective(per);
		assertEquals(per, fActivePage.getPerspective());
	}

	public void testGetLabel() {
		assertNotNull(fActivePage.getLabel());
	}

	public void testGetInput() throws Throwable {
		IAdaptable input = ResourcesPlugin.getWorkspace();
		IWorkbenchPage page = fWin.openPage(input);
		assertEquals(input, page.getInput());
	}

	public void testActivate() throws Throwable {
		MockViewPart part = (MockViewPart) fActivePage.showView(MockViewPart.ID);
		MockViewPart part2 = (MockViewPart) fActivePage.showView(MockViewPart.ID2);

		MockPartListener listener = new MockPartListener();
		fActivePage.addPartListener(listener);
		fActivePage.activate(part);

		CallHistory callTrace;

		callTrace = part2.getCallHistory();
		callTrace.clear();
		fActivePage.activate(part2);
		assert(callTrace.contains("setFocus"));
		assert(listener.getCallHistory().contains("partActivated"));

		callTrace = part.getCallHistory();
		callTrace.clear();
		fActivePage.activate(part);
		assert(callTrace.contains("setFocus"));
		assert(listener.getCallHistory().contains("partActivated"));
	}

	public void testBringToTop() throws Throwable {
		proj = FileUtil.createProject("testOpenEditor");
		IEditorPart part = fActivePage.openEditor(FileUtil.createFile("a.mock1", proj));
		IEditorPart part2 =
			fActivePage.openEditor(FileUtil.createFile("b.mock1", proj));

		MockPartListener listener = new MockPartListener();
		fActivePage.addPartListener(listener);
		CallHistory callTrace = listener.getCallHistory();

		//at this point, part2 is active
		fActivePage.bringToTop(part);
		assertEquals(callTrace.contains("partBroughtToTop"), true);

		callTrace.clear();
		fActivePage.bringToTop(part2);
		assertEquals(callTrace.contains("partBroughtToTop"), true);
	}

	public void testGetWorkbenchWindow() {
		assertEquals(fActivePage.getWorkbenchWindow(), fWin);
		IWorkbenchPage page = openTestPage(fWin);
		assertEquals(page.getWorkbenchWindow(), fWin);
	}

	public void testShowView() throws Throwable {
	/*
		javadoc: Shows a view in this page and give it focus
	*/		
		MockViewPart view = (MockViewPart) fActivePage.showView(MockViewPart.ID);
		assertNotNull(view);
		assert(
			view.getCallHistory().verifyOrder(
				view,
				new String[] { "init", "createPartControl", "setFocus" }));
	
		fActivePage.showView(MockViewPart.ID2 );
		
	/*
		javadoc: If the view is already visible, it is given focus
	*/
		CallHistory callTrace = view.getCallHistory();
		callTrace.clear();
		assertEquals(fActivePage.showView(MockViewPart.ID), view );		
		assertEquals(callTrace.contains( "setFocus" ), true );
	}

	/**
	 *	openEditor(IFile input)
	 */
	public void testOpenEditor() throws Throwable {
		proj = FileUtil.createProject("testOpenEditor");		
		
	/*
		javadoc: 1. The workbench editor registry is consulted to determine if an editor extension has been
		registered for the file type. If so, an instance of the editor extension is opened on the file
	*/
		IFile file = FileUtil.createFile("test.mock1", proj);
		IEditorPart editor = fActivePage.openEditor(file);
		assertEquals(ArrayUtil.has(fActivePage.getEditors(), editor), true);
		assertEquals(fActivePage.getActiveEditor(), editor);
		assertEquals(editor.getSite().getId(),
			fWorkbench.getEditorRegistry().getDefaultEditor(file).getId());

	/*
		javadoc: 2. Next, the native operating system will be consulted to determine if a native editor exists for 
		the file type. If so, a new process is started and the native editor is opened on the file.
	*/
		//can not be tested

	/*
		javadoc: 3. If all else fails the file will be opened in a default text editor.		
	*/
		// PR 1GkD5O0 - Fails on linux
		String platform = SWT.getPlatform();
		if (!platform.equals("motif")) {
			file = FileUtil.createFile("a.null and void", proj);
			editor = fActivePage.openEditor(file);
			assertEquals(ArrayUtil.has(fActivePage.getEditors(), editor), true);
			assertEquals(fActivePage.getActiveEditor(), editor);
			assertEquals(editor.getSite().getId(), "org.eclipse.ui.DefaultTextEditor");
		}
			
		//open another editor to take the focus away from the first editor
		fActivePage.openEditor(FileUtil.createFile("test.mock2", proj));
	
	/*	
		javadoc: If this page already has an editor open on the target object that editor is activated
	*/
		//open the editor second time.		
		assertEquals(editor, fActivePage.openEditor(file));
		assertEquals(editor, fActivePage.getActiveEditor());
	}

	/**
	 * openEditor(IFile input, String editorID)
	 */
	public void testOpenEditor2() throws Throwable {
		proj = FileUtil.createProject("testOpenEditor");
		final IFile file = FileUtil.createFile("asfasdasdf", proj);
		final String id = MockEditorPart.ID1;

	/*
		javadoc: The editor type is determined by mapping editorId to an editor extension registered with the workbench.
	*/	
		IEditorPart editor = fActivePage.openEditor(file, id);
		assertEquals(editor.getSite().getId(), id);
		assertEquals(ArrayUtil.has(fActivePage.getEditors(), editor), true);
		assertEquals(fActivePage.getActiveEditor(), editor);

		//open another editor to take the focus away from the first editor
		fActivePage.openEditor(FileUtil.createFile("test.mock2", proj));
		
	/*
		javadoc: If this page already has an editor open on the target object that editor is activated
	*/
		//open the first editor second time.
		assertEquals(fActivePage.openEditor(file, id), editor);
		assertEquals(fActivePage.getActiveEditor(), editor);
	}

	/**
	 * openEditor(IEditorInput input,String editorId)                       
	 */
	public void testOpenEditor3() throws Throwable {
		proj = FileUtil.createProject("testOpenEditor");
		final String id = MockEditorPart.ID1;
		IEditorInput input =
			new FileEditorInput(FileUtil.createFile("test.mock1", proj));
			
	/*
		javadoc: The editor type is determined by mapping editorId to an editor extension registered with the workbench
	*/
		IEditorPart editor = fActivePage.openEditor(input, id);
		assertEquals(editor.getEditorInput(), input);
		assertEquals(editor.getSite().getId(), id);
		assertEquals(ArrayUtil.has(fActivePage.getEditors(), editor), true);
		assertEquals(fActivePage.getActiveEditor(), editor);

		//open another editor to take the focus away from the first editor
		fActivePage.openEditor(FileUtil.createFile("test.mock2", proj));
		
	/*
		javadoc: If this page already has an editor open on the target object that editor is activated
	*/
		//open the first editor second time.
		assertEquals(fActivePage.openEditor(input, id), editor);
		assertEquals(fActivePage.getActiveEditor(), editor);
	}

	/**
	 * openEditor(IEditorInput input, String editorId, boolean activate) 
	 */
	public void testOpenEditor4() throws Throwable {
		proj = FileUtil.createProject("testOpenEditor");
		final String id = MockEditorPart.ID1;
		IEditorInput input =
			new FileEditorInput(FileUtil.createFile("test.mock1", proj));
		MockPartListener listener = new MockPartListener();
		fActivePage.addPartListener(listener);
		CallHistory callTrace = listener.getCallHistory();

	/*
		javadoc: The editor type is determined by mapping editorId to an editor extension 
		registered with the workbench. 
		javadoc: If activate == true the editor will be activated
	*/
		//open an editor with activation
		IEditorPart editor = fActivePage.openEditor(input, id, true);
		assertEquals(editor.getEditorInput(), input);
		assertEquals(editor.getSite().getId(), id);
		assertEquals(ArrayUtil.has(fActivePage.getEditors(), editor), true);
		assertEquals(fActivePage.getActiveEditor(), editor);
		assertEquals(callTrace.contains( "partActivated"), true);
		fActivePage.closeEditor(editor, false);

		//we need another editor so that the editor under test can receive events.
		//otherwise, events will be ignored.
		IEditorPart extra = fActivePage.openEditor(FileUtil.createFile("aaaaa", proj));

		//open an editor without activation
		callTrace.clear();
		editor = fActivePage.openEditor(input, id, false);
		assertEquals(editor.getEditorInput(), input);
		assertEquals(editor.getSite().getId(), id);
		assertEquals(ArrayUtil.has(fActivePage.getEditors(), editor), true);
		assertEquals(callTrace.contains( "partActivated"), false);
		assertEquals(callTrace.contains( "partBroughtToTop"), false);

		fActivePage.activate(extra);

	/*
		javadoc: If this page already has an editor open on the target object that editor is brought to the front
	*/
		//open the editor under test second time without activation
		callTrace.clear();
		assertEquals(fActivePage.openEditor(input, id, false), editor);
		assertEquals(callTrace.contains( "partBroughtToTop"), true);
		assertEquals(callTrace.contains( "partActivated"), false);

		//activate the other editor
		fActivePage.activate(extra);

	/*
		javadoc: If activate == true the editor will be activated
	*/
		//open the editor under test second time with activation
		callTrace.clear();
		assertEquals(fActivePage.openEditor(input, id, true), editor);
		assertEquals(callTrace.contains( "partBroughtToTop"), true);
		assertEquals(callTrace.contains( "partActivated"), true);
	}

	/**
	 * openEditor(IMarker marker)                       
	 */
	public void testOpenEditor5() throws Throwable {
		proj = FileUtil.createProject("testOpenEditor");
		IMarker marker =
			FileUtil.createFile("aa.mock2", proj).createMarker(IMarker.TASK);
		CallHistory callTrace;

	/*	
		javadoc: the cursor and selection state of the editor is then updated from information 
		recorded in the marker. 
	*/		
		//open the registered editor for the marker resource 
		IEditorPart editor = fActivePage.openEditor(marker);
		callTrace = ((MockEditorPart) editor).getCallHistory();
		assertEquals(editor.getSite().getId(), MockEditorPart.ID2);
		assertEquals(ArrayUtil.has(fActivePage.getEditors(), editor), true);
		assertEquals(fActivePage.getActiveEditor(), editor);
		assertEquals(callTrace.contains( "gotoMarker"), true);
		fActivePage.closeEditor(editor, false);

	/*	
		javadoc: If the marker contains an EDITOR_ID_ATTR attribute the attribute value will be used to 
		determine the editor type to be opened
	*/
		marker.setAttribute(IWorkbenchPage.EDITOR_ID_ATTR, MockEditorPart.ID1);
		editor = fActivePage.openEditor(marker);
		callTrace = ((MockEditorPart) editor).getCallHistory();
		assertEquals(editor.getSite().getId(), MockEditorPart.ID1);
		assertEquals(ArrayUtil.has(fActivePage.getEditors(), editor), true);
		assertEquals(fActivePage.getActiveEditor(), editor);
		assertEquals(callTrace.contains( "gotoMarker"), true);
		//do not close the editor this time

	/*
		javdoc: If this page already has an editor open on the target object that editor is activated
	*/		
		callTrace.clear();
		assertEquals(fActivePage.openEditor(marker), editor);
		assertEquals(fActivePage.getActiveEditor(), editor);
		assertEquals(callTrace.contains( "gotoMarker"), true);
		fActivePage.closeEditor(editor, false);
	}

	/**
	 *	openEditor(IMarker marker, boolean activate)                                           
	 */
	public void testOpenEditor6() throws Throwable {
		proj = FileUtil.createProject("testOpenEditor");
		IMarker marker =
			FileUtil.createFile("aa.mock2", proj).createMarker(IMarker.TASK);
		MockPartListener listener = new MockPartListener();
		fActivePage.addPartListener(listener);
		CallHistory listenerCall = listener.getCallHistory();
		CallHistory editorCall;

		//we need another editor so that the editor under test can receive events.
		//otherwise, events will be ignored.
		IEditorPart extra = fActivePage.openEditor(FileUtil.createFile("aaaaa", proj));

	/*
		javadoc: If activate == true the editor will be activated
	*/		
		//open the registered editor for the marker resource with activation
		IEditorPart editor = fActivePage.openEditor(marker, true);
		editorCall = ((MockEditorPart) editor).getCallHistory();
		assertEquals(editor.getSite().getId(), MockEditorPart.ID2);
		assertEquals(ArrayUtil.has(fActivePage.getEditors(), editor), true);
		assertEquals(fActivePage.getActiveEditor(), editor);

	/*	
		javadoc: the cursor and selection state of the editor is then updated from information 
		recorded in the marker. 
	*/		
		assertEquals(editorCall.contains( "gotoMarker"), true);
		fActivePage.closeEditor(editor, false);

		fActivePage.activate(extra);

		//open the registered editor for the marker resource without activation
		listenerCall.clear();
		editor = fActivePage.openEditor(marker, false);
		editorCall = ((MockEditorPart) editor).getCallHistory();
		assertEquals(editor.getSite().getId(), MockEditorPart.ID2);
		assertEquals(ArrayUtil.has(fActivePage.getEditors(), editor), true);
		assertEquals(listenerCall.contains( "partBroughtToTop"), false);
		assertEquals(listenerCall.contains( "partActivated"), false);	
		assertEquals(editorCall.contains( "gotoMarker"), true);		
		fActivePage.closeEditor(editor, false);
	
	/*	
		javadoc: If the marker contains an EDITOR_ID_ATTR attribute the attribute value will be used to 
		determine the editor type to be opened
	*/
		String id = MockEditorPart.ID1;
		marker.setAttribute(IWorkbenchPage.EDITOR_ID_ATTR, id);

		//open an editor with activation
		listenerCall.clear();
	
		editor = fActivePage.openEditor(marker, true);
		editorCall = ((MockEditorPart) editor).getCallHistory();
		assertEquals(editor.getSite().getId(), id);
		assertEquals(ArrayUtil.has(fActivePage.getEditors(), editor), true);
		assertEquals(fActivePage.getActiveEditor(), editor);
		assertEquals(editorCall.contains( "gotoMarker"), true);
		fActivePage.closeEditor(editor, false);

		fActivePage.activate(extra);

		//open an editor without activation
		listenerCall.clear();
		editor = fActivePage.openEditor(marker, false);
		editorCall = ((MockEditorPart) editor).getCallHistory();
		assertEquals(editor.getSite().getId(), id);
		assertEquals(ArrayUtil.has(fActivePage.getEditors(), editor), true);
		assertEquals(editorCall.contains( "gotoMarker"), true);
		assertEquals(listenerCall.contains( "partActivated"), false);
		assertEquals(listenerCall.contains( "partBroughtToTop"), false);
		//do not close the editor this time

		fActivePage.activate(extra);

	/*
		javadoc: If this page already has an editor open on the target object that editor is brought to front
	*/
		//open the editor second time without activation
		listenerCall.clear();
		assertEquals(fActivePage.openEditor(marker, false), editor);
		assertEquals(listenerCall.contains( "partBroughtToTop"), true);
		assertEquals(listenerCall.contains( "partActivated"), false);

		fActivePage.activate(extra);

	/*
		javdoc: If activate == true the editor will be activated
	*/
		//open the editor second time with activation 		
		listenerCall.clear();
		assertEquals(fActivePage.openEditor(marker, true), editor);
		assertEquals(editorCall.contains( "gotoMarker"), true);
		assertEquals(listenerCall.contains( "partBroughtToTop"), true);		
		assertEquals(listenerCall.contains( "partActivated"), true);
	}

	public void testFindView() throws Throwable {
		String id = MockViewPart.ID3;
		//id of valid, but not open view
		assertNull(fActivePage.findView(id));

		IViewPart view = fActivePage.showView(id);
		assertEquals(fActivePage.findView(id), view);

		//close view		
		fActivePage.hideView(view);
		assertNull(fActivePage.findView(id));
	}

	public void testGetViews() throws Throwable {
		int totalBefore = fActivePage.getViews().length;

		IViewPart view = fActivePage.showView(MockViewPart.ID2);
		assertEquals(ArrayUtil.has(fActivePage.getViews(), view), true);
		assertEquals(fActivePage.getViews().length, totalBefore + 1);

		fActivePage.hideView(view);
		assertEquals(ArrayUtil.has(fActivePage.getViews(), view), false);
		assertEquals(fActivePage.getViews().length, totalBefore);
	}

	public void testHideView() throws Throwable {
		IViewPart view = fActivePage.showView(MockViewPart.ID3);

		fActivePage.hideView(view);
		CallHistory callTrace = ((MockViewPart) view).getCallHistory();
		assert(callTrace.contains( "dispose"));
	}

	public void testClose() throws Throwable {
		IWorkbenchPage page = openTestPage(fWin);

		proj = FileUtil.createProject("testOpenEditor");
		final IFile file = FileUtil.createFile("aaa.mock1", proj);
		IEditorPart editor = page.openEditor(file);
		CallHistory callTrace = ((MockEditorPart) editor).getCallHistory();
		callTrace.clear();

	/*
		javadoc: If the page has open editors with unsaved content and save is true, the user will be given the opportunity to save them
	*/		
		assertEquals(page.close(), true);
		assertEquals(
			callTrace.verifyOrder(editor, new String[] { "isDirty", "dispose" }),
			true);
		assertEquals(fWin.getActivePage(), fActivePage);	
	}

	public void testCloseEditor() throws Throwable {
		proj = FileUtil.createProject("testOpenEditor");
		final IFile file = FileUtil.createFile("test.mock1", proj);
		IEditorPart editor;
		CallHistory callTrace;
		MockEditorPart mock;

	/*
		javadoc: Parameters: save - true to save the editor contents if required (recommended)
	*/
		//create a clean editor that needs to be saved on closing
		editor = fActivePage.openEditor(file);
		mock = (MockEditorPart) editor;
		mock.setSaveNeeded(true);
		callTrace = mock.getCallHistory();
		callTrace.clear();
		//close the editor with save confirmation
		assertEquals( fActivePage.closeEditor(editor, true), true );
		assertEquals(
			callTrace.verifyOrder(
				editor,
				new String[] { "isSaveOnCloseNeeded", "isDirty", "dispose" }),
			true);

	/*
		javadoc: If the editor has unsaved content and save is true, the user will be given the opportunity to save it.
	*/
		//can't be tested
			
	/*
		javadoc: Parameters: save - false to discard any unsaved changes
	*/
		//create a dirty editor
		editor = fActivePage.openEditor(file);
		mock = (MockEditorPart) editor;
		mock.setDirty(true);
		mock.setSaveNeeded(true);
		callTrace = mock.getCallHistory();
		callTrace.clear();
		//close the editor and discard changes
		assertEquals( fActivePage.closeEditor(editor, false), true );
		assertEquals(callTrace.contains( "isSaveOnCloseNeeded"), false);
		assertEquals(callTrace.contains( "isDirty"), false);
		assertEquals(callTrace.contains( "doSave"), false);
		assertEquals(callTrace.contains( "dispose"), true);
	}

	public void testCloseAllEditors() throws Throwable
	{
		int total = 5;
		final IFile[] files = new IFile[total];
		IEditorPart[] editors = new IEditorPart[total];
		CallHistory[] callTraces = new CallHistory[total];
		MockEditorPart[] mocks = new MockEditorPart[total];

		proj = FileUtil.createProject("testOpenEditor");
		for (int i = 0; i < total; i++)
			files[i] = FileUtil.createFile(i + ".mock2", proj);

	/*
		javadoc: If the page has open editors with unsaved content and save is true, the user will be given the opportunity to save them.
	*/		
		//close all clean editors with confirmation
		for( int i = 0; i < total; i ++ ){	
			editors[i] = fActivePage.openEditor(files[i]);			
			callTraces[i] = ((MockEditorPart)editors[i]).getCallHistory();		
		}
		assertEquals(fActivePage.closeAllEditors(true), true);
		for (int i = 0; i < total; i++) {
			assertEquals(callTraces[i].contains( "isDirty"), true);
			assertEquals(callTraces[i].contains( "doSave"), false);
			callTraces[i].clear();
		}
		
		//close all dirty editors with confirmation
		//can't be tested		
		
		//close all dirty editors discarding them		
		for( int i = 0; i < total; i ++ ){	
			editors[i] = fActivePage.openEditor(files[i]);
			mocks[i] = (MockEditorPart) editors[i];
			mocks[i].setDirty(true);
			callTraces[i] = mocks[i].getCallHistory();
		}
		assertEquals(fActivePage.closeAllEditors(false), true);
		for (int i = 0; i < total; i++) {
			assertEquals(callTraces[i].contains("doSave"), false);
		}
	}

	public void testSaveEditor() throws Throwable {
		proj = FileUtil.createProject("testOpenEditor");
		final IFile file = FileUtil.createFile("test.mock1", proj);
		IEditorPart editor;
		CallHistory callTrace;
		MockEditorPart mock;

		//create a clean editor
		editor = fActivePage.openEditor(file);
		mock = (MockEditorPart) editor;
		callTrace = mock.getCallHistory();
		callTrace.clear();

	/*
		javadoc: Saves the contents of the given editor if dirty. 
		If not, this method returns without effect
	*/
		//save the clean editor with confirmation		
		assertEquals(fActivePage.saveEditor(editor, true), true);
		assertEquals(callTrace.contains("isDirty"), true);
		assertEquals(callTrace.contains("doSave"), false);
	
	/*
		javadoc: If confirm is true the user is prompted to confirm the command. 
	*/
		//can't be tested

	/*
		javadoc: Otherwise, the save happens without prompt. 
	*/
		//save the clean editor without confirmation
		assertEquals(fActivePage.saveEditor(editor, false), true);
		assertEquals(callTrace.contains( "isDirty"), true);
		assertEquals(callTrace.contains("doSave"), false);

		//save the dirty editor without confirmation
		mock.setDirty(true);
		callTrace.clear();
		assertEquals(fActivePage.saveEditor(editor, false), true);
		assertEquals(
			callTrace.verifyOrder(editor, new String[] { "isDirty", "doSave" }),
			true);
	}

	public void testSaveAllEditors() throws Throwable {
		int total = 3;

		final IFile[] files = new IFile[total];
		IEditorPart[] editors = new IEditorPart[total];
		CallHistory[] callTraces = new CallHistory[total];
		MockEditorPart[] mocks = new MockEditorPart[total];

		proj = FileUtil.createProject("testOpenEditor");
		for (int i = 0; i < total; i++) {
			files[i] = FileUtil.createFile(i + ".mock2", proj);
			editors[i] = fActivePage.openEditor(files[i]);
			mocks[i] = (MockEditorPart) editors[i];
			callTraces[i] = mocks[i].getCallHistory();
		}

	/*
		javadoc: If there are no dirty editors this method returns without effect.
		javadoc: If confirm is true the user is prompted to confirm the command
	*/
		//save all clean editors with confirmation
		assertEquals(fActivePage.saveAllEditors(true), true);
		for (int i = 0; i < total; i++) {
			assertEquals(callTraces[i].contains( "isDirty"), true);
			assertEquals(callTraces[i].contains( "doSave"), false);
			callTraces[i].clear();
		}

		//save all dirty editors with confirmation can't be tested

	/*
		javadoc: Parameters: confirm - false to save unsaved changes without asking
	*/
		//save all clean editors without confirmation
		assertEquals(fActivePage.saveAllEditors(false), true);
		for (int i = 0; i < total; i++) {
			assertEquals(callTraces[i].contains( "isDirty"), true);
			assertEquals(callTraces[i].contains( "doSave"), false);
			callTraces[i].clear();
		}

		//save all dirty editors without confirmation
		for (int i = 0; i < total; i++) 
			mocks[i].setDirty(true);
		assertEquals(fActivePage.saveAllEditors(false), true);
		for (int i = 0; i < total; i++)
			assertEquals(
				callTraces[i].verifyOrder(editors[i], new String[] { "isDirty", "doSave" }),
				true);
	}

	public void testGetEditors() throws Throwable {
		proj = FileUtil.createProject("testOpenEditor");
		int totalBefore = fActivePage.getEditors().length;
		int num = 3;
		IEditorPart[] editors = new IEditorPart[num];

		for (int i = 0; i < num; i++) {
			editors[i] = fActivePage.openEditor(FileUtil.createFile(i + ".mock2", proj));
			assertEquals(ArrayUtil.has(fActivePage.getEditors(), editors[i]), true);
		}
		assertEquals(fActivePage.getEditors().length, totalBefore + num);

		fActivePage.closeEditor(editors[0], false);
		assertEquals(ArrayUtil.has(fActivePage.getEditors(), editors[0]), false);
		assertEquals(fActivePage.getEditors().length, totalBefore + num - 1);

		fActivePage.closeAllEditors(false);
		assertEquals(fActivePage.getEditors().length, 0);
	}

	public void testShowActionSet() {
		String id = MockActionDelegate.ACTION_SET_ID;
		WorkbenchPage page = (WorkbenchPage) fActivePage;

		int totalBefore = page.getActionSets().length;
		fActivePage.showActionSet(id);

		IActionSetDescriptor[] sets = ((WorkbenchPage) fActivePage).getActionSets();
		boolean found = false;
		for (int i = 0; i < sets.length; i++)
			if (id.equals(sets[i].getId()))
				found = true;
		assertEquals(found, true);

		//check that the method does not add an invalid action set to itself
		id = IConstants.FakeID;
		fActivePage.showActionSet(id);

		sets = ((WorkbenchPage) fActivePage).getActionSets();
		found = false;
		for (int i = 0; i < sets.length; i++)
			if (id.equals(sets[i].getId()))
				found = true;
		assertEquals(found, false);
		assertEquals(page.getActionSets().length, totalBefore + 1);
	}

	public void testHideActionSet() {
		WorkbenchPage page = (WorkbenchPage) fActivePage;
		int totalBefore = page.getActionSets().length;

		String id = MockWorkbenchWindowActionDelegate.SET_ID;
		fActivePage.showActionSet( id );
		assertEquals(page.getActionSets().length, totalBefore + 1);

		fActivePage.hideActionSet(id );
		assertEquals(page.getActionSets().length, totalBefore );
		
		IActionSetDescriptor[] sets = page.getActionSets();
		boolean found = false;
		for (int i = 0; i < sets.length; i++)
			if (id.equals(sets[i].getId()))
				found = true;
		assertEquals(found, false);
	}
}