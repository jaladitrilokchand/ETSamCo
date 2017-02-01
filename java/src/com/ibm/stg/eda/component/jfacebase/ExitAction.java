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
 * Exit action class
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 03/25/2009 GFS  Initial coding.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.jfacebase;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.ApplicationWindow;

public class ExitAction extends Action {

    /**
     * Constants.
     */
    public final static String MENU_ITEM_TEXT = "E&xit@Ctrl+X";
    public final static String STATUS_TEXT = "Close this application";
    
    
    /**
     * Constructor.
     * 
     * @param aWindow    An application window.
     * @param aMenuText  The menu text.
     * @param aToolTip   The tool tip text.
     */
    public ExitAction(ApplicationWindow aWindow, String aMenuText, String aToolTip) {   
        setWindow(aWindow);  

        setText(aMenuText);
        setToolTipText(aToolTip);

    }

    /**
     *  This method is run when the user clicks on the on exit menu item.
     */
    public void run() {
        getWindow().close();
    }

    /**
     * Getters
     */
    private ApplicationWindow getWindow() { return window; }

    /**
     * Setters.
     */
    private void setWindow(ApplicationWindow window) { this.window = window; }

    /**
     * Members.
     */
    private ApplicationWindow window;

}
