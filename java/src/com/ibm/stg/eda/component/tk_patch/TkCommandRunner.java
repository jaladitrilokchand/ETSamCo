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
* Class for EDA Tool Kit TK command execution class
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 03/26/2010 GFS  Initial coding.
* 04/14/2010 GFS  Added getSummary() method.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_patch;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofFile;

public class TkCommandRunner implements java.io.Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 6661240054925320896L;
	/*
     * Constants
     */
    public static final String CLASS_NAME = "TkCommandRunner";
    public static final String DELIMITER = "#";
    public static final String START_DIR = "START_DIR:";

    
    /**
     * Constructor
     * 
     * @param aStartDirectory
     * @param aCommandFile
     */
    public TkCommandRunner(String aStartDirectory, String aCommandFile) {
        setStartDirectory(aStartDirectory);
        setCommandFileName(aCommandFile);
        
    }
  
    
    /**
     * Add a TkCommand object to the command collection.
     */
    public void addCommand(TkCommand command) {
        
        // Create the collection if not created yet.
        if (commandList == null) {
            commandList = new Hashtable<String,TkCommand>();
        }
        
        // Add the command.
        commandList.put(command.getPlatform(), command);
        
    }

    
    /**
     * Return the TkCommand object for the specified platform.
     */
    public TkCommand getCommand(String platform) {
        
        TkCommand command = null;
        
        if (commandList.containsKey(platform)) {
            command =  commandList.get(platform);
        }
        
        return command;
        
    }

    
    /**
     * Return a summary of the commands to run.
     */
    public String getSummary() {
        
        StringBuffer summary = new StringBuffer();
        
        // If no commands to run then return empty string
        if ((getCommands() == null) || (getCommands().size() < 1)) {
            return summary.toString();
        }
        
        
        // Add the start directory.
        summary.append("Run commnds below ...\n");
        summary.append( " Start directory: " + getStartDirectory() + "\n");
        
        // Add entries for each command in the collection.
        if (getCommands() != null) {
            Iterator<TkCommand> iter = getCommands().values().iterator();
            while (iter.hasNext()) {
                TkCommand command =  iter.next();
                summary.append("  - on " + command.getPlatform() + " run \"" 
                               + command.getCommand() + "\"\n");
            }
        }
        if ((getCommands() == null) || (getCommands().size() < 1)) {
            summary.append("  - No platforms selected\n");
        }
        
        return summary.toString();
        
    }

    
    /**
     * Write the commands to the command file.
     * @throws IcofException 
     */
    public void writeCommandFile() throws IcofException {
        
        // Add the start dir to the file contents.
        Vector<String> contents = new Vector<String>();
        String entry = START_DIR + getStartDirectory();
        contents.add(entry);
        
        // Add entries for each command in the collection.
        Iterator<TkCommand> iter = getCommands().values().iterator();
        while (iter.hasNext()) {
            TkCommand command =  iter.next();
            entry = command.getPlatform() + command.getTechLevel() + DELIMITER 
                    + command.getCommand() + DELIMITER 
                    + command.getLogFile() + DELIMITER 
                    + command.getStatusFileName();
            contents.add(entry);
        }

        
        // Write the command file.
        try {
            commandFile.openWrite();
            commandFile.write(contents);
        }
        catch (IcofException e) { throw e ; }
        finally {
            if (commandFile.isOpen())
                commandFile.closeWrite();
        }
        
    }

    
    /**
     * Update the command status for each command.
     * @throws IcofException 
     * @throws IcofException 
     */
    public void updateCommandStatus() throws IcofException {
        
        setAllComplete(true);
        
        // Iterate through the commands updating the status if not complete.
        Iterator<TkCommand> iter = getCommands().values().iterator();
        while (iter.hasNext()) {
            TkCommand command =  iter.next();
            if ((command != null) &&
               (! command.getState().equals(TkCommand.COMPLETE))) {
                command.readStatus();
                if (! command.getState().equals(TkCommand.COMPLETE)) {
                    setAllComplete(false);
                }
            }
            
        }
        
    }

    /**
     * Return a collection of build logs.
     * @throws IcofException 
     */
    public Vector<String> getLogFiles() {
        
        Vector<String> logs = new Vector<String>();
        
        // Iterate through the commands to determine the log files.
        Iterator<TkCommand> iter = getCommands().values().iterator();
        while (iter.hasNext()) {
            TkCommand command =  iter.next();
            logs.add(command.getLogFile());
        }

        return logs;
        
    }

    
    /**
     * Remove any status files for commands that are complete
     * @throws IcofException 
     * @throws IcofException 
     */
    public void cleanupFiles() {
        
        // Iterate through the commands removing status files for 
        // complete commands.
        if (getCommands() != null) {
            Iterator<TkCommand> iter = getCommands().values().iterator();
            while (iter.hasNext()) {
                TkCommand command =  iter.next();
                if (command.getState().equals(TkCommand.COMPLETE)) {
                    command.removeStatusFile();
                }
            }
        }

        // Remove the command file.
        if (commandFile.exists()) {
            try {
                commandFile.remove(false);
            }
            catch(IcofException ignore) {}
        }
        
    }

    
    /*
     * Getters
     */
    public String getStartDirectory() { return startDirectory;}
    public String getCommandFileName() { return commandFileName;}
    public IcofFile getCommandFile() { return commandFile;}
    public Hashtable<String,TkCommand> getCommands() { return commandList; }
    public boolean isAllComplete() { return allComplete; }
    
    
    /*
     * Setters
     */
    private void setStartDirectory(String aDirectory) { startDirectory = aDirectory; }
    private void setCommandFileName(String aName) { 
        commandFileName = aName;
        setCommandFile();
    }
    private void setCommandFile() { 
        commandFile = new IcofFile(getCommandFileName(), false); }
    private void setAllComplete(boolean aState) { allComplete = aState; }
        
    
    /*
     * Members
     */
    private String startDirectory = "";
    private String commandFileName = "";
    private IcofFile commandFile;
    private Hashtable<String,TkCommand> commandList;
    private boolean allComplete = false;
    
}
