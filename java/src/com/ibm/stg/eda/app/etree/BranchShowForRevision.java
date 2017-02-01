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
 * Show the branch for the tk, comp and revision
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 10/22/2015 GFS  Initial coding.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree;

import java.util.Hashtable;
import java.util.Vector;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.CodeUpdate_Db;
import com.ibm.stg.eda.component.tk_etreedb.Component_Version_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.BranchName;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofException;

public class BranchShowForRevision extends TkAppBase {

    /**
     * Constants.
     */
    public static final String APP_NAME = "branch4rev";
    public static final String APP_VERSION = "v1.0";


    /**
     * Constructor
     * 
     * @param aContext Application context
     * @param aTk  Tool Kit to query branch for
     * @param aComp Component to query branch for
     * @param aRevision Revision to query branch for
     */
    public BranchShowForRevision(EdaContext aContext, ToolKit aTk,
                                 Component aComp, String aRevision) 
                                 throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);

	setToolKit(aTk);
	setComponent(aComp);
	setRevision(aRevision);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param aContext the application context
     * 
     * @exception IcofException Unable to construct ManageApplications object
     */
    public BranchShowForRevision(EdaContext aContext) throws IcofException {

	this(aContext, null, null, null);

    }


    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     * 
     * @param argv[] the command line arguments
     */
    public static void main(String argv[]) {

	TkAppBase myApp = null;
	try {
	    myApp = new BranchShowForRevision(null);
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
     * @param aContext Application Context
     * @throws IcofException
     */
    public void process(EdaContext xContext)
    throws IcofException {

	connectToDB(xContext);

	setBranch(xContext);
	showBranch(xContext);

	rollBackDBAndSetReturncode(xContext, APP_NAME, SUCCESS);

    }


    /**
     * Determines the branch for this tk, comp and revision
     * 
     * @param xContext Application context
     * @throws IcofException Trouble querying database.
     */
    public void setBranch(EdaContext xContext)
    throws IcofException {

	Component_Version_Db compVersion;
	compVersion = new Component_Version_Db(xContext, getToolKit().getToolKit(), 
	                                       getComponent().getComponent());
	compVersion.dbLookupByCompRelVersion(xContext);


	CodeUpdate_Db cu = new CodeUpdate_Db(compVersion, getRevision(), "", "", 
	                                     null, null, null, null, null, null);
	cu.dbLookupByCompRev(xContext, compVersion, getRevision());

	branch = new BranchName(xContext, cu.getBranch());
	branch.dbLookupByName(xContext);

    }


    /**
     * Displays the result.
     * 
     * @param xContext Application context
     * @param isProd Production flag
     * @param showResult If true print result to stdout otherwise don't
     */
    private void showBranch(EdaContext xContext) {

	System.out.println(getBranchName().getName());

    }


    protected void createSwitches(Vector<String> singleSwitches,
                                  Vector<String> argSwitches) {

	singleSwitches.add("-y");
	singleSwitches.add("-h");
	argSwitches.add("-db");
	argSwitches.add("-t");
	argSwitches.add("-c");
	argSwitches.add("-r");

    }


    protected String readParams(Hashtable<String, String> params,
                                String errors, EdaContext xContext)
                                throws IcofException {

	// Read the Tool Kit
	if (params.containsKey("-t"))
	    setToolKit(xContext, (String) params.get("-t"));
	else
	    errors += "Tool Kit (-t) is a required parameter\n";

	// Read the Component name
	if (params.containsKey("-c"))
	    setComponent(xContext, (String) params.get("-c"));
	else
	    errors += "Component (-c) is a required parameter\n";

	// Read the Revision
	if (params.containsKey("-r"))
	    setRevision((String) params.get("-r"));
	else
	    errors += "Revision (-r) is a required parameter\n";

	return errors;

    }


    protected void displayParameters(String dbMode, EdaContext xContext) {

	boolean verboseInd = getVerboseInd(xContext);
	logInfo(xContext, "App        : " + APP_NAME + "  " + APP_VERSION,
	        verboseInd);
	logInfo(xContext, "Tool Kit   : " + getToolKit().getName(),
	        verboseInd);
	logInfo(xContext, "Component  : " + getComponent().getName(),
	        verboseInd);
	logInfo(xContext, "Revision   : " + getRevision(), verboseInd);
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
	usage.append("Queries the DB for the branch assocaited with the given\n");
	usage.append("Tool Kit, Componand and SVN revision.\n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " <-t toolkit> <-c component> <-r revision>\n");
	usage.append("            [-y] [-h] [-db dbMode]\n");
	usage.append("\n");
	usage.append(" toolkit   = Tool Kit name (14.1.10, 15.1.0 ...).\n");
	usage.append(" component = Component name (ess, pds, model, einstimer ...).\n");
	usage.append(" revision  = SVN revision \n");
	usage.append(" -y        = (optional) Verbose mode (echo messages to screen)\n");
	usage.append(" dbMode    = (optional) DEV | PROD (defaults to PROD)\n");
	usage.append(" -h        = Help (shows this information)\n");
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
     * @formatter:off
     */
    private String revision;
    private BranchName branch;
    

    /**
     * Getters.
     */
    public String getRevision()  { return revision; }
    public BranchName getBranchName()  { return branch; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}


    /**
     * Setters.
     */
    private void setRevision(String aName) { revision = aName;  }


    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
	return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
    }

    // @formatter:off

}
