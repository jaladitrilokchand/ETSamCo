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
* Update a User. 
*-----------------------------------------------------------------------------
*
*-CHANGE LOG------------------------------------------------------------------
* 03/30/2011 GFS  Initial coding.
* 06/08/2011 GFS  Disabled logging.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.app.etree;

import java.util.Hashtable;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.User_Db;
import com.ibm.stg.iipmds.common.IcofException;

public class UserUpdate extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "userUpdate";
    public static final String APP_VERSION = "v1.0";

        
    /**
     * Constructor
     *
     * @param     aContext       Application context
     * @param     aUser          A User object with to update
     * @param     aNewAfsId      New AFS id
     * @param     aNewIntranetId New Intranet id
     */
    public UserUpdate(EdaContext aContext, 
    		          User_Db aUser, String aNewAfsId, String aNewIntranetId)	
    throws IcofException {

        super(aContext, APP_NAME, APP_VERSION);
        setUser(aUser);
        setAfsId(aNewAfsId);
        setIntranetId(aNewIntranetId);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public UserUpdate(EdaContext aContext) throws IcofException {

        this(aContext, null, null, null);

    }
    
    
    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     *
     * @param     argv[]            the command line arguments
     */
    public static void main(String argv[]) {

		TkAppBase myApp = null;
		EdaContext aContext = null;

		try {

			myApp = new UserUpdate(null);

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

        // Update the User
        updateUser(xContext);
        
        // Set the return code to success if we get this far.
        setReturnCode(xContext, SUCCESS);
        commitToDB(xContext, APP_NAME);
    }

    
	/**
     * Update the specified User object.
     * @param xContext       Application context
     * @throws IcofException Trouble updating the ChangeRequest
     */
    public void updateUser(EdaContext xContext) throws IcofException {

        logInfo(xContext, "Updating User ...", verboseInd);
 
        // Set the AFS id
        String myAfsId = getEditor().getAfsId();
        if (getAfsId() != null) {
        	myAfsId = getAfsId();
        	logInfo(xContext, " Using new AFS id ...", verboseInd);
        }
        
        // Set the Intranet id
        String myIntranetId = getEditor().getIntranetId();
        if (getIntranetId() != null) {
        	myIntranetId = getIntranetId();
        	logInfo(xContext, " Using new Intranet id ...", verboseInd);
        }
        
        // Construct the new ChangeRequest using the old data and update the row
        User_Db newUser = new User_Db(getEditor().getId(), 
        							  getEditor().getFirstName(), 
        							  getEditor().getLastName(), 
        							  getEditor().getEmailAddress(), 
        		                      myAfsId, 
        		                      myIntranetId);
        newUser.dbUpdateRow(xContext, getUser());
        
        logInfo(xContext, "Update complete", true);
        
    }


	protected String readParams(Hashtable<String,String>  params, String errors,
			EdaContext xContext) throws IcofException {
		// Read the User name
        if (params.containsKey("-u")) {
        	setEditor(xContext,params.get("-u"));
        }
        else {
            errors += "User (-u) is a required parameter\n";
        }

        // Read the AFS id
        if (params.containsKey("-a")) {
            setAfsId(params.get("-a"));
        }

        // Read the Intranet id
        if (params.containsKey("-i")) {
            setIntranetId( params.get("-i"));
        }
        
        // Verify Intranet or AFS ids are set
        if ((getIntranetId() == null) && (getAfsId() == null)) {
        	errors += "Either an AFS (-a) or Intranet (-i) id must be specified\n";
        }
		return errors;
	}


	protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {
		singleSwitches.add("-y");
        singleSwitches.add("-h");
        singleSwitches.add("-n");
        argSwitches.add("-db");
        argSwitches.add("-a");
        argSwitches.add("-u");
        argSwitches.add("-i");
	}


	protected void displayParameters(String dbMode, EdaContext xContext) {
		boolean verboseInd = getVerboseInd(xContext);
        logInfo(xContext, "App             : " + APP_NAME + "  " + APP_VERSION, verboseInd);
        logInfo(xContext, "User            : " + getEditor().getIntranetId(), verboseInd);
        if (getAfsId() != null) {
        	logInfo(xContext, "New AFS id      : " + getAfsId(), verboseInd);
        }
        else {
        	logInfo(xContext, "New AFS id      : NULL", verboseInd);
        }
        if (getIntranetId() != null) {
        	logInfo(xContext, "New Intranet id : " + getIntranetId(), verboseInd);
        }
        else {
        	logInfo(xContext, "New Intranet id : NULL", verboseInd);
        }
        logInfo(xContext, "DB Mode         : " + dbMode, verboseInd);
        logInfo(xContext, "Verbose         : " + getVerboseInd(xContext), verboseInd);
	}

    
    /**
     * Display this application's usage and invocation
     */
    protected void showUsage() {

        StringBuffer usage = new StringBuffer();
        usage.append("------------------------------------------------------\n");
        usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
        usage.append("------------------------------------------------------\n");
        usage.append("Updates a Users AFS and/or Intranet id.\n");
        usage.append("\n");
        usage.append("USAGE:\n");
        usage.append("------\n");
        usage.append(APP_NAME + " <-u user> < -a afs_id | -i intranet_id> \n");
        usage.append("                [-y] [-h] [-db dbMode]\n");
        usage.append("\n");
        usage.append("  user        = Intranet of user to update.\n");
        usage.append("  afs_id      = New AFS id.\n");
        usage.append("  intranet_id = New intranet id.\n");
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
    private static boolean requestHelp = false;
    
    /**
     * Getters.
     */
    public String getAfsId()  { return afsId; }
    public String getIntranetId()  { return intranetId; }
    public User_Db getEditor() { return editor; }
    public static boolean getRequestHelp() { return requestHelp; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}
        
    /**
     * Setters.
     */
    private void setAfsId(String anId) { afsId = anId; }
    private void setIntranetId(String anId) { intranetId = anId; }


	@Override
	protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
		return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
	}
	

  
    /**
     * Set the User object from the user'd id
     * @param xContext   Application context.
     * @param aUserName  AFS user id
     * @throws IcofException 
     */
    private void setEditor(EdaContext xContext, String anId) 
    throws IcofException { 
    	try {
    		if (getEditor() == null) {
    			editor = new User_Db(anId.trim(), true);
    			editor.dbLookupByIntranet(xContext);
    		}    
    		logInfo(xContext, "User: " + getEditor().toString(xContext), false);
    	}
    	catch(IcofException ex) {
    		logInfo(xContext, "User (" + anId + ") was not found in the database.", true);
    		throw ex;
    	}
    }
    
}
