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
* 01/21/2010 GFS  Added support for files extracted from CMVC.
* 02/09/2010 GFS  Removed levelHist from constructor since it is now in the 
*                 patch object.
* 03/18/2010 GFS  Set the window dimensions to a constant size.
* 10/27/2010 GFS  Changed button text to black.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.app.injectBuild;

import java.util.Iterator;

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
import com.ibm.stg.eda.component.tk_patch.TkPatch;

public class TkInjectSummaryUI extends ApplicationWindow {

    /**
     * Constructor.
     * @param aWindow
     * @param aWinTitle
     * @param aQuestion
     * @param aPatch
     * @throws Exception 
     */
    public TkInjectSummaryUI(ApplicationWindow aWindow, 
                             String aTitle, 
                             String aQuestion,
                             TkPatch aPatch) throws Exception { 

        super(aWindow.getShell());
        setWindow(aWindow);
        setTitle(aTitle);
        setQuestion(aQuestion);
        setPatch(aPatch);
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
        shell.setText(getTitle());

        // Set the window size by scaling parent's size.
        shell.setSize(SCREEN_WIDTH, SCREEN_HEIGTH);
        display = shell.getDisplay();

        // Define the Grid layout
        Composite gridComposite = new Composite(shell, SWT.BORDER);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridComposite.setLayout(gridLayout);

        // Create the LEVELHIST group.
        setTextBoxGroup(gridComposite, 2, "New LEVELHIST contents", 
                        getLevelHistContents(), SCREEN_HEIGTH / 4);

        // Create the Backup group.
        setTextBoxGroup(gridComposite, 2, "Files to be backed up", 
                        getBackupActions(), SCREEN_HEIGTH / 7);

        // Create the Copy group.
        setTextBoxGroup(gridComposite, 2, "Files to be copied",
                        getCopyActions(), SCREEN_HEIGTH / 7);
        
        // Create the button group.
        //GridUtils.setBlankSpace(gridComposite, 2, SWT.NONE);
        setButtons(gridComposite, 2);
        
        return parent;
        
    }

    
    /**
     * Gather a list of backup files.
     * 
     * @return String of copy files
     */
    private String getCopyActions() {

        StringBuffer actions = new StringBuffer();
        
        // Load the copy actions.
        Iterator<String> iter = patch.getCopyActions().keySet().iterator();
        while (iter.hasNext()) {
            String tgt =  iter.next();
            String src =  patch.getCopyActions().get(tgt);
            actions.append(tgt + "  [replaced by " + src + "]\n");

        }

        Iterator<String> iter2 = patch.getExtractFileActions().keySet().iterator();
        while (iter2.hasNext()) {
            String tgt =  iter2.next();
            actions.append(tgt + "  [replaced by file extracted from CMVC]\n");

        }

        Iterator<String> iter3 = patch.getTrackExtractList().keySet().iterator();
        while (iter3.hasNext()) {
            String track =  iter3.next();
            actions.append(track + "  [extract track from CMVC]\n");

        }
        
        return actions.toString();

    }


    /**
     * Gather a list of backup files.
     * 
     * @return String of backup files.
     */
    private String getBackupActions() {

        StringBuffer actions = new StringBuffer();
        
        // Load the copy actions.
        Iterator<String> iter = patch.getBackupActions().keySet().iterator();
        while (iter.hasNext()) {
            String target =  iter.next();
            actions.append(target);
            actions.append("\n");
        }

        return actions.toString();
    }


    /**
     * @return
     */
    private String getLevelHistContents() {

        StringBuffer contents = new StringBuffer();
        
        // Get the new contents from the levelhist object.
        Iterator<String> iter = patch.getTargetLevelHist().getNewContents().iterator();
        while (iter.hasNext()) {
            String line =  iter.next();
            contents.append(line);
            contents.append("\n");
        }
        
        return contents.toString();
    
    }


    /**
     * Define and layout the Console widgets.
     * 
     * @param comp       The parent composite widget which will hold this widget.
     * @param gridWidth  Number of columns in current grid.
     * @param heading    Text for group name
     * @param text       Text to enter in text area
     * @param heightHint Hint for height of text box. 
     */
    private void setTextBoxGroup(Composite comp, int gridWidth, 
                                 String heading, String text, 
                                 int heightHint) {

        // Create the group.
        Group group = new Group(comp, SWT.SHADOW_IN);
        group.setText(heading);

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

        
        // Create the Source Location box.
        Text messageText = new Text(group, SWT.BORDER | SWT.MULTI
                                    | SWT.V_SCROLL | SWT.H_SCROLL);
        messageText.setText(text);
        messageText.setBackground(GridUtils.getWhite(display));
        messageText.setForeground(GridUtils.getBlack(display));
        
        data = new GridData();
        data.horizontalSpan = gridWidth;
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        data.heightHint = heightHint;
        messageText.setLayoutData(data);

    }

    
    /**
     * Define and layout the Console widgets.
     * 
     * @param comp       The parent composite widget which will hold this widget.
     * @param gridWidth  Number of columns in current grid.
     */
    private void setButtons(Composite comp, int gridWidth) {

        // Create the Request label.
        Label thisLabel = new Label(comp, SWT.NONE);
        thisLabel.setText(getQuestion());

        GridData data = new GridData();
        data.horizontalSpan = gridWidth;
        thisLabel.setLayoutData(data);
        
        // Create Ok button.
        Button okButton = new Button(comp, SWT.PUSH);
        okButton.setText("Yes (continue)");
        okButton.setBackground(GridUtils.getGreen(display));
        okButton.setForeground(GridUtils.getBlack(display));
        okButton.addSelectionListener(new SelectionAdapter () {
            public void widgetSelected(SelectionEvent e) {
                setReply(true);
                getShell().dispose();
            }
        });

        data = new GridData();
        data.horizontalAlignment = SWT.CENTER;
        data.horizontalSpan = gridWidth/2;
        okButton.setLayoutData(data);

        // Create Cancel button.
        Button cancelButton = new Button(comp, SWT.PUSH);
        cancelButton.setText("No (cancel)");
        cancelButton.setBackground(GridUtils.getGreen(display));
        cancelButton.setForeground(GridUtils.getBlack(display));
        cancelButton.addSelectionListener(new SelectionAdapter () {
            public void widgetSelected(SelectionEvent e) {
                setReply(false);
                getShell().dispose();
            }
        });

        data = new GridData();
        data.horizontalAlignment = SWT.CENTER;
        data.horizontalSpan = gridWidth/2;
        cancelButton.setLayoutData(data);

    }

    /**
     * Constants
     */
    private final static int SCREEN_WIDTH = 700;
    private final static int SCREEN_HEIGTH = 700;

    
    /**
     * Members.
     */
    private Display display;
    private ApplicationWindow window;
    private Shell shell;
    private TkPatch patch;
    private String title;
    private String question;
    private boolean reply = false;
    
    
    /**
     * Getters.
     */
    private ApplicationWindow getWindow() { return window; }
    private String getTitle() { return title; }
    private String getQuestion() { return question; }
    public boolean getReply() { return reply; }

    
    /**
     * Setters.
     */
    private void setWindow(ApplicationWindow aWindow) { window = aWindow; }
    private void setShell() { shell = new Shell(getWindow().getShell()); }
    private void setTitle(String aTitle) { title = aTitle; }
    private void setQuestion(String aQuestion) { question = aQuestion; }
    private void setReply(boolean answer) { reply = answer; }
    private void setPatch(TkPatch aPatch) { patch = aPatch; }
    
}
