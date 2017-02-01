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
* Add a branch to the DB. 
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 07/14/2011 GFS  Initial coding.
* 11/27/2012 GFS  Refactored to use business objects.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.app.etree;

import java.util.Hashtable;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.Report_base;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequest;
import com.ibm.stg.iipmds.common.IcofException;

public class ChangeRequestChanges extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "cr.changes";
    public static final String APP_VERSION = "v1.0";

        
    /**
     * Constructor
     *
     * @param     aContext       Application context
     * @param     aCr            Change request to examine
     */
    public ChangeRequestChanges(EdaContext aContext, ChangeRequest aCr)
    throws IcofException {

    	super(aContext, APP_NAME, APP_VERSION);
    	setChangeRequest(aCr);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public ChangeRequestChanges(EdaContext aContext) throws IcofException {

        this(aContext, null);

    }
    
    
    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     *
     * @param     argv[]            the command line arguments
     */
    public static void main(String argv[]) {

		TkAppBase myApp = null;
		try {

			myApp = new ChangeRequestChanges(null);
			start(myApp, argv);
		}

		catch (Exception e) {
			handleExceptionInMain(e);
		}
		finally {
			handleInFinallyBlock(myApp);
		}

    }

    
    /**
     * Add, update, delete, or report on the specified applications.
     * 
     * @param aContext      Application Context
     * @throws              IcofException
     */
    public void process(EdaContext xContext) throws IcofException {

        // Connect to the database
        connectToDB(xContext);

        // Determine if branch/component are for a production or development TK.
        runQuery(xContext);
        commitToDBAndSetReturncode(xContext, APP_NAME, SUCCESS);

    }

    
    /**
     * Query for this change requests changed files and revision info.
     * 
     * @param xContext  Application context
     * @throws IcofException  Trouble querying database.
     */
    private void runQuery(EdaContext xContext) throws IcofException {
        
    	String q = "";
    	q += "select r.tkrelease_name || varchar('.') || v.tkversion_name," + 
             " c.component_name, cu.svnrevision, f.filename " +
             "from tk.changerequest as cr," +
             " tk.code_update as cu," +
             " tk.code_update_x_changerequest as cucr," +
             " tk.filename as f," +
             " tk.file_version as fv," +
             " tk.component as c," +
             " tk.component_tkrelease as cr," +
             " tk.component_tkversion as cv," +
             " tk.component_tkversion_x_changerequest as cvcr," +
             " tk.tkrelease as r," +
             " tk.tkversion as v " +
             "where cr.clearquest_id = '" + getChangeRequest().getClearQuest() + "'" +
             " and cr.changerequest_id = cucr.changerequest_id" +
             " and cucr.code_update_id = cu.code_update_id" +
             " and cu.code_update_id = fv.code_update_id" +
             " and fv.filename_id = f.filename_id" +
             " and c.component_id = cr.component_id" +
             " and cr.component_tkrelease_id = cv.component_tkrelease_id" +
             " and cv.Component_tkversion_id = cvcr.component_tkversion_id" +
             " and cvcr.changerequest_id = cr.changerequest_id" +
             " and r.tkrelease_id = cr.tkrelease_id" +
             " and v.tkversion_id = cv.tkversion_id " +
             "order by svnrevision asc";


    	String[] headers = {"Tool Kit", "Component", "Revision", "Changed file/dir"};
    	Integer[] widths = {12, 20, 10, 30};
    	
    	Report_base report = new Report_base(q, headers, widths);
    	report.setContent(xContext);
    	
    	if (! isQuiet()) {
    		System.out.println(report.getHeader());
    	}
    	System.out.println(report.getContent().toString());
    	
	}


    /**
     * Parse command line parameters
     */
	protected String readParams(Hashtable<String,String> params, String errors,
	                            EdaContext xContext) throws IcofException {

		// Read the ToolKit name
        if (params.containsKey("-cr")) {
            setChangeRequest(xContext, (String) params.get("-cr"));
        }
        else {
            errors += "Change Request (-cr) is a required parameter\n";
        }

        setQuietMode(false);
        if (params.containsKey("-q")) {
            setQuietMode(true);
        }
        
        
		return errors;
	}


	/**
	 * Define command line switches
	 */
	protected void createSwitches(Vector<String> singleSwitches, 
	                              Vector<String> argSwitches) {
		singleSwitches.add("-y");
        singleSwitches.add("-h");
        singleSwitches.add("-q");
        argSwitches.add("-db");
        argSwitches.add("-cr");
	}


	/**
	 * Show application inputs
	 */
	protected void displayParameters(String dbMode, EdaContext xContext) {
		logInfo(xContext, "App        : " + APP_NAME + "  " + APP_VERSION, verboseInd);
		logInfo(xContext, "ChangeReq  : " + getChangeRequest().getClearQuest(), verboseInd);
		logInfo(xContext, "Quiet mode : " + isQuiet(), verboseInd);
		logInfo(xContext, "DB mode    : " + dbMode, verboseInd);
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
        usage.append("Displays changed files and revisions committed against\n");
        usage.append("the specified change request.\n");
        usage.append("\n");
        usage.append("USAGE:\n");
        usage.append("------\n");
        usage.append(APP_NAME + " <-cr changeReq> [-q] [-y] [-h] [db dbMode]\n");
        usage.append("\n");
        usage.append("  changeReq   = Change request number (MDCMS00123456 ...).\n");
        usage.append("  -q          = (optional) Don't display column headings\n");
        usage.append("  -y          = (optional) Verbose mode (echo messages to screen)\n");
        usage.append("  dbMode      = (optional) DEV | PROD (defaults to PROD)\n");
        usage.append("  -h          = Help (shows this information)\n");
        usage.append("\n");
        usage.append("Return Codes\n");
        usage.append("------------\n");
        usage.append(" 0 = application ran ok\n");
        usage.append(" 1 = application error\n");
        usage.append("\n");

        System.out.println(usage);

    }

    /**
     * Members
     */
    private boolean quietMode = false;
    
    
    /**
     *  Getters
     */
    public boolean isQuiet()  { return quietMode; }
    protected String getAppName() { return APP_NAME; }
    protected String getAppVersion() { return APP_VERSION; }
 
    
    /**
     * Setters
     */
    private void setQuietMode(boolean aFlag) { quietMode = aFlag; }
    
    
	@Override
	protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
		return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
	}
    
}
