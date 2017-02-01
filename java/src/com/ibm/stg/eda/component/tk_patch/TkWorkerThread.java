/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2010 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 * CREATOR: Gregg Stadtlander
 *    DATE: 03/15/2010
 *
 *-PURPOSE---------------------------------------------------------------------
 * TK Injection build worker thread class.
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 03/15/2010 GFS  Initial coding.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.tk_patch;

import java.util.Vector;

import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofSystemUtil;




/**
 * Worker Thread class
 */
public class TkWorkerThread implements Runnable {

    // Constants
     static final String[] STATES = { "RUNNING", "COMPLETE"};

    // Getters
    public String getProgress() { return state; }
    public String getMachine() { return machine; }
    public String getLogFileName() { return logFileName; }
    public String getPlatform() { return platform; }

    

    // Members
    private String command;
    private String startDirectory;
    private String fullCommand;
    private String logFileName;
    private String machine;
    private String platform;
    private String state = STATES[0];
    
    /**
     * Constructor 
     * 
     * @param aPatch           The TK patch object
     * @param aCommand         Command to execute
     * @param aMachine         Remote machine name
     * @param aStartDirectory  Directory to execute command in
     * @throws IcofException
     */
    public TkWorkerThread(TkPatch aPatch, String aCommand, String aMachine, 
                          String aStartDirectory, String aPlatform) 
    throws IcofException {
        command = "sleep 30;" + aCommand;
        machine = aMachine;
        platform = aPlatform;
        startDirectory = aStartDirectory;
        setLogFileName(aPatch);
    }

    // Create and run the remote command
    public void run() {

        setFullCommand();
        try {
            runCommand();
        }
        catch (IcofException e) {
//            MessageDialog.openInformation(shell, 
//                                          "Possible error running command",
//                                          "See log at " + getLogFileName());
        }
        state = STATES[1];
    }

    private void setLogFileName(TkPatch aPatch) {
        logFileName = TkInjectUtils.getLogFileName(aPatch.getId(), 
                                                   aPatch.getComponent(), 
                                                   getPlatform(), true);
    }
    private void setFullCommand() {
        fullCommand = "/afs/eda/u/einslib/bin/ET_RunRemoteCommand" 
            + " -m " + machine 
            + " -l " + getLogFileName()
            + " -d " + startDirectory 
            + " -c \"" + command + "\"";
    }
    private void runCommand() throws IcofException {
        
        System.out.println("==> Command: " + fullCommand);

        // Run the command.
        Vector<String> results = new Vector<String>();
        StringBuffer errorMsg = new StringBuffer();
        System.out.println("==>Running command ...");
        IcofSystemUtil.execSystemCommand(command, errorMsg, results);
        System.out.println("==>Command complete");
        System.out.println("==>Results: " + results.toString());

    }

}