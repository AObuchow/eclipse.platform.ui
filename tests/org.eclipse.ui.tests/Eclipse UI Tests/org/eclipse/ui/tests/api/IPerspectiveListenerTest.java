package org.eclipse.ui.tests.api;

import org.eclipse.ui.*;
import junit.framework.*;
import org.eclipse.ui.junit.util.*;

public class IPerspectiveListenerTest extends UITestCase implements IPerspectiveListener {
	private int fEvent;
	private IWorkbenchWindow fWindow;
	private IWorkbenchPage fPageMask;
	private IPerspectiveDescriptor fPerMask;
	
	/**
	 * bit masks for events
	 */
	public static final int 
		NONE = 0x00,
		OPEN = 0x01,
		CLOSED = 0x02,
		ACTIVATED = 0x04,
		CHANGED = 0x08;
	
	public IPerspectiveListenerTest( String testName )
	{
		super( testName );
	}
	
	public void setUp()
	{
		fEvent = NONE;		
		fWindow = openTestWindow();		
		fWindow.addPerspectiveListener( this );
	}
	
	public void tearDown()
	{
		fWindow.removePerspectiveListener( this );
	}
	
	public void testPerspectiveActivated()
	{
		fPageMask = fWindow.getActivePage();
		fPerMask = fWorkbench.getPerspectiveRegistry().findPerspectiveWithId(EmptyPerspective.PERSP_ID );
		fPageMask.setPerspective( fPerMask );
		
		assertEquals( isActivated( fEvent ), true );
	}

	public void testPerspectiveChanged()
	{
		fPageMask = fWindow.getActivePage();
		fPerMask = fWorkbench.getPerspectiveRegistry().findPerspectiveWithId(EmptyPerspective.PERSP_ID );
		fPageMask.setPerspective( fPerMask );
		
		assertEquals( isActivated( fEvent ), true );
	}
	
	/**
	 * @see IPerspectiveListener#perspectiveActivated(IWorkbenchPage, IPerspectiveDescriptor)
	 */
	public void perspectiveActivated( IWorkbenchPage page, IPerspectiveDescriptor perspective ) 
	{
		if( page == fPageMask && perspective == fPerMask )
			fEvent |= ACTIVATED;
	}

	/**
	 * @see IPerspectiveListener#perspectiveChanged(IWorkbenchPage, IPerspectiveDescriptor, String)
	 */
	public void perspectiveChanged( IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) 
	{
		if( page == fPageMask && perspective == fPerMask )
			fEvent |= CHANGED;
	}
	
	public static boolean isOpen( int bits )
	{
		return ( ( bits & OPEN ) != 0 );		
	}

	public static boolean isClosed( int bits )
	{
		return ( ( bits & CLOSED ) != 0 );		
	}
	
	public static boolean isActivated( int bits )
	{
		return ( ( bits & ACTIVATED ) != 0 );		
	}
	
	public static boolean isChanged( int bits )
	{
		return( ( bits & CLOSED ) != 0 );
	}
}