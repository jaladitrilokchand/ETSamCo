/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2008 -- IBM Internal Use Only
*
*=============================================================================
*
* CREATOR: Gregg Stadtlander
*    DATE: 11/12/2009
*
*-PURPOSE---------------------------------------------------------------------
* A simple Tabbed example with status line
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 11/12/2009 GFS  Initial coding.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.app.injectBuild;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.ibm.stg.eda.component.jfacebase.ExitAction;
import com.ibm.stg.eda.component.jfacebase.ShowAboutAction;

public class TestTabStatus extends ApplicationWindow {

    /**
     * Constants.
     */
    final static String APP_NAME = "TestTabStatus";
    final static String APP_VERSION = "1.0";
    final static String APP_OWNER = "Gregg Stadtlander";
    
    /**
     * HelloWorld_jface1 constructor
     */
    public TestTabStatus() {
        super(null);
        setMenuActions();
    }

    
    // Actions.
    private ExitAction exitAction;
    private ShowAboutAction aboutAction;
    
    
    /**
     * Run the application.
     */
    public void run() {
        // Don't return from open() until window closes
        setBlockOnOpen(true);

        // Add the status line.
        addStatusLine();
        addMenuBar();
        
        // Open the main window
        open();

        // Dispose the display
        Display.getCurrent().dispose();
    }

    
    /**
     * Creates the main window's menu bar contents.
     * 
     * @return MenuManger
     */
    protected MenuManager createMenuManager() {
        MenuManager menuBar = new MenuManager();

        MenuManager fileMenu = new MenuManager("&File");
        MenuManager viewMenu = new MenuManager("&View");
        MenuManager helpMenu = new MenuManager("&Help");

        fileMenu.add(exitAction);
        helpMenu.add(aboutAction);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);

        return menuBar;
    }


    /**
     * Create the main window's contents.
     * 
     * @param parent the main window
     * @return Control
     */
    protected Control createContents(Composite parent) {

        final TabFolder tabFolder = new TabFolder (parent.getShell(), SWT.BORDER);

        for (int i=0; i<6; i++) {
            TabItem item = new TabItem (tabFolder, SWT.NONE);
            
            
            // Create the first group.
            Group group = new Group(tabFolder, SWT.SHADOW_IN);
            group.setText("Tool Kit Patch");
            
            // Make it have a grid layout
            GridLayout gridLayout = new GridLayout();
            gridLayout.numColumns = 3;
            group.setLayout(gridLayout);

            // Define the buttons.
            Button button1 = new Button (group, SWT.PUSH);
            button1.setText ("Button1 " + i);

            Button button2 = new Button (group, SWT.PUSH);
            button2.setText ("Button2 " + i);

            // Place them in the grid.
            GridData data = new GridData();
            data.horizontalSpan = 1;
            button1.setLayoutData(data);
            
            data = new GridData();
            data.horizontalSpan = 2;
            button2.setLayoutData(data);
            
            item.setText("TabItem " + i);
            item.setControl(group);
            
        }
        tabFolder.pack ();

        parent.getShell().pack ();
        parent.getShell().open ();

        getStatusLineManager().setMessage("hey");
        
        return parent;
        
    }


    /**
     * The application entry point
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new TestTabStatus().run();
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
    
    }
    
    
    
}

