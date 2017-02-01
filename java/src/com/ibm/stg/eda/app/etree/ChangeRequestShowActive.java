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
* Displays the user's active ChangeRequests. 
*-----------------------------------------------------------------------------
*
*-CHANGE LOG------------------------------------------------------------------
* 07/21/2011 GFS  Initial coding.
* 09/06/2011 GFS  Added support for Change Request type and severity.
* 11/29/2012 GFS  Updated to support CR's new impacted customer attribute.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.app.etree;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.ChangeRequestActive_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequest;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofStringUtil;

public class ChangeRequestShowActive extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "cr.showDefault";
    public static final String APP_VERSION = "v1.0";

        
    /**
     * Constructor
     *
     * @param     xContext  Application context
     * @param     aCqId     ChangeRequest's CQ number
     */
    public ChangeRequestShowActive(EdaContext xContext)	
    throws IcofException {

        super(xContext, APP_NAME, APP_VERSION);

    }

    
    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     *
     * @param     argv[]            the command line arguments
     */
    public static void main(String argv[]) {

    	TkAppBase myApp = null;
		try {

			myApp = new ChangeRequestShowActive(null);
			start(myApp, argv);
		}

		catch (Exception e) {

			handleExceptionInMain(e);
		} finally {

			handleInFinallyBlock(myApp);
		}

    }

    
    //--------------------------------------------------------------------------
    /**
     * Add, update, delete, or report on the specified applications.
     * 
     * @param aContext      Application Context
     * @throws              IcofException
     */
    //--------------------------------------------------------------------------
    public void process(EdaContext xContext) throws IcofException {

        // Connect to the database
        connectToDB(xContext);

        // Lookup and display the active ChangeRequests
        setActiveRequests(xContext);
        showActiveRequests(xContext);
        
        // Set the return code to success if we get this far.
        setReturnCode(xContext, SUCCESS);
        rollBackDB(xContext, APP_NAME);
        
    }

    
    /**
     * Display active ChangeRequests.
     * 
     * @param xContext Application context.
     * @throws IcofException 
     */
    private void showActiveRequests(EdaContext xContext) throws IcofException {
		
    	StringBuffer result = new StringBuffer();
    	
    	boolean noHeader = true;
    	Iterator<ChangeRequestActive_Db> iter = getActiveRequests().iterator();
    	while (iter.hasNext()) {
    		ChangeRequestActive_Db request =  iter.next();
    		request.getChangeRequest().dbLookupById(xContext);
    		request.getCompVersion().dbLookupById(xContext);
    		
    		if (noHeader) {
    			result.append("Default ChangeRequests for " + getUser().getAfsId() + "\n");
    			result.append("\n");
    			result.append(formatLine("Tool Kit", "Component", "ChangeRequest", "Description"));
    			result.append(formatLine("--------", "---------", "-------------", "-----------"));
    			noHeader = false;
    		}
    		
    		// Lookup any missing data.
    		if (! request.getCompVersion().isLoaded()) {
    			request.getCompVersion().dbLookupById(xContext);
    		}
    		if (! request.getCompVersion().getVersion().isLoaded()) {
    			request.getCompVersion().getVersion().dbLookupById(xContext);
    		}
    		if (! request.getCompVersion().getCompRelease().isLoaded()) {
    			request.getCompVersion().getCompRelease().dbLookupById(xContext);
    		}
    		if (! request.getCompVersion().getCompRelease().getRelease().isLoaded()) {
    			request.getCompVersion().getCompRelease().getRelease().dbLookupById(xContext);
    		}
    		if (! request.getCompVersion().getCompRelease().getComponent().isLoaded()) {
    			request.getCompVersion().getCompRelease().getComponent().dbLookupById(xContext);
    		}
    		
    		result.append(formatLine(request.getCompVersion().getCompRelease().getRelease().getName() + "." +
    		                         request.getCompVersion().getVersion().getName(), 
    		                         request.getCompVersion().getCompRelease().getComponent().getName(),
    		                         request.getChangeRequest().getCqName(),
    		                         request.getChangeRequest().getDescription()));
    		
    	}
		
    	
    	if (noHeader) {
    		result.append("No default Change Requests found for " + getUser().getAfsId() + "\n");
    	}
    	
    	System.out.println(result.toString());
    	
	}

    
    /**
     * Format the display line
     * @param tkText      Tool Kit text
     * @param compText    Component text
     * @param crText      Change Request text
     * @param description Change Request description
     * @return String formatted for display
     */
	private String formatLine(String tkText, String compText, String crText,
	                          String description) {
		
		// Truncate the description if over max
		int max = 25;
		if (description.length() > max) {
			String temp = description.substring(0, max);
			description = temp;
		}

		// Construct the line
		String line = IcofStringUtil.leftJustify(tkText, " ", 9) + 
		              IcofStringUtil.leftJustify(compText, " ", 11) +
		              IcofStringUtil.leftJustify(crText, " ", 15) +
		              IcofStringUtil.leftJustify(description, " ", max) + "\n";

		return line;
			
	}


	/**
     * Update the state of the new ChangeRequest to APPROVED.
     * 
     * @param xContext  Application context
     * @throws IcofException 
     */
    private void setActiveRequests(EdaContext xContext) throws IcofException {
    	
    	ChangeRequest request = 
    		new ChangeRequest(xContext, "", "", null, null, null, null);
    	activeRequests = request.getAllDefaultUser(xContext, getUser());
    	
    }

	protected String readParams(Hashtable<String,String> params, String errors, EdaContext xContext)
	throws IcofException {return "";}

	protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {
		singleSwitches.add("-y");
        singleSwitches.add("-h");
        argSwitches.add("-db");
	}


	protected void displayParameters(String dbMode, EdaContext xContext) {
		boolean verboseInd = getVerboseInd(xContext);
        logInfo(xContext, "App        : " + APP_NAME + "  " + APP_VERSION, verboseInd);
        logInfo(xContext, "DB Mode    : " + dbMode, verboseInd);
        logInfo(xContext, "Verbose    : " + getVerboseInd(xContext), verboseInd);
	}

    
    /**
     * Display this application's usage and invocation
     */
    protected void showUsage() {

        StringBuffer usage = new StringBuffer();
        usage.append("------------------------------------------------------\n");
        usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
        usage.append("------------------------------------------------------\n");
        usage.append("Displays your Default ChangeRequests.\n");
        usage.append("\n");
        usage.append("USAGE:\n");
        usage.append("------\n");
        usage.append(APP_NAME + " [-y] [-h] [-db dbMode]\n");
        usage.append("\n");
        usage.append("  -y          = (optional) Verbose mode (echo messages to screen)\n");
        usage.append("  dbMode      = (optional) DEV | PROD (defaults to PROD)\n");
        usage.append("  -h          = Help (shows this information)\n");
        usage.append("\n");
        usage.append("Return Codes\n");
        usage.append("------------\n");
        usage.append(" 0 = ok\n");
        usage.append(" 1 = error\n");
        usage.append("\n");

        System.out.println(usage);

    }
    
    
    /**
     * Members.
     */

    private Vector<ChangeRequestActive_Db> activeRequests;
    
    /**
     * Getters.
     */

    public Vector<ChangeRequestActive_Db> getActiveRequests() { return activeRequests; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}


	@Override
	protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
		return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
	}

}
