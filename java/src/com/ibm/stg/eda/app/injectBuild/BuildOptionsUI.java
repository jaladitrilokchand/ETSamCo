/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2009 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 * CREATOR: Gregg Stadtlander
 *    DATE: 03/01/2010
 *
 *-PURPOSE---------------------------------------------------------------------
 * TK Injection build options UI class.
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 03/01/2010 GFS  Initial coding.
 * 04/26/2010 GFS  Cleaned up a BEAM error. Updated to read the platforms table
 *                 to determine if all the commands are complete.
 * 10/27/2010 GFS  Changed button text to black.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.injectBuild;

import java.io.IOException;
import java.util.Vector;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.ibm.stg.eda.component.jfacebase.GridUtils;
import com.ibm.stg.eda.component.tk_patch.TkCommand;
import com.ibm.stg.eda.component.tk_patch.TkCommandRunner;
import com.ibm.stg.eda.component.tk_patch.TkInjectUtils;
import com.ibm.stg.eda.component.tk_patch.TkPatch;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofFile;
import com.ibm.stg.iipmds.common.IcofStringUtil;
import com.ibm.stg.iipmds.common.IcofSystemUtil;

public class BuildOptionsUI extends ApplicationWindow {

    /**
     * Constructor.
     * @param aWindow
     * @param aPatch
     * @param aCmvcRelease
     *  
     * @throws Exception 
     */
    public BuildOptionsUI(ApplicationWindow aWindow, 
                          TkPatch aPatch,
                          String aCmvcRelease) throws Exception { 
        super(aWindow.getShell());
        setWindow(aWindow);
        setPatch(aPatch);
        setCmvcRelease(aCmvcRelease);
        setShell();
        setCommandDirectory(getPatch().getTargetDirectory());

        readBuildOptions();

    }


    /**
     * Runs the application
     */
    public void run() {

        // Don't return from open() until window closes
        setBlockOnOpen(true);

        // Open the main window
        open();

        // Close this shell.
        getShell().dispose();

    }


    /**
     * Creates the main window's contents
     * 
     * @param parent the main window
     * @return Control
     */
    protected Control createContents(Composite parent) {

        shell = parent.getShell();
        shell.setText("EDA Tool Kit Injection Build - build options");

        // Set the window size by scaling parent's size.
        shell.setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        display = shell.getDisplay();
        
        // Define the Grid layout
        Composite gridComposite = new Composite(shell, SWT.BORDER);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        gridComposite.setLayout(gridLayout);

        // Create the additional prep options group.
        setAdditionalPrepComposite(gridComposite, 4);
        GridUtils.setBlankSpace(gridComposite, 4, SWT.HORIZONTAL);

        // Create the build command group.
        setBuildCommandsComposite(gridComposite, 4);
        GridUtils.setBlankSpace(gridComposite, 4, SWT.HORIZONTAL);

        // Create the Console widget.
        setConsoleDisplay(gridComposite, 4);

        return parent;

    }



    /**
     * Define and layout the additional prep widgets.
     * 
     * @param comp       The parent composite widget which will hold this widget.
     * @param gridWidth  Number of columns in current grid.
     */
    private void setAdditionalPrepComposite(Composite comp, int gridWidth) {

        // Create the group.
        Group group = new Group(comp, SWT.SHADOW_IN);
        group.setText("Additional Source Prep Options (run in " + 
                      patch.getTargetDirectory() + ")");

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

        // Create the "copy headers" radio button.
        copyHeadersButton = new Button(group, SWT.CHECK);
        copyHeadersButton.setText("Copy injected header files to private and " +
                                  "include directories");
        copyHeadersButton.setSelection(copyHeaderFiles());

        data = new GridData();
        data.horizontalSpan = gridWidth;
        copyHeadersButton.setLayoutData(data);
        copyHeadersButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                setCopyHeaderFiles(copyHeadersButton.getSelection());
            }
        });


        // Create the "compile msgcat" radio button.
        compileMsgcatsButton = new Button(group, SWT.CHECK);
        compileMsgcatsButton.setText("Compile injected msgcat files and copy " +
                                     "resulting header files to private and " +
        "include directores");
        compileMsgcatsButton.setSelection(compileMsgcatFiles());  

        data = new GridData();
        data.horizontalSpan = gridWidth;
        compileMsgcatsButton.setLayoutData(data);
        compileMsgcatsButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                setCompileMsgcatFiles(compileMsgcatsButton.getSelection());
            }
        });

    }


    /**
     * Define and layout the build command/platform widgets.
     * 
     * @param comp       The parent composite widget which will hold this widget.
     * @param gridWidth  Number of columns in current grid.
     */
    private void setBuildCommandsComposite(Composite comp, int gridWidth) {

        // Set the height hint for the list boxes
        int listRowSpan = 3;

        // Create the group.
        Group group = new Group(comp, SWT.SHADOW_IN);
        group.setText("Specify Build Commands and Platforms");

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


        // Create the "run in directory" label.
        Label thisLabel = new Label(group, SWT.NONE);
        thisLabel.setText("Run commands in this directory");

        data = new GridData();
        data.horizontalSpan = 1;
        data.horizontalAlignment = SWT.LEFT;
        thisLabel.setLayoutData(data);

        // Create the 32 bit command text box.
        commandDirectoryText = new Text(group, SWT.BORDER);
        commandDirectoryText.setText(getCommandDirectory());
        commandDirectoryText.setBackground(GridUtils.getWhite(display));
        commandDirectoryText.setForeground(GridUtils.getBlack(display));

        data = new GridData();
        data.horizontalSpan = gridWidth - 1;
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        //data.widthHint = shell.getSize().x * 80 / 100;
        commandDirectoryText.setLayoutData(data);


        // Create the 32 bit command label.
        thisLabel = new Label(group, SWT.NONE);
        thisLabel.setText("Command to run for 32 bit builds");

        data = new GridData();
        data.horizontalSpan = gridWidth;
        data.horizontalAlignment = SWT.LEFT;
        thisLabel.setLayoutData(data);


        // Create the 32 bit command text box.
        command32Text = new Text(group, SWT.BORDER);
        command32Text.setText(getCommandFor32());
        command32Text.setBackground(GridUtils.getWhite(display));
        command32Text.setForeground(GridUtils.getBlack(display));
        command32Text.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent event) {
                setCommandFor32(command32Text.getText());
            }
        });

        data = new GridData();
        data.horizontalSpan = gridWidth;
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        command32Text.setLayoutData(data);


        // Create the 64 bit command label.
        thisLabel = new Label(group, SWT.NONE);
        thisLabel.setText("Command to run for 64 bit builds");

        data = new GridData();
        data.horizontalSpan = gridWidth;
        data.horizontalAlignment = SWT.LEFT;
        thisLabel.setLayoutData(data);

        // Create the 64 bit command text box.
        command64Text = new Text(group, SWT.BORDER);
        command64Text.setText(getCommandFor64());
        command64Text.setBackground(GridUtils.getWhite(display));
        command64Text.setForeground(GridUtils.getBlack(display));
        command64Text.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent event) {
                setCommandFor64(command64Text.getText());
            }
        });

        data = new GridData();
        data.horizontalSpan = gridWidth;
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        command64Text.setLayoutData(data);


        // Create the Source File table.
        platformTable = new Table(group, SWT.BORDER | SWT.CHECK | 
                                  SWT.MULTI | SWT.FULL_SELECTION);
        platformTable.setHeaderVisible(true);
        platformTable.setBackground(GridUtils.getWhite(display));
        platformTable.setForeground(GridUtils.getBlack(display));

        TableColumn col1 = new TableColumn(platformTable, SWT.LEFT);
        col1.setText("Platform");
        col1.setWidth(SMALL_COL_WIDTH);

        TableColumn col2 = new TableColumn(platformTable, SWT.LEFT);
        col2.setText("Machine");
        col2.setWidth(SMALL_COL_WIDTH);   

        TableColumn col3 = new TableColumn(platformTable, SWT.LEFT);
        col3.setText("State");
        col3.setWidth(SMALL_COL_WIDTH);   

        TableColumn col4 = new TableColumn(platformTable, SWT.LEFT);
        col4.setText("Log file");
        col4.setWidth(LARGE_COL_WIDTH);   

        data = new GridData();
        data.horizontalSpan = gridWidth;
        data.verticalSpan = listRowSpan;
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        platformTable.setLayoutData(data);


        // Create the Select All button
        selectAllButton = new Button(group, SWT.CHECK);
        selectAllButton.setText("Select all platforms");
        selectAllButton.setEnabled(true);
        selectAllButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                loadPlatformsTable();
            }
        });

        data = new GridData();
        data.horizontalSpan = gridWidth - 1;
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        selectAllButton.setLayoutData(data);

        
        // Create the Show Logs button
        showLogsButton = new Button(group, SWT.PUSH);
        showLogsButton.setText("View build logs");
        showLogsButton.setEnabled(false);
        showLogsButton.setBackground(GridUtils.getGreen(display));
        showLogsButton.setForeground(GridUtils.getBlack(display));
        showLogsButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                // Open the window to view log files.
                if (runner != null) {
                    LogViewerUI logViewer = new LogViewerUI(window,
                                                            runner.getLogFiles());
                    logViewer.run();
                }
            }
        });

        data = new GridData();
        data.horizontalSpan = 1;
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        showLogsButton.setLayoutData(data);
        

        // Load the platform table.
        loadPlatformsTable();


        // Create Close Window button.
        closeButton = new Button(group, SWT.PUSH);
        closeButton.setText("Close window");
        closeButton.setBackground(GridUtils.getGreen(display));
        closeButton.setForeground(GridUtils.getBlack(display));
        closeButton.addSelectionListener(new SelectionAdapter () {
            public void widgetSelected(SelectionEvent e) {
                getShell().dispose();
            }
        });

        data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.horizontalSpan = 1;
        closeButton.setLayoutData(data);

        
        // Create Save Commands button.
        saveButton = new Button(group, SWT.PUSH);
        saveButton.setText("Save commands");
        saveButton.setBackground(GridUtils.getGreen(display));
        saveButton.setForeground(GridUtils.getBlack(display));
        saveButton.addSelectionListener(new SelectionAdapter () {
            public void widgetSelected(SelectionEvent e) {
                try {
                    saveBuildOptions();
                    console.append("\nBuild options saved ...");
                }
                catch(IcofException exp) {
                    MessageDialog.openError(shell, "Save error", 
                    "Unable to save build options.");
                }
            }
        });

        data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.horizontalSpan = 1;
        saveButton.setLayoutData(data);

        
        // Create Reload Selections Window button.
        reloadButton = new Button(group, SWT.PUSH);
        reloadButton.setText("Reload commands");
        if (optionsFile != null) {
            reloadButton.setEnabled(optionsFile.exists());
        }
        else {
            reloadButton.setEnabled(false);
        }
        reloadButton.setBackground(GridUtils.getGreen(display));
        reloadButton.setForeground(GridUtils.getBlack(display));
        reloadButton.addSelectionListener(new SelectionAdapter () {
            public void widgetSelected(SelectionEvent e) {
                try {
                    reloadBuildOptions();
                    console.append("\nBuild options reloaded ...");
                }
                catch(IcofException exp) {
                    MessageDialog.openError(shell, "Reload error", 
                    "Unable to reload build options.");
                }
            }
        });

        data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.horizontalSpan = 1;
        reloadButton.setLayoutData(data);

        
        // Create Execute button.
        executeButton = new Button(group, SWT.PUSH);
        executeButton.setText("Execute commands");
        executeButton.setBackground(GridUtils.getGreen(display));
        executeButton.setForeground(GridUtils.getBlack(display));
        executeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {

                // Execute the selected commands.
                try {
                    enableButtons(false);
                    if (! executeSelections()) {
                        enableButtons(true);
                        if (runner != null)
                            runner.cleanupFiles();
                        return;
                    }
                }
                catch (IcofException e) {
                    MessageDialog.openError(shell, "Error",
                                            "Error starting the remote commands. " +
                                            "See log files for details.");
                    e.printStackTrace();
                    enableButtons(true);
                    if (runner != null)
                        runner.cleanupFiles();
                }
                
                // Return if there were no long running commands to monitor.
                if ((runner != null) && (runner.getCommands().size() < 1)) {
                    enableButtons(true);
                    return;
                }
                
                // Long running operations should execute in its own thread.
                Thread th = new Thread("execute") {
                    public void run() {
                        
                        // Update the platforms table
                        while (! areAllCommandsComplete) {
  
                            try {
                                Thread.sleep(2000);
                            }
                            catch (InterruptedException e1) {
                                //e1.printStackTrace();
                            }

                            shell.getDisplay().asyncExec(new Runnable() {
                                public void run() {
                                    try {
                                        updatePlatformsTable(true);
                                    }
                                    catch (IcofException e) {
                                        MessageDialog.openError(shell, "Error",
                                                                "Error updating the platform table. " +
                                                                "See log files for details.");
                                        //e.printStackTrace();
                                        enableButtons(true);
                                        if (runner != null) 
                                            runner.cleanupFiles();
                                    }
                                }
                            });

                        }
                        
                        shell.getDisplay().asyncExec(new Runnable() {
                            public void run() {
                                try {
                                    updatePlatformsTable(true);
                                }
                                catch(IcofException ignore) {
                                	ignore.printStackTrace();
                                }
                                if (runner != null)
                                    runner.cleanupFiles();
                                MessageDialog.openInformation(shell, "Complete", 
                                                              "Prep/build commands are complete.");
                                enableButtons(true);

                            }
                        });

                    }
                };

                // Start the thread.
                th.start();
                
            }
            
        });

        data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.horizontalSpan = 1;
        executeButton.setLayoutData(data);
        
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
        console.setBackground(GridUtils.getGray(display));
        console.setForeground(GridUtils.getBlack(display));
        console.setText("Select the desired prep and build options then click \"Execute\"\n");

        data = new GridData();
        data.horizontalSpan = gridWidth;
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        data.heightHint = 50;
        console.setLayoutData(data);

    }


    /**
     * Load the platform table.
     */
    private void loadPlatformsTable() {

        // Turn off redraw to avoid flicker.
        platformTable.setRedraw(false);

        // Remove all entries.
        if (platformTable.getItems().length > 0) {
            platformTable.removeAll();
        }

        // Add the active files to the source table.
        String[] platforms = { "rs_aix53", "64-rs_aix53", "linux40", "64-linux40" };

        for (int i = 0; i < platforms.length; i++) {
            TableItem item = new TableItem(platformTable, SWT.NONE);
            item.setText(new String[] {platforms[i], "", "", ""} );
            if (selectAllButton != null) { 
                item.setChecked(selectAllButton.getSelection());
            }
        }

        // Turn on redraw.
        platformTable.setRedraw(true);
        platformTable.redraw();
    }


    /**
     * Load the platform table.
     * 
     * @param readStatus  If true read the status files otherwise don't read them
     * @throws IcofException 
     */
    private void updatePlatformsTable(boolean readStatus) throws IcofException {

        // Read the latest command status.
        if (readStatus) {
            runner.updateCommandStatus();
        }

        // Set the allCommandsComplete flag to true.
        areAllCommandsComplete = true;
        
        // Turn off redraw to avoid flicker.
        platformTable.setRedraw(false);

        // Updated selected platforms. Set the areAllCommandsComplete to false 
        // for an command that has a non-COMPLETE state.
        TableItem[] items = platformTable.getItems();
        for (int i = 0; i < items.length; i++) {
            if (items[i].getChecked() == true) {

                String platform = items[i].getText(0);

                TkCommand myCommand = runner.getCommand(platform);

                String machine = "";
                String state = "";
                String logFile = "";

                if (myCommand != null) {
                    if (myCommand.getMachine() != null)
                        machine = myCommand.getMachine();
                    if (myCommand.getState() != null)
                        state = myCommand.getState();
                    if (myCommand.getLogFile() != null)
                        logFile = myCommand.getLogFile();

                    items[i].setText(1, machine);
                    items[i].setText(2, state);
                    items[i].setText(3, logFile);
                    
                    if (! state.equals(TkCommand.COMPLETE)) {
                        areAllCommandsComplete = false;
                    }
                    
                }
                else {
                    areAllCommandsComplete = false;
                }

            }

        }

        // Turn on redraw.
        platformTable.setRedraw(true);
        platformTable.redraw();
        
    }

    
    /**
     * Clear the machine, state and log files from the platform table.
     * 
     * @throws IcofException 
     */
    private void clearPlatformsTable() throws IcofException {

        // Turn off redraw to avoid flicker.
        platformTable.setRedraw(false);

        // Updated selected platforms.
        TableItem[] items = platformTable.getItems();
        for (int i = 0; i < items.length; i++) {
            items[i].setText(1, "");
            items[i].setText(2, "");
            items[i].setText(3, "");
        }

        // Turn on redraw.
        platformTable.setRedraw(true);
        platformTable.redraw();
        
    }

    
    /**
     * Execute the selected commands. 
     */
    private boolean executeSelections() throws IcofException {

        // Prepare the commands (used to get command summary for confirmation)
        setCommandRunner();
        
        // Confirm actions with user.
        if (! getUserConfirmation()) {
            console.append("\nUser requested cancelation of build or no commands selected ...\n");
            return false;
        }
        
        
        // Copy header files if selected.
        if (copyHeaderFiles()) {
            console.append(" Processing header files (if any injected) ...\n");
            patch.copyHeaderFiles();
        }

        // Compile msgcat file if selected
        if (compileMsgcatFiles()) {
            console.append(" Processing msgcat files (if any injected) ...\n");
            patch.compileMsgcatFiles();
        }

        // Start the build commands if selected
        startSelectedCommands();
        return true;
        
    }


    /**
     * Show the user commands to run and ask them to verify that is what they'd 
     * like to do.
     * 
     * @return If true user wants to continue otherwise return false.
     */
    private boolean getUserConfirmation() {

        boolean doContinue = false;
        
        String question = "Is this what you'd like to do?\n\n"; 
        StringBuffer actions = new StringBuffer();
        
        // Add prep actions.
        if (copyHeaderFiles) {
            actions.append("Copy injected header files\n\n");
        }
        if (compileMsgcatFiles) {
            actions.append("Compile injected msgcat files and copy resulting header files\n\n");
        }

        // Add platform commands
        if (runner != null) {
            actions.append(runner.getSummary());
        }
        
        // Ask user to confirm.  
        if (actions.length() > 0) {
            doContinue = MessageDialog.openQuestion(shell, "Confirm commands", 
                                                    question + actions.toString());
        }
        else {
            MessageDialog.openWarning(shell, "No commands", 
                                      "No commands have been selected to run.");
        }
        
        return doContinue;
        
    }


    /**
     * Construct the TkCommandRunner object based on the user's selections.
     * @throws IcofException 
     */
    private void setCommandRunner() throws IcofException {

        // If no platforms selected then return.
        TableItem[] items = platformTable.getItems();
        boolean bSelected = false;
        for (int i = 0; i < items.length; i++) {
            if (items[i].getChecked() == true)
                bSelected = true;
        }
        if (! bSelected) {
            return;
        }
        
        // Create the TK command runner object which will write the
        // command file which hands the command/platforms to the perl script.
        String commandFile = TkInjectUtils.getLogFileName(patch.getId(), 
                                                          patch.getComponent(),
                                                          "commands", false);
        runner = new TkCommandRunner(commandDirectoryText.getText(), commandFile);
        
        // Create the command objects for each selected platform
        for (int i = 0; i < items.length; i++) {
            if (items[i].getChecked() == true) {

                // Read the platform and set the correct aix technology level.
                String platform = items[i].getText(0);
                String techLevel = "";
                if (platform.indexOf("rs_aix53") > -1) {
                    if (getCmvcRelease().indexOf("1300") > -1) {
                        techLevel = "tl04";
                    }
                    else if (getCmvcRelease().indexOf("13") > -1) {
                        techLevel = "tl05";
                    }
                    else if (getCmvcRelease().indexOf("14") > -1) {
                        techLevel = "tl07";
                    }
                }

                // Determine which command to run
                String command = "";
                if (platform.indexOf("-") > -1) {
                    command = getCommandFor64();
                }
                else {
                    command = getCommandFor32();
                }

                // Create the log file name.
                String logFile = TkInjectUtils.getLogFileName(patch.getId(), 
                                                              patch.getComponent(),
                                                              platform + "_build",
                                                              true);
                IcofFile log = new IcofFile(logFile, false);
                if (log.exists()) {
                    log.remove(false);
                }

                // Create the status file name.
                String statusFile = TkInjectUtils.getLogFileName(patch.getId(), 
                                                              patch.getComponent(),
                                                              platform + "_status",
                                                              false);
                IcofFile status = new IcofFile(statusFile, false);
                if (status.exists()) {
                    status.remove(false);
                }
                
                TkCommand buildCmd = new TkCommand(command, platform, techLevel,
                                                  logFile, statusFile, "");
                
                // Add this command to the runner object.
                runner.addCommand(buildCmd);
                
            }
        }
        
    }

    
    /**
     * Run the selected commands on remote machines.
     * @throws IcofException 
     */
    private void startSelectedCommands() throws IcofException {

        // Return if no commands to execute.
        if ((runner == null) || 
            (runner.getCommands().size() < 1) ||
            (runner.getCommandFile() == null)) {
            console.append("No build commands selected ...\n");
            return;
        }

        // Clear the machine, state and log file fields.
        clearPlatformsTable();
        
        // Write the command file and run the commands
        runner.writeCommandFile();

        Vector<String> results = new Vector<String>();
        StringBuffer errorMsg = new StringBuffer();
        String command = TkInjectUtils.FORK_IT_CMD + runner.getCommandFileName() + " &";
        execSystemCommand(command, errorMsg, results);
        
        console.append("All build commands are running ...\n");
        
    }

    
    /**
     * Save the build options to the config file. 
     * @throws IcofException 
     */
    private void saveBuildOptions() throws IcofException {

        // Construct the new contents
        Vector<String> newContents = new Vector<String>();
        newContents.add(OPT_HEADER + "#" + copyHeaderFiles());
        newContents.add(OPT_MSGCAT + "#" + compileMsgcatFiles());
        newContents.add(OPT_BUILD32 + "#" + getCommandFor32());
        newContents.add(OPT_BUILD64 + "#" + getCommandFor64());
        
        // Write the contents.
        try {
            optionsFile.openWrite();
            optionsFile.write(newContents);
        }
        catch (IcofException e) { throw e; }
        finally {
            if (optionsFile.isOpen())
                optionsFile.closeWrite();
        }
        
    }

    
    /**
     * Reload the build options 
     */
    private void reloadBuildOptions() throws IcofException {

        // Re-read the options file for this component.
        readBuildOptions();
        
        if ((optionsFile != null) && (optionsFile.exists())) {
            compileMsgcatsButton.setSelection(compileMsgcatFiles());
            copyHeadersButton.setSelection(copyHeaderFiles());
            command32Text.setText(getCommandFor32());
            command64Text.setText(getCommandFor64());
        }
        
    }


    /**
     * Read the build options from the config file
     * @throws IcofException 
     */
    private void readBuildOptions() throws IcofException {

        // Define the options file.
        if (optionsFile == null) {
            String optionsFileName = 
                TkInjectUtils.getOptionsFileName(patch.getComponent());
            optionsFile = new IcofFile(optionsFileName, false);
        }
        
        // If no options file then return.
        if (! optionsFile.exists()) {
            return;
        }
        
        // Read the options file.
        try {
            if (console != null) {
                console.append("\nReading options file ...");
            }

            optionsFile.openRead();
            optionsFile.read();
        }
        catch (IcofException e) { throw e; }
        finally {
            if (optionsFile.isOpen())
                optionsFile.closeRead();
        }

        // Parse the build options.
        for (int i = 0; i < optionsFile.getContents().size(); i++) {
            String line = (String) optionsFile.getContents().get(i);
            String value = "";
            int delimIndex = line.indexOf("#");
            value = line.substring(delimIndex + 1);
            
            int index = line.indexOf(OPT_HEADER);
            if (index > -1) {
                copyHeaderFiles = IcofStringUtil.stringToBoolean(value);
            }
            
            index = line.indexOf(OPT_MSGCAT);
            if (index > -1) {
                compileMsgcatFiles = IcofStringUtil.stringToBoolean(value);
            }

            index = line.indexOf(OPT_BUILD32);
            if (index > -1) {
                setCommandFor32(value);
            }

            index = line.indexOf(OPT_BUILD64);
            if (index > -1) {
                setCommandFor64(value);
            }

        }

    }

    
    /**
     * Enable or disable buttons.
     * 
     * @param enableThem If true enable the buttons otherwise disable them.
     */
    private void enableButtons(boolean enableThem) {
        closeButton.setEnabled(enableThem);
        executeButton.setEnabled(enableThem);
        reloadButton.setEnabled(enableThem);
        saveButton.setEnabled(enableThem);
        selectAllButton.setEnabled(enableThem);
        
        if (enableThem && (runner != null)) {
            showLogsButton.setEnabled(true);
        }
        else {
            showLogsButton.setEnabled(false);
        }
    
    }
    
    /**
     * Members.
     */
    private final static int SCREEN_WIDTH = 850;
    private final static int SCREEN_HEIGHT = 670;
    private final static int SMALL_COL_WIDTH = 120;
    private final static int LARGE_COL_WIDTH = 400;
    
    private final static String OPT_HEADER = "HEADER";
    private final static String OPT_MSGCAT = "MSGCAT";
    private final static String OPT_BUILD32 = "BUILD_32";
    private final static String OPT_BUILD64 = "BUILD_64";
    
    private Display display;
    private ApplicationWindow window;
    private static Shell shell;
    private TkPatch patch;
    private boolean copyHeaderFiles = false;    
    private boolean compileMsgcatFiles = false;
    private boolean areAllCommandsComplete = false;
    private String command32 = "";
    private String command64 = "";
    private String commandDirectory = "";
    private String cmvcRelease = "";
    private IcofFile optionsFile = null;
        
    private Text commandDirectoryText;
    private Text command32Text;
    private Text command64Text;
    private Table platformTable;
    private Button copyHeadersButton;
    private Button compileMsgcatsButton;
    private Button selectAllButton;
    private Button saveButton;
    private Button reloadButton;
    private Button executeButton;
    private Button closeButton;
    private Button showLogsButton;
    private Text console;
    private TkCommandRunner runner;


    /**
     * Getters.
     */
    private ApplicationWindow getWindow() { return window; }
    private TkPatch getPatch() { return patch; }
    private boolean copyHeaderFiles() { return copyHeaderFiles; }
    private boolean compileMsgcatFiles() { return compileMsgcatFiles; }
    private String getCommandFor32() { return command32; }   
    private String getCommandFor64() { return command64; }
    private String getCommandDirectory() { return commandDirectory; }
    private String getCmvcRelease() { return cmvcRelease; }


    /**
     * Setters.
     */
    private void setWindow(ApplicationWindow aWindow) { window = aWindow; }
    private void setShell() { shell = new Shell(getWindow().getShell()); }
    private void setPatch(TkPatch aPatch) { patch = aPatch; }
    private void setCopyHeaderFiles(boolean aFlag) { copyHeaderFiles = aFlag;  }
    private void setCompileMsgcatFiles(boolean aFlag) { compileMsgcatFiles = aFlag;  }
    private void setCommandFor32(String aCommand) { command32 = aCommand; }
    private void setCommandFor64(String aCommand) { command64 = aCommand; }
    public void setCommandDirectory(String aDir) { commandDirectory = aDir;  }
    private void setCmvcRelease(String aRelease) { cmvcRelease = aRelease;  }


    // -----------------------------------------------------------------------------
    /**
     * Execute system command without any retry logic.
     * 
     * This method will create a String array containing three elements: (for
     * AIX) (for Windows) -- "/bin/sh" -- "cmd" -- "-c" -- "/c" -- command --
     * command
     * 
     * It will then call another execSystemCommand method, passing the String
     * array. Doing so ensures that all system calls are executed in a
     * consistent manner.
     * 
     * @param command
     *            a String containing the command to be executed
     * @param errorMsg
     *            a StringBuffer that will return the contents of stdErr and
     *            stdOut if the command produces a non-zero return code.
     * @param results
     *            a Vector containing all text written to stdOut as a result of
     *            executing the command.
     * 
     * @return the return code from the command.
     * 
     * @exception IcofException
     *                Problem executing system call.
     */
    // -----------------------------------------------------------------------------
    public static synchronized void execSystemCommand(String command,
        StringBuffer errorMsg, Vector<String> results) throws IcofException {

        execSystemCommand(command, errorMsg, results, false);

    }

    
    // -----------------------------------------------------------------------------
    /**
     * Execute system command.
     * 
     * This method will create a String array containing three elements: (for
     * AIX) (for Windows) -- "/bin/sh" -- "cmd" -- "-c" -- "/c" -- command --
     * command
     * 
     * It will then call another execSystemCommand method, passing the String
     * array. Doing so ensures that all system calls are executed in a
     * consistent manner.
     * 
     * @param command
     *            a String containing the command to be executed
     * @param errorMsg
     *            a StringBuffer that will return the contents of stdErr and
     *            stdOut if the command produces a non-zero return code.
     * @param results
     *            a Vector containing all text written to stdOut as a result of
     *            executing the command.
     * @param retry
     *            true to use retry logic; false to try only once
     * 
     * @return the return code from the command.
     * 
     * @exception IcofException
     *                Problem executing system call.
     */
    // -----------------------------------------------------------------------------
    public static synchronized void execSystemCommand(String command,
                                                      StringBuffer errorMsg,
                                                      Vector<String> results, boolean retry)
        throws IcofException {

        Vector<String> shell = IcofSystemUtil.determineCommandShell();
        shell.add(command);
        String[] commandArray = new String[shell.size()];
        for (int i = 0; i < shell.size(); i++) {
            commandArray[i] =  shell.elementAt(i);
        }

        execSystemCommand(commandArray, errorMsg, results);

    }

    // -----------------------------------------------------------------------------
    /**
     * Execute system command.
     * 
     * This method will create a String array containing three elements: (for
     * AIX) (for Windows) -- "/bin/sh" -- "cmd" -- "-c" -- "/c" -- command --
     * command
     * 
     * It will then call another execSystemCommand method, passing the String
     * array. Doing so ensures that all system calls are executed in a
     * consistent manner.
     * 
     * @param command
     *            a String containing the command to be executed
     * @param errorMsg
     *            a StringBuffer that will return the contents of stdErr and
     *            stdOut if the command produces a non-zero return code.
     * @param results
     *            a Vector containing all text written to stdOut as a result of
     *            executing the command.
     * @param retry
     *            true to use retry logic; false to try only once
     * 
     * @return the return code from the command.
     * 
     * @exception IcofException
     *                Problem executing system call.
     */
    // -----------------------------------------------------------------------------
    public static synchronized void execSystemCommand(String[] commandArray,
                                                         StringBuffer errorMsg,
                                                         Vector<String> results)
    throws IcofException {

        // Clear the results vector
        if (!results.isEmpty()) {
            results.clear();
        }

        Runtime runTime = Runtime.getRuntime();
        try {
            runTime.exec(commandArray);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }
    
}
