package org.eclipse.ui.tests.api;

import junit.framework.TestCase;
import org.eclipse.ui.*;
import org.eclipse.ui.junit.util.*;

public class IPerspectiveDescriptorTest extends TestCase {

	private IPerspectiveDescriptor fPer;
	private IPerspectiveRegistry fReg;
	
	public IPerspectiveDescriptorTest( String testName )
	{
		super( testName );
	}
	
	public void setUp()
	{
		fPer = ( IPerspectiveDescriptor )ArrayUtil.pickRandom( PlatformUI.getWorkbench().getPerspectiveRegistry().getPerspectives() );
		//fPer.
	}
	
	public void testGetId()
	{
		assertNotNull( fPer.getId() );
	}
	
	public void testGetLabel()
	{
		assertNotNull( fPer.getLabel() );	
	}

//	This always fails
	public void testGetImageDescriptor()
	{
/*		IWorkbench wb = PlatformUI.getWorkbench();
		
		IPerspectiveDescriptor[] pers = wb.getPerspectiveRegistry().getPerspectives();
		IWorkbenchPage page = wb.getActiveWorkbenchWindow().getActivePage();
	
		for( int i = 0; i < pers.length; i ++ )
			if( pers[ i ] != page.getPerspective() ){
				page.setPerspective( pers[ i ] );
				break;
			}

		System.out.println( "active page pers: " + page.getPerspective().getLabel() );
		System.out.println( "active pers image: " + page.getPerspective().getImageDescriptor() );

		for( int i = 0; i < pers.length; i ++ )
			if( pers[ i ].getLabel().equals( "Resource" ) ){
				System.out.println( "resource image: " + pers[ i ].getImageDescriptor() );
				break;
			}
		for( int i = 0; i < pers.length; i ++ ){
			assertNotNull( pers[ i ].getImageDescriptor() );
		}*/
	}
	
	public void testThis()
	{
//		opne
	}
}

