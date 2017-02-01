/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2010 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 * CREATOR: Gregg Stadtlander
 *
 *-PURPOSE---------------------------------------------------------------------
 * Subversion business object.
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 07/29/2011 GFS  Initial coding. 
 * 08/03/2011 GFS  Added hostName member, getter and setter
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.tk_etreebase;

import java.net.UnknownHostException;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofSystemUtil;

public class TkSystem  {

    /**
     * Constructor - takes a Command
     * 
     * @param xContext     Application context
     * @param aCommand     Command to run
     * @throws IcofException 
     */
    public TkSystem(EdaContext xContext, String aCommand) 
    throws IcofException {
	setCommand(aCommand);
    }


    /**
     * Data Members
     */
    private String command;
    private int returnCode;
    private StringBuffer errorMsg;
    private Vector<String> results;
    private String hostName;


    /**
     * Getters
     */
    public String getCommand() { return command; }
    public int getReturnCode() { return returnCode; }
    public StringBuffer getErrorMsg() { return errorMsg; }
    public Vector<String> getResults() { return results; }
    public String getHostName() { return hostName; }

    /**
     * Setters
     */
    protected void setCommand(String aCmd) { command = aCmd; }
    protected void setHostName() {
	java.net.InetAddress addr;
	try {
	    addr = java.net.InetAddress.getLocalHost();
	    hostName = addr.getHostName();
	}
	catch (UnknownHostException ignore) {
	    hostName = null;
	}

    }



    /**
     * Initialize the command variables.
     */
    protected void initialize() {

	// Reset results.
	if (results == null) {
	    results = new Vector<String>();
	}
	else {
	    results.clear();
	}

	// Reset error buffer.
	if (errorMsg == null) {
	    errorMsg = new StringBuffer();
	}
	else {
	    errorMsg.setLength(0); 
	}

	// Reset return code.
	returnCode = -1;

    }


    /**
     * Run the command
     * 
     * @param command  Command string to execute.
     * @return If IcofException was encountered then return the message
     *         otherwise return null.
     */
    protected String execute(EdaContext xContext) throws IcofException { 

	String returnString = null;

	// Initialize the command variables
	initialize();

	// Run the command.
	try {
	    returnCode = 
	    IcofSystemUtil.execSystemCommand(command, errorMsg, results);
	}
	catch (IcofException ex) {
	    returnString = ex.getMessage();
	}

	return returnString;    	

    }

}
