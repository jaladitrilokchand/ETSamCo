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
* Update a ChangeRequest's description and component association. 
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 03/08/2011 GFS  Initial coding.
* 05/19/2011 GFS  Update to allow the component to be changed if there are
*                 no code updates for the ChangeRequest.
* 07/21/2011 GFS  Updated to de-activate this ChangeRequest if the current status
*                 is APPROVED and the status is changing.
* 08/08/2011 GFS  Removed ability to update the state. Added -f switch so 
*                 description can be specified via a file.
* 09/06/2011 GFS  Updated to support new ChangeRequest defect/feature and severity.
* 09/09/2011 GFS  Updated so help is shown if no parm specified.
* 09/16/2011 GFS  Change -cq switch to -cr.
* 11/30/2012 GFS  Updated to support CR's new impacted customer attribute.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.app.etree;

import java.util.Hashtable;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.ChangeRequestType_Db;
import com.ibm.stg.eda.component.tk_etreedb.CodeUpdate_Db;
import com.ibm.stg.eda.component.tk_etreedb.CompVersion_ChangeRequest_Db;
import com.ibm.stg.eda.component.tk_etreedb.Component_Version_Db;
import com.ibm.stg.eda.component.tk_etreedb.User_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequest;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequestSeverity;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequestType;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofCollectionsUtil;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofFile;

public class ChangeRequestUpdate extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "cr.update";
    public static final String APP_VERSION = "v1.2";

        
    /**
     * Constructor
     *
     * @param     aContext       Application context
     * @param     aComponent     Component to associate the CR with
     * @param     aDescription   ChangeRequest's description
     * @param     aCqId          ChangeRequest's CQ number
     * @param     newType       ChangeRequest's type
     * @param     newSeverity   ChangeRequest's severity
     */
    public ChangeRequestUpdate(EdaContext aContext, String aCqId,
                               Component aComponent, String aDescription,
                               User_Db aUser,
                               ChangeRequestType newType,
                               ChangeRequestSeverity newSeverity)	
    throws IcofException {

        super(aContext, APP_NAME, APP_VERSION);
        
        setChangeRequest(aContext, aCqId);
        setComponent(aComponent);
        setNewDescriptionText(aDescription);
        setUser(aUser);
        setNewType(newType);
        setNewSeverity(newSeverity);
        
    }

    
    /**
     * Constructor
     *
     * @param     aContext       Application context
     * @param     aComponent     Component to associate the CR with
     * @param     aDescription   ChangeRequest's description
     * @param     aCqId          ChangeRequest's CQ number
     * @param     newType       ChangeRequest's type
     * @param     newSeverity   ChangeRequest's severity

     */
    public ChangeRequestUpdate(EdaContext aContext, ChangeRequest aCr,
                               Component aComponent, String aDescription,
                               User_Db aUser,
                               ChangeRequestType newType,
                               ChangeRequestSeverity newSeverity)	
    throws IcofException {

        super(aContext, APP_NAME, APP_VERSION);
        
        setChangeRequest(aCr);
        setComponent(aComponent);
        setNewDescriptionText(aDescription);
        setUser(aUser);
        setNewType(newType);
        setNewSeverity(newSeverity);
        
    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public ChangeRequestUpdate(EdaContext aContext) throws IcofException {

        this(aContext, (ChangeRequest)null, null, null, null, null, null);

    }
    
    
    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     *
     * @param     argv[]            the command line arguments
     */
    public static void main(String argv[]) {

    	TkAppBase myApp = null;
		try {

			myApp = new ChangeRequestUpdate(null);
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
     * @throws IcofException 
     * @throws Exception 
     */
    //--------------------------------------------------------------------------
    public void process(EdaContext xContext) throws IcofException  {

        // Connect to the database
        connectToDB(xContext);

        // Update the Component
        updateComponent(xContext);
        
        // Update the request
        updateChangeRequest(xContext);
        
        // Set the return code to success if we get this far.
        setReturnCode(xContext, SUCCESS);
        commitToDB(xContext, APP_NAME);
        
    }

    
	/**
     * Update the Component for specified ChangeRequest object.
     * @param xContext       Application context
     * @throws IcofException Trouble updating the ChangeRequest
     */
    public void updateComponent(EdaContext xContext) throws IcofException {

        logInfo(xContext, "Updating Component ...", verboseInd);
        
        // Do nothing if a new Component wasn't specified by the user.
        if (getComponent() == null) {
        	return;
        }
                
        // Determine if there are any CodeUpdates for this ChangeRequest.
        // If so throw an exception otherwise allow the updating the Component.
        getChangeRequest().setCodeUpdates(xContext);
        Hashtable<String,CodeUpdate_Db> codeUpdates = getChangeRequest().getCodeUpdates();
        if ((codeUpdates != null) && (codeUpdates.size() > 0)) {
        	logInfo(xContext, " Can't update Component ...", true);
        	IcofException ie = 
        		new IcofException(APP_NAME,  "updateComponent()",
        		                  IcofException.SEVERE,
        		                  "Can't update the Component for this " + 
        		                  "ChangeRequest since \nchanges " + 
        		                  "have already been committed against this ChangeRequest.",
        		"");
        	throw ie;
        }
        
        // Lookup the ToolKit for this ChangeRequest.
        ToolKit toolKit = getChangeRequest().getToolKit(xContext);
        
        // Lookup the new ComponentTkVersion
		Component_Version_Db newCv = 
			new Component_Version_Db(xContext, toolKit.getToolKit(), 
					getComponent().getComponent());
		newCv.dbLookupByCompRelVersion(xContext);
        
		// Lookup the ComponentTkVersion objects for this ChangeRequest object
		CompVersion_ChangeRequest_Db cvcr;
		cvcr = new CompVersion_ChangeRequest_Db(getChangeRequest().getChangeRequest(),
		                                        null);
		Vector<Component_Version_Db>  compVers = cvcr.dbLookupCompVersions(xContext);
		
		// Update the ComponentTkVersion for the existing 
		// ComponentTkVersion_x_ChangeRequest object to be the new ComponentTkVersion.
		if ((compVers != null) && (compVers.size() == 1)) {
			
			Component_Version_Db oldCv;
			oldCv = (Component_Version_Db) compVers.firstElement();
		
			cvcr = new CompVersion_ChangeRequest_Db(getChangeRequest().getChangeRequest(),
			                                        oldCv);
			cvcr.dbUpdateCompVersion(xContext, newCv);
		
			logInfo(xContext, "Component update complete", true);

		}
		else {
        
			logInfo(xContext, " More than 1 Component is associated with this ChangeRequest...", 
        	        true);
        	IcofException ie = new IcofException(APP_NAME,  "updateComponent()",
        	                                     IcofException.SEVERE,
        	                                     "Can't update Component for this " + 
                                                 "ChangeRequest since it already " +
                                                 "has CodeUpdates associated with it.",
                                                 "");
        	throw ie;
			
		}
    }


	/**
     * Update the specified ChangeRequest object.
     * @param xContext       Application context
	 * @throws IcofException 
	 * @throws Exception 
     */
    public void updateChangeRequest(EdaContext xContext) throws IcofException {

        logInfo(xContext, "Updating ChangeRequest ...", verboseInd);
        
        // Update the description if it was specified
        String myDescription = getChangeRequest().getDescription(); 
        if (getDescription(xContext) != null) {
        	myDescription = getDescription(xContext);
        }

        // Update the type if it was specified
        ChangeRequestType myType = getChangeRequest().getType();
        if (getNewType() != null) {
        	myType = getNewType();
        }

        // Update the severity if it was specified
        ChangeRequestSeverity mySeverity = getChangeRequest().getSeverity();
        if (getNewSeverity() != null) {
        	mySeverity = getNewSeverity();
        }

        // Perform the update.
        getChangeRequest().dbUpdate(xContext, getChangeRequest().getClearQuest(), 
        	                            myDescription, getChangeRequest().getStatus(),
        	                            myType, mySeverity, 
        	                            getChangeRequest().getImpactedCustomer(),
        	                            getUser());
        logInfo(xContext, "Update complete", true);
        
    }


    /**
     * Determine the new description.
     * @param xContext
     * @return New description or null if no new description
     * @throws IcofException 
     */
    private String getDescription(EdaContext xContext) throws IcofException {
    	
    	String myDesc = null;
    	
    	// Read the text description
    	if (getNewDescriptionText() != null) {
    		myDesc = getNewDescriptionText();
    	}

    	// Read the file description
    	else if (getNewDescriptionFile() != null) {
    		IcofFile file = new IcofFile(getNewDescriptionFile(), false);
    		file.openRead();
    		file.read();
    		file.closeRead();
    		
    		if (file.getContents() != null) {
    			myDesc = IcofCollectionsUtil.getVectorAsString(file.getContents(),
    			                                               "\n");
    		}
    	}
    	
    	if (myDesc != null)
    		logInfo(xContext, " Using new description ...", verboseInd);
    	
    	return myDesc;
    	
	}


	protected String readParams(Hashtable<String,String> params, String errors,
			EdaContext xContext) throws IcofException {
		// Read the Component name
        if (params.containsKey("-c")) {
            setComponent(xContext,  params.get("-c"));
        }

        // Read the ClearQuest name
        if (params.containsKey("-cr")) {
            setChangeRequest(xContext,  params.get("-cr"));
        }
        else if (params.containsKey("-cq")) {
            setChangeRequest(xContext,  params.get("-cq"));
        }
        else {
            errors += "ChangeRequest (-cr) is a required parameter\n";
        }

        // Read the description text
        if (params.containsKey("-d")) {
            setNewDescriptionText( params.get("-d"));
        }
        
        // Read the description file
        if (params.containsKey("-f")) {
            setNewDescriptionFile( params.get("-f"));
        }
        // Verify both description file and text were not specified.
        if ((getNewDescriptionText() != null) && (getNewDescriptionFile() != null)) {
        	errors += "A new description can be specified via a file (-f) or text (-d) but NOT both.\n";
        }

        // Set the new Type flag
        if (params.containsKey("-defect")) {
        	setNewType(xContext, ChangeRequestType_Db.DEFECT);
        }
        if (params.containsKey("-feature")) {
        	setNewType(xContext, ChangeRequestType_Db.FEATURE);
        }
        if (params.containsKey("-defect") && params.containsKey("-feature")) {
        	errors += "Please specify either a defect (-defect) or feature " + 
                      "(-feature) but not both.\n";
        }
        
        // Set the Severity
        if (params.containsKey("-sev")) {
        	setNewSeverity(xContext,  params.get("-sev"));
        }

        // Verify component, description, feature/defect or severity was
        // set
        if ((getNewDescriptionFile() == null) && 
            (getNewDescriptionText() == null) && 
            (getComponent() == null) &&
            (getNewType() == null) &&
            (getNewSeverity() == null)) {
        	errors += "A new description (-d or -f), component (-c), " + 
        	          "defect/feature (-defect/-feature) or severity (-sev) "+
        	          "must be specified.\n";
        }
		return errors;
	}


	protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {
		singleSwitches.add("-y");
        singleSwitches.add("-h");
        singleSwitches.add("-defect");
        singleSwitches.add("-feature");
        argSwitches.add("-db");
        argSwitches.add("-cq");
        argSwitches.add("-cr");
        argSwitches.add("-d");
        argSwitches.add("-c");
        argSwitches.add("-f");
        argSwitches.add("-sev");
	}


	protected void displayParameters(String dbMode, EdaContext xContext) {
		logInfo(xContext, "App        : " + APP_NAME + "  " + APP_VERSION, verboseInd);
		logInfo(xContext, "ChangeRequest : " + getChangeRequest().getClearQuest(), verboseInd);
		if (getComponent() != null)
			logInfo(xContext, "Component  : " + getComponent().getName(), verboseInd);
		else 
			logInfo(xContext, "Component  : null", verboseInd);
		if (getNewDescriptionText() != null)
			logInfo(xContext, "Description: " + getNewDescriptionText(), verboseInd);
		if (getNewDescriptionText() != null)
			logInfo(xContext, "Description: " + getNewDescriptionFile(), verboseInd);
		if (getNewType() != null)
			logInfo(xContext, "Type       : " + getNewType().getName(), verboseInd);
		else 
			logInfo(xContext, "Type       : null", verboseInd);
		if (getNewSeverity() != null)
			logInfo(xContext, "Severity   : " + getNewSeverity().getName(), verboseInd);
		else 
			logInfo(xContext, "Severity   : null", verboseInd);
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
        usage.append("Updates 1 or more attributes of the specified ChangeRequest.\n");
        usage.append("\n");
        usage.append("USAGE:\n");
        usage.append("------\n");
        usage.append(APP_NAME + " <-cr ChangeRequest> \n");
        usage.append("          <-d description | -f description_file | -c component\n");
        usage.append("           | -feature | -defect | -sev severity>\n");
        usage.append("          [-y] [-h] [-db dbMode]\n");
        usage.append("\n");
        usage.append("  ChangeRequest    = ChangeReuqest id (MDCMS######### ...).\n");
        usage.append("  component        = New Component name (ess, pds, model, einstimer ...).\n");
        usage.append("  description      = New customer facing description of this change (release note)\n");
        usage.append("                     which will replace existing change description (3000 chars max)\n");
        usage.append("  description_file = Full path to file containing new customer facing \n");
        usage.append("                     description (release note) of this change description which \n");
        usage.append("                     will replace existing change description (3000 chars max)\n");
        usage.append("  -defect          = Specify if this ChangeRequest is a defect.\n");
        usage.append("  -feature         = Specify if this ChangeRequest is a feature.\n");
        usage.append("  severity         = Defect/feature impact/priority (1, 2, 3, 4)\n");
        usage.append("                     where 1 means \"extreme impact/resolve immediately\" down to\n");
        usage.append("                           4 means \"very little impact/work into future release\"\n");
        usage.append("  -y               = (optional) Verbose mode (echo messages to screen)\n");
        usage.append("  dbMode           = (optional) DEV | PROD (defaults to PROD)\n");
        usage.append("  -h               = Help (shows this information)\n");
        usage.append("\n");
        usage.append("Return Codes\n");
        usage.append("------------\n");
        usage.append(" 0 = ok\n");
        usage.append(" 1 = error\n");
        usage.append("\n");
        usage.append("Examples\n");
        usage.append("------------\n");
        usage.append(" To update Change Request's MDCMS123456789 release note to\n");
        usage.append("  --> cr.update -cr MDCMS123456789 -d \"description of change here\" \n");
        usage.append("  or if release note is in file /tmp/release.note\n");
        usage.append("  --> cr.update -cr MDCMS123456789 -f /tmp/release.note \n");
        usage.append(" To update the component a Change Request MDCMS123456789 is assigned to\n");
        usage.append("  --> cr.update -cr MDCMS123456789 -c new_component \n");
        usage.append("\n");

        System.out.println(usage);

    }
    
    
    /**
     * Members.
     */
    private String newDescriptionText;
    private String newDescriptionFile;
    private ChangeRequestType newType;
    private ChangeRequestSeverity newSeverity;

    
    /**
     * Getters.
     */

    public String getNewDescriptionText()  { return newDescriptionText; }
    public String getNewDescriptionFile()  { return newDescriptionFile; }
    public ChangeRequestType getNewType()  { return newType; }
    public ChangeRequestSeverity getNewSeverity()  { return newSeverity; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}
        
    /**
     * Setters.
     */

    private void setNewDescriptionText(String aDesc) { newDescriptionText = aDesc; }
    private void setNewDescriptionFile(String aFile) { newDescriptionFile = aFile; }
    private void setNewType(ChangeRequestType aType) { newType = aType; }
    private void setNewSeverity(ChangeRequestSeverity aSev) { newSeverity = aSev; }



 
    /**
     * Set the ChangeRequestType_Db object from the type name
     * 
     * @param xContext  Application context.
     * @param aName     Type name.
     * @throws IcofException 
     */
    private void setNewType(EdaContext xContext, String aName) 
    throws IcofException { 
    	
        if (getNewType() == null) {
            newType = new ChangeRequestType(xContext, aName.trim().toUpperCase());
            newType.dbLookupByName(xContext);
        }    
        logInfo(xContext, "Type: " + getNewType().toString(xContext), 
                getVerboseInd(xContext));
        
    }

    
    /**
     * Set the ChangeRequestSeverity_Db object from the severity name
     * 
     * @param xContext  Application context.
     * @param aName     Severity name.
     * @throws IcofException 
     */
    private void setNewSeverity(EdaContext xContext, String aName) 
    throws IcofException { 
    	
        if (getNewSeverity() == null) {
            newSeverity = new ChangeRequestSeverity(xContext, aName.trim().toUpperCase());
            newSeverity.dbLookupByName(xContext);
        }    
        logInfo(xContext, "Severity: " + getNewSeverity().toString(xContext), 
                getVerboseInd(xContext));
        
    }


	@Override
	protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
		return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
	}

    
}
