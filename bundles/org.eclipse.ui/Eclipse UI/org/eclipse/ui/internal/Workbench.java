package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;

import org.eclipse.core.boot.IPlatformRunnable;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.window.WindowManager;

import org.eclipse.ui.*;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.ui.internal.model.WorkbenchAdapterBuilder;
import org.eclipse.ui.internal.registry.AcceleratorConfiguration;
import org.eclipse.ui.internal.registry.AcceleratorRegistry;

/**
 * The workbench class represents the top of the ITP user interface.  Its primary
 * responsability is the management of workbench windows and other ISV windows.
 */
public class Workbench implements IWorkbench, IPlatformRunnable, IExecutableExtension {
	private static final String VERSION_STRING = "0.046"; //$NON-NLS-1$
	private static final String P_PRODUCT_INFO = "productInfo"; //$NON-NLS-1$
	private static final String DEFAULT_PRODUCT_INFO_FILENAME = "product.ini"; //$NON-NLS-1$
	private static final String DEFAULT_WORKBENCH_STATE_FILENAME = "workbench.xml"; //$NON-NLS-1$

	private WindowManager windowManager;
	private EditorHistory editorHistory;
	private PerspectiveHistory perspHistory;
	private boolean runEventLoop;
	private boolean isStarting = false;
	private boolean isClosing = false;
	private IPluginDescriptor startingPlugin; // the plugin which caused the workbench to be instantiated
	private String productInfoFilename;
	private ProductInfo productInfo;
	private PlatformInfo platformInfo;
	private String[] commandLineArgs;
	private Window.IExceptionHandler handler;
	private AcceleratorConfiguration acceleratorConfiguration;
	/**
	 * Workbench constructor comment.
	 */
	public Workbench() {
		super();
		WorkbenchPlugin.getDefault().setWorkbench(this);
	}
	/**
	 * Get the extenders from the registry and adds them to the 
	 * extender manager.
	 */
	private void addAdapters() {
		WorkbenchAdapterBuilder builder = new WorkbenchAdapterBuilder();
		builder.registerAdapters();
	}
	/**
	 * Close the workbench
	 *
	 * Assumes that busy cursor is active.
	 */
	private boolean busyClose() {
		isClosing = true;
		Platform.run(new SafeRunnableAdapter() {
			public void run() {
				XMLMemento mem = recordWorkbenchState();
				//Save the IMemento to a file.
				saveWorkbenchState(mem);
			}
			public void handleException(Throwable e) {
				if (e.getMessage() == null) {
					message = WorkbenchMessages.getString("ErrorClosingNoArg"); //$NON-NLS-1$
				} else {
					message = WorkbenchMessages.format("ErrorClosingOneArg", new Object[] { e.getMessage()}); //$NON-NLS-1$
				}

				if (!MessageDialog.openQuestion(null, WorkbenchMessages.getString("Error"), message)) //$NON-NLS-1$
					isClosing = false;
			}
		});
		if (!isClosing)
			return false;

		Platform.run(new SafeRunnableAdapter(WorkbenchMessages.getString("ErrorClosing")) { //$NON-NLS-1$
			public void run() {
				isClosing = windowManager.close();
			}
		});

		if (!isClosing)
			return false;

		if (WorkbenchPlugin.getPluginWorkspace() != null)
			disconnectFromWorkspace();

		runEventLoop = false;
		return true;
	}
	/**
	 * Opens a new workbench window and page with a specific perspective.
	 *
	 * Assumes that busy cursor is active.
	 */
	private IWorkbenchWindow busyOpenWorkbenchWindow(String perspID, IAdaptable input) throws WorkbenchException {
		// Create a workbench window (becomes active window)
		WorkbenchWindow newWindow = new WorkbenchWindow(this, getNewWindowNumber());
		newWindow.create(); // must be created before adding to window manager
		windowManager.add(newWindow);

		// Create the initial page.
		newWindow.busyOpenPage(perspID, input);

		// Open after opening page, to avoid flicker.
		newWindow.open();

		return newWindow;
	}


	/*
	 * @see IWorkbench#clonePage(IWorkbenchPage)
	 * @deprecated This experimental API will be removed.
	 */
	public IWorkbenchPage clonePage(IWorkbenchPage page) throws WorkbenchException {
		return null;
	}

	/**
	 * Closes the workbench.
	 */
	public boolean close() {
		final boolean[] ret = new boolean[1];
		BusyIndicator.showWhile(null, new Runnable() {
			public void run() {
				ret[0] = busyClose();
			}
		});
		return ret[0];
	}
	/**
	 * Connect to the core workspace.
	 */
	private void connectToWorkspace() {
		// Nothing to do right now.
	}
	/**
	 * Disconnect from the core workspace.
	 */
	private void disconnectFromWorkspace() {
		//Save the workbench.
		final MultiStatus status = new MultiStatus(WorkbenchPlugin.PI_WORKBENCH, 1, WorkbenchMessages.getString("ProblemSavingWorkbench"), null); //$NON-NLS-1$
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				try {
					status.merge(ResourcesPlugin.getWorkspace().save(true, monitor));
				} catch (CoreException e) {
					status.merge(e.getStatus());
				}
			}
		};
		try {
			new ProgressMonitorDialog(null).run(false, false, runnable);
		} catch (InvocationTargetException e) {
			status.merge(new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, 1, WorkbenchMessages.getString("InternalError"), e.getTargetException())); //$NON-NLS-1$
		} catch (InterruptedException e) {
			status.merge(new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, 1, WorkbenchMessages.getString("InternalError"), e)); //$NON-NLS-1$
		}
		ErrorDialog.openError(null, WorkbenchMessages.getString("ProblemsSavingWorkspace"), //$NON-NLS-1$
		null, status, IStatus.ERROR | IStatus.WARNING);
		if (!status.isOK()) {
			WorkbenchPlugin.log(WorkbenchMessages.getString("ProblemsSavingWorkspace"), status); //$NON-NLS-1$
		}
	}
	/**
	 * @see IWorkbench
	 */
	public IWorkbenchWindow getActiveWorkbenchWindow() {

		Display display = Display.getCurrent();
		// Display will be null if SWT has not been initialized or
		// this method was called from wrong thread.
		if (display == null)
			return null;
		Control shell = display.getActiveShell();
		while (shell != null) {
			Object data = shell.getData();
			if (data instanceof IWorkbenchWindow)
				return (IWorkbenchWindow) data;
			shell = shell.getParent();
		}
		Shell shells[] = display.getShells();
		for (int i = 0; i < shells.length; i++) {
			Object data = shells[i].getData();
			if (data instanceof IWorkbenchWindow)
				return (IWorkbenchWindow) data;
		}
		return null;
	}
	/**
	 * Returns the command line arguments, excluding any which were filtered out by the launcher.
	 */
	public String[] getCommandLineArgs() {
		return commandLineArgs;
	}
	/**
	 * Returns the editor history.
	 */
	public EditorHistory getEditorHistory() {
		if (editorHistory == null) {
			IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
			editorHistory = new EditorHistory(store.getInt(IPreferenceConstants.RECENT_FILES));
		}
		return editorHistory;
	}
	/**
	 * Returns the perspective history.
	 */
	public PerspectiveHistory getPerspectiveHistory() {
		if (perspHistory == null) {
			perspHistory = new PerspectiveHistory(getPerspectiveRegistry());
		}
		return perspHistory;
	}
	/**
	 * Returns the editor registry for the workbench.
	 *
	 * @return the workbench editor registry
	 */
	public IEditorRegistry getEditorRegistry() {
		return WorkbenchPlugin.getDefault().getEditorRegistry();
	}
	/*
	 * Returns the number for a new window.  This will be the first
	 * number > 0 which is not used to identify another window in
	 * the workbench.
	 */
	private int getNewWindowNumber() {
		// Get window list.
		Window[] windows = windowManager.getWindows();
		int count = windows.length;

		// Create an array of booleans (size = window count).  
		// Cross off every number found in the window list.  
		boolean checkArray[] = new boolean[count];
		for (int nX = 0; nX < count; nX++) {
			if (windows[nX] instanceof WorkbenchWindow) {
				WorkbenchWindow ww = (WorkbenchWindow) windows[nX];
				int index = ww.getNumber() - 1;
				if (index >= 0 && index < count)
					checkArray[index] = true;
			}
		}

		// Return first index which is not used.
		// If no empty index was found then every slot is full.
		// Return next index.
		for (int index = 0; index < count; index++) {
			if (!checkArray[index])
				return index + 1;
		}
		return count + 1;
	}
	/**
	 * Returns the perspective registry for the workbench.
	 *
	 * @return the workbench perspective registry
	 */
	public IPerspectiveRegistry getPerspectiveRegistry() {
		return WorkbenchPlugin.getDefault().getPerspectiveRegistry();
	}
	/**
	 * Returns the preference manager for the workbench.
	 *
	 * @return the workbench preference manager
	 */
	public PreferenceManager getPreferenceManager() {
		return WorkbenchPlugin.getDefault().getPreferenceManager();
	}
	/**
	 * @return the product info object
	 */
	public ProductInfo getProductInfo() {
		return productInfo;
	}
	/**
	 * @return the platform info object
	 */
	public PlatformInfo getPlatformInfo() {
		return platformInfo;
	}
	/**
	 * Returns the shared images for the workbench.
	 *
	 * @return the shared image manager
	 */
	public ISharedImages getSharedImages() {
		return WorkbenchPlugin.getDefault().getSharedImages();
	}
	/* (non-Javadoc)
	 * Method declared on IWorkbench.
	 */
	public IMarkerHelpRegistry getMarkerHelpRegistry() {
		return WorkbenchPlugin.getDefault().getMarkerHelpRegistry();
	}
	/*
	 * Return the current window manager being used by the workbench
	 */
	protected WindowManager getWindowManager() {
		return windowManager;
	}
	/*
	 * Return the active AcceleratorConfiguration
	 */
	public AcceleratorConfiguration getActiveAcceleratorConfiguration() {
		return acceleratorConfiguration;
	}
	/*
	 * Answer the workbench state file.
	 */
	private File getWorkbenchStateFile() {
		IPath path = WorkbenchPlugin.getDefault().getStateLocation();
		path = path.append(DEFAULT_WORKBENCH_STATE_FILENAME);
		return path.toFile();
	}
	/**
	 * Returns the workbench window count.
	 * <p>
	 * @return the workbench window count
	 */
	public int getWorkbenchWindowCount() {
		return windowManager.getWindows().length;
	}
	/**
	 * @see IWorkbench
	 */
	public IWorkbenchWindow[] getWorkbenchWindows() {
		Window[] windows = windowManager.getWindows();
		IWorkbenchWindow[] dwindows = new IWorkbenchWindow[windows.length];
		System.arraycopy(windows, 0, dwindows, 0, windows.length);
		return dwindows;
	}
	/**
	 * Initializes the workbench.
	 *
	 * @return true if init succeeded.
	 */
	private boolean init(String[] commandLineArgs) {
		isStarting = true;

		this.commandLineArgs = commandLineArgs;
		if (WorkbenchPlugin.getDefault().isDebugging()) {
			WorkbenchPlugin.DEBUG = true;
			ModalContext.setDebugMode(true);
		}
		initializeProductImage();
		connectToWorkspace();
		addAdapters();
		windowManager = new WindowManager();
		WorkbenchColors.startup();
		initializeFonts();
		initializeAcceleratorConfiguration();

		// deadlock code
		boolean avoidDeadlock = true;
		for (int i = 0; i < commandLineArgs.length; i++) {
			if (commandLineArgs[i].equalsIgnoreCase("-allowDeadlock")) //$NON-NLS-1$
				avoidDeadlock = false;
		}
		if (avoidDeadlock) {
			try {
				Display display = Display.getCurrent();
				UIWorkspaceLock uiLock = new UIWorkspaceLock(WorkbenchPlugin.getPluginWorkspace(), display);
				WorkbenchPlugin.getPluginWorkspace().setWorkspaceLock(uiLock);
				display.setSynchronizer(new UISynchronizer(display, uiLock));
			} catch (CoreException e) {
				e.printStackTrace(System.out);
			}
		}

		openWindows();
		openWelcomeDialog();

		isStarting = false;
		return true;
	}
	/**
	 * Initialize the workbench AcceleratorConfiguration with the stored values.
	 */
	private void initializeAcceleratorConfiguration() {
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		String id = store.getString(IWorkbenchConstants.ACCELERATOR_CONFIGURATION_ID);
		if (id == null)
			id = IWorkbenchConstants.DEFAULT_ACCELERATOR_CONFIGURATION_ID;
		AcceleratorRegistry registry = WorkbenchPlugin.getDefault().getAcceleratorRegistry();
		acceleratorConfiguration = registry.getConfiguration(id);
		if(acceleratorConfiguration!=null)
			acceleratorConfiguration.initializeScopes();
	}
	/**
	 * Initialize the workbench fonts with the stored values.
	 */
	private void initializeFonts() {
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		FontRegistry registry = JFaceResources.getFontRegistry();
		initializeFont(JFaceResources.DIALOG_FONT, registry, store);
		initializeFont(JFaceResources.BANNER_FONT, registry, store);
		initializeFont(JFaceResources.HEADER_FONT, registry, store);
		initializeFont(JFaceResources.TEXT_FONT, registry, store);
	}
	/**
	 * Initialize the specified font with the stored value.
	 */
	private void initializeFont(String fontKey, FontRegistry registry, IPreferenceStore store) {
		if (store.isDefault(fontKey))
			return;
		FontData[] font = PreferenceConverter.getFontDataArray(store, fontKey);
		registry.put(fontKey, font);
	}
	/**
	 * Initialize the product image obtained from the product info file
	 */
	private void initializeProductImage() {
		ImageDescriptor descriptor = getProductInfo().getProductImageDescriptor();
		if (descriptor == null) {
			// if none was supplied we use a default
			URL path = null;
			try {
				path = new URL(WorkbenchPlugin.getDefault().getDescriptor().getInstallURL(), WorkbenchImages.ICONS_PATH + "obj16/prod.gif"); //$NON-NLS-1$
			} catch (MalformedURLException e) {
			};
			descriptor = ImageDescriptor.createFromURL(path);
		}
		WorkbenchImages.getImageRegistry().put(IWorkbenchGraphicConstants.IMG_OBJS_DEFAULT_PROD, descriptor);
		Image image = WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_OBJS_DEFAULT_PROD);
		if (image != null) {
			Window.setDefaultImage(image);
		}
	}
	/**
	 * Returns true if the workbench is in the process of closing
	 */
	public boolean isClosing() {
		return isClosing;
	}
	/**
	 * Returns true if the workbench is in the process of starting
	 */
	public boolean isStarting() {
		return isStarting;
	}
	/*
	 * Create the initial workbench window.
	 * @return true if the open succeeds
	 */
	private void openFirstTimeWindow() {
		// Create the window.
		WorkbenchWindow newWindow = new WorkbenchWindow(this, getNewWindowNumber());
		newWindow.create();
		windowManager.add(newWindow);

		// Create the initial page.
		try {
			IContainer root = WorkbenchPlugin.getPluginWorkspace().getRoot();
			newWindow.openPage(getPerspectiveRegistry().getDefaultPerspective(), root);
		} catch (WorkbenchException e) {
			MessageDialog.openError(newWindow.getShell(), WorkbenchMessages.getString("Problems_Opening_Page"), //$NON-NLS-1$
			e.getMessage());
		}
		newWindow.open();
	}
	/*
	 * Create the workbench UI from a persistence file.
	 */
	private boolean openPreviousWorkbenchState() {
		// Read the workbench state file.
		final File stateFile = getWorkbenchStateFile();
		// If there is no state file return false.
		if (!stateFile.exists())
			return false;

		final boolean result[] = { true };
		Platform.run(new SafeRunnableAdapter(WorkbenchMessages.getString("ErrorReadingState")) { //$NON-NLS-1$
			public void run() throws Exception {
				FileInputStream input = new FileInputStream(stateFile);
				InputStreamReader reader = new InputStreamReader(input, "utf-8");
				// Restore the workbench state.
				IMemento memento = XMLMemento.createReadRoot(reader);
				String version = memento.getString(IWorkbenchConstants.TAG_VERSION);
				if ((version == null) || (!version.equals(VERSION_STRING))) {
					reader.close();
					MessageDialog.openError((Shell) null, WorkbenchMessages.getString("Restoring_Problems"), //$NON-NLS-1$
					WorkbenchMessages.getString("Invalid_workbench_state_ve")); //$NON-NLS-1$
					stateFile.delete();
					result[0] = false;
					return;
				}
				restoreState(memento);
				reader.close();
			}
			public void handleException(Throwable e) {
				super.handleException(e);
				result[0] = false;
				stateFile.delete();
			}

		});
		return result[0];
	}
	/**
	 * Open the Welcome dialog
	 */
	private void openWelcomeDialog() {
		// See if a welcome page is specified
		ProductInfo info = ((Workbench) PlatformUI.getWorkbench()).getProductInfo();
		URL url = info.getWelcomePageURL();
		if (url == null)
			return;

		// Show the quick start wizard the first time the workbench opens.
		if (WorkbenchPlugin.getDefault().getPreferenceStore().getBoolean(IPreferenceConstants.WELCOME_DIALOG)) {
			QuickStartAction action = new QuickStartAction(this);
			action.run();
			// Don't show it again
			WorkbenchPlugin.getDefault().getPreferenceStore().setValue(IPreferenceConstants.WELCOME_DIALOG, false);
		}

	}
	/*
	 * Open the workbench UI. 
	 */
	private void openWindows() {
		if (!openPreviousWorkbenchState())
			openFirstTimeWindow();
	}
	
	/**
	 * Opens a new page with the default perspective.
	 * The "Open Perspective" and "Reuse Perspective" preferences are consulted and implemented.
	 * @deprecated This experimental API will be removed.
	 */
	public IWorkbenchPage openPage(IAdaptable input) throws WorkbenchException {
		return showPerspective(getPerspectiveRegistry().getDefaultPerspective(), getActiveWorkbenchWindow(), input);
	}
	/**
	 * Opens a new workbench page with a specified perspective and input.
	 * The "Open Perspective" and "Reuse Perspective" preferences are consulted and implemented.
	 * @deprecated This experimental API will be removed.
	 */
	public IWorkbenchPage openPage(String perspId, IAdaptable input, int keyStateMask) throws WorkbenchException {
		return showPerspective(perspId, getActiveWorkbenchWindow(), input);
	}
	/**
	 * Return the alternate mask for this platform. It is control on win32 and
	 * shift alt on other platforms. 
	 * @return int
	 */
	private int alternateMask() {
		if (SWT.getPlatform().equals("win32")) //$NON-NLS-1$
			return SWT.CONTROL;
		else
			return SWT.ALT | SWT.SHIFT;
	}
	/**
	 * Opens a new window and page with the default perspective.
	 */
	public IWorkbenchWindow openWorkbenchWindow(IAdaptable input) throws WorkbenchException {
		return openWorkbenchWindow(getPerspectiveRegistry().getDefaultPerspective(), input);
	}
	/**
	 * Opens a new workbench window and page with a specific perspective.
	 */
	public IWorkbenchWindow openWorkbenchWindow(final String perspID, final IAdaptable input) throws WorkbenchException {
		// Run op in busy cursor.
		final Object[] result = new Object[1];
		BusyIndicator.showWhile(null, new Runnable() {
			public void run() {
				try {
					result[0] = busyOpenWorkbenchWindow(perspID, input);
				} catch (WorkbenchException e) {
					result[0] = e;
				}
			}
		});
		if (result[0] instanceof IWorkbenchWindow)
			return (IWorkbenchWindow) result[0];
		else if (result[0] instanceof WorkbenchException)
			throw (WorkbenchException) result[0];
		else
			throw new WorkbenchException(WorkbenchMessages.getString("Abnormal_Workbench_Conditi")); //$NON-NLS-1$
	}
	
	/**
	 * Reads the platform and product info.
	 * This info contains the platform and product name, product images,
	 * copyright etc.
	 *
	 * @return true if the method succeeds
	 */
	private boolean readPlatformAndProductInfo() {
		platformInfo = new PlatformInfo();
		productInfo = new ProductInfo();

		boolean success = true;

		try {
			platformInfo.readINIFile();
		} catch (CoreException e) {
			WorkbenchPlugin.log("Error reading platform info file", e.getStatus()); //$NON-NLS-1$
			success = false;
		}

		try {
			productInfo.readINIFile();
		} catch (CoreException e) {
			WorkbenchPlugin.log("Error reading product info file", e.getStatus()); //$NON-NLS-1$
			success = false;
		}

		return success;
	}

	/**
	 * Record the workbench UI in a document
	 */
	private XMLMemento recordWorkbenchState() {
		XMLMemento memento = XMLMemento.createWriteRoot(IWorkbenchConstants.TAG_WORKBENCH);
		saveState(memento);
		return memento;
	}
	/**
	 * @see IPersistable
	 */
	public void restoreState(IMemento memento) {
		// Read perspective history.
		// This must be done before we recreate the windows, because it is
		// consulted during the recreation.
		IMemento childMem = memento.getChild(IWorkbenchConstants.TAG_PERSPECTIVE_HISTORY);
		if (childMem != null)
			getPerspectiveHistory().restoreState(childMem);

		IMemento mruMemento = memento.getChild(IWorkbenchConstants.TAG_MRU_LIST); //$NON-NLS-1$
		if (mruMemento != null)
			getEditorHistory().restoreState(mruMemento);

		// Get the child windows.
		IMemento[] children = memento.getChildren(IWorkbenchConstants.TAG_WINDOW);

		// Read the workbench windows.
		for (int x = 0; x < children.length; x++) {
			childMem = children[x];
			WorkbenchWindow newWindow = new WorkbenchWindow(this, getNewWindowNumber());
			newWindow.create();
			newWindow.restoreState(childMem);
			windowManager.add(newWindow);
			newWindow.open();
		}
	}
	/**
	 * Runs the workbench.
	 */
	public Object run(Object arg) {
		String[] commandLineArgs = new String[0];
		if (arg != null && arg instanceof String[])
			commandLineArgs = (String[]) arg;
		if (!readPlatformAndProductInfo())
			return null;
		if (getProductInfo().getAppName() != null)
			Display.setAppName(getProductInfo().getAppName());
		Display display = new Display();
		//Workaround for 1GEZ9UR and 1GF07HN
		display.setWarnings(false);
		try {
			handler = new ExceptionHandler(this);
			Window.setExceptionHandler(handler);
			boolean initOK = init(commandLineArgs);
			Platform.endSplash();
			if (initOK) {
				runEventLoop();
			}
			shutdown();
		} finally {
			if (!display.isDisposed())
			  display.dispose();
		}
		return null;
	}
	/**
	 * run an event loop for the workbench.
	 */
	protected void runEventLoop() {
		Display display = Display.getCurrent();
		runEventLoop = true;
		while (runEventLoop) {
			try {
				if (!display.readAndDispatch())
					display.sleep();
			} catch (Throwable t) {
				handler.handleException(t);
			}
		}
	}
	/**
	 * @see IPersistable
	 */
	public void saveState(IMemento memento) {
		// Save the version number.
		memento.putString(IWorkbenchConstants.TAG_VERSION, VERSION_STRING);

		// Save the workbench windows.
		IWorkbenchWindow[] windows = getWorkbenchWindows();
		for (int nX = 0; nX < windows.length; nX++) {
			WorkbenchWindow window = (WorkbenchWindow) windows[nX];
			IMemento childMem = memento.createChild(IWorkbenchConstants.TAG_WINDOW);
			window.saveState(childMem);
		}
		getEditorHistory().saveState(memento.createChild(IWorkbenchConstants.TAG_MRU_LIST)); //$NON-NLS-1$
		// Save perspective history.
		getPerspectiveHistory().saveState(memento.createChild(IWorkbenchConstants.TAG_PERSPECTIVE_HISTORY)); //$NON-NLS-1$
	}
	/**
	 * Save the workbench UI in a persistence file.
	 */
	private boolean saveWorkbenchState(XMLMemento memento) {
		// Save it to a file.
		File stateFile = getWorkbenchStateFile();
		try {
			FileOutputStream stream = new FileOutputStream(stateFile);
			OutputStreamWriter writer = new OutputStreamWriter(stream, "utf-8"); //$NON-NLS-1$
			memento.save(writer);
			writer.close();
		} catch (IOException e) {
			stateFile.delete();
			MessageDialog.openError((Shell) null, WorkbenchMessages.getString("SavingProblem"), //$NON-NLS-1$
			WorkbenchMessages.getString("ProblemSavingState")); //$NON-NLS-1$
			return false;
		}

		// Success !
		return true;
	}
	/*
	 * Sets the active accelerator configuration to be the configuration
	 * with the given id.
	 */
	public void setActiveAcceleratorConfiguration(AcceleratorConfiguration config) {
		if(config!=null) {
			acceleratorConfiguration = config;	
			acceleratorConfiguration.initializeScopes();
		}
	}
	/**
	 * @see IExecutableExtension
	 */
	public void setInitializationData(IConfigurationElement configElement, String propertyName, Object data) {
		startingPlugin = configElement.getDeclaringExtension().getDeclaringPluginDescriptor();
		productInfoFilename = (String) ((Map) data).get(P_PRODUCT_INFO);
	}
	/* (non-Javadoc)
	 * Method declared on IWorkbench.
	 */
	public IWorkbenchPage showPerspective(String perspectiveId, IWorkbenchWindow window)
		throws WorkbenchException
	{
		Assert.isNotNull(perspectiveId);
		
		// If the specified window has the requested perspective open, then the window
		// is given focus and the perspective is shown. Page's input is ignored.
		WorkbenchWindow win = (WorkbenchWindow) window;
		if (win != null) {
			WorkbenchPage page = win.getActiveWorkbenchPage();
			if (page != null) {
				Iterator enum = page.getOpenedPerspectives();
				while (enum.hasNext()) {
					Perspective persp = (Perspective) enum.next();
					if (perspectiveId.equals(persp.getDesc().getId())) {
						win.getShell().open();
						page.setPerspective(persp.getDesc());
						return page;
					}
				}
			}
		}
		
		// If another window that has the workspace root as input and the requested
		// perpective open and active, then the window is given focus.
		IAdaptable input = WorkbenchPlugin.getPluginWorkspace().getRoot();
		IWorkbenchWindow[] windows = getWorkbenchWindows();
		for (int i = 0; i < windows.length; i++) {
			win = (WorkbenchWindow) windows[i];
			if (window != win) {
				WorkbenchPage page = win.getActiveWorkbenchPage();
				if (page != null) {
					boolean inputSame = false;
					if (input == null)
						inputSame = (page.getInput() == null);
					else
						inputSame = input.equals(page.getInput());
					if (inputSame) {
						Perspective persp = page.getActivePerspective();
						if (perspectiveId.equals(persp.getDesc().getId())) {
							win.getShell().open();
							return page;
						}
					}
				}
			}
		}
			
		// Otherwise the requested perspective is opened and shown in the specified
		// window, and the window is given focus.
		win = (WorkbenchWindow) window;
		if (win != null) {
			IWorkbenchPage page = win.getActiveWorkbenchPage();
			IPerspectiveDescriptor desc = getPerspectiveRegistry().findPerspectiveWithId(perspectiveId);
			if (desc == null)
				throw new WorkbenchException(WorkbenchMessages.getString("WorkbenchPage.ErrorRecreatingPerspective")); //$NON-NLS-1$
			win.getShell().open();
			if (page == null)
				page = win.openPage(perspectiveId, input);
			else
				page.setPerspective(desc);
			return page;
		}

		// Just throw an exception....
		throw new WorkbenchException(WorkbenchMessages.format("Workbench.showPerspectiveError", new Object[] { perspectiveId })); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * Method declared on IWorkbench.
	 */
	public IWorkbenchPage showPerspective(String perspectiveId, IWorkbenchWindow window, IAdaptable input) 
		throws WorkbenchException
	{
		Assert.isNotNull(perspectiveId);

		// If the specified window has the requested perspective open and the same requested
		// input, then the window is given focus and the perspective is shown.
		boolean inputSameAsWindow = false;
		WorkbenchWindow win = (WorkbenchWindow) window;
		if (win != null) {
			WorkbenchPage page = win.getActiveWorkbenchPage();
			if (page != null) {
				boolean inputSame = false;
				if (input == null)
					inputSame = (page.getInput() == null);
				else
					inputSame = input.equals(page.getInput());
				if (inputSame) {
					inputSameAsWindow = true;
					Iterator enum = page.getOpenedPerspectives();
					while (enum.hasNext()) {
						Perspective persp = (Perspective) enum.next();
						if (perspectiveId.equals(persp.getDesc().getId())) {
							win.getShell().open();
							page.setPerspective(persp.getDesc());
							return page;
						}
					}
				}
			}
		}
		
		// If another window has the requested input and the requested
		// perpective open and active, then that window is given focus.
		IWorkbenchWindow[] windows = getWorkbenchWindows();
		for (int i = 0; i < windows.length; i++) {
			win = (WorkbenchWindow) windows[i];
			if (window != win) {
				WorkbenchPage page = win.getActiveWorkbenchPage();
				if (page != null) {
					boolean inputSame = false;
					if (input == null)
						inputSame = (page.getInput() == null);
					else
						inputSame = input.equals(page.getInput());
					if (inputSame) {
						Perspective persp = page.getActivePerspective();
						if (perspectiveId.equals(persp.getDesc().getId())) {
							win.getShell().open();
							return page;
						}
					}
				}
			}
		}

		// If the specified window has the same requested input but not the requested
		// perspective, then the window is given focus and the perspective is opened and shown.
		if (inputSameAsWindow) {
			WorkbenchPage page = win.getActiveWorkbenchPage();
			if (page != null) {
				IPerspectiveDescriptor desc = getPerspectiveRegistry().findPerspectiveWithId(perspectiveId);
				if (desc == null)
					throw new WorkbenchException(WorkbenchMessages.getString("WorkbenchPage.ErrorRecreatingPerspective")); //$NON-NLS-1$
				win.getShell().open();
				page.setPerspective(desc);
				return page;
			}			
		}
		
		// Otherwise the requested perspective is opened and shown in a new window, and the
		// window is given focus.
		IWorkbenchWindow newWindow = openWorkbenchWindow(perspectiveId, input);
		return newWindow.getActivePage();
	}
	
	/**
	 * shutdown the application.
	 */
	private void shutdown() {
		WorkbenchColors.shutdown();
	}
	/*
	 * Answer true if the state file is good.
	 */
	private boolean testStateFile() {
		// If there is no state file return false.
		File stateFile = getWorkbenchStateFile();
		if (!stateFile.exists())
			return false;

		// There is a file.  Look for a version tag in the first few lines.
		boolean bVersionTagFound = false;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(stateFile));
			for (int i = 0; i < 3; i++) {
				String line = reader.readLine();
				if (line != null && line.indexOf(VERSION_STRING) >= 0) {
					bVersionTagFound = true;
					break;
				}
			}
			reader.close();
		} catch (IOException e) {
			bVersionTagFound = false;
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e2) {
				}
			}
		}

		// If the version string was found return true, else show an error and return false.
		if (bVersionTagFound) {
			return true;
		} else {
			stateFile.delete();
			MessageDialog.openError((Shell) null, WorkbenchMessages.getString("Restoring_Problem"), //$NON-NLS-1$
			WorkbenchMessages.getString("ErrorReadingWorkbenchState")); //$NON-NLS-1$
			return false;
		}
	}

	/**
	 * Saves the current state of the workbench to a file.
	 */
	public boolean saveSnapshot(File stateFile) {
		XMLMemento memento = recordWorkbenchState();
		try {
			FileOutputStream stream = new FileOutputStream(stateFile);
			OutputStreamWriter writer = new OutputStreamWriter(stream, "utf-8");
			memento.save(writer);
			writer.close();
		} catch (IOException e) {
			stateFile.delete();
			MessageDialog.openError((Shell) null, WorkbenchMessages.getString("SavingProblem"), //$NON-NLS-1$
			WorkbenchMessages.getString("ProblemSavingState")); //$NON-NLS-1$
			return false;
		}

		// Success !
		return true;
	}

	/**
	 * Restores the state of the workbench from a file.
	 * All existing windows are closed.
	 * <p>
	 * This method creates an async runnable and returns before
	 * the runnable executes.  This allows the caller to backtrack into
	 * the event loop before any windows are closed.
	 * </p>
	 */
	public boolean restoreSnapshot(final File stateFile) {
		Display disp = Display.getCurrent();
		disp.asyncExec(new Runnable() {
			public void run() {
				asyncRestoreSnapshot(stateFile);
			}
		});
		return true;
	}

	/**
	 * Restores the state of the workbench from a file.
	 * All existing windows are closed.
	 */
	private boolean asyncRestoreSnapshot(final File stateFile) {
		if (!stateFile.exists())
			return false;

		// Close the existing windows.
		isClosing = true;
		isClosing = windowManager.close();
		if (!isClosing)
			return false;
		isClosing = false;

		try {
			FileInputStream input = new FileInputStream(stateFile);
			InputStreamReader reader = new InputStreamReader(input, "utf-8");
			// Restore the workbench state.
			IMemento memento = XMLMemento.createReadRoot(reader);
			String version = memento.getString(IWorkbenchConstants.TAG_VERSION);
			if ((version == null) || (!version.equals(VERSION_STRING))) {
				reader.close();
				MessageDialog.openError((Shell) null, WorkbenchMessages.getString("Restoring_Problems"), //$NON-NLS-1$
				WorkbenchMessages.getString("Invalid_workbench_state_ve")); //$NON-NLS-1$
				stateFile.delete();
				return false;
			}
			restoreState(memento);
			reader.close();
			return true;
		} catch (Exception e) {
			stateFile.delete();
			return false;
		}
	}

	/**
	 * Creates the action delegate for each action extension contributed by
	 * a particular plugin.  The delegates are only created if the
	 * plugin itself has been activated.
	 * 
	 * @param pluginId the plugin id.
	 */
	public void refreshPluginActions(String pluginId) {
		WWinPluginAction.refreshActionList();
	}
}