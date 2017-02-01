/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2011 -- IBM Internal Use Only
*
*=============================================================================
*
* CREATOR: Gregg Stadtlander
*
*-PURPOSE---------------------------------------------------------------------
* Add, remove or display Component Types associated with a ComponentRelease. 
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 10/04/2011 GFS  Initial coding.
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
import com.ibm.stg.eda.component.tk_etreedb.ComponentType_ComponentRelease_Db;
import com.ibm.stg.eda.component.tk_etreedb.ComponentType_Db;
import com.ibm.stg.eda.component.tk_etreedb.Component_Release_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.ComponentType;
import com.ibm.stg.eda.component.tk_etreeobjs.Release;
import com.ibm.stg.iipmds.common.IcofException;

public class ComponentReleaseType extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "compRelType";
    public static final String APP_VERSION = "v1.0";

        
    /**
     * Constructor
     *
     * @param  xContext        Application context
     * @param  aRel            A Release object    
     * @param  aComp           A Component object
     */
    public ComponentReleaseType(EdaContext xContext, Release aRel, 
                                Component aComp)
    throws IcofException {

        super(xContext, APP_NAME, APP_VERSION);
        setCompRelease(xContext, aComp, aRel);

    }

    /**
     * Constructor
     *
     * @param  xContext   Application context
     * @param  aCompRel   A Component_Release_Db object    
     */
    public ComponentReleaseType(EdaContext xContext, Component_Release_Db aCompRel)
    throws IcofException {

        super(xContext, APP_NAME, APP_VERSION);
        setCompRelease(aCompRel);

    }


    
    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public ComponentReleaseType(EdaContext xContext) throws IcofException {

        this(xContext, null, null);

    }
    
    
    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     *
     * @param     argv[]            the command line arguments
     */
    public static void main(String argv[]) {

    	TkAppBase myApp = null;
		try {

			myApp = new ComponentReleaseType(null);
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

        // Determine the action to run
        if (doAddAction()) {
        	add(xContext, getAddCompType());
        }
        
        if (doDeleteAction()) {
        	delete(xContext, getDeleteCompType());
        }

        if (doShowAction()) {
        	show(xContext);
        }
        else {
        	// Show results even if add or delete requested
        	if (doAddAction() || doDeleteAction())
        		show(xContext);
        }
        
        commitToDBAndSetReturncode(xContext, APP_NAME, SUCCESS);
        
    }

    
    /**
     * Add the Component Type to this ComponentRelease.
     * 
     * @param xContext  Application context
     * @param compType  Component Type to add.
     * @throws IcofException 
     */
	public void add(EdaContext xContext, ComponentType compType) 
	throws IcofException {

		logInfo(xContext, "Adding ComponentType to ComponentRelease ...",
		        isVerbose(xContext));
		
		ComponentType_ComponentRelease_Db crct = 
			new ComponentType_ComponentRelease_Db(getCompRelease(), 
			                                      compType.getDbObject());
		crct.dbAddRow(xContext);
		
		logInfo(xContext, "ComponentType added to ComponentRelease ...", true);
		
	}

	
    /**
     * Delete the Component Type from this ComponentRelease.
     * 
     * @param xContext  Application context
     * @param compType  Component Type to delete.
     * @throws IcofException 
     */
	public void delete(EdaContext xContext, ComponentType compType) 
	throws IcofException {

		logInfo(xContext, "Deleting ComponentType from ComponentRelease ...",
		        isVerbose(xContext));
		
		ComponentType_ComponentRelease_Db crct = 
			new ComponentType_ComponentRelease_Db(getCompRelease(), 
			                                      compType.getDbObject());
		crct.dbDeleteRow(xContext);
		
		logInfo(xContext, "Component Type deleted from ComponentRelease ...", 
		        true);
		
	}

	
    /**
     * Show the Component Type associated with this ComponentRelease.
     * 
     * @param xContext  Application context
     * @throws IcofException 
     */
	public void show(EdaContext xContext) 
	throws IcofException {

		logInfo(xContext, "Displaying ComponentTypes for ComponentRelease ...",
		        isVerbose(xContext));
		
		// Lookup the Component Types for this Component Release
		ComponentType_ComponentRelease_Db crct = 
			new ComponentType_ComponentRelease_Db(getCompRelease(), null);
		Hashtable<String,ComponentType_Db>  compTypes = crct.dbLookupComponentTypes(xContext);
		
		if (compTypes.size() > 0) {
			logInfo(xContext, "Component Types for " +
			        getCompRelease().getComponent().getName() + " " +
			        getCompRelease().getRelease().getName(), true);
		}
		else {
			logInfo(xContext, 
			        "No Component Types associated with this ComponentRelease", 
			        true);
		}
		
		// Display the results.
		Iterator<ComponentType_Db>  iter = compTypes.values().iterator();
		while (iter.hasNext()) {
			ComponentType_Db type =  iter.next();
			type.dbLookupById(xContext);
			logInfo(xContext, type.getName(), true);
		}
		
	}


	protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {
		singleSwitches.add("-y");
        singleSwitches.add("-h");
        singleSwitches.add("-show");
        argSwitches.add("-db");
        argSwitches.add("-c");
        argSwitches.add("-r");
        argSwitches.add("-add");
        argSwitches.add("-delete");
	}

	protected String readParams(Hashtable<String,String> params, String errors,
			EdaContext xContext) throws IcofException {
		// Read the Component
        String compName = "";
        if (params.containsKey("-c")) {
            compName = (String) params.get("-c");
        }
        else {
            errors += "Component (-c) is a required parameter\n";
        }

        // Read the Release 
        if (params.containsKey("-r")) {
            setCompRelease(xContext, compName, (String) params.get("-r"));
        }
        else {
            errors += "Release (-r) is a required parameter\n";
        }

        // Read the action
        if (params.containsKey("-show")) { 
        	setShowAction(true);
        }
        if (params.containsKey("-add")) {
        	setAddAction(true);
        	setAddCompType(xContext, (String) params.get("-add"));
        }
        if (params.containsKey("-delete")) { 
        	setDeleteAction(true);
        	setDeleteCompType(xContext, (String) params.get("-delete"));
        } 

        // Verify an action was specified
        if ((! doShowAction()) && (! doAddAction()) && (! doDeleteAction())) {
        	errors += "An action (-show, -add or -delete) is required\n";
        }
		return errors;
	}

	protected void displayParameters(String dbMode, EdaContext xContext) {
		logInfo(xContext, "App           : " + APP_NAME + "  " + APP_VERSION, verboseInd);
		logInfo(xContext, "Release       : " + getCompRelease().getRelease().getName(), verboseInd);
		logInfo(xContext, "Component     : " + getCompRelease().getComponent().getName(), verboseInd);
		if (doShowAction())
		logInfo(xContext, "Action        : show", verboseInd);
		if (doAddAction())
			logInfo(xContext, "Action        : add " + getAddCompType().getName(),
			        verboseInd);
		if (doDeleteAction())
			logInfo(xContext, "Action        : delete " + getDeleteCompType().getName(),
			        verboseInd);
		if (getAddCompType() != null)
			logInfo(xContext, "Add Comp Type : " + getAddCompType().getName(), verboseInd);
		if (getDeleteCompType() != null)
			logInfo(xContext, "Del Comp Type : " + getDeleteCompType().getName(), verboseInd);
		logInfo(xContext, "DB Mode       : " + dbMode, verboseInd);
		logInfo(xContext, "Verbose       : " + getVerboseInd(xContext), verboseInd);
	}

    
    /**
     * Display this application's usage and invocation
     */
    protected void showUsage() {

        StringBuffer usage = new StringBuffer();
        usage.append("------------------------------------------------------\n");
        usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
        usage.append("------------------------------------------------------\n");
        usage.append("Add, remove or show Component Types associated with a \n");
        usage.append("Release specific Component. \n");
        usage.append("\n");
        usage.append("USAGE:\n");
        usage.append("------\n");
        usage.append(APP_NAME + " <-c component> <-r release>\n");
        usage.append("            <-show | -add  comp_type | -delete comp_type>\n");
        usage.append("            [-y] [-h] [-db dbMode]\n");
        usage.append("\n");
        usage.append("  component = Component name (ess, model, nutshell ...).\n");
        usage.append("  release   = Tool Kit release (14.1, 15.1 ...).\n");
        usage.append("  show      = Show the Component Types associated with this Component and Release\n");
        usage.append("  add       = Add the comp_type (32-bit, build_support ...) to this CompRelease.\n");
        usage.append("  delete    = Delete the comp_type (32-bit, build_support ...) from this CompRelease.\n");
        usage.append("  -y        = (optional) Verbose mode (echo messages to screen)\n");
        usage.append("  dbMode    = (optional) DEV | PROD (defaults to PROD)\n");
        usage.append("  -h        = Help (shows this information)\n");
        usage.append("\n");
        usage.append("Return Codes\n");
        usage.append("------------\n");
        usage.append(" 0 = application ran ok\n");
        usage.append(" 1 = application error\n");
        usage.append("");

        System.out.println(usage);

    }
    
    
    /**
     * Members.
     */
    private ComponentType addComponentType;
    private ComponentType deleteComponentType;
    private Component_Release_Db compRelease;
    private boolean showAction = false;
    private boolean addAction = false;
    private boolean deleteAction = false;
    private static boolean requestHelp = false;

    
    /**
     * Getters.
     */
    public ComponentType getAddCompType()  { return addComponentType; }
    public ComponentType getDeleteCompType()  { return deleteComponentType; }
    public Component_Release_Db getCompRelease()  { return compRelease; }
    public boolean doShowAction()  { return showAction; }
    public boolean doAddAction()  { return addAction; }
    public boolean doDeleteAction()  { return deleteAction; }
    public static boolean getRequestHelp() { return requestHelp; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}
        

    /**
     * Setters.
     */
    private void setCompRelease(Component_Release_Db aCompRel) { compRelease = aCompRel; }
    private void setShowAction(boolean aFlag) { showAction = aFlag; }
    private void setAddAction(boolean aFlag) { addAction = aFlag; }
    private void setDeleteAction(boolean aFlag) { deleteAction = aFlag; }

   

    /**
     * Set the ComponentType object from the component type name
     * @param xContext       Application context.
     * @param aName          Component Type name
     * @throws IcofException 
     */
    private void setAddCompType(EdaContext xContext, String aName) 
    throws IcofException { 
        if (getAddCompType() == null) {
            addComponentType = new ComponentType(xContext, aName.trim());
            addComponentType.dbLookupByName(xContext);
        }    
        logInfo(xContext, "Add Component Type: " + getAddCompType().toString(xContext),
                isVerbose(xContext));
    }


    /**
     * Set the ComponentType object from the component type name
     * @param xContext       Application context.
     * @param aName          Component Type name
     * @throws IcofException 
     */
    private void setDeleteCompType(EdaContext xContext, String aName) 
    throws IcofException { 
        if (getDeleteCompType() == null) {
            deleteComponentType = new ComponentType(xContext, aName.trim());
            deleteComponentType.dbLookupByName(xContext);
        }    
        logInfo(xContext, "Delete Component Type: " + getDeleteCompType().toString(xContext),
                isVerbose(xContext));
    }


    /**
     * Set the Component_Release_Db object from the Component and Release
     * @param xContext       Application context.
     * @param aName          Component Type name
     * @throws IcofException 
     */
    private void setCompRelease(EdaContext xContext, Component aComp, Release aRel) 
    throws IcofException { 
    	
    	// Return if nothing to look up
    	if ((aComp == null) || (aRel == null))
    		return;
    	
    	compRelease = new Component_Release_Db(aRel.getRelease(), aComp.getComponent());
    	compRelease.dbLookupByRelComp(xContext);
    	logInfo(xContext, "Component Release: " + getCompRelease().toString(xContext), 
    	        isVerbose(xContext));
    }

    
    /**
     * Set the Component_Release_Db object from the Component and Release names
     * @param xContext       Application context.
     * @param aName          Component Type name
     * @throws IcofException 
     */
    private void setCompRelease(EdaContext xContext, String compName, String relName) 
    throws IcofException {
    	
    	// Lookup the Component
    	Component comp = new Component(xContext, compName);
    	comp.dbLookupByName(xContext);
    	logInfo(xContext, "Component: " + comp.toString(xContext), isVerbose(xContext));
    	
    	// Lookup the Release
    	Release rel = new Release(xContext, relName, "");
    	rel.dbLookupByName(xContext);
    	logInfo(xContext, "Release: " + rel.toString(xContext), isVerbose(xContext));
    	
    	// Lookup the ComponentRelease
    	compRelease = new Component_Release_Db(rel.getRelease(), comp.getComponent());
    	compRelease.dbLookupByRelComp(xContext);
    	if (! compRelease.getComponent().isLoaded())
    		compRelease.getComponent().dbLookupById(xContext);
    	if (! compRelease.getRelease().isLoaded())
    		compRelease.getRelease().dbLookupById(xContext);
    	logInfo(xContext, "Component Release: " + getCompRelease().toString(xContext), 
    	        isVerbose(xContext));
    	
    }

	@Override
	protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
		return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
	}
    
}
