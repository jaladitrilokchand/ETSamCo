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
* Show the Component owner. 
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 07/26/2010 GFS  Initial coding.
* 10/05/2010 GFS  Updated showResults() to be more efficient.
* 11/11/2010 GFS  Updated to work with new dynamic role associations.
* 01/27/2011 GFS  Removed support for -a switch and cleaned up messages.
* 03/15/2012 GFS  Turned off logging.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.app.etree;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkConstants;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.CompTkRelRole_User_Db;
import com.ibm.stg.eda.component.tk_etreedb.Component_Release_Db;
import com.ibm.stg.eda.component.tk_etreedb.Release_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.TkUserRelCompRole;
import com.ibm.stg.iipmds.common.IcofException;

public class ShowComponent extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "showComponent";
    public static final String APP_VERSION = "v1.1";

        
    /**
     * Constructor
     *
     * @param     aContext    Application context
     * @param     aRelease    TK RelVersion object
     * @param     aComponent  TK component object
     *
     */
    public ShowComponent(EdaContext aContext, Release_Db aRelease, 
                         Component aComponent)
    throws IcofException {

        super(aContext, APP_NAME, APP_VERSION);

        setRelease(aRelease);
        setComponent(aComponent);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public ShowComponent(EdaContext aContext) throws IcofException {

        this(aContext, null, null);

    }
    
    
    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     *
     * @param     argv[]            the command line arguments
     */
    public static void main(String argv[]) {

    	TkAppBase myApp = null;
		try {

			myApp = new ShowComponent(null);
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

        // Validate the TK release and component.
        if (! setReleaseComponent(xContext)) {
        	setReturnCode(xContext, TkConstants.NOTHING_TO_DO);
        }
        else {
        	// Return the results.
        	showResults(xContext);
        	setReturnCode(xContext, SUCCESS);
        	
        }
        commitToDB(xContext, APP_NAME);
        
    }

    
    /**
     * Verifies the Tk Release and component are valid
     * 
     * @param xContext  Application context
     * @throws IcofException 
     */
    private boolean setReleaseComponent(EdaContext xContext) throws IcofException {

        // Look up the TkRelComponent object
    	try {
    		releaseComponent = new Component_Release_Db(getRelease(), 
    		                                            getComponent().getComponent());
    		releaseComponent.dbLookupByRelComp(xContext);
    	}
    	catch(IcofException ex) {
    		logInfo(xContext, "This Component (" + getComponent().getName() + 
    				") is not associated with this Release (" + getRelease().getName() 
    				+ ")", verboseInd);
    		return false;
    	}
        
    	return true;
    	
    }


    /**
     * Show all the results.
     * 
     * @param  xContext  Application context
     * @throws IcofException 
     */
    private void showResults(EdaContext xContext) 
    throws IcofException {

        // Lookup all the Users and Roles for this RelComponent object.
        CompTkRelRole_User_Db roleTable = 
            new CompTkRelRole_User_Db(getReleaseComp(), null, null);
        Hashtable<String,TkUserRelCompRole> myRoles = roleTable.dbLookupByRelComp(xContext);
        
        // Display the roles and user.
        Iterator<TkUserRelCompRole> iter = myRoles.values().iterator();
        while (iter.hasNext()) {
            TkUserRelCompRole role =  iter.next();
            System.out.println(role.getRole().getName() + " : " + role.getUser().getIntranetId());
        }
        
    }


	protected String readParams(Hashtable<String,String> params, String errors,
			EdaContext xContext) throws IcofException {
		// Read the release name
        if (params.containsKey("-r")) {
            setRelease(xContext, params.get("-r"));
        }
        else {
            errors += "TkRelease (-r) is a required parameter\n";
        }
        
        // Read the component name
        if (params.containsKey("-c")) {
            setComponent(xContext, params.get("-c"));
        }
        else {
            errors += "Componennt (-c) is a required parameter\n";
        }
		return errors;
	}


	protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {
		singleSwitches.add("-y");
        singleSwitches.add("-h");
        argSwitches.add("-db");
        argSwitches.add("-r");
        argSwitches.add("-c");
	}


	protected void displayParameters(String dbMode, EdaContext xContext) {
		boolean verboseInd = getVerboseInd(xContext);
        logInfo(xContext, "App      : " + APP_NAME + "  " + APP_VERSION, verboseInd);
        if (getRelease() != null) {
            logInfo(xContext, "Release  : " + getRelease().getName(), verboseInd);
        }
        else {
            logInfo(xContext, "Release  : null", verboseInd);
        }
        if (getComponent() != null) {
            logInfo(xContext, "Component: " + getComponent().getName(), verboseInd);
        }
        else {
            logInfo(xContext, "Component: null", verboseInd);
        }
        logInfo(xContext, "DB Mode  : " + dbMode, verboseInd);
        logInfo(xContext, "Verbose  : " + getVerboseInd(xContext), verboseInd);
	}

    
    /**
     * Display this application's usage and invocation
     */
    protected void showUsage() {

        StringBuffer usage = new StringBuffer();
        usage.append("------------------------------------------------------\n");
        usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
        usage.append("------------------------------------------------------\n");
        usage.append("Displays data for a specific TK Component.\n");
        usage.append("\n");
        usage.append("USAGE:\n");
        usage.append("------\n");
        usage.append(APP_NAME + " <-r release> <-c component> [-y] [-h] [-db dbMode]\n");
        usage.append("\n");
        usage.append("  release   = TK release and version (14.1 or 1401).\n");
        usage.append("  component = TK component name (einstimer, edautls, ...)\n");
        usage.append("  -h        = Help (shows this information)\n");
        usage.append("  -y        = (optional) Verbose mode (echo messages to screen)\n");
        usage.append("  dbMode    = (optional) DEV | PROD (defaults to PROD)\n");
        usage.append("\n");
        usage.append("Return Codes\n");
        usage.append("------------\n");
        usage.append(" 0 = ok\n");
        usage.append(" 1 = error\n");
        usage.append(" 2 = no data found for query\n");
        usage.append("\n");

        System.out.println(usage);

    }
    
    
    /**
     * Members.
     */
    private Release_Db release;
    private Component_Release_Db releaseComponent;

    
    /**
     * Getters.
     */
    public Release_Db getRelease() { return release; }
    public Component_Release_Db getReleaseComp() { return releaseComponent; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}

    
    /**
     * Setters.
     */
    private void setRelease(Release_Db aRel) { release = aRel;  }

    
    /**
     * Set the TkRelease object from the release name
     * @param xContext   Application context.
     * @param aReleaesName  TK release version name like 14.1 or 1401
     * @throws IcofException 
     */
    private void setRelease(EdaContext xContext, String aReleaseName) 
    throws IcofException { 
    	try {
    		if (getRelease() == null) {
    			release = new Release_Db(xContext, aReleaseName.trim());
    			release.dbLookupByName(xContext, false);
    		}    
    		logInfo(xContext, "Release: " + getRelease().toString(xContext), false);
    	}
    	catch(IcofException ex) {
    		logInfo(xContext, "Tk Release (" + getRelease().getName() + 
    				") was not found in the database.", true);
    		throw ex;
    	}

    }


	@Override
	protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
		return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
	}

}
