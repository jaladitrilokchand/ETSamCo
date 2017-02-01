/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2009 -- IBM Internal Use Only
*
*=============================================================================
*
* CREATOR: Gregg Stadtlander
*    DATE: 03/25/2009
*
*-PURPOSE---------------------------------------------------------------------
* Show About action class
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 03/25/2009 GFS  Initial coding.
* 11/17/2009 GFS  Format updates.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.jfacebase;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.ApplicationWindow;

public class ShowAboutAction extends Action {

    /**
     * Constants.
     */
    public final static String MENU_ITEM_TEXT = "A&bout@Ctrl+b";
    public final static String STATUS_TEXT = "Display application build info.";
    
    
    /**
     * Constructor
     * 
     * @param win         An application window.
     * @param aMenuText   The menu text.
     * @param aToolTip    The tool tip text.
     * @param anAppName   The application name.
     * @param aAboutText    The about application text.
     * @param aStatusText The status line text (if null don't set status line).
     */
    public ShowAboutAction(ApplicationWindow win, String aMenuText, 
                           String aToolTip, String anAppName, 
                           String aAboutText, String aStatusText) {   
        
        setWindow(win);
        setAppName(anAppName);
        setMessage(aAboutText);
        setStatus(aStatusText);

        // Set this window's menu bar text and tool tip .
        setText(aMenuText);
        setToolTipText(aToolTip);
    
    }

    /**
     *  This method is run when the user clicks on the on show->about menu item.
     *  Opens a dialog box to display the specified message.
     */
    public void run() {
        
        // Display the status line text.
        if (getStatus() != null) {
            window.setStatus(getStatus());
        }
        
        // Open the dialog box and show the message.
        MessageDialog.openInformation(getWindow().getShell(), getAppName(),
                                      getMessage());
        
        // Clear the status line text. 
        if (getStatus() != null) {
            window.setStatus("");
        }
        
    }

    /**
     * Getters.
     */
    private String getAppName() { return appName; }
    private String getMessage() { return message; }
    private String getStatus() { return status; }
    private ApplicationWindow getWindow() { return window; }

    /**
     * Setters.
     */
    private void setAppName(String appName) { this.appName = appName; }
    private void setMessage(String message) { this.message = message; }
    private void setStatus(String status) { this.status = status; }
    private void setWindow(ApplicationWindow window) { this.window = window; }

    /**
     * Members
     */
    private ApplicationWindow window;
    private String status;
    private String appName;
    private String message;
    
}
