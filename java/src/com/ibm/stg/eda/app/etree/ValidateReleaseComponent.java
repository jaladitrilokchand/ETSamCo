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
* Validate an EDA TK release and component. 
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 05/17/2010 GFS  Initial coding.
* 06/08/2011 GFS  Disabled logging.
* 12/22/2011 GFS  Updated to make the Component be case insensitive.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.app.etree;

import java.util.Hashtable;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.Component_Db;
import com.ibm.stg.eda.component.tk_etreedb.Component_Release_Db;
import com.ibm.stg.eda.component.tk_etreedb.Release_Db;
import com.ibm.stg.iipmds.common.IcofException;

public class ValidateReleaseComponent extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "validateRelComp";
    public static final String APP_VERSION = "v1.1";
    public static final int BAD_RELEASE_RC = 1;
    public static final int BAD_COMPONENT_RC = 2;
    public static final int BAD_RELEASE_AND_COMPONENT_RC = 3;

    
    /**
     * Constructor
     *
     * @param     aContext       the application context
     * @param     aReleaseName   the TK releaes name
     * @param     aComponentName the TK component name
     *
     */
    public ValidateReleaseComponent(EdaContext aContext,
                                    String aReleaseName,
                                    String aComponentName) throws IcofException {

        super(aContext, APP_NAME, APP_VERSION);

        setReleaseName(aReleaseName);
        setComponentName(aComponentName);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public ValidateReleaseComponent(EdaContext aContext) throws IcofException {

        this(aContext, "", "");

    }

    
    
    /**
	 * Instantiate the ValidateReleaesComponent class and process the arguments.
	 * 
	 * @param argv
	 *            [] the command line arguments
	 */
	public static void main(String argv[]) {

		TkAppBase myApp = null;
		EdaContext aContext = null;
		try {

			myApp = new ValidateReleaseComponent(null);

			// Read and verify input parameters and get a database connection.
			start(myApp, aContext, argv);
		}

		catch (Exception e) {

			handleExceptionInMain(e, aContext);
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
        
        // Look up the TkRelelase
        boolean foundRelease = true;
        Release_Db release = new Release_Db(xContext, getReleaseName());
        try {
            release.dbLookupByName(xContext, false);
        }
        catch(IcofException ie) {
            foundRelease = false;
        }
        
        // Look up the Component
        boolean foundComponent = true;
        Component_Db component = new Component_Db(getComponentName());
        try {
            component.dbLookupByName(xContext);
        }
        catch(IcofException ie) {
            foundComponent = false;
        }
        
        // Look up the Component for this release
        boolean foundRelComponent = true;
        if (foundComponent && foundRelease) {

            Component_Release_Db relComp = new Component_Release_Db(release, component);
            try {
                relComp.dbLookupByRelComp(xContext);
            }
            catch(IcofException ie) {
                foundRelComponent = false;
            }
            
        }
        else {
            if (! foundRelease && ! foundComponent)
                foundRelComponent = false;
        }
        
        // Set the return code.
        if (! foundRelComponent) {
            setReturnCode(xContext, BAD_RELEASE_AND_COMPONENT_RC);
        }
        else if (! foundComponent) {
            setReturnCode(xContext, BAD_COMPONENT_RC);
        }
        else if (! foundRelease) {
            setReturnCode(xContext, BAD_RELEASE_RC);
        }
        else {
            setReturnCode(xContext, SUCCESS);
        }
        
        rollBackDB(xContext, APP_NAME);
        
    }


	protected String readParams(Hashtable<String,String>  params, String errors,EdaContext xContext)  throws IcofException{
		// Read the release name
        if (params.containsKey("-r")) {
            setReleaseName( params.get("-r"));
        }
        else {
            errors += "Release name (-r) is a required parameter.";
        }

        // Read the component name
        if (params.containsKey("-c")) {
            setComponentName(params.get("-c"));
        }
        else {
            errors += "Component name (-c) is a required parameter.";
        }

        // Read the case sensitivity
        setCaseSensitive(true);
        if (params.containsKey("-i")) {
            setCaseSensitive(false);
        }
		return errors;
	}


	protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {
		singleSwitches.add("-i");
        singleSwitches.add("-y");
        singleSwitches.add("-h");
        argSwitches.add("-db");
        argSwitches.add("-r");
        argSwitches.add("-c");
	}


	protected void displayParameters(String dbMode, EdaContext xContext) {
		boolean verboseInd = getVerboseInd(xContext);
        logInfo(xContext, "App      : " + APP_NAME + "  " + APP_VERSION, verboseInd);
        logInfo(xContext, "Release  : " + getReleaseName(), verboseInd);
        logInfo(xContext, "Component: " + getComponentName(), verboseInd);
        logInfo(xContext, "Case sensitive: " + isCaseSensitive(), verboseInd);
        logInfo(xContext, "Mode     : " + dbMode, verboseInd);
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
        usage.append("Validates the specified TK release and component.\n");
        usage.append("\n");
        usage.append("USAGE:\n");
        usage.append("------\n");
        usage.append(APP_NAME + " <-r release> <-c component> [-i] [-y] [-h] [-db dbMode]\n");
        usage.append("\n");
        usage.append("  release   = TK release and version (14.1 or 1401).\n");
        usage.append("  component = TK component name (einstimer, edautls, ...)\n");
        usage.append("  -i        = Ignore case component\n");
        usage.append("  -y        = (optional) Verbose mode (echo messages to screen)\n");
        usage.append("  -h        = Help (shows this information)\n");
        usage.append("  dbMode    = (optional) DEV | PROD (defaults to PROD)\n");
        usage.append("\n");
        usage.append("Return Codes\n");
        usage.append("------------\n");
        usage.append(" 0 = release and component OK\n");
        usage.append(" 1 = error (release not found)\n");
        usage.append(" 2 = error (component not found)\n");
        usage.append(" 3 = error (release and component not found)\n");
        usage.append("\n\n");
        usage.append("------------------------------------------------------\n");

        System.out.println(usage);

    }
    
    
    /**
     * Members.
     */
    private String releaseName;
    private String componentName;
    private boolean caseSensitive;

    
    
    /**
     * Getters.
     */
    public String getReleaseName() { return releaseName; }
    public String getComponentName() { return componentName; }
    public boolean isCaseSensitive() { return caseSensitive; }
    public static boolean getRequestHelp() { return requestHelp; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}

    
    /**
     * Setters.
     */
    private void setReleaseName(String aName) { releaseName = aName;  }
    private void setComponentName(String aName) { componentName = aName;  }
    private void setCaseSensitive(boolean aFlag) { caseSensitive = aFlag;  }


	@Override
	protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
		return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
	}

    
}

