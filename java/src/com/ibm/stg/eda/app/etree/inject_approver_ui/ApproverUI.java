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
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.app.etree.inject_approver_ui;

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
import com.ibm.stg.eda.app.etree.transmit_ui.CqUserUI;
import com.ibm.stg.eda.app.etree.transmit_ui.InjectUtils;
import com.ibm.stg.eda.component.jfacebase.ExitAction;
import com.ibm.stg.eda.component.jfacebase.GridUtils;
import com.ibm.stg.eda.component.jfacebase.ShowAboutAction;
import com.ibm.stg.eda.component.jfacebase.ShowHelpAction;
import com.ibm.stg.eda.component.tk_patch.CqInjectRequest;
import com.ibm.stg.eda.component.tk_patch.CqInjectRequests;
import com.ibm.stg.eda.component.tk_patch.TkInjectUtils;
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

public class ApproverUI extends ApplicationWindow {

    /**
     * Constants.
     */
    final static String APP_NAME = "approve.all";
    final static String APP_VERSION = "1.0";
    final static String APP_OWNER = "Gregg Stadtlander";
    final static String HELP_FILE = "help_text/package_help.txt";
    final static String AUTHORIZED_ID = "svnlib@us.ibm.com";
    
    
    /**
     * Constructor
     */
    public ApproverUI() {
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
        setInjectTable(gridComposite, GRID_COLS_WIDE);

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

        // Create the Action button
        actionButton = new Button(group, SWT.PUSH);
        actionButton.setText("Approve selected injects");
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

        
        // Create the dryrun button
        dryrunButton = new Button(group, SWT.CHECK);
        dryrunButton.setText("Dry run mode (don't run commands)");
        dryrunButton.setEnabled(false);
        //dryrunButton.setBackground(ltGreenColor);
        dryrunButton.setForeground(GridUtils.getBlack(display));
        dryrunButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
        	if (isDryRun())
        	    setDryRun(false);
        	else 
        	    setDryRun(true);
        	console.append("Dry run: " + isDryRun());
            }
        });

        data = new GridData();
        data.horizontalSpan = 2;
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        dryrunButton.setLayoutData(data);
        
        
    }
    

    /**
     * Run the selected actions on the selected patches
     * @throws Exception 
     *
     */
    private void takeAction() throws Exception {
	
	String actions = " * Update Injection Request to Approved\n";
	
	// Get confirmation from the user.
	if (! bDryRun) {
	    boolean confirm =  MessageDialog.openConfirm(shell, 
	                                                 "Take actions?",
	                                                 "Are you sure you'd like to " +
	                                                 "take these actions on selected " +
	                                                 "injects?\n\n" + actions);
	    if (! confirm) {
		console.append("\nActions canceled by user !!!");
		return;
	    }
	}

	long startTime = System.currentTimeMillis();

	// If user wants to update the CQ records then get their CQ id and pw
	if (isCqAction())
	    if (setCqUser() == false) {
		console.append("Actions (CQ id/pw) cancelled by user !!!\n");
		return;
	    }
	
               
	execCqUpdate(getInjects());
	
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
     * Update the selected patches as available
     *
     * @param patches 
     * @param patch
     * @return
     * @throws Exception 
     */
    private boolean execCqUpdate(CqInjectRequests injects) throws Exception {

	boolean bSuccess = true;
	
	for (CqInjectRequest inject : injects.getInjects().values()) {
	    if (getSelectedInjects().contains(inject.getId())) {
		
		console.append("Updating " + inject.getId() + " to Approved\n");
		
		if (! updateCqState(inject))
		    bSuccess = false;
	
		setStatus(inject, bSuccess);
		
	    }
		
	}
	 
	return bSuccess;
	
    }
    

    /**
     * Update the inject status
     *
     * @param inject
     * @param bSuccess
     */
    private void setStatus(CqInjectRequest inject, boolean bSuccess) {

	// Skip if dry run mode
	if (isDryRun())
	    return;
	
	for (TableItem item : getInjectTable().getItems()) {
	    if (item.getText(0).equals(inject.getId())) {
		if (bSuccess) {
		    item.setBackground(GridUtils.getGreen(display));
		    item.setText(5, "Success");
		}
		else { 
		    item.setBackground(GridUtils.getRed(display));
		    item.setText(5, "FAILED - CQ approval");
		}
		
		break;
	    }
	}
	
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
            loadInjectTable();
          }
          public void widgetDefaultSelected(SelectionEvent e) {}
        });
        
        data = new GridData();
        data.horizontalSpan = 1;
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        toolKitCombo.setLayoutData(data);
      
    }
 
 
    /**
     * Define and layout the Patch data widgets.
     * 
     * @param comp       The +/parent composite widget which will hold this widget.
     * @param gridWidth  Number of columns in current grid.
     */
    private void setInjectTable(Composite comp, int gridWidth) {

        // Create the group.
        Group group = new Group(comp, SWT.SHADOW_IN);
        group.setText("Reviewed Injection Requests");

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
        injectTable = new Table(group, 
                               SWT.CHECK | SWT.BORDER | 
                               SWT.V_SCROLL | SWT.H_SCROLL);
        //patchTable.setBackground(GridUtils.getWhite(display));
        injectTable.setBackground(GridUtils.getWhite(display));
        injectTable.setForeground(GridUtils.getBlack(display));
        injectTable.setLinesVisible(true);
        injectTable.setHeaderVisible(true);
        
        data = new GridData(GridData.FILL, GridData.FILL, true, true);
        data.horizontalSpan = gridWidth;
        data.heightHint = 250;
        injectTable.setLayoutData(data);
        
        injectTable.addListener (SWT.Selection, new Listener () {
            public void handleEvent(Event event) {
        	TableItem item = (TableItem)event.item;
        	String patchName = item.getText(0);
        	if (event.detail == SWT.CHECK) {
        	    if (getSelectedInjects().contains(patchName))
        		getSelectedInjects().remove(patchName);
        	    else
        		getSelectedInjects().add(patchName);
        	}
        	enableActionButtons(! getSelectedInjects().isEmpty());
            }
        });
       
        String[] titles = {"Inject Request ID    ", 
                           "Tool Kit       ", 
                           "Component      ", 
                           "Developer                    ",
                           "Emergency? ",
                           "My status               "};
       	for (String title : titles) {
       	    TableColumn column = new TableColumn(getInjectTable(), SWT.NONE);
       	    column.setText(title);
       	}   

       	int i = 0;
       	for (String title : titles) {
       	    getInjectTable().getColumn(i++).pack();
       	}   

       	// Table row count	
        countLabel = new Label(group, SWT.NONE);
        countLabel.setText("0 injection requests");
       	
        // Create the Refresh button.
        fetchInjectsButton = new Button(group, SWT.PUSH);
        fetchInjectsButton.setText("Fetch injects");
        fetchInjectsButton.setBackground(ltGreenColor);
        fetchInjectsButton.setForeground(GridUtils.getBlack(display));
        fetchInjectsButton.setEnabled(true);
        fetchInjectsButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                Runnable runnable = new Runnable() {
                    public void run() {
                        try {
                            fetchInjectData();
                            loadInjectTable();
                            countLabel.setText(getInjects().getInjects().size() + 
                                               " injection requests");
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
                toggleButton.setEnabled(! getInjects().getInjects().isEmpty());
            }
        });
        
        // Place it in the grid composite.
        data = new GridData();
        data.horizontalSpan = 1;
        data.horizontalAlignment = SWT.CENTER;
        data.grabExcessHorizontalSpace = true;
        fetchInjectsButton.setLayoutData(data);
        
        // Create the Refresh button.
        toggleButton = new Button(group, SWT.CHECK);
        toggleButton.setText("Select all inject requests");
        //toggleButton.setBackground(ltGreenColor);
        toggleButton.setForeground(GridUtils.getBlack(display));
        toggleButton.setEnabled(false);
        toggleButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
        	Button button = (Button) e.widget;
        	if (button.getSelection())
        	    toggleInjectSelections(true);
        	else
        	    toggleInjectSelections(false);
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
     * Toggle the inject selections 
     */
    private void toggleInjectSelections(boolean selectAll) {
	
	if (selectAll)
	    console.append("Selecting all injects .. \n");
	else
	    console.append("Unselecting all injects .. \n");
	
	// Update the table selections
	getSelectedInjects().clear();
	for (TableItem item : getInjectTable().getItems()) {
	    item.setChecked(selectAll);
	    if (selectAll) 
		getSelectedInjects().add(item.getText(0));
	}
	
	enableActionButtons(! getSelectedInjects().isEmpty());
	
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
	fetchInjectsButton.setEnabled(state);
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
            new ApproverUI().run();

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
            String logFilePath = "/afs/btv/data/aes/";
            if (appMode.equals(Constants.PROD)) {
                logFilePath += "prod";
            }
            else {
                logFilePath += "dev";
            }
            logFilePath += "/logs/TkInject";
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
    private void fetchInjectData() throws Exception {

        // Prepare the XML file
	String sQuery = TkInjectUtils.getInjectQueryString("svnlib@us.ibm.com", 
	                                                   getAuthIdPassword(),
	                                                   "Reviewed Injection Requests");
	
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
        if (injects != null)
            injects = null;
        
        injects = new CqInjectRequests(labels, objects);
        if (getInjects().getInjects().isEmpty()) {
            MessageDialog.openInformation(shell, "Info",
                                          "No Injection Requests found " +
                                          "in the reviewed state.");
            console.append("No reviewed injection requests found\n");
        }
  
        loadFilters();
        
    }

    
    /**
     * Load the TK and State filters based on the patches
     *
     */
    private void loadFilters() {
	
	ArrayList<String> tks = new ArrayList<String>();
	
	for (CqInjectRequest inject : getInjects().getInjects().values()) {
	    if (! tks.contains(inject.getToolKit()))
		tks.add(inject.getToolKit());
	}

	Collections.sort(tks);
	
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
    public void loadInjectTable() {
        
        // Turn off redraw to avoid flicker.
	getInjectTable().setRedraw(false);

        // Clear the data first.
        getInjectTable().removeAll();
        getSelectedInjects().clear();
        
        String tkFilter = toolKitCombo.getText();
        
        // Load the table with patch data
        if (getInjects() != null) {
            for (CqInjectRequest inject : getInjects().getInjects().values()) {
        	
        	if (tkFilter.isEmpty()) {
        	    addToTable(inject);
        	}
        	else if (tkFilter.equals(inject.getToolKit())) {
        	    addToTable(inject);
        	}
            }

        }

        // Resize the columns
        for (int i = 0; i < 4; i++) {
            getInjectTable().getColumn(i).pack();
        }

        // Turn on redraw.
        getInjectTable().setRedraw(true);
        getInjectTable().redraw();
        
        // Reset the buttons.
        enableActionButtons(false);
        
    }
   
    
    /**
     * Add this inject to the inject table
     *
     * @param patch
     */
    private void addToTable(CqInjectRequest anInject) {

	TableItem item = new TableItem (getInjectTable(), SWT.NONE);
	
	String emergency = "No";
	if (anInject.getEmergency())
	    emergency = "Yes";
		
	item.setText(new String[] { anInject.getId(),
	                            anInject.getToolKit(),
	                            anInject.getComponent(),
	                            anInject.getDeveloper(),
	                            emergency,
	                            "" });
	item.setBackground(GridUtils.getWhite(display));
	
    }



    /**
     * Updates the inject request state
     * 
     * @param inject  Inject request to update
     * @throws Exception 
     */
    private boolean updateCqState(CqInjectRequest inject)
    throws Exception {

	if (isDryRun()) {
	    console.append("[DRY RUN]: " + "Run CQ web service to update " +
	    		   "Inject Request to Approved \n");
	}
	
        // Prepare the XML file
        String myUpdate = TkInjectUtils.getUpdateString("tk_injectionrequest");
        myUpdate = myUpdate.replaceAll("##USERID##", getCqId());
        myUpdate = myUpdate.replaceAll("##PASSWORD##", getCqPassword());
        myUpdate = myUpdate.replaceAll("##RECORD##", inject.getId());
        myUpdate = myUpdate.replaceAll("##ACTION##", "Approve");
        
        if (isDryRun())
	    return true;
        
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
            console.append("\nInjection Request set to Approved\n");
            bSuccess = true;
        }
        else {
            console.append("\nERROR: unable to update Inject Request (" + 
                          inject.getId() + ") to Approved\n");
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
	    throw new IcofException("PackagingUtils", "getAuthIdPassword()",
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
    public CqInjectRequests getInjects() { return injects; }
    public Table getPatchTable() { return patchTable; }
    public Table getInjectTable() { return injectTable; }
    public HashSet<String> getSelectedInjects() { return selectedInjects; }
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
    public CqInjectRequests injects = null;
    public TkXmittal xmittal = null;
    private HashSet<String> selectedInjects = new HashSet<String>();
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
    private Table injectTable;
    
    private Combo toolKitCombo;
    
    private Button fetchInjectsButton;
    private Button actionButton;
    private Button toggleButton;
    private Button dryrunButton;
    
    Label countLabel;
    
    // Colors
    private Color ltBlueColor;
    private Color ltGreenColor;
    
    
}

