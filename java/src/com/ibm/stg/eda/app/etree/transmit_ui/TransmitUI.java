/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2013 -- IBM Internal Use Only
*
*=============================================================================
*
* CREATOR: Gregg Stadtlander
*
*-PURPOSE---------------------------------------------------------------------
* User interface class to capture Transmit data
*-----------------------------------------------------------------------------
*
*-CHANGE LOG------------------------------------------------------------------
* 10/31/2013 GFS  Initial coding.
* 05/01/2014 GFS  Added support ofr build and transmit ready states
* 06/27/2014 GFS  Updated to auto detect advance src/dest locations from the TK
* 02/02/2015 GFS  Updated new pkg processing detection algorithm so new TKs
*                 will not need to be added each time they are released.
* 06/08/2015 GFS  Fixed a bug in isNewProcess() where 14.1.10 was determined 
*                 to NOT be the new process because "10" is less than "6" when
*                 doing string compares.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.app.etree.transmit_ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import com.ibm.stg.eda.component.jfacebase.ExitAction;
import com.ibm.stg.eda.component.jfacebase.GridUtils;
import com.ibm.stg.eda.component.jfacebase.ShowAboutAction;
import com.ibm.stg.eda.component.jfacebase.ShowHelpAction;
import com.ibm.stg.eda.component.tk_patch.TkInjectUtils;
import com.ibm.stg.eda.component.tk_patch.TkPatch;
import com.ibm.stg.eda.component.tk_patch.TkXmittal;
import com.ibm.stg.iipmds.common.Constants;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofFile;
import com.ibm.stg.iipmds.common.IcofSystemUtil;
import com.ibm.stg.iipmds.common.SessionLog;
import com.ibm.stg.iipmds.icof.component.clearquest.cqFetchOutput;
import com.ibm.stg.iipmds.icof.component.mom.AppContext;
import com.ibm.stg.iipmds.icof.component.util.IcofUtil;
import com.ibm.stg.iipmds.icof.webclient.clearquest.CqClientUtil;
import com.ibm.stg.iipmds.icof.webclient.clearquest.CqService_PortType;
import com.ibm.stg.iipmds.icof.webclient.clearquest.CqService_PortTypeProxy;

public class TransmitUI extends ApplicationWindow {

    /**
     * Constants.
     */
    final static String APP_NAME = "pkg.all";
    final static String APP_VERSION = "1.2";
    final static String APP_OWNER = "Gregg Stadtlander";
    final static String HELP_FILE = "help_text/package_help.txt";
    final static String PROD = "/afs/eda/data/edainfra/tools/enablement/prod/bin";
    final static String DEV = "/afs/eda/data/edainfra/tools/enablement/dev/bin";
    final static String ADVANCE = PROD + File.separator + "svnAdvance";
    final static String TRANSMIT = PROD + File.separator + "transmit";
    final static String PACKAGE = PROD + File.separator + "pkg.create";
    final static String TK_SHOW = PROD + File.separator + "tk.show";
    final static String AUTHORIZED_ID = "svnlib@us.ibm.com";
    
    
    /**
     * Constructor
     */
    public TransmitUI() {
      super(null);
      setMenuActions();
    }

    
    /**
     * Runs the application
     */
    public void run() {

        // Don't return from open() until window closes
        setBlockOnOpen(true);

        // Open the main window
        //addStatusLine();
        addMenuBar();
        open();

        // Dispose the display
        Display.getCurrent().dispose();
    }
 
    
    /**
     * Creates the main window's status line.
     * 
     * @return StatusLineManger
     */
    protected StatusLineManager createStatusLineManager() {
        return new StatusLineManager();
    }


    /**
     * Creates the main window's menu bar contents.
     * 
     * @return MenuManger
     */
    protected MenuManager createMenuManager() {
        MenuManager menuBar = new MenuManager();

        MenuManager fileMenu = new MenuManager("&File");
        fileMenu.add(exitAction);
        menuBar.add(fileMenu);

        MenuManager helpMenu = new MenuManager("&Help");
        helpMenu.add(showHelpAction);
        helpMenu.add(aboutAction);
        menuBar.add(helpMenu);
        
        return menuBar;
    }

    
    /**
     * Creates the main window's contents
     * 
     * @param parent the main window
     * @return Control
     */
    protected Control createContents(Composite parent) {

        shell = parent.getShell();
        shell.setText("EDA Tool Kit Patches");
        shell.setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        display = shell.getDisplay();

        // Define the Grid layout
        Composite gridComposite = new Composite(shell, SWT.BORDER);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = GRID_COLS_WIDE;
        gridComposite.setLayout(gridLayout);
        
        ltGreenColor = new Color(Display.getCurrent(), new RGB(84, 245, 78));
        ltBlueColor = new Color(Display.getCurrent(), new RGB(188, 219, 245));
        //gridComposite.setBackground(ltBlueColor);

        // Create the Patch widgets.
        setPatchTable(gridComposite, GRID_COLS_WIDE);

        // Create the Filter widgets.
        setFilters(gridComposite, GRID_COLS_WIDE);
        
        // Create the Action widgets.
        setActions(gridComposite, GRID_COLS_WIDE);
                
        // Create the Console widget.
        setConsoleDisplay(gridComposite, GRID_COLS_WIDE);
        
        // Initialize widget contents.
        enableCqButton(true);
        enableActionButtons(false);
         
        return parent;
        
    }


    /**
     * Define and layout the action buttons
     *
     * @param gridComposite
     * @param gridColsWide
     */
    private void setActions(Composite comp, int gridWidth) {

	// Create the group.
        Group group = new Group(comp, SWT.SHADOW_IN);
        group.setText("Actions");

        // Make it have a grid layout
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = gridWidth;
        group.setLayout(gridLayout);

        // Place the group in the composite.
        GridData data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        data.horizontalSpan = gridWidth;
        group.setLayoutData(data);
        
        // Create the Advance checkbox button
        advanceButton = new Button(group, SWT.CHECK);
        advanceButton.setText("Advance");
        advanceButton.setEnabled(true);

        data = new GridData();
        data.horizontalSpan = 1;
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        advanceButton.setLayoutData(data);

        // Create the Package/Transmit button
        packageButton = new Button(group, SWT.CHECK);
        packageButton.setText("Package Dels");
        packageButton.setEnabled(true);

        data = new GridData();
        data.horizontalSpan = 1;
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        packageButton.setLayoutData(data);

        // Create the label.
        Label thisLabel = new Label(group, SWT.NONE);
        thisLabel.setText("ClearQuest Action");

        data = new GridData();
        data.horizontalSpan = 1;
        thisLabel.setLayoutData(data);

        // CQ Action combo
        cqActionCombo = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY);
        String[] items =  {"", "Available", "BuildReady", "TransmitReady"};
        cqActionCombo.setItems(items);
        //cqActionCombo.setBackground(ltBlueColor);
        cqActionCombo.addSelectionListener(new SelectionListener() {
          public void widgetSelected(SelectionEvent e) {
              if (cqActionCombo.getText().isEmpty())
        	  setCqAction(false);
              else 
        	  setCqAction(true);
          }
          public void widgetDefaultSelected(SelectionEvent e) {}
        });
        
        data = new GridData();
        data.horizontalSpan = 1;
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        cqActionCombo.setLayoutData(data);
        
        // Create the dryrun button
        dryrunButton = new Button(group, SWT.PUSH);
        dryrunButton.setText("Show actions only");
        dryrunButton.setEnabled(false);
        dryrunButton.setBackground(ltGreenColor);
        dryrunButton.setForeground(GridUtils.getBlack(display));
        dryrunButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
        	Runnable runnable = new Runnable() {
        	    public void run() {
        		try {
        		    setDryRun(true);
        		    takeAction();
        		}
        		catch (Exception ex) {
        		    System.out.println(IcofException.printStackTraceAsString(ex));
        		    // Display the error dialog
        		    MessageDialog.openError(shell, "Error",
        		                            "Unable to take selected actions!\n " +
        		                            "Message: " + ex.getMessage());
        		    return;
        		}

        	    }
		 
        	};
        	console.append("DRYRUN: starting actions ...\n");
        	BusyIndicator.showWhile(null, runnable);
        	console.append("DRYRUN: actions complete\n");
            }
        });

        data = new GridData();
        data.horizontalSpan = 2;
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        dryrunButton.setLayoutData(data);
        
        // Create the Action button
        actionButton = new Button(group, SWT.PUSH);
        actionButton.setText("Take selected actions");
        actionButton.setEnabled(false);
        actionButton.setBackground(ltGreenColor);
        actionButton.setForeground(GridUtils.getBlack(display));
        actionButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
        	Runnable runnable = new Runnable() {
        	    public void run() {
        		try {
        		    setDryRun(false);
        		    takeAction();
        		}
        		catch (Exception ex) {
        		    System.out.println(IcofException.printStackTraceAsString(ex));
        		    // Display the error dialog
        		    MessageDialog.openError(shell, "Error",
        		                            "Unable to take selected actions!\n " +
        		                            "Message: " + ex.getMessage());
        		    return;
        		}

        	    }
		 
        	};
        	console.append("Starting actions ...\n");
        	BusyIndicator.showWhile(null, runnable);
        	console.append("Actions complete\n");
            }
        });

        data = new GridData();
        data.horizontalSpan = 2;
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        actionButton.setLayoutData(data);
        
    }

    /**
     * Run the selected actions on the selected patches
     * @throws Exception 
     *
     */
    private void takeAction() throws Exception {
	
	String actions = "\n";
	if (advanceButton.getSelection())
	    actions = " * Advance\n";
	if (packageButton.getSelection())
	    actions += " * Package/transmit\n";
	if (isCqAction())
	    actions += " * Update TK Patch state to " + cqActionCombo.getText() + "\n";
	if (actions.equals("\n")) 
	    return;
	
	// Get confirmation from the user.
	if (! bDryRun) {
	    boolean confirm =  MessageDialog.openConfirm(shell, 
	                                                 "Take actions?",
	                                                 "Are you sure you'd like to " +
	                                                 "these actions on selected " +
	                                                 "patches?\n\n" + actions);
	    if (! confirm) {
		console.append("\nActions canceled by user !!!");
		return;
	    }
	}

	long startTime = System.currentTimeMillis();

	// If user wants to update the CQ records then get their CQ id and pw
	if (isCqAction() && ! isDryRun())
	    if (setCqUser() == false) {
		console.append("Actions (CQ id/pw) cancelled by user !!!\n");
		return;
	    }
	
        // Create a collection of patches keyed by component in case more than
        // 1 patch per component
        setPatchesByComp();
                
        // Run the selected actions
	for (String compName : getPatchesByComp().keySet()) {

	    String patches = getPatchesByComp().get(compName);
	    String[] tokens = patches.split("[,]");
	    TkPatch patch = (TkPatch) getXmittal().getPatches().get(tokens[0]);
	    
	    boolean bSuccess = true;
	    if (advanceButton.getSelection()) {
		bSuccess = execAdvance(patch);
		setStatus(patch, "Advance", bSuccess);
	    }
	    if (bSuccess && packageButton.getSelection()) {
		bSuccess = execTransmit(patches, patch);
		setStatus(patch, "Package", bSuccess);
	    }
	    if (bSuccess && isCqAction()) {
		bSuccess = execCqUpdate(patches);
		setStatus(patch, "CQ Update to " + cqActionCombo.getText(), bSuccess);
	    }
	}
	
	long endTime = System.currentTimeMillis();
	double minutes = (endTime - startTime) / 1000.0 / 60.0;
	String diff = String.format("%.3f", minutes);
	console.append("Elapsed time: " + diff + " minutes\n");
	System.out.println("Actions complete .. elapsed time: " + diff + " minutes\n");
	
    }
    
    
    /**
     * Get the user's CQ id and password
     *
     */
    private boolean setCqUser() {

	boolean bAnswer = true;
	
	if (getCqId().isEmpty() || getCqPassword().isEmpty()) {

	    CqUserUI userUI = new CqUserUI(this, getCqId(), getCqPassword());
	    userUI.run();
	    if (userUI.getDone()) {
		setCqId(userUI.getUserName()); 
		setCqPassword(userUI.getUserPassword());
	    }
	    else {
		bAnswer = false;
	    }

	}	
	
	return bAnswer;
	
    }


    /**
     * Create a collection of patches keyed by component name
     *
     */
    private void setPatchesByComp() {

	if (! getPatchesByComp().isEmpty())
	    getPatchesByComp().clear();
	    
	for (String name : getSelectedPatches()) {
	    
	    TkPatch patch = (TkPatch) getXmittal().getPatches().get(name);
	    String compName = patch.getComponent();
	    
	    String patches = "";
	    if (getPatchesByComp().containsKey(compName)) {
		patches = getPatchesByComp().get(compName) + ",";
	    }
	    patches += name;
	    getPatchesByComp().put(compName, patches);
	}
	
    }


    /**
     * Set the status for the patch
     *
     * @param patch 
     * @param action
     * @param bSuccess
     */
    private void setStatus(TkPatch patch, String action, boolean bSuccess) {

	// Skip if a dryrun
	if (isDryRun())
	    return;
	
	String statusText;
	if (bSuccess)
	    statusText = "OK";
	else 
	    statusText = "FAIL";
	
	for (TableItem item : getPatchTable().getItems()) {
	    if (item.getText(0).equals(patch.getId())) {
		item.setText(new String[] { patch.getId(),
		                            patch.getRelease(),
		                            patch.getComponent(),
		                            patch.getState(),
		                            statusText + " - " + action});
		if (! bSuccess)
		    item.setBackground(GridUtils.getRed(display));
		else 
		    item.setBackground(GridUtils.getGreen(display));
		
		break;
	    }
	}
	
    }


    /**
     * Update the selected patches as available
     *
     * @param patches 
     * @param patch
     * @return
     * @throws Exception 
     */
    private boolean execCqUpdate(String patches) throws Exception {

	String[] tokens = patches.split("[,]");
	boolean bSuccess = true;
	for (String patch : tokens) {
	    System.out.println("Updating " + patch + " to " + cqActionCombo.getText());
	    if (! updateCqState(patch))
		bSuccess = false;
		
	}
	 
	return bSuccess;
	
    }


    /**
     * Execute the patch transmit/packaging script 
     * @param patches 
     *
     * @param patch Patch to be  processed
     * 
     * @return
     */
    private boolean execTransmit(String patches, TkPatch aPatch) {

	// Construct the transmit or pkg.create command
	String command;
	String aTk = aPatch.getRelease();
	if (isNewProcess(aTk))
	    command = getPackageCommand(patches, aPatch);
	else
	    command = getTransmitCommand(patches, aPatch);
 
	// Run the command
	console.append("Starting xmit - " + aPatch.getComponent() + "\n");
	boolean bSuccess = runCommand(command);
	if (bSuccess)
	    console.append(" completed .. OK\n");
	else 
	    console.append(" completed .. FAILED\n");
	
	return bSuccess;
	
    }


    /**
     * Execute the specified command
     *
     * @param command Command to run
     * @return
     */
    private boolean runCommand(String command) {

	StringBuffer errors = new StringBuffer();
	Vector<String> results = new Vector<String>();
	boolean bSuccess = false;
	try {
	    if (isDryRun()) {
		System.out.println("[DRY RUN]: " + command);
		bSuccess = true;
	    }
	    else {
		System.out.println("Running: " + command);
		int rc = IcofSystemUtil.execSystemCommand(command, errors, results);
		if (rc == 0)
		    bSuccess = true;
	    }
	}
	catch(Exception trap) {
	    System.out.println("ERRORS: " + errors.toString());
	}

	return bSuccess;
	
    }


    /**
     * Construct the transmit command
     * @param patches 
     *
     * @param aPatch
     * @return
     */
    private String getTransmitCommand(String patches, TkPatch aPatch) {

	String tk = aPatch.getRelease();
	if (tk.startsWith("18.1")) 
	    tk = tk.replace("18.1", "14.1");
	
	String comp = aPatch.getComponent();
	
	String command = TRANSMIT + 
	                 " -v " + tk + 
	                 " -c " + comp + 
	                 " -T " + patches +
	                 " -t -y -q";
	
	return command;
	
    }


    /**
     * Construct the pkg.create command
     * @param patches 
     *
     * @param aPatch
     * @return
     */
    private String getPackageCommand(String patches, TkPatch aPatch) {

	String tk = aPatch.getRelease();
	String comp = aPatch.getComponent();
	
	String command = PACKAGE + 
	                 " -t " + tk + 
	                 " -c " + comp + 
	                 " -T " + patches +
	                 " -y";
	
	return command;
	
    }


    /**
     * Execute the advance action
     *
     * @param aPatch Patch to be advanced
     */
    private boolean execAdvance(TkPatch aPatch) {

	// Parse the release name and convert 14.1 (or 18.1) to 1401
	String aTk = aPatch.getRelease();
	if (aTk.indexOf("18.1") > -1)
	    aTk = aTk.replaceFirst("18", "14");
	String aRel = aTk.substring(0, aTk.lastIndexOf("."));
	String release = aRel.replace(".", "0");
		
	// Determine the advance src/dest locations
	String command = TK_SHOW + " -t " + aTk;
	StringBuffer errors = new StringBuffer();
	Vector<String> results = new Vector<String>();
	boolean bSuccess = false;
	try {
	    System.out.println("Running: " + command);
	    int rc = IcofSystemUtil.execSystemCommand(command, errors, results);
	    if (rc == 0)
		bSuccess = true;
	}
	catch(Exception trap) {
	    System.out.println("ERRORS: " + errors.toString());
	}
	
	String stage = "";
	for (String line : results) {
	    if (line.startsWith("Stage")) {
		String[] tokens = line.split("[:]");
		stage = tokens[1].trim();
	    }
	}
	
	String advSrc = "";
	String advDest = "";
	if (stage.isEmpty()) {
	    String msg = "Unable to determine stage for Tool Kit(" +
		         aTk + ")\n";
	    console.append(msg);
	    MessageDialog.openError(shell, "Error", msg);
	    
	}
	else if (stage.toLowerCase().equals("preview")) {
	    advSrc = "shipb";
	    advDest = "ship";
	}
	else if (stage.toLowerCase().equals("production")) {
	    advSrc = "tkb";
	    advDest = "tk";
	}
	else {
	    advSrc = stage.toLowerCase();
	    advDest = advSrc.replace("tkb", "tk");
	}
 
	
	// Create the command
	String compRel = aPatch.getComponent() + "." + release;
	command = ADVANCE + " -r " + compRel;
	command += " -s " + advSrc; 
	command += " -d " + advDest;
	
	// Run the command
	console.append("Starting advance - " + aPatch.getComponent() + "\n");
	bSuccess = runCommand(command);
	if (bSuccess)
	    console.append(" completed .. OK\n");
	else 
	    console.append(" completed .. FAILED\n");
	
	return bSuccess;
	
    }


    /**
     * Determines if this tool kit uses the new or old process
     *
     * @param aTk A Tool kit name
     * @return True if aTk uses the new packaging/install process
     */
    private boolean isNewProcess(String aTk) {

	boolean reply = false;
	
	// Parse the TK name x.y.z
	String tokens[] = aTk.split("[.]"); 
	String release = tokens[0];
	String version = tokens[2];
		
	// Check for 13.1.* tks as they use the old process
	if (release.equals("13") || release.equals("17")) 
	    return reply;
	
	// Use old process for any TK equal to or less than 14.1.6/18.1.6
	int minOldVersion = 6;
	
	char letters[] = version.toCharArray();
	String myVersion = "";
	for (char letter : letters) {
		if ((letter >= '0') && (letter <= '9')) {
			myVersion += letter;
		}
	}
	int currentVersion = Integer.valueOf(myVersion);
	// Test for 14.1.7 or higher
	if (currentVersion > minOldVersion)
	    reply = true;
	// Test for 14.1.6z or higher
	else if ((currentVersion == minOldVersion) && (version.length() > 1)) {
		reply = true;
	}
	
	return reply; 
	
    }


    /**
     * Define and layout the CQ query widgets.
     * 
     * @param comp       The parent composite widget which will hold this widget.
     * @param gridWidth  Number of columns in current grid.
     */
    private void setFilters(Composite comp, int gridWidth) {

        // Create the group.
        Group group = new Group(comp, SWT.SHADOW_IN);
        group.setText("Patch Filters");

        // Make it have a grid layout
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = gridWidth;
        group.setLayout(gridLayout);

        // Place the group in the composite.
        GridData data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        data.horizontalSpan = gridWidth;
        group.setLayoutData(data);
        
        // Create the label.
        Label thisLabel = new Label(group, SWT.NONE);
        thisLabel.setText("Tool Kit");

        data = new GridData();
        data.horizontalSpan = 1;
        thisLabel.setLayoutData(data);

        // Tool kit filter here
        toolKitCombo = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY);
        toolKitCombo.setItems(new String[] {""});
        //toolKitCombo.setBackground(ltBlueColor);
        toolKitCombo.addSelectionListener(new SelectionListener() {
          public void widgetSelected(SelectionEvent e) {
            loadPatchTable();
          }
          public void widgetDefaultSelected(SelectionEvent e) {}
        });
        
        data = new GridData();
        data.horizontalSpan = 1;
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        toolKitCombo.setLayoutData(data);
        
        // Create the label.
        thisLabel = new Label(group, SWT.NONE);
        thisLabel.setText("State");

        data = new GridData();
        data.horizontalSpan = 1;
        thisLabel.setLayoutData(data);
        
        // State filter
        stateCombo = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY);
        stateCombo.setItems(new String[] {""});
        //stateCombo.setBackground(ltBlueColor);
        stateCombo.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
        	loadPatchTable();
            }
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
        
        data = new GridData();
        data.horizontalSpan = 1;
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        stateCombo.setLayoutData(data);
      
    }
 
 
    /**
     * Define and layout the Patch data widgets.
     * 
     * @param comp       The +/parent composite widget which will hold this widget.
     * @param gridWidth  Number of columns in current grid.
     */
    private void setPatchTable(Composite comp, int gridWidth) {

        // Create the group.
        Group group = new Group(comp, SWT.SHADOW_IN);
        group.setText("Tool Kit Patches in Process");

        // Make it have a grid layout
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = gridWidth;
        group.setLayout(gridLayout);

        // Place the group in the composite.
        GridData data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        data.horizontalSpan = gridWidth;
        group.setLayoutData(data);

        // Create the Patch table.
        patchTable = new Table(group, 
                               SWT.CHECK | SWT.BORDER | 
                               SWT.V_SCROLL | SWT.H_SCROLL);
        //patchTable.setBackground(GridUtils.getWhite(display));
        patchTable.setBackground(GridUtils.getWhite(display));
        patchTable.setForeground(GridUtils.getBlack(display));
        patchTable.setLinesVisible(true);
	patchTable.setHeaderVisible(true);
        
        data = new GridData(GridData.FILL, GridData.FILL, true, true);
        data.horizontalSpan = gridWidth;
        data.heightHint = 250;
        patchTable.setLayoutData(data);
        
        patchTable.addListener (SWT.Selection, new Listener () {
            public void handleEvent(Event event) {
        	TableItem item = (TableItem)event.item;
        	String patchName = item.getText(0);
        	if (event.detail == SWT.CHECK) {
        	    if (getSelectedPatches().contains(patchName))
        		getSelectedPatches().remove(patchName);
        	    else
        		getSelectedPatches().add(patchName);
        	}
        	enableActionButtons(! getSelectedPatches().isEmpty());
            }
        });
       
        String[] titles = {"Patch ID          ", 
                           "Tool Kit   ", 
                           "Component      ", 
                           "State       ",
                           "Build Ready ",
                           "Xmit Ready ",                           
                           "Action Status     "};
       	for (String title : titles) {
       	    TableColumn column = new TableColumn(getPatchTable(), SWT.NONE);
       	    column.setText(title);
       	}   

       	int i = 0;
       	for (String title : titles) {
       	    getPatchTable().getColumn(i++).pack();
       	}   

       
        // Create the Refresh button.
        fetchPatchesButton = new Button(group, SWT.PUSH);
        fetchPatchesButton.setText("Fetch patches");
        fetchPatchesButton.setBackground(ltGreenColor);
        fetchPatchesButton.setForeground(GridUtils.getBlack(display));
        fetchPatchesButton.setEnabled(true);
        fetchPatchesButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                Runnable runnable = new Runnable() {
                    public void run() {
                        try {
                            fetchClearQuestData();
                            loadPatchTable();
                        }
                        catch (Exception ex) {
                            System.out.println(IcofException.printStackTraceAsString(ex));
                            // Display the error dialog
                            MessageDialog.openError(shell, "Error",
                                                    "Unable to query ClearQuest " +
                                                    "for patches!\n " +
                                                    "Message: " + ex.getMessage());
                            return;
                        }
                        
                    }
                };
                console.append("Querying ClearQuest ...\n");
                BusyIndicator.showWhile(null, runnable);
                console.append("Query complete\n");
                toggleButton.setEnabled(true);
            }
        });
        
        // Place it in the grid composite.
        data = new GridData();
        data.horizontalSpan = 1;
        data.horizontalAlignment = SWT.CENTER;
        data.grabExcessHorizontalSpace = true;
        fetchPatchesButton.setLayoutData(data);
        
        // Create the Refresh button.
        toggleButton = new Button(group, SWT.PUSH);
        toggleButton.setText("Toggle selections");
        toggleButton.setBackground(ltGreenColor);
        toggleButton.setForeground(GridUtils.getBlack(display));
        toggleButton.setEnabled(false);
        toggleButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
        	togglePatchSelections();
            }
            public void widgetDefaultSelected(SelectionEvent event) {
        	togglePatchSelections();
            }
        });
        
        // Place it in the grid composite.
        data = new GridData();
        data.horizontalSpan = 1;
        data.horizontalAlignment = SWT.CENTER;
        data.grabExcessHorizontalSpace = true;
        toggleButton.setLayoutData(data);
        
    }
  
    
    /**
     * Toggle the patch selections 
     */
    private void togglePatchSelections() {
	
	HashSet<String> newSelections = new HashSet<String>();
	for (TableItem item : getPatchTable().getItems()) {
	    
	    String patchName = item.getText(0);
	    if (getSelectedPatches().contains(patchName)) {
		item.setChecked(false);
	    }
	    else {
		item.setChecked(true);
		newSelections.add(patchName);
	    }
	    
	}
	
	getSelectedPatches().clear();
	getSelectedPatches().addAll(newSelections);
	
	enableActionButtons(! getSelectedPatches().isEmpty());
	
    }
    
    
    /**
     * Disable or enable the "selected" buttons
     * 
     * @param enableFlag  If true set to disabled
     */
    private void enableActionButtons(boolean state) {
	actionButton.setEnabled(state);
	dryrunButton.setEnabled(state);
    }
    
    /**
     * Disable or enable the Clear Quest button
     * 
     * @param enableFlag  If true set to enabled
     */
    private void enableCqButton(boolean state) {
	fetchPatchesButton.setEnabled(state);
    }    

    
    /**
     * Define and layout the Console widgets.
     * 
     * @param comp       The parent composite widget which will hold this widget.
     * @param gridWidth  Number of columns in current grid.
     */
    private void setConsoleDisplay(Composite comp, int gridWidth) {

        // Create the group.
        Group group = new Group(comp, SWT.SHADOW_IN);
        group.setText("Console");

        // Make it have a grid layout
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = gridWidth;
        group.setLayout(gridLayout);

        // Place the group in the composite.
        GridData data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        data.horizontalSpan = gridWidth;
        group.setLayoutData(data);

        // Create the Injection Request table.
        console = new Text(group, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        console.setBackground(GridUtils.getWhite(display));
        console.setForeground(GridUtils.getBlack(display));
        console.setText("Click \"Fetch patches\" button to view all patches in process\n");

        data = new GridData();
        data.horizontalSpan = gridWidth;
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        data.heightHint = 50;
        console.setLayoutData(data);
        
    }


    /**
     * Defines the menu actions.
     */
    private void setMenuActions() {

        // Define the exit action.
        exitAction = new ExitAction(this, ExitAction.MENU_ITEM_TEXT, 
                                    ExitAction.STATUS_TEXT);
        
        // Define the show about action.
        aboutAction = new ShowAboutAction(this, ShowAboutAction.MENU_ITEM_TEXT, 
                                          ShowAboutAction.STATUS_TEXT,
                                          APP_NAME, 
                                          "Application:  " + APP_NAME + "\n\n" +
                                          "Build:  " + APP_VERSION + "\n" +
                                          "Author:  " + APP_OWNER,
                                          "Showing \"Help->About\" window ...");
        
        // Define the show help action.
        String binDir = IcofUtil.constructAesBinDirName(myContext);
        IcofFile helpFile = new IcofFile(binDir + IcofFile.separator + HELP_FILE,
                                         false);
        showHelpAction = new ShowHelpAction(this, ShowHelpAction.MENU_ITEM_TEXT,
                                            ShowHelpAction.STATUS_TEXT, helpFile);
    
    }
    
    
    /**
     * The application entry point
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
       try {
            
            processArgs(args);
            new TransmitUI().run();

        }
        catch (Exception e) {

            try {

                // Print out diagnostics about the exception.
                String sMsg = "\n";
                sMsg += "*******************\n";
                sMsg += "Error stack trace:\n";
                sMsg += "*******************\n";
                sMsg += IcofException.printStackTraceAsString(e);
                sMsg += "*******************\n";
                if (myContext != null)
                    myContext.getSessionLog().log(SessionLog.ERROR, sMsg);
                else {
                    System.out.println(sMsg);
                    System.out.flush();
                }

            }
            catch (Exception e2) {
                e2.printStackTrace(System.out);
            }

        }
        finally {

            try {

                // Log the return code.
                if (myContext != null) {
                    myContext.getSessionLog().close();
                }

                // Tell user about the log file.
                if ((myContext != null) && (myContext.getSessionLog() != null)) {
                    System.out.println("See " + myContext.getSessionLog()
                                       .getFullPath()
                                       + " for more details.");
                }

            }
            catch (Exception ignore) {
            }

        }
    }

    
    /**
     * Parses and verifies the command line arguments.
     * 
     * @param argv              The command line arguments.
     * @exception IcofException Invalid application invocation
     */
    private static void processArgs(String argv[]) throws IcofException {

        // Parse input parameters
        Hashtable<String,String> htParams = new Hashtable<String,String>();
        String sSyntax = "m:";
        String sError = IcofSystemUtil.parseCmdLine(argv, sSyntax, htParams);

        // Check arguments.
        if ((htParams.size() < 2) || ((sError != null) && (!sError.equals("")))) {
            // Add application invocation if present
            sError = "\nINVOCATION: ";
            sError += APP_NAME + " ";
            for (int i = 0; i < argv.length; i++) {
                sError += argv[i] + " ";
            }
        }

        // Read and verify the application mode parameter
        String appMode = Constants.PROD;
        if (htParams.containsKey("-m")) {
            appMode =  htParams.get("-m");
        }

        // Create the AppContext, now that the appMode is known.
        String userid = System.getProperty(Constants.USER_NAME_PROPERTY_TAG);
        String usersAddress = InjectUtils.getUsersAddress(userid);

        try {
            String logFilePath = PROD;
            if (! appMode.equals(Constants.PROD)) {
        	logFilePath += DEV;
            }
            logFilePath += File.separator + ".." + File.separator + "logs";
            SessionLog slog = new SessionLog(APP_NAME,
                                             userid,
                                             appMode,
                                             true,
                                             SessionLog.TRACE,
                                             SessionLog.DEFAULT_CONVERSION_PATTERN,
                                             logFilePath);
            myContext = new AppContext(usersAddress, userid, appMode, slog);

            // Set up the database connection.
            AppContext.getDB2DriverInstance();
            myContext.connectToDB(Constants.DB_ACCESS_ID,
                                  Constants.DB_ACCESS_CODE);

        }
        catch (IcofException ie) {
            throw ie;
        }

        myContext.getSessionLog().log(SessionLog.INFO,
                                      "Mode : " + myContext.getAppMode());

        // Throw an exception if application usage was incorrect
        if (sError != null) {
            String msg = "Invalid application invocation or verification " +
            "errors.\n Error = " + sError;
            myContext.getSessionLog().log(SessionLog.ERROR, msg);

        }

    }
    

    /**
     * Query CQ for Patches ready to transmit
     * 
     * @throws Exception 
     */
    private void fetchClearQuestData() throws Exception {

        // Prepare the XML file
	String sQuery = TkInjectUtils.getPatchQueryString("svnlib@us.ibm.com", 
	                                                  getAuthIdPassword());
//	String sQuery = TkInjectUtils.getPatchQueryString("stadtlag@us.ibm.com", 
//	                                                  "t0lttr0t");
	
        // Set the SSL properties.
        CqClientUtil.setSSLProperties();           
        
        // Call the service.
        CqService_PortTypeProxy proxy = new CqService_PortTypeProxy();
        CqService_PortType client = proxy.getCqService_PortType();
        proxy.setEndpoint(CqClientUtil.getServiceAddress(Constants.PROD));
        String sResult = client.runQuery(sQuery);
        
        // Parse the response.
        cqFetchOutput xCqFetchOutput = new cqFetchOutput(sResult);
        sResult = xCqFetchOutput.getResultsAsDelimited("##", true).toString();
        Vector<String[]> objects = xCqFetchOutput.getResultObjects();
        Vector<String[]> labels = xCqFetchOutput.getFieldLabels();
        
        // Destroy an old patch object if it existed before creating a new one.
        if (xmittal != null)
            xmittal = null;
        
        xmittal = new TkXmittal(labels, objects);
        
        loadFilters();
        
    }

    
    /**
     * Load the TK and State filters based on the patches
     *
     */
    private void loadFilters() {
	
	ArrayList<String> tks = new ArrayList<String>();
	ArrayList<String> states = new ArrayList<String>();
	
	for (TkPatch patch : getXmittal().getPatches().values()) {
	    if (! states.contains(patch.getState()))
		states.add(patch.getState());
	    if (! tks.contains(patch.getRelease()))
		tks.add(patch.getRelease());
	}

	Collections.sort(tks);
	Collections.sort(states);
	
	stateCombo.removeAll();
	stateCombo.add("");
	for (String state : states) {
	    stateCombo.add(state);
	}
	stateCombo.pack();
	
	toolKitCombo.removeAll();
	toolKitCombo.add("");
	for (String tk : tks) {
	    toolKitCombo.add(tk);
	}
	toolKitCombo.pack();
	
    }


    /**
     * Loads the TK Inject Request data into the UI.
     */
    public void loadPatchTable() {
        
        // Turn off redraw to avoid flicker.
        patchTable.setRedraw(false);

        // Clear the data first.
        getPatchTable().removeAll();
        getSelectedPatches().clear();
        
        String tkFilter = toolKitCombo.getText();
        String stateFilter = stateCombo.getText();
        
        // Load the table with patch data
        if (getXmittal() != null) {
            for (TkPatch patch : getXmittal().getPatches().values()) {
        	
        	if (tkFilter.isEmpty() && stateFilter.isEmpty()) {
        	    addPatchToTable(patch);
        	}
        	else if (tkFilter.isEmpty() && stateFilter.equals(patch.getState())) {
        	    addPatchToTable(patch);
        	}
        	else if (stateFilter.isEmpty() && tkFilter.equals(patch.getRelease())) {
        	    addPatchToTable(patch);
        	}
        	else if (tkFilter.equals(patch.getRelease()) && stateFilter.equals(patch.getState())) {
        	    addPatchToTable(patch);
        	}
            }

        }

        // Resize the columns
        for (int i = 0; i < 4; i++) {
            getPatchTable().getColumn(i).pack();
        }

        // Turn on redraw.
        patchTable.setRedraw(true);
        patchTable.redraw();
        
        // Reset the buttons.
        enableActionButtons(false);
        
    }
    

    

    
    /**
     * Add this patch to the patch table
     *
     * @param patch
     */
    private void addPatchToTable(TkPatch patch) {

	TableItem item = new TableItem (patchTable, SWT.NONE);
	item.setText(new String[] { patch.getId(),
	                            patch.getRelease(),
	                            patch.getComponent(),
	                            patch.getState(),
	                            patch.getBuildReady(),
	                            patch.getTransmitReady(),
	                            "n/a" });
	item.setBackground(GridUtils.getWhite(display));
	
    }


    /**
     * Parses and verifies the command line arguments.
     * 
     * @param argv              The command line arguments.
     * @throws Exception 
     * @throws Exception 
     */
    private boolean updateCqState(String patchName)
    throws Exception {

	if (isDryRun()) {
	    System.out.println("[DRY RUN]: " + "Run CQ web service to update " +
	    		"TK Patch to " + cqActionCombo.getText());
	    return true;
	}
	
        // Prepare the XML file
        String myUpdate = TkInjectUtils.getUpdateString();
        myUpdate = myUpdate.replaceAll("##USERID##", getCqId());
        myUpdate = myUpdate.replaceAll("##PASSWORD##", getCqPassword());
        myUpdate = myUpdate.replaceAll("##RECORD##", patchName);
        myUpdate = myUpdate.replaceAll("##ACTION##", cqActionCombo.getText());
        
        // Set the SSL properties.
        CqClientUtil.setSSLProperties();           

        // Call the service.
        CqService_PortTypeProxy proxy = new CqService_PortTypeProxy();
        CqService_PortType client = proxy.getCqService_PortType();
        proxy.setEndpoint(CqClientUtil.getServiceAddress(Constants.PROD));
        String sResult = client.update(myUpdate);

        // Let the user know the action completed or not.
        boolean bSuccess = false;
        if (sResult.indexOf("<description>null</description>") > -1) {
            console.append("\nTK Patch set to Available ...");
            bSuccess = true;
        }
        else {
            console.append("\nWARNING: unable to update TK Patch (" + 
                          patchName + ") to Available");
        }

        return bSuccess;
        
    }

    
    /**
     * Read the svnlib id AFS password from a secure file
     * 
     * @return password
     * @throws IcofException
     */
    private String getAuthIdPassword()
    throws IcofException {

	String path = "/afs/eda/u/svnlib/private/svnlib.funcid";
	IcofFile pwFile = new IcofFile(path, false);
	pwFile.openRead();
	pwFile.read();
	pwFile.closeRead();

	if (pwFile.getContents().isEmpty()) {
	    throw new IcofException("PackagingUtils", 
	                            "getAuthIdPassword()",
				    IcofException.SEVERE,
				    "unable to read intranet password from "
				    + "secure file for "
				    + AUTHORIZED_ID, "File: " + path);
	}

	String pw = (String) pwFile.getContents().firstElement();
	return pw.trim();

    }

    
    /*
     * Getters
     */
    public TkXmittal getXmittal() { return xmittal; }
    public Table getPatchTable() { return patchTable; }
    public HashSet<String> getSelectedPatches() { return selectedPatches; }
    public HashMap<String, String> getPatchesByComp() { return patchesByComp; }
    public boolean isDryRun() { return bDryRun; }
    public String getCqId() { return cqId; }
    public String getCqPassword() { return cqPassword; }
    public boolean isCqAction() { return cqAction; }
    
    
    /*
     * Setters
     */
    private void setDryRun(boolean aFlag) { bDryRun = aFlag; }
    private void setCqId(String aName) { cqId = aName; }
    private void setCqPassword(String aName) { cqPassword = aName; }
    private void setCqAction(boolean aFlag) { cqAction = aFlag; }
    
    
    // Members
    private static AppContext myContext = null;
    public TkXmittal xmittal = null;
    private HashSet<String> selectedPatches = new HashSet<String>();
    private HashMap<String, String> patchesByComp = new HashMap<String, String>();
    private boolean bDryRun = false;
    private String cqId = "";
    private String cqPassword = "";
    private boolean cqAction = false;
    
    // Window constants.
    private final static int GRID_COLS_WIDE = 4;
    private final static int SCREEN_WIDTH = 700;
    private final static int SCREEN_HEIGHT = 650;
    
    // Actions.
    private ExitAction exitAction;
    private ShowAboutAction aboutAction;
    private ShowHelpAction showHelpAction;
    
    // Widgets.
    private Display display = null;
    private static Shell shell = null;
    private Text console;
    private Table patchTable;
    
    private Combo toolKitCombo;
    private Combo stateCombo;
    private Combo cqActionCombo;
    
    private Button fetchPatchesButton;
    private Button advanceButton;
    private Button packageButton;
    private Button actionButton;
    private Button toggleButton;
    private Button dryrunButton;
    
    // Colors
    private Color ltBlueColor;
    private Color ltGreenColor;
    
    
}

