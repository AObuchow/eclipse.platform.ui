package org.eclipse.ui.tests.api;

import junit.framework.TestCase;
import org.eclipse.jface.resource.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.test.harness.util.*;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.registry.*;

public class IEditorRegistryTest extends TestCase {
	private IEditorRegistry fReg;
	private IProject proj;	

	public IEditorRegistryTest( String testName )
	{
		super( testName );		
	}
	
	public void setUp()
	{                                                              
		fReg = PlatformUI.getWorkbench().getEditorRegistry();		
	}
	
	public void tearDown()
	{
		if( proj != null ){
			try{
				FileUtil.deleteProject( proj );
			}catch( CoreException e )
			{
				fail();
			}
		}
	}

	public void testGetFileEditorMappings()
	{
		assert( ArrayUtil.check( fReg.getFileEditorMappings() ) );
	}
	
	/**
	 *	tests both of the following:
	 *	IEditorDescriptor[] getEditors(IFile file) 
	 *	IEditorDescriptor[] getEditors(String filename)  
	 */
	public void testGetEditors() throws Throwable
	{		
		IEditorDescriptor[] editors, editors2;
		String[][] maps = {
			{"a.mock1", MockEditorPart.ID1 },
			{"b.mock2", MockEditorPart.ID2 }
		};
		
		proj = FileUtil.createProject("testProject");
		
		for( int i = 0; i < maps.length; i ++ ){
			editors = fReg.getEditors( maps[i][0] );
			assertEquals( editors.length, 1 );
			assertEquals( editors[ 0 ].getId(), maps[i][1] );
			editors2 = fReg.getEditors( FileUtil.createFile( maps[i][0], proj ) );
			assertEquals( ArrayUtil.equals( editors, editors2 ), true );
		}

		//there is no matching editor
		String fileName = IConstants.UnknownFileName[0];
		editors = fReg.getEditors( fileName );
		assertEquals( editors.length, 0 );
		editors = fReg.getEditors( FileUtil.createFile( fileName, proj ));
		assertEquals( editors.length, 0 );
	}

	public void testFindEditor() 
	{
		String id = MockEditorPart.ID1;	
		IEditorDescriptor editor = fReg.findEditor( id );		
		assertEquals( editor.getId(), id );		
		
		//editor is not found
		id = IConstants.FakeID;
		editor = fReg.findEditor( id );		
		assertNull( editor );
	}

	/**
	 * getDefaultEditor()
	 */	
	public void testGetDefaultEditor()
	{		
		assertNotNull( fReg.getDefaultEditor() );	
	}	

	/**
	 * getDefaultEditor(String fileName)
	 */
	public void testGetDefaultEditor2()
	{
		IEditorDescriptor editor = fReg.getDefaultEditor( "a.mock1" );
		assertEquals( editor.getId(), MockEditorPart.ID1 );
		
		// same extension with different name
		IEditorDescriptor editor2 = fReg.getDefaultEditor( "b.mock1" );
		assertEquals( editor, editor2 );
				
		//editor not found		
		assertNull( fReg.getDefaultEditor( IConstants.UnknownFileName[0] ) );
	}

	/**
	 * getDefaultEditor(IFile file)	
	 */
	public void testGetDefaultEditor3() throws Throwable
	{
		proj = FileUtil.createProject("testProject");
		
		IFile file = FileUtil.createFile("Whats up.bro", proj);
		String id = MockEditorPart.ID1;
		fReg.setDefaultEditor( file, id );
		IEditorDescriptor editor = fReg.getDefaultEditor( file );
		assertEquals( editor.getId(), id );
		
		//attach an IFile object with a registered extension to a different editor
		file = FileUtil.createFile("ambush.mock1", proj);
		id = MockEditorPart.ID2;
		fReg.setDefaultEditor( file, id );
		editor = fReg.getDefaultEditor( file );
		assertEquals( editor.getId(), id );
		
		//a non-registered IFile object with a registered extension
		String name = "what.mock2";
		file = FileUtil.createFile( name, proj);
		editor = fReg.getDefaultEditor( file );
		assertEquals( editor, fReg.getDefaultEditor( name ) );
		
		//a non-registered IFile object with an unregistered extension
		name = IConstants.UnknownFileName[0];
		file = FileUtil.createFile( name, proj);
		assertNull( fReg.getDefaultEditor( file ) );
	}
	
	public void testSetDefaultEditor() throws Throwable
	{
		proj = FileUtil.createProject("testProject");		
		IFile file = FileUtil.createFile("good.file", proj);

		String id = MockEditorPart.ID1;
		fReg.setDefaultEditor( file, id );	
		IEditorDescriptor editor = fReg.getDefaultEditor( file );
		assertEquals( editor.getId(), id );
		
		//change the default editor
		id = MockEditorPart.ID2;
		fReg.setDefaultEditor( file, id );	
		editor = fReg.getDefaultEditor( file );
		assertEquals( editor.getId(), id );
		
		//register the default editor with an invalid editor id
		fReg.setDefaultEditor( file, IConstants.FakeID );	
		assertNull( fReg.getDefaultEditor( file ) );		
	}
	
	/**
	 *	tests both of the following:
	 *	getImageDescriptor(IFile file)    
	 * 	getImageDescriptor(String filename)  
	 */
	public void testGetImageDescriptor() throws Throwable
	{
		proj = FileUtil.createProject("testProject");		
		
		
		IFileEditorMapping[] mappings = fReg.getFileEditorMappings();
		ImageDescriptor image1, image2; 
		String fileName;
		
		fileName = "a.mock1";
		IFile file = FileUtil.createFile(fileName, proj);
		image1 = fReg.getImageDescriptor( fileName );
		image2 = fReg.getDefaultEditor( fileName).getImageDescriptor();
		assertEquals( image1, image2 );
		//for getImageDescriptor(IFile file)
		assertEquals( image1, fReg.getImageDescriptor( file ) );
		
		//same extension, different file name
		fileName = "b.mock1";
		file = FileUtil.createFile(fileName, proj);
		assertEquals( image1, fReg.getImageDescriptor( fileName ) );
		assertEquals( image1, fReg.getImageDescriptor( file ) );
		
		//default image		
		fileName = "a.nullAndVoid";
		file = FileUtil.createFile(fileName, proj);
		image1 = fReg.getImageDescriptor( fileName );
		image2 = fReg.getImageDescriptor( "b.this_is_not_good" );				
		assertNotNull( image1 );
		assertEquals( image1, image2 );
		assertEquals( image2, fReg.getImageDescriptor( file ) );
	}
	
	public void testAddPropertyListener() throws Throwable
	{
		final String METHOD = "propertyChanged";
		
		//take out mappings from the registry and put them back right away
		//so that the event gets triggered without making change to the registry
		IFileEditorMapping[] src = fReg.getFileEditorMappings();
		FileEditorMapping[] maps = new FileEditorMapping[src.length];
		System.arraycopy( src, 0, maps, 0, src.length);

		MockPropertyListener listener = new MockPropertyListener( fReg, fReg.PROP_CONTENTS );
		fReg.addPropertyListener( listener );
		CallHistory callTrace = listener.getCallHistory();

		//multiple listener
		MockPropertyListener listener2 = new MockPropertyListener( fReg, fReg.PROP_CONTENTS );
		fReg.addPropertyListener( listener2 );
		CallHistory callTrace2 = listener2.getCallHistory();

		//fire!!		
		callTrace.clear();
		callTrace2.clear();
		((EditorRegistry)fReg).setFileEditorMappings( maps );				
		assertEquals( callTrace.contains( METHOD ), true);
		assertEquals( callTrace2.contains( METHOD ), true);
		
		//add the same listener second time
		fReg.addPropertyListener( listener );
		
		//fire!!		
		callTrace.clear();
		((EditorRegistry)fReg).setFileEditorMappings( maps );				
		//make sure the method was called only once
		assertEquals( callTrace.verifyOrder( 
			listener, new String[] { METHOD } ), true);
		
		fReg.removePropertyListener( listener );
		fReg.removePropertyListener( listener2 );
	}
	
	public void testRemovePropertyListener()
	{
		IFileEditorMapping[] src = fReg.getFileEditorMappings();
		FileEditorMapping[] maps = new FileEditorMapping[src.length];
		System.arraycopy( src, 0, maps, 0, src.length);
	
		MockPropertyListener listener = new MockPropertyListener( fReg, fReg.PROP_CONTENTS );
		fReg.addPropertyListener( listener );
		//remove the listener immediately after adding it
		fReg.removePropertyListener( listener );
		CallHistory callTrace = listener.getCallHistory();
	
		//fire!!		
		callTrace.clear();
		((EditorRegistry)fReg).setFileEditorMappings( maps );				
		assertEquals( callTrace.contains( "propertyChanged" ), false );
		
		//removing the listener that is not registered yet should have no effect
		try{
			fReg.removePropertyListener( listener );	
		}catch( Throwable e )
		{
			fail();
		}
	}
}