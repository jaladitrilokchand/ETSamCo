/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2009 -- IBM Internal Use Only
*
*=============================================================================
*
* CREATOR: Gregg Stadtlander
*    DATE: 04/12/2010
*
*-PURPOSE---------------------------------------------------------------------
* TK Injection log file viewer.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 04/12/2010 GFS  Initial coding.
* 10/27/2010 GFS  Changed button text to black.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.app.etree.transmit_ui;

import java.util.Hashtable;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.ibm.stg.eda.component.jfacebase.GridUtils;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofFile;

public class LogViewerUI extends ApplicationWindow {

    /**
     * Constructor.
     * @param aWindow
     * @param aLogCollection
     * @throws Exception 
     */
    public LogViewerUI(ApplicationWindow aWindow, 
                       Vector<String> aLogCollection) { 
        
        super(aWindow.getShell());
        setWindow(aWindow);
        setLogs(aLogCollection);
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
        shell.setText("Injection Build - log viewer");

        // Set the window size by scaling parent's size.
        shell.setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        display = shell.getDisplay();

        // Define the Grid layout
        Composite gridComposite = new Composite(shell, SWT.BORDER);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridComposite.setLayout(gridLayout);

        // Create the Inject Request group.
        try {
            setLogComposite(gridComposite, 4);
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
    private void setLogComposite(Composite comp, int gridWidth)
    throws IcofException {

        // Create the group.
        Group group = new Group(comp, SWT.SHADOW_IN);
        group.setText("Build log file");

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
        thisLabel.setText("Log files");

        data = new GridData();
        data.horizontalSpan = 1;
        thisLabel.setLayoutData(data);

        // Set the log file combo.
        logCombo = new Combo(group, SWT.NULL);
        logCombo.add("Select one ...");
        for(int i = 0; i < getLogs().size(); i++) {
            String log = (String) getLogs().get(i);
            int index = log.lastIndexOf("/");
            String file = log.substring(index + 1);
            String dir = log.substring(0, index);
            logCombo.add(file);
            locations.put(file, dir);
        }        
        logCombo.select(0);
        logCombo.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                viewContents(logCombo.getItem(logCombo.getSelectionIndex()));
            }
            public void widgetDefaultSelected(SelectionEvent e) {
                //System.out.println("Default selected index: " + locationCombo.getSelectionIndex() + ", selected item: " + (locationCombo.getSelectionIndex() == -1 ? "<null>" : locationCombo.getItem(locationCombo.getSelectionIndex())) + ", text content in the text field: " + locationCombo.getText());
            }
        });

        data = new GridData();
        data.horizontalSpan = gridWidth - 1;
        data.horizontalAlignment = SWT.CENTER;
        logCombo.setLayoutData(data);


        // Create the Files Updated box.
        logContentsText = new Text(group, SWT.BORDER | SWT.MULTI | 
                                   SWT.V_SCROLL | SWT.H_SCROLL);
        logContentsText.setText("");
        logContentsText.setEditable(false);
        logContentsText.setBackground(GridUtils.getWhite(display));
        logContentsText.setForeground(GridUtils.getBlack(display));

        data = new GridData();
        data.horizontalSpan = gridWidth;
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        data.heightHint = logContentsText.getLineHeight() * 32;
        logContentsText.setLayoutData(data);


        // Create Close button.
        Button closeButton = new Button(group, SWT.PUSH);
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
        data.horizontalSpan = gridWidth / 2;
        closeButton.setLayoutData(data);
        
    }

    
    /**
     * Load the contents tables with files.
     * 
     * @throws IcofException 
     */
    private void viewContents(String file) {

        // Return if default is selected.
        if (file.indexOf("Select") > -1) {
            return;
        }
        
        
        // Read the log file.
        String fileName = (String) locations.get(file);
        fileName += "/" + file;
        IcofFile myLog = new IcofFile(fileName, false);

        try {
            myLog.openRead();
            myLog.read();
        }
        catch(IcofException ex) {
            MessageDialog.openWarning(shell, "Warning",
                                      "Unable to open selected file. " +
                                      "File: " + myLog.getAbsolutePath());
            return;
        }
        finally {
            if (myLog.isOpenRead()) {
             try {  
                 myLog.closeRead();
             }
             catch(IcofException ignore) {}
            }
        }

        // Load the contents into the Text box.
        StringBuffer contents = new StringBuffer();
        for (int i = 0; i < myLog.getContents().size(); i++) {
            String line = (String) myLog.getContents().get(i);
            contents.append(line + "\n");
        }
        logContentsText.setText(contents.toString());
        
    }


    /**
     * Constants
     */
    private final static int SCREEN_WIDTH = 630;
    private final static int SCREEN_HEIGHT = 650;
    

    /**
     * Members.
     */
    private Display display;
    private ApplicationWindow window;
    private Shell shell;
    private Vector<String> logs;
    private Hashtable<String,String> locations = new Hashtable<String,String>();
    private Text logContentsText;
    private Combo logCombo;
    
    
    /**
     * Getters.
     */
    private ApplicationWindow getWindow() { return window; }
    public Vector<String> getLogs() { return logs; }
    public Vector<String> getLocations() { return logs; }

    
    /**
     * Setters.
     */
    private void setWindow(ApplicationWindow aWindow) { window = aWindow; }
    private void setShell() { shell = new Shell(getWindow().getShell()); }
    public void setLogs(Vector<String> aCollection) { logs = aCollection;  }
    
}
