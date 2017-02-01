/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2009 -- IBM Internal Use Only
*
*=============================================================================
*
* CREATOR: Gregg Stadtlander
*    DATE: 11/17/2009
*
*-PURPOSE---------------------------------------------------------------------
* TK Injection Source Locator class.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 11/17/2009 GFS  Initial coding.
* 02/01/2010 GFS  Reworked to include adding a file not the inject request and
*                 improved the flow to remove files which should not be injected.
* 02/17/2010 GFS  Added support for shipb CMVC release and extracting tracks 
*                 from CMVC. Added the select all button.
* 03/18/2010 GFS  Set the window dimensions to a constant size.   
* 04/29/2010 GFS  Updated to run saveLocation() when a request is opened for
*                 the first time.  
* 10/12/2010 GFS  Changed text on Close Window button.       
* 10/27/2010 GFS  Changed button text to black.  
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.app.injectBuild;

import java.io.File;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.ibm.stg.eda.component.jfacebase.GridUtils;
import com.ibm.stg.eda.component.tk_levelhist.CMVC_File;
import com.ibm.stg.eda.component.tk_levelhist.CMVC_Track;
import com.ibm.stg.eda.component.tk_patch.TkInjectRequest;
import com.ibm.stg.eda.component.tk_patch.TkInjectUtils;
import com.ibm.stg.eda.component.tk_patch.TkPatch;
import com.ibm.stg.eda.component.tk_patch.TkSource;
import com.ibm.stg.iipmds.common.IcofCollectionsUtil;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofFile;

public class TkSourceLocatorUI extends ApplicationWindow {

    /**
     * Constructor.
     * @param aWindow
     * @param aPatch
     * @param aRequest
     * @param aShipbReleaseFlag 
     * @throws Exception 
     */
    public TkSourceLocatorUI(ApplicationWindow aWindow, 
                             TkPatch aPatch, 
                             String aRequest,
                             boolean aShipbReleaseFlag) throws Exception { 
        super(aWindow.getShell());
        setWindow(aWindow);
        setPatch(aPatch);
        setRequestName(aRequest);
        setShell();
        setRequest(getRequestName());
        setShipbReleaseFlag(aShipbReleaseFlag);

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
        shell.setText("EDA Tool Kit Injection Build");

        // Set the window size by scaling parent's size.
        shell.setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        display = shell.getDisplay();

        // Define the Grid layout
        Composite gridComposite = new Composite(shell, SWT.BORDER);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 4;
        gridComposite.setLayout(gridLayout);

        // Create the Inject Request group.
        setRequestComposite(gridComposite, 4);

        // Create the Injection Source files group.
        setInjectionContentsComposite(gridComposite, 3);

        // Create the Source Location group.
        setSourceLocation(gridComposite, 3);
        
        // Run the default process if it hasn't been run before.
        try {
            if (request.getSourceFiles() == null) {
                saveLocation();
            }
        }
        catch(IcofException ex) {
            // Display the error dialog
            MessageDialog.openError(shell, "Error", "Message: " + ex.getMessage());
        }
        loadInjectSourceTable();
        
        return parent;
        
    }

    
    /**
     * Define and layout the Console widgets.
     * 
     * @param comp       The parent composite widget which will hold this widget.
     * @param gridWidth  Number of columns in current grid.
     */
    private void setRequestComposite(Composite comp, int gridWidth) {

        // Create the group.
        Group group = new Group(comp, SWT.SHADOW_IN);
        group.setText("Injection Request Data");

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

        // Create the Request label.
        Label thisLabel = new Label(group, SWT.NONE);
        thisLabel.setText("Injection Request");

        data = new GridData();
        data.horizontalSpan = gridWidth;
        thisLabel.setLayoutData(data);

        // Create the Request box.
        Text requestText = new Text(group, SWT.BORDER | SWT.READ_ONLY);
        requestText.setBackground(GridUtils.getGray(display));
        requestText.setForeground(GridUtils.getBlack(display));

        String requestDescription = getRequestName(); 
        if (request != null) {
            requestDescription += " (" + request.getDeveloper() 
                                + ") Tracks: " + request.getAllTracksAsString();
        }
        requestText.setText(requestDescription);
        
        data = new GridData();
        data.horizontalSpan = gridWidth;
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        requestText.setLayoutData(data);

        
        // Create the Files changed label.
        thisLabel = new Label(group, SWT.NONE);
        thisLabel.setText("Files changed");

        data = new GridData();
        data.horizontalSpan = gridWidth;
        thisLabel.setLayoutData(data);
        
        // Create the Files Updated box.
        Text filesUpdatedText = new Text(group, SWT.BORDER | SWT.MULTI | 
                                         SWT.V_SCROLL | SWT.H_SCROLL);
        if (getRequest() != null) {
            filesUpdatedText.setText(getRequest().getFilesUpdated());
        }
        else {
            filesUpdatedText.setText(MISSING_REQUEST);
        }
        filesUpdatedText.setBackground(GridUtils.getGray(display));
        filesUpdatedText.setForeground(GridUtils.getBlack(display));

        data = new GridData();
        data.horizontalSpan = gridWidth;
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        data.heightHint = filesUpdatedText.getLineHeight() * 5;
        filesUpdatedText.setLayoutData(data);

    }


    /**
     * Define and layout the Injection Request's Source files widgets.
     * 
     * @param comp       The parent composite widget which will hold this widget.
     * @param gridWidth  Number of columns in current grid.
     */
    private void setInjectionContentsComposite(Composite comp, int gridWidth) {

        // Set the height hint for the list boxes
        int listRowSpan = 5;
        int minRows = 5;
        
        // Create the group.
        Group group = new Group(comp, SWT.SHADOW_IN);
        group.setText("Manage Injection Contents");

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

        // Create the Source File table.
        sourceTable = new Table(group, SWT.BORDER | SWT.CHECK | 
                                SWT.MULTI | SWT.FULL_SELECTION);
        sourceTable.setHeaderVisible(true);
        sourceTable.setBackground(GridUtils.getWhite(display));
        sourceTable.setForeground(GridUtils.getBlack(display));
        
        TableColumn sourceCol = new TableColumn(sourceTable, SWT.LEFT);
        sourceCol.setText("Source File(s) To Inject");
        sourceCol.setWidth(shell.getSize().x / 2);   

        data = new GridData();
        data.horizontalSpan = 1;
        data.verticalSpan = listRowSpan;
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        data.heightHint = sourceTable.getItemHeight() * minRows;
        sourceTable.setLayoutData(data);

        
        // Create Add button.
        Button addButton = new Button(group, SWT.PUSH);
        addButton.setText("<-- Add");
        addButton.setBackground(GridUtils.getGreen(display));
        addButton.setForeground(GridUtils.getBlack(display));
        addButton.addSelectionListener(new SelectionAdapter () {
            public void widgetSelected(SelectionEvent e) {
                try {
                    addCheckedFiles();
                }
                catch(IcofException ex) {
                    MessageDialog.openError(shell, "Error",
                                          "Message: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });

        data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.horizontalSpan = 1;
        addButton.setLayoutData(data);

        
        // Create the Removed Source File table.
        removedTable = new Table(group, SWT.BORDER | SWT.CHECK | 
                                 SWT.MULTI | SWT.FULL_SELECTION);
        removedTable.setHeaderVisible(true);
        removedTable.setBackground(GridUtils.getWhite(display));
        removedTable.setForeground(GridUtils.getBlack(display));
        
        TableColumn removeCol = new TableColumn(removedTable, SWT.LEFT);
        removeCol.setText("Removed File(s)");
        removeCol.setWidth(shell.getSize().x / 2);   

        data = new GridData();
        data.horizontalSpan = 1;
        data.verticalSpan = listRowSpan;
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        data.heightHint = removedTable.getItemHeight() * minRows;
        removedTable.setLayoutData(data);

        
        // Create Remove button.
        Button removeButton = new Button(group, SWT.PUSH);
        removeButton.setText("Remove -->");
        removeButton.setBackground(GridUtils.getGreen(display));
        removeButton.setForeground(GridUtils.getBlack(display));
        removeButton.addSelectionListener(new SelectionAdapter () {
            public void widgetSelected(SelectionEvent e) {
                try {
                    removeCheckedFiles();
                }
                catch(IcofException ex) {
                    // Display the error dialog
                    MessageDialog.openError(shell, "Error",
                                          "Message: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });

        data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.horizontalSpan = 1;
        removeButton.setLayoutData(data);

        
        // Load the tables.
        loadContentTables();
        
    }

    
    /**
     * Define and layout the Source File Location widgets.
     * 
     * @param comp       The parent composite widget which will hold this widget.
     * @param gridWidth  Number of columns in current grid.
     */
    private void setSourceLocation(Composite comp, int gridWidth) {

        // Create the group.
        Group group = new Group(comp, SWT.SHADOW_IN);
        group.setText("Manage Patch Compatible Source File Locations");

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

        // Create the Source File table.
        injectSourceTable = new Table(group, SWT.BORDER | SWT.CHECK | 
                                SWT.MULTI | SWT.FULL_SELECTION);
        injectSourceTable.setHeaderVisible(true);
        injectSourceTable.setLinesVisible(true);
        injectSourceTable.setBackground(GridUtils.getWhite(display));
        injectSourceTable.setForeground(GridUtils.getBlack(display));
        
        TableColumn col1 = new TableColumn(injectSourceTable, SWT.LEFT);
        TableColumn col2 = new TableColumn(injectSourceTable, SWT.LEFT);
        col1.setText("Source File");
        col2.setText("Injected File Location");
        col1.setWidth(shell.getSize().x / 3);   
        col2.setWidth(shell.getSize().x);

        data = new GridData();
        data.horizontalSpan = gridWidth;
        data.heightHint = injectSourceTable.getItemHeight() * 6;
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        injectSourceTable.setLayoutData(data);
        

        // Create the Select All button
        selectAllButton = new Button(group, SWT.CHECK);
        selectAllButton.setText("Select all Source Files");
        selectAllButton.setEnabled(true);
        selectAllButton.setSelection(true);
        selectAllButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                loadInjectSourceTable();
            }
        });

        data = new GridData();
        data.horizontalSpan = gridWidth;
        data.grabExcessHorizontalSpace = true;
        selectAllButton.setLayoutData(data);

        loadInjectSourceTable();
        
        
        // Create the Source Location label.
        Label thisLabel = new Label(group, SWT.NONE);
        thisLabel.setText("New Injected Source Location");

        data = new GridData();
        data.horizontalSpan = gridWidth - 1;
        data.grabExcessHorizontalSpace = true;
        thisLabel.setLayoutData(data);

        // Create the Source Location label.
        thisLabel = new Label(group, SWT.NONE);
        thisLabel.setText("Use this location");

        data = new GridData();
        data.horizontalSpan = 1;
        data.horizontalAlignment = SWT.CENTER;
        thisLabel.setLayoutData(data);

        
        // Create the Source Location box.
        sourceText = new Text(group, SWT.BORDER);
        sourceText.setText("                                                    ");
        sourceText.setBackground(GridUtils.getWhite(display));
        sourceText.setForeground(GridUtils.getBlack(display));

        data = new GridData();
        data.horizontalSpan = gridWidth - 1;
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        sourceText.setLayoutData(data);
        
        // Set the location combo.
        locationCombo = new Combo(group, SWT.NULL);
        String[] locations;
        if (isShipbRelease() == false) {
            locations = new String[]{"Parse request", "BUILD", "DEV", "PROD", 
                                     TkInjectUtils.CMVC_FILE_EXT, "Other"};
        }
        else {
            locations = new String[]{TkInjectUtils.CMVC_TRACK_EXT};
        }
        locationCombo.setItems(locations);
        locationCombo.select(0);
        updateSourceLocation(locations[0]);
        
        locationCombo.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                updateSourceLocation(locationCombo.getItem(locationCombo.getSelectionIndex()));
            }
            public void widgetDefaultSelected(SelectionEvent e) { }
        });

        data = new GridData();
        data.horizontalSpan = 1;
        data.horizontalAlignment = SWT.CENTER;
        locationCombo.setLayoutData(data);
        
        
        // Create Save button.
        Button saveButton = new Button(group, SWT.PUSH);
        saveButton.setText("Set Source Location for Checked Files");
        saveButton.setBackground(GridUtils.getGreen(display));
        saveButton.setForeground(GridUtils.getBlack(display));
        saveButton.addSelectionListener(new SelectionAdapter () {
            public void widgetSelected(SelectionEvent e) {
                try {
                    saveLocation();
                    loadInjectSourceTable();
                }
                catch(IcofException ex) {
                    // Display the error dialog
                    MessageDialog.openError(shell, "Error",
                                          "Message: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });

        data = new GridData();
        data.horizontalAlignment = SWT.CENTER;
        data.horizontalSpan = gridWidth/2;
        saveButton.setLayoutData(data);

        // Create Cancel button.
        Button closeButton = new Button(group, SWT.PUSH);
        closeButton.setText("Save and close");
        closeButton.setBackground(GridUtils.getGreen(display));
        closeButton.setForeground(GridUtils.getBlack(display));
        closeButton.addSelectionListener(new SelectionAdapter () {
            public void widgetSelected(SelectionEvent e) {
                getShell().dispose();
            }
        });

        data = new GridData();
        data.horizontalAlignment = SWT.CENTER;
        data.horizontalSpan = gridWidth/2;
        closeButton.setLayoutData(data);

    }

 
    /**
     * Add the active source files to the source table.
     */
    private void loadInjectSourceTable() {
        
        // Turn off redraw to avoid flicker.
        injectSourceTable.setRedraw(false);
        
        // Remove all entries.
        if (injectSourceTable.getItems().length > 0) {
            injectSourceTable.removeAll();
        }
        
        // Add the active files to the source table.
        Iterator<String> iter = getRequest().getSourceFiles().keySet().iterator();
        while (iter.hasNext()) {
            String key =  iter.next();
            TkSource sourceFile =  getRequest().getSourceFiles().get(key);
            if (sourceFile.isActive()) {
                TableItem item = new TableItem(injectSourceTable, SWT.NONE);
                item.setText(new String[] {sourceFile.getName(), sourceFile.getFullPath()});
                if (! sourceFile.isPathValid()) {
                    item.setBackground(GridUtils.getYellow(display));
                }
                if (selectAllButton != null) 
                    item.setChecked(selectAllButton.getSelection());
            }
        }

        // Turn on redraw.
        injectSourceTable.setRedraw(true);
        injectSourceTable.redraw();
        
    }


    /**
     * Add the active source files to the source table.
     */
    private void loadContentTables() {
        
        // Turn off redraw to avoid flicker.
        sourceTable.setRedraw(false);
        removedTable.setRedraw(false);
        
        // Remove all entries.
        if (sourceTable.getItems().length > 0) {
            sourceTable.removeAll();
        }
        if (removedTable.getItems().length > 0) {
            removedTable.removeAll();
        }
        
        // Add the files to the source/removed tables.
        Iterator<String>  iter = getRequest().getSourceFiles().keySet().iterator();
        while (iter.hasNext()) {
            String key =  iter.next();
            TkSource sourceFile = (TkSource) getRequest().getSourceFiles().get(key);
            if (sourceFile.isActive()) {
                TableItem item = new TableItem(sourceTable, SWT.NONE);
                item.setText(new String[] {sourceFile.getName()});
            }
            else {
                TableItem item = new TableItem(removedTable, SWT.NONE);
                item.setText(new String[] {sourceFile.getName()});
            }
        }

        // Turn on redraw.
        sourceTable.setRedraw(true);
        removedTable.setRedraw(true);
        sourceTable.redraw();
        removedTable.redraw();

        
    }

    
    /*
     * Update the source file location based on the user's selection.
     * 
     * @param location 
     */
    private void updateSourceLocation(String location) {
        
        // Determine which button was selected.
        if (location.equals("Parse request")) {
            sourceText.setText(getRequest().getLocationGuess());
        }
        else if (location.equals("Other")) {
            setOtherFileLocation();
        }
        else if (location.equals(TkInjectUtils.CMVC_FILE_EXT)) {
            sourceText.setText(TkInjectUtils.CMVC_FILE_EXT);
        }
        else if (location.equals(TkInjectUtils.CMVC_TRACK_EXT)) {
            sourceText.setText(TkInjectUtils.CMVC_TRACK_EXT);
        }
        else {
        	String dir = "/afs/eda/" + location.toLowerCase() + "/" + 
        	             patch.getComponent() + "/" + patch.getToolVersion();
        	sourceText.setText(dir);
            
        }
        
    }

    
     /*
     * Save the source location back into the TkPatch object. 
     */
    private void saveLocation() throws IcofException {

        // For each checked file update the source location.
        TableItem[] items = injectSourceTable.getItems();
        
        for(int i = 0; i < items.length;i++) {
            if (items[i].getChecked() == true) {
                getRequest().updateSourceLocation(items[i].getText(0),
                                                  sourceText.getText());
            }
        }

        // Update the Tk Patch
        patch.getInjectRequests().put(requestName, request);        
     
    }


    /*
     * Add checked files to the Inject Source file list. 
     */
    private void addCheckedFiles() throws IcofException {

        // For each checked file in the removed table add it to the source table
        TableItem[] items = removedTable.getItems();
  
        boolean foundCheckedItems = false;
        for(int i = 0; i < items.length;i++) {
            if (items[i].getChecked() == true) {
                String fileName = items[i].getText(0);
                TkSource sourceFile = (TkSource) getRequest().getSourceFiles().get(fileName);
                sourceFile.setIsActive(true);
                foundCheckedItems = true;
            }
        }

        // If no files were checked see if the user wants to add a file.
        if (! foundCheckedItems) {
            try {
                addNewFiles();
            }
            catch (Exception ex) {
                MessageDialog.openError(shell, "Error",
                                        "Encountered trouble adding 1 or more " +
                                        "files to this inject request.\n" +
                                        "Message: " + ex.getMessage());
                  ex.printStackTrace();
            }
        }
        
        
        // Update the tables
        loadInjectSourceTable();
        loadContentTables();
     
    }


    /**
     * Allows the user to add files to the inject request that are not in the
     * changed files for the inject request's CMVC tracks.
     * @throws Exception 
     */
    private void addNewFiles() throws Exception {

        // Set the directory based on the location guess.
        String dirName = getRequest().getLocationGuess();
        if (dirName.equals("")) {
            dirName = "/afs/eda/";
        }
        IcofFile directory = new IcofFile(dirName, true);
        if (directory.exists()) {
            if (! directory.isDirectory()) {
                directory = new IcofFile(directory.getParent(), true);
            }
        }
        
        
        // Allow the user to specify files to add to this inject request.
        AddFileUI addFiles = new AddFileUI(this, directory.getAbsolutePath());
        addFiles.run();

        // If there are any files then add them to the list.
        if (addFiles.getFiles() != null) {

            Iterator<String> iter = addFiles.getFiles().iterator();
            while (iter.hasNext()) {
                String fileName =  iter.next();

                // Look up this file name in the build LEVELHIST file to
                // set the path.
                String path = lookupFile(fileName);
                
                TableItem item = new TableItem(sourceTable, SWT.NONE);
                item.setText(new String[] {path, ""});

                // Create the TkSource object and add it to the request.
                String fullPath = addFiles.getDirectory() + File.separator + fileName;
                TkSource sourceFile = new TkSource(path, fullPath, true);
                getRequest().getSourceFiles().put(path, sourceFile);

                // Create the CMVC_File object and add it to the primary track object.
                CMVC_File cmvcFile = new CMVC_File("", path, "1.1", "create",
                                                   getRequest().getDeveloper(), 
                                                   null);
                CMVC_Track cmvcTrack = 
                    (CMVC_Track) getPatch().getCmvcTracks().get(getRequest().getTrackPrimary());
                cmvcTrack.getFiles().put(path, cmvcFile);
            
            }
        }
        
        // Reload the tables.
        loadContentTables();
        loadInjectSourceTable();
        
    }

    
    /**
     * Look up the input file in the LEVELHIST.  Return the path used for the 
     * from the LEVELHIST entry.  If no LH file found or no entry then return
     * the original file.
     * 
     * @param aFile  File name.
     * @return       File's path from the LEVELHIST file.
     * @throws IcofException 
     */
    private String lookupFile(String aFile) throws IcofException {

        String path = aFile;
        
        //System.out.println("Searching build LH for " + aFile);
        
        // If build LH file does not exist then return.
        if (! patch.getBuildLevelHist().exists()) {
            //System.out.println(" Build LH is empty.");
            return path;
        }
        
        
        // Search the LEVELHIST file for the file from the bottom to the top.
        boolean found = false;
        String line = "";
        for (int i = patch.getBuildLevelHist().getContents().size()-1; (i >= 0) && (! found); i--) {
            line = (String) patch.getBuildLevelHist().getContents().get(i);
            //System.out.println(" Searching " + line + " for " + aFile);
            if (line.indexOf(aFile) > -1) {
                //System.out.println(" Found it ...");
                found = true;
            }
        }
        
        // Parse the line to find the token containing file.
        if (found) {
            Vector<String> tokens = new Vector<String>();
            IcofCollectionsUtil.parseString(line, " ", tokens, true);
            
            Iterator<String> iter = tokens.iterator();
            boolean foundEntry = false;
            while (iter.hasNext() && !foundEntry) {
                String entry =  iter.next();
                //System.out.println(" Token: " + entry);
                if (entry.indexOf(aFile) > -1) {
                    //System.out.println(" Found it ...");
                    path = entry;
                    foundEntry = true;
                }
            }
        }
        
        return path;
        
    }


    /*
     * Removed checked files from the Inject Source file list. 
     */
    private void removeCheckedFiles() throws IcofException {

        // For each checked file remove it from the source list.
        TableItem[] items = sourceTable.getItems();
        
        for(int i = 0; i < items.length;i++) {
            if (items[i].getChecked() == true) {
                String fileName = items[i].getText(0);
                TkSource sourceFile = (TkSource) getRequest().getSourceFiles().get(fileName);
                sourceFile.setIsActive(false);
            }
        }

        // Update the tables
        loadInjectSourceTable();
        loadContentTables();
     
    }


    /*
     * Show a directory dialog window for the user to select an alternate
     * directory. 
     */
    private void setOtherFileLocation() {

        // Set the initial path.
        String dirName = getRequest().getLocationGuess();
        if (dirName.equals("")) {
            dirName = sourceText.getText(); 
        }
        if (dirName.equals("")) {
            dirName = "/afs/eda/"; 
        }
        
        IcofFile dir = new IcofFile(dirName, true);
        if (! dir.isDirectory()) {
            dir = new IcofFile(dir.getParent(), true);
        }
        
        //System.out.println("Directory name: " + dir.getAbsolutePath());

        // Set the initial filter path according to anything they've selected
        // or typed in.
        DirectoryDialog dlg = new DirectoryDialog(shell);
        dlg.setFilterPath(dir.getAbsolutePath());
        dlg.setText("Inject source location");
        dlg.setMessage("Select a directory");

        // Calling open() will open and run the dialog. It will return
        // the selected directory, or null if user cancels
        String newDirName = dlg.open();
        if (newDirName != null) {
            sourceText.setText(newDirName);
        }
        
    }
    
    
    /*
     * Constants.
     */
    private static String MISSING_REQUEST = "Request not found ...";
    private final static int SCREEN_WIDTH = 700;
    private final static int SCREEN_HEIGHT = 710;
    
    /**
     * Members.
     */
    private Display display;
    private ApplicationWindow window;
    private Shell shell;
    private TkPatch patch;
    private TkInjectRequest request;
    private String requestName;
    
    private Text sourceText;
    private Combo locationCombo;
    private Table injectSourceTable;
    private Table sourceTable;
    private Table removedTable;
    private Button selectAllButton;
    private boolean shipbReleaseFlag;

    
    /**
     * Getters.
     */
    private ApplicationWindow getWindow() { return window; }
    private TkInjectRequest getRequest() { return request; }
    private String getRequestName() { return requestName; }
    private TkPatch getPatch() { return patch; }
    private boolean isShipbRelease() { return shipbReleaseFlag; }

    
    /**
     * Setters.
     */
    private void setWindow(ApplicationWindow aWindow) { window = aWindow; }
    private void setShell() { shell = new Shell(getWindow().getShell()); }
    private void setPatch(TkPatch aPatch) { patch = aPatch; }
    private void setRequestName(String aRequest) { requestName = aRequest; }
    private void setShipbReleaseFlag(boolean aFlag) { shipbReleaseFlag = aFlag; }
    private void setRequest(String aRequest) throws Exception {
        TreeMap<String,TkInjectRequest> requests = getPatch().getInjectRequests();
        request = null;
        if ((requests != null) && (requests.size() > 0)) {
            if (requests.containsKey(getRequestName())) {
                request =  requests.get(getRequestName());
            }
        }
        
        if (request == null) {
            throw new Exception("Unable to locate request (" + getRequestName() 
                                + ") in this patch.");
        }
    }     
    
}
