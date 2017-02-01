/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2009 -- IBM Internal Use Only
*
*=============================================================================
*
* CREATOR: Gregg Stadtlander
*    DATE: 11/09/2009
*
*-PURPOSE---------------------------------------------------------------------
* User interface class to capture ToolKit injection data.
*-----------------------------------------------------------------------------
*
*-CHANGE LOG------------------------------------------------------------------
* 11/09/2009 GFS  Initial coding.
* 02/01/2010 GFS  Removed the Edit menu and pop up which allowed removing 
*                 source files from the injection. Gave all the text boxes 
*                 borders for an improved user experience.
* 02/09/2010 GFS  Updated setMissingBuildTracks() to search build and dev for
*                 the LEVELHIST file.
* 03/04/2010 GFS  Fixed a bug in the shipb injection processing.  Merged 
*                 changes from bugfix and test streams. 
* 03/09/2010 GFS  Moved setMissingTracks() to the "Query CMVC" button actions.
* 04/26/2010 GFS  Changed enableButtons() method to disable "Mark Built" button
*                 if tk_patch state is not Ready.
* 09/28/2010 GFS  Added support for writing to the .update file when the
*                 patch is marked built.
* 10/12/2010 GFS  Changed some text fields.    
* 10/27/2010 GFS  Changed button text to black.  
* 03/15/2012 GFS  Updated to use new version of TkInjectUtils.getQueryString()
*                 which does a better job of handling special chars in id/pw.
* 04/09/2012 GFS  Removed support for shipb and added support for xtinct.
* 05/08/2012 GFS  Added support for second xtinct area.  
* 05/10/2012 GFS  Updated target directories with tk versions.   
* 08/30/2012 GFS  Updated to check for duplicate changed files every time the
*                 user edits the inject locations to ensure no new duplicate
*                 files where added during the edit process.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.app.injectBuild;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.ibm.stg.eda.component.jfacebase.ExitAction;
import com.ibm.stg.eda.component.jfacebase.GridUtils;
import com.ibm.stg.eda.component.jfacebase.ShowAboutAction;
import com.ibm.stg.eda.component.jfacebase.ShowHelpAction;
import com.ibm.stg.eda.component.tk_levelhist.CMVC;
import com.ibm.stg.eda.component.tk_levelhist.CMVC_Track;
import com.ibm.stg.eda.component.tk_patch.TkInjectRequest;
import com.ibm.stg.eda.component.tk_patch.TkInjectUtils;
import com.ibm.stg.eda.component.tk_patch.TkPatch;
import com.ibm.stg.eda.component.tk_patch.TkSource;
import com.ibm.stg.iipmds.common.Constants;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofFile;
import com.ibm.stg.iipmds.common.IcofStringUtil;
import com.ibm.stg.iipmds.common.IcofSystemUtil;
import com.ibm.stg.iipmds.common.SessionLog;
import com.ibm.stg.iipmds.icof.component.clearquest.cqFetchOutput;
import com.ibm.stg.iipmds.icof.component.mom.AppContext;
import com.ibm.stg.iipmds.icof.component.util.IcofUtil;
import com.ibm.stg.iipmds.icof.webclient.clearquest.CqClientUtil;
import com.ibm.stg.iipmds.icof.webclient.clearquest.CqService_PortType;
import com.ibm.stg.iipmds.icof.webclient.clearquest.CqService_PortTypeProxy;

public class InjectBuildUI extends ApplicationWindow {

    /**
     * Constants.
     */
    final static String APP_NAME = "injectBuild";
    final static String APP_VERSION = "3.0";
    final static String APP_OWNER = "Gregg Stadtlander";
    final static String HELP_FILE = "help_text/injectBuild_help.txt";
    
    
    /**
     * Constructor
     */
    public InjectBuildUI() {
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
        if (!debug) {
            shell.setText("EDA Tool Kit Injection Build");
        }
        else {
            shell.setText("EDA Tool Kit Injection Build -- DEBUG MODE ON");
        }
        shell.setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        display = shell.getDisplay();

        // Define the status line manager.
        //statusLM = getStatusLineManager();

        // Define the Grid layout
        Composite gridComposite = new Composite(shell, SWT.BORDER);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = GRID_COLS_WIDE;
        gridComposite.setLayout(gridLayout);

        // Create the TK Patch Input widgets.
        setPatchInputs(gridComposite, GRID_COLS_WIDE);

        // Create the CMVC and target location input widgets.
        setCmvcTargetInputs(gridComposite, GRID_COLS_WIDE);
        
        // Create the Injection Request widgets.
        setRequestInput(gridComposite, GRID_COLS_WIDE);

        // Create the Console widget.
        setConsoleDisplay(gridComposite, GRID_COLS_WIDE);
        
        // Initialize widget contents.
        enableReadPatchButton();
        enableSelectedButtons();
        
        // Verify this machine can access CMVC
        setCmvc();
        
        return parent;
        
    }

    
    /**
     * Create the CMVC object and determine if this machine has access to
     * CMVC.
     */
    private void setCmvc() {

        // Create the CMVC object.
        cmvc = new CMVC();
        
        // Warn the user if no CMVC access
        if (! cmvc.isActive()) {
            // Display the error dialog
            MessageDialog.openError(shell, "Error",
                                    "Unable to access CMVC from this machine.");
        }
        
    }


    /**
     * Define the new/seed DIP information input.
     * 
     * @param comp       Parent composite widget which will hold this widget.
     * @param gridWidth  Number of columns in current grid.
     */
    private void setPatchInputs(Composite comp, int gridWidth) {

        // Create the first group.
        Group group = new Group(comp, SWT.SHADOW_IN);
        group.setText("Tool Kit Patch");

        // Make it have a grid layout
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        group.setLayout(gridLayout);

        // Place the first group in the grid composite.
        GridData data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        data.horizontalSpan = gridWidth / 2;
        group.setLayoutData(data);

        // Create the second group.
        Group group2 = new Group(comp, SWT.SHADOW_IN);
        group2.setText("Patch Data");

        // Make it have a grid layout
        GridLayout gridLayout2 = new GridLayout();
        gridLayout2.numColumns = 2;
        group2.setLayout(gridLayout2);

        // Place the second group in the grid composite.
        data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        data.horizontalSpan = gridWidth / 2;
        group2.setLayoutData(data);

        // Load the new and seed dip input widgets.
        setPatchInput(group, gridWidth);
        setPatchData(group2, gridWidth);

    }
    
    
    /**
     * Define and layout the ToolKit Release data widgets.
     * 
     * @param comp       The parent composite widget which will hold this widget.
     * @param gridWidth  Number of columns in current grid.
     */
    private void setPatchInput(Group group, int gridWidth) {

        // Create the user id label.
        Label thisLabel = new Label(group, SWT.NONE);
        thisLabel.setText("Intranet id");

        GridData data = new GridData();
        data.horizontalSpan = 1;
        thisLabel.setLayoutData(data);

        // Create the user id Text box.
        useridText = new Text(group, SWT.BORDER);
        useridText.setBackground(GridUtils.getWhite(display));
        useridText.setForeground(GridUtils.getBlack(display));
        useridText.setText("");
        
        useridText.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent event) {
                enableReadPatchButton();
            }
          });
        
        data = new GridData();
        data.horizontalSpan = 1;
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = SWT.FILL;
        useridText.setLayoutData(data);
        
        // Create the password label.
        thisLabel = new Label(group, SWT.NONE);
        thisLabel.setText("Intranet pw");

        data = new GridData();
        data.horizontalSpan = 1;
        thisLabel.setLayoutData(data);

        // Create the password Text box.
        passwordText = new Text(group, SWT.BORDER | SWT.PASSWORD);
        passwordText.setBackground(GridUtils.getWhite(display));
        passwordText.setForeground(GridUtils.getBlack(display));
        passwordText.setCursor(null);
        passwordText.setText("");

        passwordText.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent event) {
                enableReadPatchButton();
            }
          });

        
        data = new GridData();
        data.horizontalSpan = 1;
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = SWT.FILL;
        passwordText.setLayoutData(data);
        
        // Create the patch label.
        thisLabel = new Label(group, SWT.NONE);
        thisLabel.setText("TK Patch #");

        data = new GridData();
        data.horizontalSpan = 1;
        thisLabel.setLayoutData(data);

        // Create the component Text box.
        patchText = new Text(group, SWT.BORDER);
        patchText.setBackground(GridUtils.getWhite(display));
        patchText.setForeground(GridUtils.getBlack(display));
        patchText.setText("");

        patchText.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent event) {
                enableReadPatchButton();
            }
          });

        data = new GridData();
        data.horizontalSpan = 1;
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = SWT.FILL;
        patchText.setLayoutData(data);

        // Create the Get Tracks button.
        readPatchButton = new Button(group, SWT.PUSH);
        readPatchButton.setText("Read Patch");
        readPatchButton.setBackground(GridUtils.getGreen(display));
        readPatchButton.setForeground(GridUtils.getBlack(display));
        readPatchButton.setEnabled(false);
        readPatchButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {

                Runnable runnable = new Runnable() {
                    public void run() {
                      clearPatchData();
                      clearInjectRequestsData();
                      try { 
                          fetchPatchData();
                      }
                      catch (Exception ex) {
                          // Display the error dialog
                          MessageDialog.openError(shell, "Error",
                                                  "Unable fetch data for specified" +
                                                  " TK Patch from CQ.\n\n" +
                                                  "Please verify your CQ user id, " +
                                                  "password and Patch number are correct.");
                          //ex.printStackTrace();
                          return;
                      }
                      try { 
                          loadCmvcInputs();
                          setCmvcQueried(false);
                          setRequestsSelected(false);
                          enableSelectedButtons();
                      }
                      catch (Exception ex) {
                          // Display the error dialog
                          MessageDialog.openError(shell, "Error",
                                                "Message: " + ex.getMessage());
                      }

                      loadPatchData();
                      //loadInjectRequestsData();
                    }
                  };
                  console.append("Querying ClearQuest for Patch data ...\n");
                  BusyIndicator.showWhile(null, runnable);
                  console.append("CQ query complete.\n");
                  console.append("Please review CMVC release and click \"Read changed files ...\" button.\n");
            }
        });
        
        // Place it in the grid composite.
        data = new GridData();
        data.horizontalSpan = 2;
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        readPatchButton.setLayoutData(data);
        
    }

    
    /**
     * Define and layout the ToolKit Release data widgets.
     * 
     * @param comp       The parent composite widget which will hold this widget.
     * @param gridWidth  Number of columns in current grid.
     */
    private void setPatchData(Group group, int gridWidth) {

        // Create the component label.
        Label thisLabel = new Label(group, SWT.NONE);
        thisLabel.setText("Tool");

        GridData data = new GridData();
        data.horizontalSpan = 1;
        thisLabel.setLayoutData(data);

        // Create the tool Text box.
        toolText = new Text(group, SWT.BORDER|SWT.READ_ONLY);
        toolText.setBackground(GridUtils.getGray(display));
        toolText.setForeground(GridUtils.getBlack(display));
        
        data = new GridData();
        data.horizontalSpan = 1;
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        toolText.setLayoutData(data);

        // Create the version label.
        thisLabel = new Label(group, SWT.NONE);
        thisLabel.setText("Tool Version");

        data = new GridData();
        data.horizontalSpan = 1;
        thisLabel.setLayoutData(data);

        // Create the version Text box.
        versionText = new Text(group, SWT.BORDER | SWT.READ_ONLY);
        versionText.setBackground(GridUtils.getGray(display));
        versionText.setForeground(GridUtils.getBlack(display));
        
        data = new GridData();
        data.horizontalSpan = 1;
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        versionText.setLayoutData(data);

        // Create the State label.
        thisLabel = new Label(group, SWT.NONE);
        thisLabel.setText("Patch State");

        data = new GridData();
        data.horizontalSpan = 1;
        thisLabel.setLayoutData(data);

        // Create the state Text box.
        stateText = new Text(group, SWT.BORDER | SWT.READ_ONLY);
        stateText.setBackground(GridUtils.getGray(display));
        stateText.setForeground(GridUtils.getBlack(display));
        
        data = new GridData();
        data.horizontalSpan = 1;
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        stateText.setLayoutData(data);
        
        // Create the injector label.
//        thisLabel = new Label(group, SWT.NONE);
//        thisLabel.setText("Injector");
//
//        data = new GridData();
//        data.horizontalSpan = 1;
//        thisLabel.setLayoutData(data);

        // Create the injector Text box.
//        injectorText = new Text(group, SWT.BORDER | SWT.READ_ONLY);
//        injectorText.setBackground(GridUtils.getGray(display));
//        injectorText.setForeground(GridUtils.getBlack(display));
//        
//        data = new GridData();
//        data.horizontalSpan = 1;
//        data.horizontalAlignment = SWT.FILL;
//        data.grabExcessHorizontalSpace = true;
//        injectorText.setLayoutData(data);
        
    }

    
    /**
     * Define the CMVC and Target location inputs.
     * 
     * @param comp       Parent composite widget which will hold this widget.
     * @param gridWidth  Number of columns in current grid.
     */
    private void setCmvcTargetInputs(Composite comp, int gridWidth) {

        // Create the first group.
        Group group = new Group(comp, SWT.SHADOW_IN);
        group.setText("CMVC Release");

        // Make it have a grid layout
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        group.setLayout(gridLayout);

        // Place the first group in the grid composite.
        GridData data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        data.horizontalSpan = gridWidth / 2;
        group.setLayoutData(data);

        // Create the second group.
        Group group2 = new Group(comp, SWT.SHADOW_IN);
        group2.setText("Injection Target Location");

        // Make it have a grid layout
        GridLayout gridLayout2 = new GridLayout();
        gridLayout2.numColumns = 2;
        group2.setLayout(gridLayout2);

        // Place the second group in the grid composite.
        data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        data.horizontalSpan = gridWidth / 2;
        group2.setLayoutData(data);

        // Load the new and seed dip input widgets.
        setCmvcInput(group, gridWidth);
        setTargetData(group2, gridWidth);

    }


    /**
     * Define the CMVC widgets.
     * @param group
     * @param gridWidth
     */
    private void setCmvcInput(Group group, int gridWidth) {
        
        // Create the CMVC release Text box.
        cmvcReleaseText = new Text(group, SWT.BORDER);
        cmvcReleaseText.setBackground(GridUtils.getWhite(display));
        cmvcReleaseText.setForeground(GridUtils.getBlack(display));
        
        GridData data = new GridData();
        data.horizontalSpan = gridWidth;
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        cmvcReleaseText.setLayoutData(data);

        
        // Create the use shipb release checkbox.
        useShipbButton = new Button(group, SWT.CHECK);
        useShipbButton.setText("Use \"shipb\" CMVC release");
        useShipbButton.setEnabled(false);
        useShipbButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                changeCmvcRelease();
            }
        });
        
        data = new GridData();
        data.horizontalSpan = gridWidth;
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        useShipbButton.setLayoutData(data);

        
        // Create the Get Tracks button.
        readCmvcButton = new Button(group, SWT.PUSH);
        readCmvcButton.setText("Read changed files from CMVC");
        readCmvcButton.setBackground(GridUtils.getGreen(display));
        readCmvcButton.setForeground(GridUtils.getBlack(display));
        readCmvcButton.setEnabled(false);
        readCmvcButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {

                Runnable runnable = new Runnable() {
                    public void run() {
                        try { 
                            fetchFileInfoCmvc();
                            setInjectionLocation();
                        }
                        catch (Exception ex) {
                            // Display the error dialog
                            MessageDialog.openError(shell, "Error",
                                                    "Unable fetch track/file data" +
                                                    " from CMVC.\n\n" +
                                                    "Message: " + ex.getMessage());
                            ex.printStackTrace();
                            return;
                        }
                        try { 
                            setCmvcQueried(true);
                            setRequestsSelected(false);
                            setPatchSourceLocations();
                            enableSelectedButtons();
                            loadInjectRequestsData();
                            setDuplicateChangedFiles();
                            setMissingBuildTracks();                            
                        }
                        catch (Exception ex) {
                            // Display the error dialog
                            MessageDialog.openError(shell, "Error",
                                                  "Message: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                        
                    }

                };
                console.append("Querying CMVC for changed files ...\n");
                BusyIndicator.showWhile(null, runnable);
                console.append("CMVC query complete - please review results\n");
            }
        });
        
        // Place it in the grid composite.
        data = new GridData();
        data.horizontalSpan = 2;
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        readCmvcButton.setLayoutData(data);
        
    }

    
    /*
     * Change the CMVC release - toggle between build and shipb release 
     */
    private void changeCmvcRelease() {

        // Set the release name.
        if (useShipbButton.getSelection() == true) {
            cmvc.setRelease(cmvc.getShipbRelease());
        }
        else {
            cmvc.setRelease(cmvc.getBuildRelease());
        }
        cmvcReleaseText.setText(cmvc.getRelease());
        patch.setRelease(cmvc.getRelease());

        
        // Reset the tracks.
        patch.clearTrackList();
        
        // Reset the list of tracks.
        if (patch.getDuplicateFiles() != null)
            patch.getDuplicateFiles().clear();
        patch.initializeMissingBuildTracks();
        
        // Reset the read from CMVC flag;
        setCmvcQueried(false);
        
        // Clear the Inject Request table.
        clearInjectRequestsData();
        
        // Reset the buttons.
        enableSelectedButtons();
        
    }
    
        
    /*
     * Determine if the same file is updated in multiple injection requests. 
     */
    private void setDuplicateChangedFiles() {
        
        // Initialize the changed file collection.
        patch.setDuplicateFiles();
                
        // Warn the user if the same changed file is updated in multiple
        // injection requests.
        if (patch.getDuplicateFiles().size() > 0) {
            StringBuffer message = new StringBuffer();
            message.append("The following changed file(s) were found in multiple "
                           + "injection requests.\n");
            
            Iterator<String> iter = patch.getDuplicateFiles().keySet().iterator();
            while (iter.hasNext()) {
                String path =  iter.next();
                String requests =  patch.getDuplicateFiles().get(path);
                message.append(" * " + path + " -- " + requests + "\n");
            }
            
            MessageDialog.openWarning(shell, "Warning",  message.toString());
            
        }
        
    }

    /**
     * @param group2
     * @param gridWidth
     */
    private void setTargetData(Group group2, int gridWidth) {
        
        // Create the tkb Target radio button.
        tkbLocationButton = new Button(group2, SWT.RADIO);
        tkbLocationButton.setText("tkb (17.1.20)");
        
        GridData data = new GridData();
        data.horizontalSpan = gridWidth / 2;
        tkbLocationButton.setLayoutData(data);
        tkbLocationButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                setInjectionLocation();
                enableSelectedButtons();
            }
        });

        
        // Create the xtinct Target radio button.
        xtinctLocationButton = new Button(group2, SWT.RADIO);
        xtinctLocationButton.setText("xtinct (17.1.19)");
        
        data = new GridData();
        data.horizontalSpan = gridWidth / 2;
        xtinctLocationButton.setLayoutData(data);
        xtinctLocationButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                setInjectionLocation();
                enableSelectedButtons();
            }
        });

        // Create the xtinct(venice) Target radio button.
        xtinct2LocationButton = new Button(group2, SWT.RADIO);
        xtinct2LocationButton.setText("xtinct/venice (17.1.20V)");

        data = new GridData();
        data.horizontalSpan = gridWidth / 2;
        xtinct2LocationButton.setLayoutData(data);
        xtinct2LocationButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                setInjectionLocation();
                enableSelectedButtons();
            }
        });        
        
        // Create the injection location text box.
        locationText = new Text(group2, SWT.BORDER | SWT.READ_ONLY);
        locationText.setBackground(GridUtils.getGray(display));
        locationText.setForeground(GridUtils.getBlack(display));
        locationText.setText("");
        
        data = new GridData();
        data.horizontalSpan = gridWidth;
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = SWT.FILL;
        locationText.setLayoutData(data);
        
    }


    /**
     * Define and layout the Injection data widgets.
     * 
     * @param comp       The parent composite widget which will hold this widget.
     * @param gridWidth  Number of columns in current grid.
     */
    private void setRequestInput(Composite comp, int gridWidth) {

        // Create the group.
        Group group = new Group(comp, SWT.SHADOW_IN);
        group.setText("Injection Requests");

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

        // Create the Injection Request table.
        requestTree = new Tree(group, SWT.CHECK | SWT.BORDER);
        requestTree.setBackground(GridUtils.getWhite(display));
        requestTree.setForeground(GridUtils.getBlack(display));

        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = gridWidth;
        data.verticalSpan = 5;
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        data.heightHint = 100;
        requestTree.setLayoutData(data);
        
        requestTree.addListener (SWT.Selection, new Listener () {
            public void handleEvent (Event e) {
                TreeItem [] allRows = requestTree.getItems();
                boolean enable = false;
                for (int i = 0; (i < allRows.length) && (enable != true); i++ ) {
                    if (allRows[i].getChecked() == true)
                        enable = true;
                    TreeItem [] children = allRows[i].getItems();
                    for (int j = 0; (j < children.length) && (enable != true); j++ ) {
                        if (children[j].getChecked() == true)
                            enable = true;
                    }

                }
                setRequestsSelected(enable);
                enableSelectedButtons();

            }
        });
       
        
        // Create the Select All button
        selectAllButton = new Button(group, SWT.CHECK);
        selectAllButton.setText("Select all Injection Requests");
        selectAllButton.setEnabled(false);
        selectAllButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                loadInjectRequestsData();
            }
        });

        data = new GridData();
        data.horizontalSpan = gridWidth;
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        selectAllButton.setLayoutData(data);

        
        // Create the Edit button.
        editSelectedButton = new Button(group, SWT.PUSH);
        editSelectedButton.setText("Edit selected");
        editSelectedButton.setBackground(GridUtils.getGreen(display));
        editSelectedButton.setForeground(GridUtils.getBlack(display));
        editSelectedButton.setEnabled(false);
        editSelectedButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                Runnable runnable = new Runnable() {
                    public void run() {
                        try {
                            editRequests(false);
                        }
                        catch (Exception ex) {
                            System.out.println(IcofException.printStackTraceAsString(ex));
                            // Display the error dialog
                            MessageDialog.openError(shell, "Error",
                                                    "Unable to edit specified" +
                                                    " Injection Request\n " +
                                                    "Message: " + ex.getMessage());
                            return;
                        }
                        
                    }
                };
                console.append("Editting Injection Requests ...\n");
                BusyIndicator.showWhile(null, runnable);
                console.append("Edits complete\n");
            }
        });

        
        
        // Place it in the grid composite.
        data = new GridData();
        data.horizontalSpan = 1;
        data.horizontalAlignment = SWT.CENTER;
        data.grabExcessHorizontalSpace = true;
        editSelectedButton.setLayoutData(data);

        // Create the Start Injections button.
        startInjectButton = new Button(group, SWT.PUSH);
        startInjectButton.setText("Copy source for selected requests");
        startInjectButton.setBackground(GridUtils.getGreen(display));
        startInjectButton.setForeground(GridUtils.getBlack(display));
        startInjectButton.setEnabled(false);
        startInjectButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                Runnable runnable = new Runnable() {
                    public void run() {
                        try {
                            injectSelectedRequests();
                        }
                        catch(Exception ex) {
                            // Display the error dialog
                            MessageDialog.openError(shell, "Error",
                                                    "Unable to inject specified" +
                                                    " Injection Requests\n " +
                                                    "Message: " + ex.toString());
                            ex.printStackTrace(System.out);
                            //myContext.getSessionLog().log(ie)

                            return;
                        }

                    }
                };
                console.append("Copying source files ...\n");
                BusyIndicator.showWhile(null, runnable);
                
            }
        });

        // Place it in the grid composite.
        data = new GridData();
        data.horizontalSpan = 1;
        data.horizontalAlignment = SWT.CENTER;
        //data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        startInjectButton.setLayoutData(data);
    
        
        // Create the Edit button.
        rebuildSelectedButton = new Button(group, SWT.PUSH);
        rebuildSelectedButton.setText("Rebuild selected");
        rebuildSelectedButton.setBackground(GridUtils.getGreen(display));
        rebuildSelectedButton.setForeground(GridUtils.getBlack(display));
        rebuildSelectedButton.setEnabled(false);
        rebuildSelectedButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                Runnable runnable = new Runnable() {
                    public void run() {
                        try {
                            rebuildSelectedRequests();
                        }
                        catch (Exception ex) {
                            System.out.println(IcofException.printStackTraceAsString(ex));
                            // Display the error dialog
                            MessageDialog.openError(shell, "Error",
                                                    "Unable to rebuild selected " +
                                                    "Injection Request.\n " +
                                                    "Message: " + ex.getMessage());
                            return;
                        }
                        
                    }
                };
                console.append("Rebuilding Injection Requests ...\n");
                BusyIndicator.showWhile(null, runnable);
                console.append("Rebuilding complete\n");
            }
        });

        // Place it in the grid composite.
        data = new GridData();
        data.horizontalSpan = 1;
        data.horizontalAlignment = SWT.CENTER;
        data.grabExcessHorizontalSpace = true;
        rebuildSelectedButton.setLayoutData(data);
        

        // Create the Mark Built button.
        markBuiltButton = new Button(group, SWT.PUSH);
        markBuiltButton.setText("Mark Patch Built");
        markBuiltButton.setBackground(GridUtils.getGreen(display));
        markBuiltButton.setForeground(GridUtils.getBlack(display));
        markBuiltButton.setEnabled(false);
        markBuiltButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                Runnable runnable = new Runnable() {
                    public void run() {
                        try {
                            markRequestBuilt();
                            enableSelectedButtons();
                        }
                        catch (Exception ex) {
                            System.out.println(IcofException.printStackTraceAsString(ex));
                            // Display the error dialog
                            MessageDialog.openError(shell, "Error",
                                                    "Unable to set Tk Patch " +
                                                    "state to Built.\n " +
                                                    "Message: " + ex.getMessage());
                            return;
                        }
                        
                    }
                };
                console.append("Marking Patch built ...\n");
                BusyIndicator.showWhile(null, runnable);
                console.append("Update complete\n");
            }
        });

        // Place it in the grid composite.
        data = new GridData();
        data.horizontalSpan = 1;
        data.horizontalAlignment = SWT.CENTER;
        data.grabExcessHorizontalSpace = true;
        markBuiltButton.setLayoutData(data);

        
    }
    
    
    

    /*
     * Disable or enable the "Read Patch" buttons
     * 
     */
    private void enableReadPatchButton() {
        
        if (useridText.getText().equals("") ||
            passwordText.getText().equals("") ||
            patchText.getText().equals("")) {
            readPatchButton.setEnabled(false);
        }
        else {
            readPatchButton.setEnabled(true);
        }

    }
    
    
    /*
     * Disable or enable the "selected" buttons
     * 
     * @param enableFlag  If true set to disabled
     */
    private void enableSelectedButtons() {
        
        // The Read ... from CMVC button is only disabled at the start when
        // there's not patch defined.
        if (patch == null) {
            readCmvcButton.setEnabled(false);
            useShipbButton.setEnabled(false);
            markBuiltButton.setEnabled(false);
            
        }
        else {
            readCmvcButton.setEnabled(cmvc.isActive());
            if ((patch != null) && (patch.getState().equals("Ready"))) {
                markBuiltButton.setEnabled(true);
            }
            else {
                markBuiltButton.setEnabled(false);
            }
            if (cmvc.isActive()) {
                useShipbButton.setEnabled(cmvc.hasShipbRelease());
            }
            else {
                useShipbButton.setEnabled(false);
            }
        }
        
        // If CMVC has not been queried then remaining buttons are disabled.
        if (! isCmvcQueried()) {
            selectAllButton.setEnabled(false);
            editSelectedButton.setEnabled(false);
            startInjectButton.setEnabled(false);
            
            // switch back to false when UI testing is completed
            rebuildSelectedButton.setEnabled(false);
            //rebuildSelectedButton.setEnabled(true);
        }
        
        // Determine if any requests have been selected.
        else {
            selectAllButton.setEnabled(true);
            editSelectedButton.setEnabled(areRequestsSelected());

            // Turn off this button if no requests selected
            if (! areRequestsSelected()) {
                startInjectButton.setEnabled(areRequestsSelected());
                rebuildSelectedButton.setEnabled(areRequestsSelected());
            }
            
            // Otherwise turn on the button only if the inject location is set
            // and this patch is in the "Ready" state. For debug disable the 
            // state check.
            else {
                if (debug) {  
                    if ((patch == null) ||
                        (patch.getTargetLocation() == null) || 
                         patch.getTargetLocation().equals("")) {
                        startInjectButton.setEnabled(false);
                        rebuildSelectedButton.setEnabled(false);
                    }
                    else {
                        startInjectButton.setEnabled(true);
                        if ((patch != null) && (patch.getState().equals("Ready"))) {
                            rebuildSelectedButton.setEnabled(true);
                        }
                        else {
                            rebuildSelectedButton.setEnabled(false);
                        }
                    }
                }
                else {
                    if ((patch == null) ||
//                        (patch.getState() == null) ||
//                        (! patch.getState().equals("Ready")) ||
                        (patch.getTargetLocation() == null) ||
                        patch.getTargetLocation().equals("")) {
                        startInjectButton.setEnabled(false);
                        rebuildSelectedButton.setEnabled(false);
                    }
                    else {
                        startInjectButton.setEnabled(true);
                        rebuildSelectedButton.setEnabled(true);
                    }
                    
                }

            }
        }
        
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
        console.setText("Enter Tool Kit Patch data and click \"Read Patch\" button\n");

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
            new InjectBuildUI().run();

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
     * Parses and verifies the command line arguments.
     * 
     * @param argv              The command line arguments.
     * @throws Exception 
     * @throws Exception 
     */
    private void fetchPatchData() throws Exception {

        // Prepare the XML file
        String sQuery = TkInjectUtils.getQueryString(useridText.getText(),
                                                     passwordText.getText(),
                                                     getPatchName());
        
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
        if (patch != null) {
            patch = null;
        }
        patch = new TkPatch(labels, objects);
        
    }

    
    /**
     * Parses and verifies the command line arguments.
     * 
     * @param argv              The command line arguments.
     * @throws Exception 
     * @throws Exception 
     */
    private void markRequestBuilt() throws Exception {

        // Verify the inject target location is set.
    	if ((patch == null) ||
    		(patch.getTargetLocation() == null) || 
    		patch.getTargetLocation().equals("")) {
    		MessageDialog.openInformation(shell, "Invalid Injection Target Location",
    		                              "Please select an Injection Target " +
    		"Location before marking this patch built.");
    		return;
    	}
        
        // Get confirmation from the user.
        boolean confirm =  MessageDialog.openConfirm(shell, 
                                                    "Take BuildComplete action?",
                                                    "Are you sure you'd like to mark this " +
                                                    "TK Patch as BuildComplete?");
        if (! confirm) {
            console.append("\nBuildComplete action canceled by user");
            return;
        }

        // Update the .update file in the target directory.
        patch.setUpdateToBuilt(cmvc, debug);
        console.append("\nUpdating .update file ...");
        
        // Prepare the XML file
        String myUpdate = TkInjectUtils.getModifyString();
        myUpdate = myUpdate.replaceAll("##USERID##", useridText.getText());
        myUpdate = myUpdate.replaceAll("##PASSWORD##", passwordText.getText());
        myUpdate = myUpdate.replaceAll("##RECORD##", getPatchName());
        
        // Set the SSL properties.
        CqClientUtil.setSSLProperties();           

        // Call the service.
        CqService_PortTypeProxy proxy = new CqService_PortTypeProxy();
        CqService_PortType client = proxy.getCqService_PortType();
        proxy.setEndpoint(CqClientUtil.getServiceAddress(Constants.PROD));
        String sResult = client.update(myUpdate);

        // Let the user know the action completed or not.
        if (sResult.indexOf("<description>null</description>") > -1) {
          MessageDialog.openInformation(shell, "Build Complete",
                                        "TK Patch (" + patch.getId() + 
                                        ") was updated to BuildComplete.\n");
          // Update the State text field to Built 
          stateText.setText(TkPatch.STATE_BUILT);
          patch.setState(TkPatch.STATE_BUILT);
          console.append("\nTK Patch set to BuildComplete ...");
        }
        else {
            MessageDialog.openWarning(shell, "Warning",
                                      "Unable to take BuildComplete action.  " +
                                      "See log file at " + 
                                      patch.getLogFile().getAbsolutePath());
        }
        
    }


    /*
     * Query CMVC to get the files associated with each track to be injected. 
     */
    private void fetchFileInfoCmvc() throws IcofException {

        // Initialize the track collection.
        patch.initializeTrackList();
        patch.initializeMissingCmvcTracks();
        
        // Iterate through all tracks and read the track/file info from CMVC.
        Iterator<TkInjectRequest> iter = patch.getInjectRequests().values().iterator();
        while (iter.hasNext()) {
            TkInjectRequest request =  iter.next();
         
            Iterator<String> iter2 = request.getAllTracks().iterator();
            while (iter2.hasNext()) {
                String track =  iter2.next();
                
                // Read this track's data from CMVC.
                CMVC_Track myTrack = cmvc.readTrack(track);
                patch.addCmvcTrack(myTrack);
                if (myTrack.getAbstractText().equals("")) {
                    String entry = "  * Track " + track + " (" + 
                                   request.getId() + ")\n";
                    patch.getMissingCmvcTracks().append(entry);
                }
            }
            
        }

        
        // Warn the user if any tracks are missing from CMVC.
        if (patch.getMissingCmvcTracks().length() > 0) {
            MessageDialog.openWarning(shell, "Warning",
                                      "The following track(s) were not found in " +
                                      "this CMVC release.\n" +
                                      patch.getMissingCmvcTracks().toString());
        }
        
    }

    
    /**
     * Reads the patch number and returns it in MDCMS######## format 
     * 
     * @return Patch in MDCMS######## format
     */
    private String getPatchName() {
        
        // Read the Patch widget
        String patch = patchText.getText();
        
        if (patch.startsWith("MDCMS")) {
            return patch;
        }
        else {
            return "MDCMS" + IcofStringUtil.padString(patch, 8, "0", true);
        }

    }

    
    /**
     * Loads the TK Inject Request data into the UI.
     */
    public void loadInjectRequestsData() {
        
        // Turn off redraw to avoid flicker.
        requestTree.setRedraw(false);

        // Clear the data first.
        clearInjectRequestsData();
        
        // Load the tree with inject request data.
        if (patch != null) {
            Iterator<TkInjectRequest> iter = 
            	patch.getInjectRequests().values().iterator();
            while (iter.hasNext()) {
                TkInjectRequest request =  iter.next();

                TreeItem item = new TreeItem (requestTree, SWT.NONE);
                item.setText(request.getId() + " [Tracks: " + 
                             request.getAllTracksAsString() + "]");

                boolean foundError = false;
                if (request.getSourceFiles() != null) {
                    Iterator<TkSource> iter2 = request.getSourceFiles().values().iterator();
                    while (iter2.hasNext()) {
                        TkSource source =  iter2.next();

                        TreeItem child = new TreeItem (item, SWT.NONE);
                        if (source.isActive()) {
                            child.setText(source.getName() + " [Path: " + 
                                          source.getFullPath() + "]");
                        }
                        else {
                            child.setText(source.getName() + " [DO NOT INJECT]");
                        }

                        // Disable this file is not active
                        if (! source.isActive()) {
                            child.setBackground(GridUtils.getGray(display));
                        }

                        // Highlight this file if full path is not valid.
                        else if (! source.isPathValid()) {
                            child.setBackground(GridUtils.getYellow(display));
                            foundError = true;
                        }
                      
                    }
                }
                
                // If one of the files had a file not found warning then
                // set the request yellow. If CMVC has not been queried then
                // also set the request to yellow.
                if (foundError || !isCmvcQueried()) {
                    item.setBackground(GridUtils.getYellow(display));
                }

                // Select if Select All radio button is selected.
                if (selectAllButton != null) { 
                    item.setChecked(selectAllButton.getSelection());
                }
                
            }
        }

        // Turn on redraw.
        requestTree.setRedraw(true);
        requestTree.redraw();
        
        // Reset the buttons.
        setRequestsSelected(selectAllButton.getSelection());
        enableSelectedButtons();
        
    }
    

    /**
     * Clears the TK Inject Request date into the UI.
     */
    private void clearInjectRequestsData() {
        
        // Cycle through the inject requests.
        TreeItem[] rows = requestTree.getItems(); 
        for (int i = 0; i < rows.length; i++) {
            rows[i].setBackground(GridUtils.getWhite(display));
            rows[i].removeAll();
        }

        requestTree.setItemCount(0);

    }

    
    /**
     * Loads the TK patch data into the UI. 
     */
    private void loadPatchData() {
        if (patch != null) {
            versionText.setText(patch.getRelease());
            //injectorText.setText(patch.getInjector());
            toolText.setText(patch.getComponent());
            stateText.setText(patch.getState());
        }
        else {
            // Display the error dialog
            Status status = new Status(IStatus.ERROR, APP_NAME, 0,
                                       "Returned patch is empty.", null);
            ErrorDialog.openError(shell, "Error",
                                  "Unable data for fetch specified TK Patch from CQ",
                                  status);
        }
    }

    
    /*
     * Load the CMVC release input.
     */
    private void loadCmvcInputs() {

        // Load the CMVC release in the UI
        if (cmvc != null) {
            cmvc.setReleases(patch.getToolName(), patch.getRelease());
            cmvcReleaseText.setText(cmvc.getRelease());  
        }
        else {
            cmvcReleaseText.setText("");
        }
        
    }
    
    
    /**
     * Clears the TK patch data into the UI. 
     */
    private void clearPatchData() {
        versionText.setText("");
        //injectorText.setText("");
        toolText.setText("");
        //shipbLocationButton.setSelection(false);
        tkbLocationButton.setSelection(false);
        xtinctLocationButton.setSelection(false);
        xtinct2LocationButton.setSelection(false);
        locationText.setText("");
                
    }


    /*
     * Edit the selected or all Injection Requests.
     * 
     * @param editAllRequest  If true edit all the Injection requests
     *                        otherwise edit only checked requests.
     */
    private void editRequests(boolean editAllRequests) throws Exception {
        
        // Determine which Inject Request was selected.
        TreeItem[] rows = requestTree.getItems();

        // Edit the selected rows.
        for (int i = 0; i < rows.length; i++) {
            String entry = rows[i].getText(0);
            String injectRequestId = entry.substring(0, entry.indexOf(" "));

            // Process the request first.
            if ((rows[i].getChecked() == true) || editAllRequests) {

                console.append("Edit " + injectRequestId + " ...\n");

                // Allow the user to specify a new location.
                TkSourceLocatorUI locator = 
                	new TkSourceLocatorUI(this, patch, injectRequestId,
                	                      useShipbButton.getSelection());
                locator.run();
            }

        }
        
        // Update the Inject Request table.
        loadInjectRequestsData();
        
        // After editing the files to be injected determine if there are still
        // duplicate files
        setDuplicateChangedFiles();

    }

    
    /*
     * Determine the selected injection requests
     */
    private HashSet<TkInjectRequest> setSelectedRequests() throws Exception {
        
        // Determine which Inject Request was selected.
        TreeItem[] rows = requestTree.getItems();

        // Create a list of selected requests.
        HashSet<TkInjectRequest> selectedRequests = new HashSet<TkInjectRequest>();
        for (int i = 0; i < rows.length; i++) {
            if (rows[i].getChecked() == true) {
                String entry = rows[i].getText(0);
                String injectRequestId = entry.substring(0, entry.indexOf(" "));

                if (patch.getInjectRequests().containsKey(injectRequestId)) {
                    if (! selectedRequests.contains(patch.getInjectRequests().get(injectRequestId))) {
                        selectedRequests.add(patch.getInjectRequests().get(injectRequestId));
                    }
                }
                else {
                    throw new IcofException(CLASS_NAME,
                                            "InjectSelectedRequests()",
                                            IcofException.SEVERE,
                                            "Unable to locate selected " +
                                            "injection request in patch.",
                                            "Request: " + injectRequestId);
                }
                
            }
            
        }

        return selectedRequests;
        
    }

    
    /*
     * Edit the selected Injection Requests.
     */
    private void injectSelectedRequests() throws Exception {

        // Determine which inject requests were selected.
        HashSet<TkInjectRequest> selectedRequests = setSelectedRequests();
        patch.setSelectedItems(selectedRequests);
        
        // Set the injection location.
        setInjectionLocation();
         
        // Create the target LEVELHIST object
        patch.setTargetLevelHist();
        patch.getTargetLevelHist().readFile(false);

        // Set the new LEVELHIST contents - don't add them yet.
        patch.getTargetLevelHist().setNewContents(patch.getSelectedRequests(),
                                                  patch.getTargetLocation(),
                                                  patch, 
                                                  useShipbButton.getSelection());

        // Set the copy and backup actions.
        patch.setBackupActions();
        patch.setCopyExtractActions();
        
        // Get user's confirmation on the updates and actions.
        console.append("Requesting user confirmation ...\n");
        boolean doContinue = getUserReview();
        if (! doContinue) {
            console.append("User canceled injection!\n");
            return;
        }
        
        // Get user's confirmation on the extract track command.
        if ((useShipbButton.getSelection() == true) && 
            (patch.getTrackExtractList().size() > 0)) {
            patch.setTrackExtractCommand(cmvc, debug);
            InputDialog dlg = new InputDialog(shell, "", 
                                              "Verify/edit the CMVC track extract command",
                                              patch.getTrackExtractCommand(), null);
            if (dlg.open() == Window.OK) {
                patch.setTrackExtractCommand(dlg.getValue());
            }
            else {
                console.append("User canceled injection!\n");
                return;
            }
        }

            
        // Perform the updates.
        console.append("Performing updates ...\n");
        patch.backupSource(debug);
        if (useShipbButton.getSelection() == false) {
            patch.getTargetLevelHist().appendNewContents(patch, debug);
            patch.copySource(debug);
            patch.extractSource(cmvc, debug);
        }
        else {
            patch.logIt("Running command: " + patch.getTrackExtractCommand(),
                        false);
            cmvc.runCommand(patch.getTrackExtractCommand());
            patch.logIt(" rc: " + cmvc.getReturnCode(), false);
            patch.logIt(" results: " + cmvc.getResultsAsString(), false);
            patch.logIt(" error msg: " + cmvc.getErrorMsg().toString(), false);
            console.append("Ran command: " + patch.getTrackExtractCommand() + "\n");
            console.append("Return code: " + cmvc.getReturnCode() + "\n");

            if (cmvc.getReturnCode() != 0) {
                MessageDialog.openInformation(shell, "Possible CMVC Error",
                                              "There may have been an error with " +
                                              "the CMVC etraction command.\n" +
                                              "Results: " + 
                                              cmvc.getResultsAsString() + "\n" +
                                              "Error message: " + 
                                              cmvc.getErrorMsg().toString() + "\n");
                return;
            }
            
        }
        
        // Inform the user updates are complete. 
        console.append("Updates complete!\n");
        patch.logIt("\nUpdates are complete\n", false);
        console.append("See details in patch log at " + 
                       patch.getLogFile().getAbsolutePath() + "\n");
        
        MessageDialog.openInformation(shell, "Updates Complete",
                                      "Selected source files for TK Patch (" + patch.getId() 
                                      + ") have been copied into the selected " +
                                      		"target directory.\n" 
                                      + "See details in " 
                                      + patch.getLogFile().getAbsolutePath()
                                      + "\n\nThank you!");
       
    }

    
    /*
     * Rebuild the selected Injection Requests.
     */
    private void rebuildSelectedRequests() throws Exception {

        // Determine which inject requests were selected.
        HashSet<TkInjectRequest> selectedRequests = setSelectedRequests();
        patch.setSelectedItems(selectedRequests);
        
        // Set the injection location.
        setInjectionLocation();
            
        // Have the user specify and select the build options they'd like run.
        console.append("Selecting build commands ...\n");

        BuildOptionsUI buildOptions = 
        	new BuildOptionsUI(this, patch, cmvc.getRelease());
        buildOptions.run();        
        
        // Inform the user updates are complete. 
        console.append("Updates complete!\n");
        patch.logIt("\nUpdates are complete\n", false);
        console.append("See details in patch log at " + 
                       patch.getLogFile().getAbsolutePath() + "\n");
        
//        MessageDialog.openInformation(shell, "Rebuild Actions Complete",
//                                      "Rebuilding of TK Patch (" + patch.getId() 
//                                      + ") is complete.\n" 
//                                      + "See details in " 
//                                      + patch.getLogFile().getAbsolutePath()
//                                      + "\n\nThank you!");
       
    }

 
    /**
     * 
     * @return
     * @throws Exception 
     * @throws Exception 
     */
    private boolean getUserReview() throws Exception {

        // Allow the user to confirm the changes.
        TkInjectSummaryUI summary = new TkInjectSummaryUI(this, 
                                                          "Confirm Updates", 
                                                          "Are these changes correct?",
                                                          patch);
        summary.run();
        
        return summary.getReply();
        
    }


    /**
     * Read the injection target location radio buttons and set the injection
     * target directory.
     */
    private void setInjectionLocation() {

        String location = "";        
        if (tkbLocationButton.getSelection() == true) {
            location = "tkb";
        }
        else if (xtinctLocationButton.getSelection() == true) {
            location = "xtinct";
        }
        else if (xtinct2LocationButton.getSelection() == true) {
            location = "xtinct2";
        }

        // Save the patch location.
        if ((patch != null) && (! location.equals(""))) {
            patch.setTargetLocation(location);
            locationText.setText(patch.getTargetDirectory());
        }

    }

    
    /*
     * Set the source locations for all injection requests.
     */
    private void setPatchSourceLocations() throws IcofException {
        
        console.append("Setting patch source locations ...\n");
        
        // For each inject request determine the source files and verify 
        // they can be found.
        Vector<String> missingTracks = new Vector<String>();
        Iterator<TkInjectRequest> iter = patch.getInjectRequests().values().iterator();
        while (iter.hasNext()) {
            TkInjectRequest request =  iter.next();
            request.setSourceFiles(patch.getCmvcTracks());
            if (request.getMissingTracks() != null) {
                missingTracks.addAll(request.getMissingTracks());
            }
        }
        
    }


    /*
     * Determine if any tracks are missing from the build LEVELHIST file.
     * 
     * @param tracks Collection of CMVC track 
     */
    private void setMissingBuildTracks() throws IcofException {
        
        // If the LEVELHIST file exists then read it.  If it doesn't exist then
        // warn the user.
        if (! patch.getBuildLevelHist().exists()) {
            MessageDialog.openWarning(shell, "Warning",
                                      "Unable to locate a LEVELHIST file in " +
                                      "build or dev. If this is expected then " +
                                      "please continue.\n");
            return;
        }

        // Look for tracks missing from the build LEVELHIST file.
        patch.setMissingTracks();

        // Warn the user of any tracks are missing.
        if (patch.getMissingBuildTracks().length() > 0) {
            MessageDialog.openWarning(shell, "Warning",
                                      "The following track(s) were not found in " +
                                      "the build LEVELHIST file.\n" +
                                      patch.getMissingBuildTracks().toString());
        }
        
    }
    
    
    /*
     * Getters
     */
    private boolean areRequestsSelected() { return requestsSelected; }
    private boolean isCmvcQueried() { return cmvcQueried; }
    public TkPatch getPatch() { return patch; }
    public Tree getRequestTree() { return requestTree; }

    
    /*
     * Setters
     */
    private void setRequestsSelected(boolean aFlag) { requestsSelected = aFlag; }
    private void setCmvcQueried(boolean aFlag) { cmvcQueried = aFlag; }

    
    /**
     * Members.
     */
    private static AppContext myContext = null;
    
    // Window constants.
    private final static String CLASS_NAME = "InjectBuildUI";
    private final static int GRID_COLS_WIDE = 4;
    private final static int SCREEN_WIDTH = 700;
    private final static int SCREEN_HEIGHT = 700;
    
    // Actions.
    private ExitAction exitAction;
    private ShowAboutAction aboutAction;
    private ShowHelpAction showHelpAction;
    
    // Widgets.
    private Display display = null;
    private static Shell shell = null;
    private Text toolText;
    private Text versionText;
    //private Text injectorText;
    private Text patchText;
    private Text console;
    private Text useridText;
    private Text passwordText;
    private Text stateText;
    private Text cmvcReleaseText;
    private Text locationText;
    private Tree requestTree;
    
    private Button editSelectedButton;
    private Button startInjectButton;
    private Button readPatchButton;
    private Button readCmvcButton;
    private Button tkbLocationButton;
    private Button xtinctLocationButton;
    private Button xtinct2LocationButton;
    private Button rebuildSelectedButton;
    private Button selectAllButton;
    private Button useShipbButton;
    private Button markBuiltButton;
    
   
    public TkPatch patch = null;
    private CMVC cmvc;
    private boolean cmvcQueried = false;
    private boolean requestsSelected = false;
    private boolean debug = false;
    
}

