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
* Add a Component to a TK Release in the ETREE database. 
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 08/11/2010 GFS  Initial coding.
* 09/30/2010 GFS  Renamed to UpdateComponent.  Updated to support adding a new
*                 component or updating an existing component.
* 11/11/2010 GFS  Updated to work with new dynamic role associations.
* 01/20/2011 GFS  Updated to use the toolkit (14.1.1) instead of release (14.1).
* 02/07/2011 GFS  Updated to set create the ComponentVersion row.
* 06/08/2011 GFS  Disabled logging. Updated to prevent duplicate entries.
* 11/27/2012 GFS  Refactored to use business objects and support all flavors
*                 of the tool kit name.
* 02/15/2013 GFS  Added ability to remove users from roles.
* 05/22/2013 GFS  Updated to support owner vs team lead.  Added support for 
*                 injector. Added support for backup roles. Fixed usage text.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.app.etree;
import java.util.Hashtable;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.CompTkRelRole_Db;
import com.ibm.stg.eda.component.tk_etreedb.CompTkRelRole_User_Db;
import com.ibm.stg.eda.component.tk_etreedb.Component_Db;
import com.ibm.stg.eda.component.tk_etreedb.Component_Release_Db;
import com.ibm.stg.eda.component.tk_etreedb.Component_Version_Db;
import com.ibm.stg.eda.component.tk_etreedb.User_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofException;


public class UpdateComponent extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "updateComponent";
    public static final String APP_VERSION = "v1.5";

        
    /**
     * Constructor
     *
     * @param     aContext       Application context
     * @param     aToolKit       ToolKit object
     * @param     componentName  Component name to add.
     *
     */
    public UpdateComponent(EdaContext aContext, ToolKit aToolKit, 
    		               String componentName)
    throws IcofException {

        super(aContext, APP_NAME, APP_VERSION);

        setToolKit(aToolKit);
        setComponentName(componentName);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public UpdateComponent(EdaContext aContext) throws IcofException {

        this(aContext, null, null);

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

			myApp = new UpdateComponent(null);
			// Read and verify input parameters and get a database connection.
			start(myApp, aContext, argv);
		}

		catch (Exception e) {

			handleExceptionInMain(e, aContext);
		}
        finally {

            // Get the application return code
            int rc = FAILURE;
            if (myApp != null) {
                rc = myApp.getReturnCode(null);
            }

            if (rc == SUCCESS)
            	System.out.println("Update complete.");

            System.exit(rc);
            
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

        updateIt(xContext);
        
        // Set the return code to success if we get this far.
        setReturnCode(xContext, SUCCESS);
        commitToDB(xContext, APP_NAME);
        
    }

    
    /**
     * Perform the requested updates
     * 
     * @param xContext  Application context
     * @throws IcofException 
     */
	private void updateIt(EdaContext xContext) throws IcofException {
		
		// Create the Component object.
        createComp(xContext);
        
        // Either add the Component to a Release or update the existing Release
        // Component object.
        if (isNewComp()) {
            setCompRelease(xContext);
            setCompVersion(xContext);
            updateRoles(xContext);
        }
        else {
            updateReleaseComp(xContext);
            setCompVersion(xContext);
        }
        
		
	}


	/**
     * Update the specified TkReleaseComponent object.
     * @param context
     * @throws IcofException 
     */
    private void updateReleaseComp(EdaContext xContext) throws IcofException {

        // Lookup the existing TkRelComponent object.
        Component_Release_Db origRelComp = 
        	new Component_Release_Db(getToolKit().getToolKit().getRelease(),
        	                         getComponentDb());
        try {
            origRelComp.dbLookupByRelComp(xContext);
            logInfo(xContext, "Found Component (" + getComponentName() + 
                    ") associated with this Release (" 
                    + getToolKit().getToolKit().getRelease().getName() + ").", true);
        }
        catch(IcofException ie) {
            // Add the component to the TkRelease if it wasn't found.
            setCompRelease(xContext);
            return;
        }

        // Add user/roles for this RelComponent object.
        setCompRelease(origRelComp);
        updateRoles(xContext);
        
    }


    /**
     * Lookup the role for the specified role name
     * 
     * @param xContext  Application context
     * @param roleName  Name of role to lookup
     * @return
     * @throws IcofException 
     */
    private CompTkRelRole_Db getRole(EdaContext xContext, String roleName)
    throws IcofException {

        logInfo(xContext, "Looking up role (" + roleName + ") in db", false);
        CompTkRelRole_Db role = new CompTkRelRole_Db(roleName, "");
        role.dbLookupByName(xContext);
        
        return role;
    }


    /**
     * Associates the Component with the TK Release in the database.
     * 
     * @param xContext  Application context.
     * @throws IcofException 
     */
    private void setCompRelease(EdaContext xContext) throws IcofException {

        // Create the TkRelComponent object.
        compRelease = new Component_Release_Db(getToolKit().getToolKit().getRelease(), 
        		                              getComponentDb());
        
        // Assign the component to the release.
        try {
        	compRelease.dbLookupByRelComp(xContext);
            logInfo(xContext, "Component (" + getComponentName() + 
                    ") already associated with this Release (" 
                    + getToolKit().getToolKit().getRelease().getName() + ").", true);

        }
        catch(IcofException ie) {
            compRelease.dbAddRow(xContext, getUser());
            logInfo(xContext, "Component (" + getComponentName() + 
                    ") associated with Release (" 
                    + getToolKit().getToolKit().getRelease().getName() + ").", true);
        }

    }


    /**
     * Associates the ComponentRelease with the TK Version in the database.
     * 
     * @param xContext  Application context.
     * @throws IcofException 
     */
    private void setCompVersion(EdaContext xContext) throws IcofException {

        // Create the TkRelComponent object.
        compVersion = 
        	new Component_Version_Db(xContext, getToolKit().getToolKit(),
        	                         getComponentDb());
        
        // Assign the component to the version.
        try {
            compVersion.dbLookupByCompRelVersion(xContext);
            logInfo(xContext, "Component (" + getComponentName() + 
                    ") already associated with this Tool Kit (" 
                    + getToolKit().getToolKit().getDisplayName() + ").", true);

        }
        catch(IcofException ie) {
            compVersion.dbAddRow(xContext, getUser());
            logInfo(xContext, "Component (" + getComponentName() + 
                    ") associated with Version (" 
                    + getToolKit().getToolKit().getDisplayName() + ").", true);
        }

    }


    /**
     * Update the builder, injecter, owner and transmitter for this RelComp.
     * 
     * @param xContext 
     * @throws IcofException 
     * 
     */
    private void updateRoles(EdaContext xContext) throws IcofException {

        // Add user/roles for this RelComponent object.
    	if (getAddAction()) {
    		addRole(xContext, getBuilder(), "builder");
    		addRole(xContext, getInjector(), "injector");
    		addRole(xContext, getOwner(), "owner");
    		addRole(xContext, getTransmitter(), "transmitter");
    		addRole(xContext, getBackupBuilder(), "backup builder");
    		addRole(xContext, getBackupInjector(), "backup injector");
    		addRole(xContext, getBackupOwner(), "backup owner");
    		addRole(xContext, getBackupTransmitter(), "backup transmitter");
    		addRole(xContext, getSvnReader(), "svn read");
    		addRole(xContext, getSvnWriter(), "svn write");
    	}
    	else {
    		removeRole(xContext, getBuilder(), "builder");
    		removeRole(xContext, getInjector(), "injector");
    		removeRole(xContext, getOwner(), "owner");
    		removeRole(xContext, getTransmitter(), "transmitter");
    		removeRole(xContext, getBackupBuilder(), "backup builder");
    		removeRole(xContext, getBackupInjector(), "backup injector");
    		removeRole(xContext, getBackupOwner(), "backup owner");
    		removeRole(xContext, getBackupTransmitter(), "backup transmitter");
    		removeRole(xContext, getSvnReader(), "svn read");
    		removeRole(xContext, getSvnWriter(), "svn write");
    	}
    	
    }
    

    /**
     * Add the user to the specified role.
     * 
     * @param xContext  Application context
     * @param user      User to add to role
     * @param roleName  Name of role to add user to
     * @throws IcofException 
     */
    private void addRole(EdaContext xContext, User_Db user, String roleName) 
    throws IcofException {
    	
    	// Do nothing if the user is null
    	if (user == null) {
    		logInfo(xContext, "Nothing to add to Role (" + roleName + ")", 
    		        getVerboseInd(xContext));
    		return;
    	}

    	logInfo(xContext, "Adding User (" + user.getIntranetId() + 
    	        ") to Role (" + roleName + ")", getVerboseInd(xContext));

    	// Create the correct Component-Release-Role object
    	CompTkRelRole_Db role = getRole(xContext, roleName.toUpperCase());
    	CompTkRelRole_User_Db userRole = new CompTkRelRole_User_Db(getCompRelease(),
    	                                                           user, role);
    	
    	// If user is not already this role then add them
    	try {
    		userRole.dbLookupByAll(xContext);
    		logInfo(xContext, "This user (" + user.getIntranetId() +
    		        ") is already a " + roleName + " for this Componenet and ToolKit",
    		        true);
    	}
    	catch(IcofException trap) {
    		userRole.dbAddRow(xContext, getUser());
    		logInfo(xContext, "Added " + roleName + " - " 
    		        + user.getIntranetId(), true);
    	}

	}


    /**
     * Remove the user from the specified role.
     * 
     * @param xContext  Application context
     * @param user      User to remove from role
     * @param roleName  Name of role to remove user from
     * @throws IcofException 
     */
    private void removeRole(EdaContext xContext, User_Db user, String roleName) 
    throws IcofException {
    	
    	// Do nothing if the user is null
    	if (user == null) {
    		logInfo(xContext, "Nothing to remove from Role (" + roleName + ")", 
    		        getVerboseInd(xContext));
    		return;
    	}
    	
    	logInfo(xContext, "Removing User (" + user.getIntranetId() + 
    	        ") from Role (" + roleName + ")", getVerboseInd(xContext));
    	
    	// Create the correct Component-Release-Role object
    	CompTkRelRole_Db role = getRole(xContext, roleName.toUpperCase());
    	CompTkRelRole_User_Db userRole = new CompTkRelRole_User_Db(getCompRelease(),
    	                                                           user, role);
    	
    	// If user is this role then remove them
    	try {
    		userRole.dbLookupByAll(xContext);
    		userRole.dbDeleteRow(xContext, getUser());
    		logInfo(xContext, "Removed " + roleName + " - " 
    		        + user.getIntranetId(), true);
    	}
    	catch(IcofException trap) {
    		logInfo(xContext, "This user (" + user.getIntranetId() +
    		        ") is not a member of " + roleName + 
    		        " for this Componenet and ToolKit",
    		        true);
    	}
         
	}

    
	/**
     * Create a Component object and add to database.
     * @param xContext       Application context
     * @throws IcofException 
     */
    private void createComp(EdaContext xContext)
    throws IcofException {

        // Create the Component object.
        Component_Db comp = new Component_Db(getComponentName());
        
        // If the Component doesn't exist and the isNewComp is not true then
        // error otherwise lookup the Component or add it.
        boolean foundComp = false;
        try {
            logInfo(xContext, "Looking up Component ...", getVerboseInd(xContext));
            comp.dbLookupByName(xContext);
            foundComp = true;
        }
        catch(IcofException ie) {
            if (! isNewComp()) {
                throw new IcofException(APP_NAME, "createComp()", IcofException.SEVERE,
                                        "Component not found in database.  If you " +
                                        "are trying to add a new Component you " +
                                        "must use the -n switch.\n",
                                        null);
            }
        }
        
        // Add the component if not already in the db.
        if (! foundComp) {
            logInfo(xContext, "Added Component (" + getComponentName() + ")", true);
            comp.dbAddRow(xContext, getUser());
        }
        else {
            logInfo(xContext, "Component (" + getComponentName() + 
                    ") already exists in the database.", true);
        }

        setComponent(comp);
        logInfo(xContext, " New Component: " + getComponentDb().toString(xContext),
                getVerboseInd(xContext));
        
    }


	protected String readParams(Hashtable<String, String> params, String errors,
	                            EdaContext xContext) 
	throws IcofException {
		
		// Read the release name
        if (params.containsKey("-t")) {
        	setToolKit(xContext, params.get("-t"));
        }
        else {
            errors += "ToolKit (-t) is a required parameter\n";
        }
        
        // Read the component name
        if (params.containsKey("-c")) {
            setComponentName( params.get("-c"));
        }
        else {
            errors += "Componennt (-c) is a required parameter\n";
        }

        // Read the builder intranet id
        if (params.containsKey("-b")) {
            setBuilder(xContext, (String) params.get("-b"));
        }
        if (params.containsKey("-bb")) {
            setBackupBuilder(xContext, (String) params.get("-bb"));
        }

        // Read the injecter intranet id
        if (params.containsKey("-i")) {
            setInjector(xContext, (String) params.get("-i"));
        }
        if (params.containsKey("-bi")) {
            setBackupInjector(xContext, (String) params.get("-bi"));
        }
        
        // Read the component owner intranet id
        if (params.containsKey("-o")) {
            setOwner(xContext, (String) params.get("-o"));
        }
        if (params.containsKey("-bo")) {
            setBackupOwner(xContext, (String) params.get("-bo"));
        }

        // Read the Transmitter intranet id
        if (params.containsKey("-x")) {
            setTransmitter(xContext, (String) params.get("-x"));
        }
        if (params.containsKey("-bx")) {
            setBackupTransmitter(xContext, (String) params.get("-bx"));
        }

        // Read the SVN access ids
        if (params.containsKey("-svn_read")) {
            setSvnReader(xContext, (String) params.get("-svn_read"));
        }
        if (params.containsKey("-svn_write")) {
            setSvnWriter(xContext, (String) params.get("-svn_write"));
        }

        
        // Read the New Component switch.
        setNewCompFlag(false);
        if (params.containsKey("-n")) {
            setNewCompFlag(true);
        }

        // Read the Remove switch.
        setAddAction(true);
        if (params.containsKey("-remove")) {
            setAddAction(false);
        }

        
        return errors;
        
	}


	protected void createSwitches(Vector<String> singleSwitches, 
	                              Vector<String> argSwitches) {
		singleSwitches.add("-y");
        singleSwitches.add("-h");
        singleSwitches.add("-n");
        singleSwitches.add("-remove");
        argSwitches.add("-db");
        argSwitches.add("-t");
        argSwitches.add("-c");
        argSwitches.add("-b");
        argSwitches.add("-bb");
        argSwitches.add("-i");
        argSwitches.add("-bi");
        argSwitches.add("-o");
        argSwitches.add("-bo");
        argSwitches.add("-x");
        argSwitches.add("-bx");
        argSwitches.add("-svn_read");
        argSwitches.add("-svn_write");
	}


	protected void displayParameters(String dbMode, EdaContext xContext) {
		
		boolean verboseInd = getVerboseInd(xContext);
        logInfo(xContext, "App        : " + APP_NAME + "  " + APP_VERSION, verboseInd);
        if (getToolKit() != null) {
            logInfo(xContext, "ToolKit    : " + getToolKit().getToolKit().getDisplayName(), verboseInd);
        }
        else {
            logInfo(xContext, "ToolKit    : null", verboseInd);
        }
        if (getComponentName() != null) {
            logInfo(xContext, "Component  : " + getComponentName(), verboseInd);
        }
        else {
            logInfo(xContext, "Component  : null", verboseInd);
        }

        logInfo(xContext, "Is New Component  : " + isNewComp(), verboseInd);

        if (getBuilder() != null) {
            logInfo(xContext, "Builder           : " + getBuilder().getIntranetId(), verboseInd);
        }
        else {
            logInfo(xContext, "Builder           : null", verboseInd);
        }
        if (getBackupBuilder() != null) {
            logInfo(xContext, "Backup Builder    : " + getBackupBuilder().getIntranetId(), verboseInd);
        }
        else {
            logInfo(xContext, "Backup Builder    : null", verboseInd);
        }
        
        if (getInjector() != null) {
            logInfo(xContext, "Injector          : " + getInjector().getIntranetId(), verboseInd);
        }
        else {
            logInfo(xContext, "Injector          : null", verboseInd);
        }
        if (getBackupInjector() != null) {
            logInfo(xContext, "Backup Injector   : " + getBackupInjector().getIntranetId(), verboseInd);
        }
        else {
            logInfo(xContext, "Backup Injector   : null", verboseInd);
        }

        if (getOwner() != null) {
            logInfo(xContext, "Onwer             : " + getOwner().getIntranetId(), verboseInd);
        }
        else {
            logInfo(xContext, "Owner             : null", verboseInd);
        }
        if (getBackupOwner() != null) {
            logInfo(xContext, "Backup Owner      : " + getBackupOwner().getIntranetId(), verboseInd);
        }
        else {
            logInfo(xContext, "Backup Owner      : null", verboseInd);
        }

        if (getTransmitter() != null) {
            logInfo(xContext, "Transmitter       : " + getTransmitter().getIntranetId(), verboseInd);
        }
        else {
            logInfo(xContext, "Transmitter       : null", verboseInd);
        }
        if (getBackupTransmitter() != null) {
            logInfo(xContext, "Backup Transmitter: " + getBackupTransmitter().getIntranetId(), verboseInd);
        }
        else {
            logInfo(xContext, "Backup Transmitter: null", verboseInd);
        }
        if (getSvnReader() != null) {
            logInfo(xContext, "SVN Reader        : " + getSvnReader().getIntranetId(), verboseInd);
        }
        else {
            logInfo(xContext, "SVN Reader        : null", verboseInd);
        }
        if (getSvnWriter() != null) {
            logInfo(xContext, "SVN Writer        : " + getSvnWriter().getIntranetId(), verboseInd);
        }
        else {
            logInfo(xContext, "SVN Writer        : null", verboseInd);
        }
        
        logInfo(xContext, "Add Action            : " + getAddAction(), verboseInd);
        logInfo(xContext, "DB Mode               : " + dbMode, verboseInd);
        logInfo(xContext, "Verbose               : " + getVerboseInd(xContext), verboseInd);
	}

    
	/**
	 * Display this application's usage and invocation
	 */
	protected void showUsage() {

		StringBuffer usage = new StringBuffer();
		usage.append("------------------------------------------------------\n");
		usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
		usage.append("------------------------------------------------------\n");
		usage.append("Updates the specified Component for the specified Release.\n");
		usage.append("If you want to add the Component then use the -n switch.\n");
		usage.append("\n");
		usage.append("USAGE:\n");
		usage.append("------\n");
		usage.append(APP_NAME + " <-t tool_kit> <-c component>\n");
		usage.append("                [-b builder] [-bb backup_builder\n");
		usage.append("                [-i injector] [-bi backup_injector]\n");
		usage.append("                [-o owner] [-bo backup owner]\n");
		usage.append("                [-x transmitter] [-bx backup transmitter]\n");
		usage.append("                [-svn_read read] [-svn_write writer]\n");
		usage.append("                [-remove]\n");
		usage.append("                [-y] [-h] [-n] [-db dbMode]\n");
		usage.append("\n");
		usage.append("  tool_Kit  = TK name (14.1.1, 14.1.2 ...).\n");
		usage.append("  component = Component name (einstimer, edautls, ...)\n");
		usage.append("  -b        = (optional) Builder's intranet id\n");
		usage.append("  -bb       = (optional) Backup builder's intranet id\n");
		usage.append("  -i        = (optional) Injector's intranet id\n");
		usage.append("  -bi       = (optional) Backupo injector's intranet id\n");
		usage.append("  -o        = (optional) Component owner's intranet id\n");
		usage.append("  -bo       = (optional) Backup component owner's intranet id\n");
		usage.append("  -x        = (optional) Transmitter's intranet id\n");
		usage.append("  -bx       = (optional) Backup transmitter's intranet id\n");
		usage.append("  -n        = (optional) Add component if it doesn't exist\n");
		usage.append("  -svn_read = (optional) SVN reader's intranet id\n");
		usage.append("  -svn_write = (optional) SVN writer's intranet id\n");
		usage.append("  -remove   = (optional) Remove the specified builder, injector, owner or xmitter\n");
		usage.append("  -y        = (optional) Verbose mode (echo messages to screen)\n");
		usage.append("  -h        = Help (shows this information)\n");
		usage.append("  dbMode    = (optional) DEV | PROD (defaults to PROD)\n");
		usage.append("\n");
		usage.append("Return Codes\n");
		usage.append("------------\n");
		usage.append(" 0 = ok\n");
		usage.append(" 1 = error\n");
		usage.append(" 2 = nothing to add\n");
		usage.append("\n");

		System.out.println(usage);

	}
    
    
    /**
     * Members.
     */
    private String componentName;
    private Component_Db component;
    private Component_Release_Db compRelease;
    private Component_Version_Db compVersion;
    private boolean newCompFlag = false;
    private User_Db builder = null;
    private User_Db injector = null;
    private User_Db owner = null;
    private User_Db transmitter = null;
    private User_Db backupBuilder = null;
    private User_Db backupInjector = null;
    private User_Db backupOwner = null;
    private User_Db backupTransmitter = null;
    private User_Db svnReader = null;
    private User_Db svnWriter = null;
    private boolean addAction = true;
    
    
    /**
     * Getters.
     */
    public String getComponentName() { return componentName; }
    public Component_Db getComponentDb() { return component; }
    public Component_Release_Db getCompRelease() { return compRelease; }
    public Component_Version_Db getCompVersion() { return compVersion; }
    public boolean isNewComp() { return newCompFlag; }
    public User_Db getBuilder() { return builder; }
    public User_Db getInjector() { return injector; }
    public User_Db getOwner() { return owner; }
    public User_Db getTransmitter() { return transmitter; }
    public User_Db getBackupBuilder() { return backupBuilder; }
    public User_Db getBackupInjector() { return backupInjector; }
    public User_Db getBackupOwner() { return backupOwner; }
    public User_Db getBackupTransmitter() { return backupTransmitter; }
    public User_Db getSvnReader() { return svnReader; }
    public User_Db getSvnWriter() { return svnWriter; }
    public boolean getAddAction() { return addAction; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}
       
    
    /**
     * Setters.
     */
    private void setNewCompFlag(boolean aFlag) { newCompFlag = aFlag;  }
    private void setComponentName(String aName) { componentName = aName;  }
    private void setComponent(Component_Db aComp) { component = aComp;  }
    private void setCompRelease(Component_Release_Db aCompRel) { compRelease = aCompRel;  }
    private void setAddAction(boolean aFlag) { addAction = aFlag; }

    
    /**
     * Set the Builder object
     * @param xContext   Application context.
     * @param anIntranet Intranet id
     * @throws IcofException 
     */
    private void setBuilder(EdaContext xContext, String anIntranet) 
    throws IcofException { 
        if ((getBuilder() == null) && (anIntranet != null)) {
            builder = new User_Db(anIntranet, true);
            try {
                builder.dbLookupByIntranet(xContext);
            }
            catch(IcofException ie) {
                // Not found so populate from Blue Pages and add to DB
                builder.populateFromBluePages(xContext);
                builder.dbAddRow(xContext);
            }
        }    
        logInfo(xContext, "Builder: " + getBuilder().toString(xContext), 
                getVerboseInd(xContext));
    }

    
    /**
     * Set the Backup Builder object
     * @param xContext   Application context.
     * @param anIntranet Intranet id
     * @throws IcofException 
     */
    private void setBackupBuilder(EdaContext xContext, String anIntranet) 
    throws IcofException { 
        if ((getBackupBuilder() == null) && (anIntranet != null)) {
            backupBuilder = new User_Db(anIntranet, true);
            try {
                backupBuilder.dbLookupByIntranet(xContext);
            }
            catch(IcofException ie) {
                // Not found so populate from Blue Pages and add to DB
                backupBuilder.populateFromBluePages(xContext);
                backupBuilder.dbAddRow(xContext);
            }
        }    
        logInfo(xContext, "Backup Builder: " + getBackupBuilder().toString(xContext),
                getVerboseInd(xContext));
    }
    
    /**
     * Set the Injector object
     * @param xContext   Application context.
     * @param anIntranet Intranet id
     * @throws IcofException 
     */
    private void setInjector(EdaContext xContext, String anIntranet) 
    throws IcofException { 
        if ((getInjector() == null) && (anIntranet != null)) {
            injector = new User_Db(anIntranet, true);
            try {
                injector.dbLookupByIntranet(xContext);
            }
            catch(IcofException ie) {
                // Not found so populate from Blue Pages and add to DB
                injector.populateFromBluePages(xContext);
                injector.dbAddRow(xContext);
            }
        }    
        logInfo(xContext, "Injector: " + getInjector().toString(xContext), 
                getVerboseInd(xContext));
    }

    
    /**
     * Set the Backup Injector object
     * @param xContext   Application context.
     * @param anIntranet Intranet id
     * @throws IcofException 
     */
    private void setBackupInjector(EdaContext xContext, String anIntranet) 
    throws IcofException { 
        if ((getBackupInjector() == null) && (anIntranet != null)) {
            backupInjector = new User_Db(anIntranet, true);
            try {
                backupInjector.dbLookupByIntranet(xContext);
            }
            catch(IcofException ie) {
                // Not found so populate from Blue Pages and add to DB
                backupInjector.populateFromBluePages(xContext);
                backupInjector.dbAddRow(xContext);
            }
        }    
        logInfo(xContext, "Backup Injector: " + getBackupInjector().toString(xContext),
                getVerboseInd(xContext));
    }

    
    /**
     * Set the Transmitter object
     * @param xContext   Application context.
     * @param anIntranet Intranet id
     * @throws IcofException 
     */
    private void setTransmitter(EdaContext xContext, String anIntranet) 
    throws IcofException { 
        if ((getTransmitter() == null) && (anIntranet != null)) {
            transmitter = new User_Db(anIntranet, true);
            try {
                transmitter.dbLookupByIntranet(xContext);
            }
            catch(IcofException ie) {
                // Not found so populate from Blue Pages and add to DB
                transmitter.populateFromBluePages(xContext);
                transmitter.dbAddRow(xContext);
            }
        }    
        logInfo(xContext, "Transmitter " + getTransmitter().toString(xContext), 
                getVerboseInd(xContext));
    }

    
    /**
     * Set the Backup Transmitter object
     * @param xContext   Application context.
     * @param anIntranet Intranet id
     * @throws IcofException 
     */
    private void setBackupTransmitter(EdaContext xContext, String anIntranet) 
    throws IcofException { 
        if ((getBackupTransmitter() == null) && (anIntranet != null)) {
            backupTransmitter = new User_Db(anIntranet, true);
            try {
                backupTransmitter.dbLookupByIntranet(xContext);
            }
            catch(IcofException ie) {
                // Not found so populate from Blue Pages and add to DB
                backupTransmitter.populateFromBluePages(xContext);
                backupTransmitter.dbAddRow(xContext);
            }
        }    
        logInfo(xContext, "Backup Transmitter " + getBackupTransmitter().toString(xContext),
                getVerboseInd(xContext));
    }

    
    /**
     * Set the Owner object
     * @param xContext   Application context.
     * @param anIntranet Intranet id
     * @throws IcofException 
     */
    private void setOwner(EdaContext xContext, String anIntranet) 
    throws IcofException { 
        if ((getOwner() == null)  && (anIntranet != null)) {
            owner = new User_Db(anIntranet, true);
            try {
                owner.dbLookupByIntranet(xContext);
            }
            catch(IcofException ie) {
                // Not found so populate from Blue Pages and add to DB
                owner.populateFromBluePages(xContext);
                owner.dbAddRow(xContext);
            }
        }    
        logInfo(xContext, "Owner: " + getOwner().toString(xContext), 
                getVerboseInd(xContext));
    }

    
    /**
     * Set the Backup Owner object
     * @param xContext   Application context.
     * @param anIntranet Intranet id
     * @throws IcofException 
     */
    private void setBackupOwner(EdaContext xContext, String anIntranet) 
    throws IcofException { 
        if ((getBackupOwner() == null)  && (anIntranet != null)) {
            backupOwner = new User_Db(anIntranet, true);
            try {
                backupOwner.dbLookupByIntranet(xContext);
            }
            catch(IcofException ie) {
                // Not found so populate from Blue Pages and add to DB
                backupOwner.populateFromBluePages(xContext);
                backupOwner.dbAddRow(xContext);
            }
        }    
        logInfo(xContext, "Backup Owner: " + getBackupOwner().toString(xContext),
                getVerboseInd(xContext));
    }


    /**
     * Set the SVN Reader object
     * @param xContext   Application context.
     * @param anIntranet Intranet id
     * @throws IcofException 
     */
    private void setSvnReader(EdaContext xContext, String anIntranet) 
    throws IcofException { 
        if ((getSvnReader() == null)  && (anIntranet != null)) {
            svnReader = new User_Db(anIntranet, true);
            try {
                svnReader.dbLookupByIntranet(xContext);
            }
            catch(IcofException ie) {
                // Not found so populate from Blue Pages and add to DB
                svnReader.populateFromBluePages(xContext);
                svnReader.dbAddRow(xContext);
            }
        }    
        logInfo(xContext, "SVN Reader: " + getSvnReader().toString(xContext),
                getVerboseInd(xContext));
    }

    /**
     * Set the SVN Writer object
     * @param xContext   Application context.
     * @param anIntranet Intranet id
     * @throws IcofException 
     */
    private void setSvnWriter(EdaContext xContext, String anIntranet) 
    throws IcofException { 
        if ((getSvnWriter() == null)  && (anIntranet != null)) {
            svnWriter = new User_Db(anIntranet, true);
            try {
                svnWriter.dbLookupByIntranet(xContext);
            }
            catch(IcofException ie) {
                // Not found so populate from Blue Pages and add to DB
                svnWriter.populateFromBluePages(xContext);
                svnWriter.dbAddRow(xContext);
            }
        }    
        logInfo(xContext, "SVN Writer: " + getSvnWriter().toString(xContext),
                getVerboseInd(xContext));
    }


    
	@Override
	protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
		return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
	}
    
	
}
