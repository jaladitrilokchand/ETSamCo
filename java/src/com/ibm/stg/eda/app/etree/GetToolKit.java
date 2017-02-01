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
* Show the TK Release Version in the specified state. 
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 01/17/2011 GFS  Initial coding.
* 06/09/2011 GFS  Disabled logging.
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
import com.ibm.stg.eda.component.tk_etreedb.RelVersion_Db;
import com.ibm.stg.eda.component.tk_etreedb.Release_Db;
import com.ibm.stg.eda.component.tk_etreedb.StageName_Db;
import com.ibm.stg.iipmds.common.IcofException;

public class GetToolKit extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "getToolKit";
    public static final String APP_VERSION = "v1.0";


    /**
     * Constructor
     *
     * @param     aContext    Application context
     * @param     aRelease    TK RelVersion object
     * @param     aComponent  TK component object
     *
     */
    public GetToolKit(EdaContext aContext, Release_Db aRelease, 
    		          StageName_Db aStageName)
    throws IcofException {

        super(aContext, APP_NAME, APP_VERSION);
        
        setRelease(aRelease);
        setStageName(aStageName);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public GetToolKit(EdaContext aContext) throws IcofException {

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

			myApp = new GetToolKit(null);
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

        // Look up the TK Release and Version
        setToolKits(xContext);
        showResults(xContext);
        
        // Set the return code.
        setReturnCode(xContext, SUCCESS);
        commitToDB(xContext, APP_NAME);
        
    }

    
    /**
     * Show all the valid release, version and component combinations.
     * 
     * @param  xContext  Application context
     * @throws IcofException 
     */
    private void showResults(EdaContext xContext) 
    throws IcofException {
    	
    	// Iterate through the tool kit collection
    	String results = "";

    	Iterator<RelVersion_Db> iter = getToolKits().iterator();
    	while (iter.hasNext()) {
    		RelVersion_Db version = (RelVersion_Db) iter.next();
    		version.getRelease().dbLookupById(xContext);
    		if (isQuietMode()) {
	    		if (results.length() < 1)
	    			results = getName(xContext, version);
	    		else 
	    			results += "," + getName(xContext, version);
    		}
    		else {
    			version.getStageName().dbLookupById(xContext);
	    		if (results.length() < 1)
	    			results = version.toString(xContext);
	    		else 
	    			results += "\n" + version.toString(xContext);
    		}
    	}
    	
        // If no versions found.
        if (getToolKits().size() < 1) {
        	System.out.println("No TK Versions found for " + getStageName().getName());
        }
        else {
        	System.out.println(results);
        }
        
    }

    /**
     * Return the desired TK name
     * @param xContext Application context
     * @param version  TK object
     * @return
     */
    private String getName(EdaContext xContext, RelVersion_Db version) {
    	if (getUseAltName() == true) {
    		return version.getAltDisplayName();
    	}
    	
		return version.getDisplayName();
		
	}


	/**
     * Show all the valid release, version and component combinations.
     * 
     * @param  xContext  Application context
     * @throws IcofException 
     */
    public void setToolKits(EdaContext xContext) throws IcofException {

    	// Lookup the TK Versions for this stage name.
    	RelVersion_Db thisVersion = new RelVersion_Db(getRelease(), "");
    	toolKits = thisVersion.dbLookupByStageName(xContext, getStageName());

    }


	protected String readParams(Hashtable<String,String> params, String errors,
			EdaContext xContext) throws IcofException {
		// Read the stage name
        if (params.containsKey("-s")) {
            setStageName(xContext,  params.get("-s"));
        }
        else {
            errors += "StageName (-s) is a required parameter\n";
        }

        // Read the release name
        if (params.containsKey("-r")) {
            setRelease(xContext,  params.get("-r"));
        }
        else {
            errors += "Release (-r) is a required parameter\n";
        }
        
        // Set quiet option
        setQuietMode(false);
        if (params.containsKey("-q")) {
            setQuietMode(true);
        }

        // Set alt name option
        setUseAltName(false);
        if (params.containsKey("-alt")) {
            setUseAltName(true);
        }

        return errors;
        
	}


	protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {
		singleSwitches.add("-y");
        singleSwitches.add("-h");
        singleSwitches.add("-q");
        singleSwitches.add("-alt");
        argSwitches.add("-db");
        argSwitches.add("-s");
        argSwitches.add("-r");
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

        if (getStageName() != null) {
            logInfo(xContext, "StageName: " + getStageName().getName(), verboseInd);
        }
        else {
            logInfo(xContext, "StageName: null", verboseInd);
        }
        logInfo(xContext, "DB Mode  : " + dbMode, verboseInd);
        logInfo(xContext, "Quiet    : " + isQuietMode(), verboseInd);
        logInfo(xContext, "Alt name : " + getUseAltName(), verboseInd);
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
        usage.append("Displays Tool Kits for the specified stage name.\n");
        usage.append("\n");
        usage.append("USAGE:\n");
        usage.append("------\n");
        usage.append(APP_NAME + " <-r release> <-s stageName> [-alt]\n");
        usage.append("               [-q] [-y] [-h] [-db dbMode]\n");
        usage.append("\n");
        usage.append("  release   = TK Release name (like 14.1, 1401, 15.1 ...)\n");
        usage.append("  stageName = StageName (ready, development, ship, tk, xtinct/tk14.1.2)\n");
        usage.append("  -alt      = Return alternate (server) tool kit name\n");
        usage.append("  -q        = (optional) Display comma delimited list of components\n");
        usage.append("  -y        = (optional) Verbose mode (echo messages to screen)\n");
        usage.append("  -h        = Help (shows this information)\n");
        usage.append("  dbMode    = (optional) DEV | PROD (defaults to PROD)\n");
        usage.append("\n");

        System.out.println(usage);

    }
    
    
    /**
     * Members.
     */
    private StageName_Db stageName;
    private Release_Db release;
    private Vector<RelVersion_Db> toolKits = null;
    private boolean quietMode = false;
    private boolean useAltName = false;
    
    /**
     * Getters.
     */
    public StageName_Db getStageName() { return stageName; }
    public Release_Db getRelease() { return release; }
    public Vector<RelVersion_Db> getToolKits() { return toolKits; }
    public static boolean getRequestHelp() { return requestHelp; }
    public boolean isQuietMode() { return quietMode; }
    public boolean getUseAltName() { return useAltName; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}
    
    /**
     * Setters.
     */

    private void setStageName(StageName_Db aName) { stageName = aName;  }
    private void setRelease(Release_Db aRel) { release = aRel;  }
    private void setQuietMode(boolean aFlag) { quietMode = aFlag;  }
    private void setUseAltName(boolean aFlag) { useAltName = aFlag;  }
    
    /**
     * Set the StageName_db object from the stageName name
     * @param xContext    Application context.
     * @param aStageName  StageName like ready, development, ship, tk, xtinct
     * @throws IcofException 
     */
    private void setStageName(EdaContext xContext, String aStageName) 
    throws IcofException { 
        if (getStageName() == null) {
            stageName = new StageName_Db(aStageName.trim().toUpperCase());
            stageName.dbLookupByName(xContext);
        }    
        logInfo(xContext, "StageName: " + getStageName().toString(xContext), false);
    }

    /**
     * Set the Release_db object from the release name
     * @param xContext      Application context.
     * @param aReleaseName  StageName like 14.1, 1401. 15.1 ...
     * @throws IcofException 
     */
    private void setRelease(EdaContext xContext, String aRelease) 
    throws IcofException { 
        if (getRelease() == null) {
            release = new Release_Db(xContext, aRelease.trim());
            release.dbLookupByName(xContext, false);
        }    
        logInfo(xContext, "Release: " + getRelease().toString(xContext), false);
    }


	@Override
	protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
		return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
	}

    
}
