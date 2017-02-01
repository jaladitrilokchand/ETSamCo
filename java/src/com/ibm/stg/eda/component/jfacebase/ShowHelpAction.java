/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2009 -- IBM Internal Use Only
*
*=============================================================================
*
* CREATOR: Gregg Stadtlander
*    DATE: 03/26/2009
*
*-PURPOSE---------------------------------------------------------------------
* Show About action class
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 03/26/2009 GFS  Initial coding.
* 11/17/2009 GFS  Format updates.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.jfacebase;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.ibm.stg.iipmds.common.IcofFile;

public class ShowHelpAction extends Action {

    /**
     * Constants.
     */
    public final static String MENU_ITEM_TEXT = "&Get Help@Ctrl+g";
    public final static String STATUS_TEXT = "Display application help";

    /**
     * Constructor.
     * @param aWindow
     * @param aMenuText
     * @param aToolTip
     * @param aHelpFile
     */
    public ShowHelpAction(ApplicationWindow aWindow, 
                          String aMenuText, String aToolTip, 
                          IcofFile aHelpFile) {   
        setWindow(aWindow);
        setHelpFile(aHelpFile);
        
        setText(aMenuText);
        setToolTipText(aToolTip);
    }


    /**
     * This method is run when the user clicks on the on show->Get help menu 
     * item. Reads the specified help file and display the help text in a dialog
     * box.
     */
    public void run() {

        final Shell myShell = new Shell(getWindow().getShell(), SWT.SHELL_TRIM);
        myShell.setText("Application Help");

        // Set the window size to 80% parent's width and 90% parent's height.
        Point parentSize = myShell.getSize();
        int width = parentSize.x * getScaleX() / 100;
        int height = parentSize.y * getScaleY() / 100;
        myShell.setSize(width, height);

        // Add a grid layout to this window.
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        myShell.setLayout(gridLayout);

        // Read the Text input from the help file and display it.
        String text = "";
        
        if (getHelpFile().exists()) {  
            try {
                getHelpFile().read();
            }
            catch(Exception trap) {
                text = "Unable to read help file for this application. Could\n" +
                       "not read file - " + getHelpFile().getAbsolutePath();
            }
        }
        else {
            text = "Unable to read help file content.  Could not find \n" +
            "file: " + getHelpFile().getAbsolutePath();
        }
        StringBuffer textBuffer = new StringBuffer(text);
        for (int i = 0; i < getHelpFile().getContents().size(); i ++) {
        	textBuffer.append(getHelpFile().getContents().get(i));
        	textBuffer.append("\n");
        }
        text = textBuffer.toString();

        // Create Text input and display the help text.
        Text helpText = new Text(myShell,
                                 SWT.MULTI|SWT.BORDER|SWT.V_SCROLL|SWT.H_SCROLL|SWT.WRAP);
        helpText.setText(text);
        helpText.setFocus();
        helpText.setBackground(myShell.getDisplay().getSystemColor(SWT.COLOR_WHITE));

        GridData data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.verticalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        helpText.setLayoutData(data);

        // Create close button.
        Button closeButton = new Button(myShell, SWT.PUSH);
        closeButton.setText("Close");
        closeButton.addSelectionListener(new SelectionAdapter () {
            public void widgetSelected(SelectionEvent e) {
                myShell.dispose();
            }
        });

        data = new GridData();
        closeButton.setLayoutData(data);

        myShell.open();

    }

    
    /**
     * Members.
     */
    private ApplicationWindow window;
    private IcofFile helpFile;
    private int xScaleFactor = 80;
    private int yScaleFactor = 90;

    /**
     * Getters.
     */
    private ApplicationWindow getWindow() { return window; }
    private IcofFile getHelpFile() { return helpFile; }
    private int getScaleY() { return yScaleFactor; }
    private int getScaleX() { return xScaleFactor; }


    /**
     * Setters.
     */
    private void setWindow(ApplicationWindow window) { this.window = window; }
    private void setHelpFile(IcofFile helpFile) { this.helpFile = helpFile; }
    public void setScaleY(int scaleHeight) { yScaleFactor = scaleHeight; }
    public void setScaleX(int scaleWidth) { xScaleFactor = scaleWidth;  }

    
}
