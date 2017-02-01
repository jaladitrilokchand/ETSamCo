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
* Add a new User. 
*-----------------------------------------------------------------------------
*
*-CHANGE LOG------------------------------------------------------------------
* 03/31/2011 GFS  Initial coding.
* 06/08/2011 GFS  Disabled logging.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.app.etree;

import java.util.Hashtable;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkConstants;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.User_Db;
import com.ibm.stg.iipmds.common.Constants;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofStringUtil;

public class UserAdd extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "userAdd";
    public static final String APP_VERSION = "v1.0";

        
    /**
     * Constructor
     *
     * @param     aContext     Application context
     * @param     anAfsId      New AFS id
     * @param     anIntranetId New Intranet id
     */
    public UserAdd(EdaContext aContext,String anAfsId, String anIntranetId)	
    throws IcofException {

        super(aContext, APP_NAME, APP_VERSION);
        setAfsId(anAfsId);
        setIntranetId(anIntranetId);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public UserAdd(EdaContext aContext) throws IcofException {

        this(aContext, null, null);

    }
    
    
    /**
     * MAIN
     *
     * @param     argv[]            the command line arguments
     */
    public static void main(String argv[]) {


		TkAppBase myApp = null;
		EdaContext aContext = null;

		try {

			myApp = new UserAdd(null);
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

        // Add the User
        addUser(xContext);
        
        // Set the return code to success if we get this far.
        setReturnCode(xContext, SUCCESS);
        commitToDB(xContext, APP_NAME);
        
    }

    
	/**
     * Update the specified User object.
     * @param xContext       Application context
     * @throws IcofException Trouble updating the ChangeRequest
     */
    public void addUser(EdaContext xContext) throws IcofException {

        logInfo(xContext, "Adding new User ...", verboseInd);
        
        // If new user does not exist in add add them
        User_Db newUser = new User_Db(xContext, getAfsId(),  getIntranetId());
        try {
        	newUser.dbLookupByIntranet(xContext);
        	logInfo(xContext, "User already exists in database", true);
        }
        catch(IcofException trap) {
        	newUser.dbAddRow(xContext, getEditor());
        	logInfo(xContext, "User add to database", true);
        }
        
    }


    /**
     * Read command line arguments and verify application invocation
     * 
     * @param argv
     *        [] the command line arguments
     * 
     * @return the application context
     * @exception IcofException
     *            unable to process arguments
     */
    protected EdaContext initializeApp(String argv[]) throws IcofException {

        // Get the userid
        String userid = System.getProperty(Constants.USER_NAME_PROPERTY_TAG);

        // Parse the command line args
        Hashtable<String,String>  params = new Hashtable<String,String> ();
        Vector<String>  singleSwitches = new Vector<String> ();
        Vector<String> argSwitches = new Vector<String> ();
        createSwitches(singleSwitches, argSwitches);
        
        // Parse the command line parameters.
        String errors = "";
        String invocationMsg = "";
        String dbMode = TkConstants.EDA_PROD;
        
        EdaContext xContext =  parseAndConnectToDb(argv, params, singleSwitches, 
                                                   argSwitches, userid, 
                                                   errors, invocationMsg,
                                                   dbMode, APP_NAME);
        if(null ==xContext){
        	return null;
        }
        
        // Set the user object.
        setEditor(xContext, userid);
        
        errors = readParams(params, errors, xContext);
        logAndDisplay(params,errors, invocationMsg, dbMode, xContext, APP_NAME, APP_VERSION);
        return xContext;

    }


	protected String readParams(Hashtable<String,String>  params, String errors,EdaContext xContext ) throws IcofException{
		// Read the AFS id
        if (params.containsKey("-a")) {
            setAfsId( params.get("-a"));
        }
        else {
            errors += "AFS id (-a) is a required parameter\n";
        }
        if (getAfsId().indexOf("@") > 0) {
        	errors += "AFS id contains and @ symbol which is probably incorrect\n";
        }
        
        // Read the Intranet id
        if (params.containsKey("-i")) {
            setIntranetId(params.get("-i"));
        }
        else {
            errors += "Intranet id (-i) is a required parameter\n";
        }
        if (IcofStringUtil.occurrencesOf(getIntranetId(), "@") != 1) {
        	errors += "Intranet id does not contain a single @ symbol which is probably incorrect\n";
        }
		return errors;
	}


	protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {
		singleSwitches.add("-y");
        singleSwitches.add("-h");
        singleSwitches.add("-n");
        argSwitches.add("-db");
        argSwitches.add("-a");
        argSwitches.add("-i");
	}


	protected void displayParameters(String dbMode, EdaContext xContext) {
		boolean verboseInd = getVerboseInd(xContext);
        logInfo(xContext, "App         : " + APP_NAME + "  " + APP_VERSION, verboseInd);
        logInfo(xContext, "AFS id      : " + getAfsId(), verboseInd);
        logInfo(xContext, "Intranet id : " + getIntranetId(), verboseInd);
        logInfo(xContext, "DB Mode     : " + dbMode, verboseInd);
        logInfo(xContext, "Verbose     : " + getVerboseInd(xContext), verboseInd);
	}

    
    /**
     * Display this application's usage and invocation
     */
    protected void showUsage() {

        StringBuffer usage = new StringBuffer();
        usage.append("------------------------------------------------------\n");
        usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
        usage.append("------------------------------------------------------\n");
        usage.append("Adds a new Users to the ETREE database.\n");
        usage.append("\n");
        usage.append("USAGE:\n");
        usage.append("------\n");
        usage.append(APP_NAME + " <-a afs_id> <-i intranet_id> [-y] [-h] [-db dbMode]\n");
        usage.append("\n");
        usage.append("  afs_id      = AFS id of new user.\n");
        usage.append("  intranet_id = Intranet id of new user.\n");
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
    private String afsId;
    private String intranetId;
    private User_Db editor;

    
    /**
     * Getters.
     */
    public String getAfsId()  { return afsId; }
    public String getIntranetId()  { return intranetId; }
    public User_Db getEditor() { return editor; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}

        
    /**
     * Setters.
     */
    private void setAfsId(String anId) { afsId = anId; }
    private void setIntranetId(String anId) { intranetId = anId; }

    
    /**
     * Set the User object from the user'd id
     * @param xContext   Application context.
     * @param aUserName  AFS user id
     * @throws IcofException 
     */
    private void setEditor(EdaContext xContext, String aUserid) 
    throws IcofException { 
        if (getEditor() == null) {
            editor = new User_Db(aUserid, false);
            try {
                editor.dbLookupByAfs(xContext);
            }
            catch(IcofException ie) {
                // Not found so populate from AFS and add to DB
                editor.populateFromAfs(xContext);
                editor.dbAddRow(xContext);
            }
        }    
        logInfo(xContext, "Editor " + getEditor().toString(xContext), false);
    }


	@Override
	protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
		return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
	}
    
}
