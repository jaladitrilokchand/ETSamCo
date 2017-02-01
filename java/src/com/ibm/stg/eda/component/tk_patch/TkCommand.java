/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2009 -- IBM Internal Use Only
*
*=============================================================================
*
* CREATOR: Gregg Stadtlander
*    DATE: 03/26/2010
*
*-PURPOSE---------------------------------------------------------------------
* Class for EDA Tool Kit TK build command
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 03/26/2010 GFS  Initial coding.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_patch;

import java.util.Vector;

import com.ibm.stg.iipmds.common.IcofCollectionsUtil;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofFile;

public class TkCommand implements java.io.Serializable {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 2507705084936692340L;
	/*
     * Constants
     */
    public static final String CLASS_NAME = "TkCommand";
    public static final String COMPLETE = "COMPLETE";

    
    /**
     * Constructor 
     * 
     * @param aCommand 
     * @param aPlatform
     * @param aTechLevel
     * @param aLogFile
     * @param aStatusFile
     * @param anEmailAddress
     */
    public TkCommand(String aCommand,
                     String aPlatform,
                     String aTechLevel,
                     String aLogFile,
                     String aStatusFile,
                     String anEmailAddress) {
        setCommand(aCommand);
        setPlatform(aPlatform);
        setTechLevel(aTechLevel);
        setLogFileName(aLogFile);
        setStatusFileName(aStatusFile);
        setEmailAddress(anEmailAddress);
    }
    
    
    /**
     * Set the status (machine name and state) from the status file.
     * @throws IcofException 
     */
    public void readStatus() throws IcofException {
        
        // Read the status file.
        if (statusFile == null) {
            statusFile = new IcofFile(getStatusFileName(), false);
        }
        
        try {
            if (statusFile.exists()) {
                statusFile.openRead();
                statusFile.read();
            }
        }
        catch (IcofException trap) { 
            throw trap;
        }
        finally {
            if (statusFile.exists() && statusFile.isOpen())
                statusFile.closeRead();
        }

        // Parse the line to determine the state and machine name
        if (statusFile.getContents().size() > 0) {
            String line = (String) statusFile.getContents().firstElement();
            Vector<String>  tokens = new Vector<String> ();
            IcofCollectionsUtil.parseString(line, TkCommandRunner.DELIMITER, tokens, true);
            if (tokens.size() >= 1) {
                setState((String) tokens.get(0));
            }
            if (tokens.size() >= 2) {
                setMachine((String) tokens.get(1));
            }
        }
        
    }

    
    /**
     * Remove the status file if it exists.
     * @throws IcofException 
     */
    public void removeStatusFile() {
        
        // Return if no status file defined.
        if (statusFile == null) {
            return;
        }
        
        // Remove the status file.
        if (statusFile.exists()) {
            try {
                statusFile.remove(false);
            }
            catch(IcofException ignore) {}
        }
        
    }

    
    /*
     * Getters
     */
    public String getCommand() { return command; }
    public String getPlatform() { return platform; }
    public String getTechLevel() { return techLevel; }
    public String getEmailAddress() { return emailAddress; }
    public String getLogFile() { return logFileName; }
    public String getMachine() { return machine; }
    public String getState() { return state; }
    public String getStatusFileName() { return statusFileName; }
    
    
    /*
     * Setters
     */
    private void setCommand(String aCommand) { command = aCommand; }
    private void setPlatform(String aPlatform) { platform = aPlatform; }
    private void setTechLevel(String aLevel) { techLevel = aLevel; }
    private void setEmailAddress(String anAddress) { emailAddress = anAddress; }
    private void setLogFileName(String aLogFile) { logFileName = aLogFile; }
    private void setMachine(String aMachine) { machine = aMachine; }
    private void setState(String aState) { state = aState; }
    private void setStatusFileName(String aStatusFile) { statusFileName = aStatusFile; }
    
    
    /*
     * Members
     */
    private String command = "";
    private String platform = "";
    private String techLevel = "";
    private String emailAddress = "";
    private String logFileName = "";
    private String machine = "";
    private String state = "";
    private String statusFileName = "";
    private IcofFile statusFile;
   
}
