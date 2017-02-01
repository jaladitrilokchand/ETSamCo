/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2009 -- IBM Internal Use Only
*
*=============================================================================
*
* CREATOR: Gregg Stadtlander
*    DATE: 02/02/2010
*
*-PURPOSE---------------------------------------------------------------------
* TK Injection add file to inject request user interface.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 02/02/2010 GFS  Initial coding.
* 03/18/2010 GFS  Set the window dimensions to a constant size.  
* 10/27/2010 GFS  Changed button text to black.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.app.injectBuild;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
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
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofFile;

public class AddFileUI extends ApplicationWindow {

    /**
     * Constructor.
     * @param aWindow
     * @param aDirectoryName
     * @throws Exception 
     */
    public AddFileUI(TkSourceLocatorUI aWindow, 
                     String aDirectoryName) throws Exception { 
        
        super(aWindow.getShell());
        setWindow(aWindow);
        setDirectory(aDirectoryName);
        setShell();

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
        gridLayout.numColumns = 1;
        gridComposite.setLayout(gridLayout);

        // Create the Inject Request group.
        try {
            setAddFileComposite(gridComposite, 4);
        }
        catch (IcofException e) {
            MessageDialog.openError(shell, "Error",
                                    "Unable to determine directory contents.\n");
            e.printStackTrace();
        }

        return parent;
        
    }

    

    /**
     * Define and layout the Console widgets.
     * 
     * @param comp       The parent composite widget which will hold this widget.
     * @param gridWidth  Number of columns in current grid.
     * @throws IcofException 
     */
    private void setAddFileComposite(Composite comp, int gridWidth)
    throws IcofException {

        // Create the group.
        Group group = new Group(comp, SWT.SHADOW_IN);
        group.setText("Add Files to Inject Request");

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

        // Create the Directory label.
        Label thisLabel = new Label(group, SWT.NONE);
        thisLabel.setText("Directory");

        data = new GridData();
        data.horizontalSpan = gridWidth;
        thisLabel.setLayoutData(data);

        // Create the Directory text box.
        directoryText = new Text(group, SWT.BORDER);
        directoryText.setText(getDirectory());
        directoryText.setBackground(GridUtils.getWhite(display));
        directoryText.setForeground(GridUtils.getBlack(display));

        data = new GridData();
        data.horizontalSpan = gridWidth - 1;
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = SWT.FILL;
        directoryText.setLayoutData(data);

        
        // Create Read Contents button.
        Button readContentsButton = new Button(group, SWT.PUSH);
        readContentsButton.setText("Read directory");
        readContentsButton.setBackground(GridUtils.getGreen(display));
        readContentsButton.setForeground(GridUtils.getBlack(display));
        readContentsButton.addSelectionListener(new SelectionAdapter () {
            public void widgetSelected(SelectionEvent e) {
                try {
                    loadContentsTable();
                }
                catch (IcofException e1) {
                    MessageDialog.openError(shell, "Error",
                                            "Unable to read directory contents.\n");
                    e1.printStackTrace();
                }
            }
        });

        data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.horizontalSpan = 1;
        readContentsButton.setLayoutData(data);


        // Create the Directory Contents table.
        dirListingTable = new Table(group, SWT.BORDER | SWT.CHECK | 
                                  SWT.MULTI | SWT.FULL_SELECTION);
        dirListingTable.setHeaderVisible(true);
        dirListingTable.setBackground(GridUtils.getWhite(display));
        dirListingTable.setForeground(GridUtils.getBlack(display));

        TableColumn removeCol = new TableColumn(dirListingTable, SWT.LEFT);
        removeCol.setText("Directory Contents (select 1 or more files to add)");
        removeCol.setWidth(shell.getSize().x * 90 / 100);   
        
        data = new GridData();
        data.horizontalSpan = gridWidth;
        data.verticalSpan = 2;
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        data.heightHint = 200;
        dirListingTable.setLayoutData(data);
            

        // Create Done button.
        Button doneButton = new Button(group, SWT.PUSH);
        doneButton.setText("Save checked files and close");
        doneButton.setBackground(GridUtils.getGreen(display));
        doneButton.setForeground(GridUtils.getBlack(display));
        doneButton.addSelectionListener(new SelectionAdapter () {
            public void widgetSelected(SelectionEvent e) {
                setFiles();
                getShell().dispose();
            }
        });

        data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.horizontalSpan = gridWidth / 2;
        doneButton.setLayoutData(data);


        // Create Cancel button.
        Button closeButton = new Button(group, SWT.PUSH);
        closeButton.setText("Cancel");
        closeButton.setBackground(GridUtils.getGreen(display));
        closeButton.setForeground(GridUtils.getBlack(display));
        closeButton.addSelectionListener(new SelectionAdapter () {
            public void widgetSelected(SelectionEvent e) {
                files = null;
                getShell().dispose();
            }
        });

        data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.horizontalSpan = gridWidth / 2;
        closeButton.setLayoutData(data);
        
    }

    
    /**
     * Load the contents tables with files.
     * 
     * @throws IcofException 
     */
    private void loadContentsTable() throws IcofException {

        // Determine the directory contents.
        IcofFile myDir = new IcofFile(directoryText.getText(), true);
        if (! myDir.exists()) {
            MessageDialog.openError(shell, "Error",
                                    "Specified directory doesn't exist. " 
                                    + "Please verify the path and try again.\n");
            return;
        }
        if (myDir.isFile()) {
            myDir = new IcofFile(myDir.getParent(), true);
        }
        Vector<String> myFiles = myDir.listRecursive();
        setDirectory(myDir.getAbsolutePath());
        directoryText.setText(myDir.getAbsolutePath());
        
        
        // Keep the directories/file after the source directory name then sort
        // that list.
        String[] sortedFiles = new String[myFiles.size()];
        Iterator<String>  iter = myFiles.iterator();
        int i = 0;
        while (iter.hasNext()) {
            String fullPath =  iter.next();
            String path = fullPath.substring(myDir.getAbsolutePath().length() + 1,
                                             fullPath.length());
            sortedFiles[i++] = path;
        }
        Arrays.sort(sortedFiles);
        
        // Clear the table contents first.
        if (dirListingTable.getItems().length > 0) {
            dirListingTable.removeAll();
        }
        
        
        // Load the contents table.
        for (i = 0; i < sortedFiles.length; i++) {
            TableItem item = new TableItem(dirListingTable, SWT.NONE);
            item.setText(new String[] {sortedFiles[i]});

            // If this listing is a directory then disable it.
            IcofFile listing = new IcofFile(myDir.getAbsolutePath() 
                                            + IcofFile.separator + sortedFiles[i],
                                            false);
            if (listing.isDirectory()) {
                item.setGrayed(true);
            }

        }
        
    }


    /*
     * Save the checked files to add to the inject request.
     */
    private void setFiles() {
        
        // Initialize the files collection.
        if (getFiles() == null) {
            files = new Vector<String>();
        }
        else {
            files.clear();
        }
        
        // Save all checked files to a vector.
        TableItem[] items = dirListingTable.getItems();
        for (int i = 0; i < items.length; i++) {
            if (items[i].getChecked()) {
                files.add(items[i].getText(0));
            }
            
        }
        
    }

    /**
     * Constants
     */
    private final static int SCREEN_WIDTH = 630;
    private final static int SCREEN_HEIGHT = 500;
    

    /**
     * Members.
     */
    private Display display;
    private ApplicationWindow window;
    private Shell shell;
    private String directoryName;
    private Text directoryText;
    private Table dirListingTable;
    private Vector<String> files;
    
    
    /**
     * Getters.
     */
    private ApplicationWindow getWindow() { return window; }
    public String getDirectory() { return directoryName; }
    public Vector<String> getFiles() { return files; }

    
    /**
     * Setters.
     */
    private void setWindow(ApplicationWindow aWindow) { window = aWindow; }
    private void setShell() { shell = new Shell(getWindow().getShell()); }
    public void setDirectory(String aDirName) { directoryName = aDirName;  }
    
}
