/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2009 -- IBM Internal Use Only
*
*=============================================================================
*
* CREATOR: Gregg Stadtlander
*
*-PURPOSE---------------------------------------------------------------------
* Capture the user's CQ id and password
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 05/06/2014 GFS  Initial coding.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.app.etree.transmit_ui;

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
import org.eclipse.swt.widgets.Text;
import com.ibm.stg.eda.component.jfacebase.GridUtils;
import com.ibm.stg.iipmds.common.IcofException;

public class CqUserUI extends ApplicationWindow {

    /**
     * Constructor.
     * @param aWindow
     * @throws Exception 
     */
    public CqUserUI(ApplicationWindow aWindow,
                    String aName, String aPw) { 
        
        super(aWindow.getShell());
        setWindow(aWindow);
        setShell();
        setUserName(aName);
        setUserPassword(aPw);

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
        shell.setText("EDA Tool Kit");

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
	    setUserComposite(gridComposite, 2);
	}
	catch (IcofException e) {
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
    private void setUserComposite(Composite comp, int gridWidth)
    throws IcofException {

        // Create the group.
        Group group = new Group(comp, SWT.SHADOW_IN);
        group.setText("ClearQuest user data");

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
        Label nameLabel = new Label(group, SWT.NONE);
        nameLabel.setText("CQ id (intranet id)");

        data = new GridData();
        data.horizontalSpan = gridWidth/2;
        nameLabel.setLayoutData(data);

        // Create the user name text box.
        nameText = new Text(group, SWT.BORDER);
        nameText.setText(getUserName());
        nameText.setBackground(GridUtils.getWhite(display));
        nameText.setForeground(GridUtils.getBlack(display));

        data = new GridData();
        data.horizontalSpan = gridWidth/2;
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = SWT.FILL;
        nameText.setLayoutData(data);

        // Create the user password label.
        Label pwLabel = new Label(group, SWT.NONE);
        pwLabel.setText("CQ password");

        data = new GridData();
        data.horizontalSpan = gridWidth/2;
        pwLabel.setLayoutData(data);

        // Create the user password text box.
        passwordText = new Text(group, SWT.BORDER | SWT.PASSWORD);
        passwordText.setText(getUserPassword());
        passwordText.setBackground(GridUtils.getWhite(display));
        passwordText.setForeground(GridUtils.getBlack(display));

        data = new GridData();
        data.horizontalSpan = gridWidth/2;
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = SWT.FILL;
        passwordText.setLayoutData(data);
        
        // Create Done button.
        Button doneButton = new Button(group, SWT.PUSH);
        doneButton.setText("Ok");
        doneButton.setBackground(GridUtils.getGreen(display));
        doneButton.setForeground(GridUtils.getBlack(display));
        doneButton.addSelectionListener(new SelectionAdapter () {
            public void widgetSelected(SelectionEvent e) {
        	setUserName(nameText.getText());
        	setUserPassword(passwordText.getText());
        	setDone(true);
                getShell().dispose();
            }
        });

        data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.horizontalSpan = gridWidth/2;
        doneButton.setLayoutData(data);

        // Create Cancel button.
        Button closeButton = new Button(group, SWT.PUSH);
        closeButton.setText("Cancel");
        closeButton.setBackground(GridUtils.getGreen(display));
        closeButton.setForeground(GridUtils.getBlack(display));
        closeButton.addSelectionListener(new SelectionAdapter () {
            public void widgetSelected(SelectionEvent e) {
        	setDone(false);
                getShell().dispose();
            }
        });

        data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.horizontalSpan = gridWidth/2;
        closeButton.setLayoutData(data);
        
    }

    
    /**
     * Constants
     */
    private final static int SCREEN_WIDTH = 350;
    private final static int SCREEN_HEIGHT = 200;
    

    /**
     * Members.
     */
    private Display display;
    private ApplicationWindow window;
    private Shell shell;
    private boolean done;
    private String userName;
    private String userPassword;
    private Text nameText;
    private Text passwordText;
    
    
    /**
     * Getters.
     */
    private ApplicationWindow getWindow() { return window; }
    public boolean getDone() { return done; }
    public String getUserName() { return userName; }
    public String getUserPassword() { return userPassword; }
    
    
    /**
     * Setters.
     */
    private void setWindow(ApplicationWindow aWindow) { window = aWindow; }
    private void setDone(boolean aFlag) { done = aFlag; }
    private void setShell() { shell = new Shell(getWindow().getShell()); }
    private void setUserName(String aName) { userName = aName; }
    private void setUserPassword(String aPw) { userPassword = aPw; }
    
}
