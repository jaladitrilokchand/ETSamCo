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
 * Generate a text and/or web levelhist file 
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 02/04/2011 GFS  Initial coding.
 * 06/10/2011 GFS  Disabled logging.  Updated to lookup DB objects on the fly.
 * 08/29/2011 GFS  Fixed issue with incorrect LH data.
 * 11/27/2012 GFS  Refactored to use business objects and support all flavors
 *                 of the tool kit name.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkLevelHistUtils;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.CodeUpdate_Db;
import com.ibm.stg.eda.component.tk_etreedb.Component_Version_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.TkCodeUpdate;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofCollectionsUtil;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofFile;

public class GenerateLevelHist extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "generateLevelHist";
    public static final String APP_VERSION = "v1.0";


    /**
     * Constructor
     *
     * @param  aContext    Application context
     * @param  aTk         Object representing the tool kit (14.1.1, 14.1.2 ...)
     * @param  aComponent  Object representing the component
     * @param  aRevision   Revision to gather LH data for  
     * @param  bUseLast    If true use last extracted revisions otherwise
     *                     get LH data for all revisions
     * @param  aLh         LH file object      
     * @param  aLhWeb      LH web file object     
     */
    public GenerateLevelHist(EdaContext aContext, 
                             ToolKit aTk, 
                             Component aComponent,
                             String aRevision,
                             boolean bUseLsat, 
                             IcofFile aLh,
                             IcofFile aLhWeb)
                             throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);

	setToolKit(aTk);
	setComponent(aComponent);
	setRevision(aRevision);
	setLastExtract(bUseLsat);
	setLevelHist(aLh);
	setLevelHistWeb(aLhWeb);
    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public GenerateLevelHist(EdaContext aContext) throws IcofException {

	this(aContext, null, null, "", false, null, null);

    }


    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     *
     * @param     argv[]            the command line arguments
     */
    public static void main(String argv[]) {

	TkAppBase myApp = null;
	try {

	    myApp = new GenerateLevelHist(null);
	    start(myApp, argv);
	}
	catch (Exception e) {
	    handleExceptionInMain(e);
	} finally {
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

	// Log the requested event
	generate(xContext);

	// Disconnect from the database
	try {
	    xContext.getConnection().rollback();
	}
	catch(SQLException se) {
	    throw new IcofException(APP_NAME, "proces()", IcofException.SEVERE,
	                            "Unable to rollback DB transactions.\n",
	                            se.getMessage());
	}finally{
	    disconnectFromDB(xContext);
	}

    }


    /**
     * Generate LEVELHIST data
     * 
     * @param xCcontext EdaContext object
     * @throws IcofException 
     */
    private void generate(EdaContext xContext) throws IcofException {

	logInfo(xContext, "Generating LEVELHIST data ...", verboseInd);

	// Set required objects.
	setCompVersion(xContext);

	// Create a list of ComponentUpdates which the user wants added to 
	// the LEVELHIST.
	setCodeUpdates(xContext);

	// Get LEVELHIST data for each ComponentUpdate.
	setTkCodeUpdates(xContext);

	// Display/write LEVELHIST data.
	writeLevelHistFiles(xContext);
	setReturnCode(xContext, SUCCESS);

    }


    /**
     * Set the Component_Version_Db object.
     * 
     * @param xContext Application context object
     * @throws IcofException 
     */
    private void setCompVersion(EdaContext xContext) throws IcofException {

	logInfo(xContext, "Setting ComponentVersion object ...", verboseInd);
	compVersion = new Component_Version_Db(xContext, 
	                                       getToolKit().getToolKit(), 
	                                       getComponent().getComponent());
	compVersion.dbLookupByCompRelVersion(xContext);

    }


    /**
     * Determine which ComponentUpdate objects the user wants listed in the
     * LEVEHIST file.
     * 
     * @param xContext  Application context object
     * @throws IcofException 
     */
    private void setCodeUpdates(EdaContext xContext) throws IcofException {

	logInfo(xContext, "Reading CodeUpdates(revisions) ...", verboseInd);
	codeUpdates = new Vector<CodeUpdate_Db>();

	if (useLastExtract) {

	    // Find the last extracted CodeUpdates
	    logInfo(xContext, " Querying for Last Extracted revisions ...", verboseInd);
	    CodeUpdate_Db cu = new CodeUpdate_Db(getCompVersion(), "", "", "",
	                                         null, null);
	    codeUpdates = cu.dbLookupExtractedLast(xContext);
	    logInfo(xContext, " Extracted revisions found: " + codeUpdates.size(), verboseInd);

	}
	else {

	    // Find the last extracted CodeUpdates
	    logInfo(xContext, " Querying for Extraction since " + getRevision(), verboseInd);
	    CodeUpdate_Db cu = new CodeUpdate_Db(getCompVersion(), "", "", "",
	                                         null, null);
	    codeUpdates = cu.dbLookupExtractedSince(xContext, getRevision());
	    logInfo(xContext, " Extracted revisions found: " + codeUpdates.size(), verboseInd);

	}

    }


    /**
     * Create a collection of TkCompUpdate business objects which contain the
     * FuncUpdates, CodeUpdates and changed files.
     * 
     * @param xContext  Application context object
     * @throws IcofException 
     */
    private void setTkCodeUpdates(EdaContext xContext) throws IcofException {

	logInfo(xContext, "Reading LEVELHIST data ...", verboseInd);
	lhData = new Vector<String>();

	// For each CodeUpdate in the collection find the TkCodeUpdate
	// and lookup the LEVELHIST data.
	tkCodeUpdates = new Vector<TkCodeUpdate>();
	Iterator<CodeUpdate_Db> iter = getCodeUpdates().iterator();
	while (iter.hasNext()) {
	    CodeUpdate_Db myCodeUp =  iter.next();
	    logInfo(xContext, " Reading Revision: " + myCodeUp.getRevision(), verboseInd);
	    logInfo(xContext, "  -> Extracted on: " + myCodeUp.getCreatedOn(), verboseInd);
	    TkCodeUpdate cu = new TkCodeUpdate(xContext, myCodeUp.getId());
	    tkCodeUpdates.add(cu);

	}

    }


    /**
     * Write the LEVELHIST files based on the extracted code updates.
     * 
     * @param context
     * @throws IcofException 
     */
    private void writeLevelHistFiles(EdaContext xContext) throws IcofException {

	logInfo(xContext, "Writing LEVELHIST data ...", verboseInd);

	// If no FuncUpdates then return;
	if ((getTkCodeUpdates() == null) || (getTkCodeUpdates().size() < 1)) {
	    return;
	}


	// Add LEVELHIST entries for each ComponentUpdate
	Timestamp lastTms = null;
	Iterator<TkCodeUpdate> iter = getTkCodeUpdates().iterator();

	Vector<String> lhData = new Vector<String>();
	boolean first = true;
	while (iter.hasNext()) {

	    TkCodeUpdate codeUpdate = (TkCodeUpdate) iter.next();
	    logInfo(xContext, " CodeUpdate: " + codeUpdate.getCodeUpdate().getId(),
	            verboseInd);

	    // Create the LEVELHIST header if first CodeUpdate or if
	    // extracted timestamp has changed
	    if ((first) || 
	    (! codeUpdate.getCodeUpdate().getExtractedOn().equals(lastTms))) {
		if (codeUpdate.getCodeUpdate().getExtractedOn() == null)
		    lhData.addAll(TkLevelHistUtils.getNewHeader());
		else 
		    lhData.addAll(TkLevelHistUtils.getNewHeader(codeUpdate.getCodeUpdate().getExtractedOn()));
		lastTms = codeUpdate.getCodeUpdate().getExtractedOn();
		first = false;
	    }

	    // Add the CodeUpdate description and changed files
	    logInfo(xContext, 
	            IcofCollectionsUtil.getVectorAsString(codeUpdate.getLevelHistEntry(xContext), "\n"),
	            verboseInd);
	    lhData.addAll(codeUpdate.getLevelHistEntry(xContext));
	    lhData.add("");

	}

	// Append the LEVELHIST entries to the LEVELHIST file
	appendToLevelhist(xContext, getLevelHist(), lhData, false);
	appendToLevelhist(xContext, getLevelHistWeb(), lhData, true);
	if ((getLevelHist() == null) && (getLevelHistWeb() == null)) {
	    String entries = IcofCollectionsUtil.getVectorAsString(lhData, "\n");
	    logInfo(xContext, "\n" + entries, true);
	}

    }


    /**
     * Write the LEVELHIST updates to the specified LEVELHIST file.
     * 
     * @param context     Application context
     * @param levelHist   LEVELHIST file object
     * @param updates     List of updates to append
     * @param isHtmlFile   If true append "<br>\n" to each update entry otherwise 
     *                    append "\n" only.
     * @throws IcofException 
     */
    private void appendToLevelhist(EdaContext context,
                                   IcofFile levelHist,
                                   Vector<String> updates,
                                   boolean isHtmlFile) throws IcofException {

	// If no LEVELHIST file then return.
	if (levelHist == null) {
	    return;
	}

	// Convert the update entries into a  string.
	StringBuffer entries = new StringBuffer();
	Iterator<String> iter = updates.iterator();
	while (iter.hasNext()) {
	    String entry =  iter.next();
	    if (isHtmlFile)
		entry += "<br>";
	    entry += "\n";
	    entries.append(entry);
	}

	// Append updates for the LEVELHIST file.
	levelHist.validate(true);
	try {
	    levelHist.openAppend();
	    levelHist.writeLine(entries.toString(), true);
	}
	catch(IcofException ex) {
	    throw new IcofException(APP_NAME, "appendToLevelHist()",
	                            IcofException.SEVERE,
	                            "Unable to append to LEVELHIST file.\n",
	                            levelHist.getAbsolutePath() + "\n" 
	                            + ex.getMessage());
	}
	finally {
	    if (levelHist.isOpenAppend()) {
		levelHist.closeAppend();
	    }
	}
    }


    protected String readParams(Hashtable<String,String> params, String errors,
                                EdaContext xContext) 
                                throws IcofException {
	// Read the Tool Kit name
	if (params.containsKey("-t")) {
	    setToolKit(xContext, params.get("-t"));
	}
	else {
	    errors += "ToolKit (-t) is a required parameter\n";
	}

	// Read the Component
	if (params.containsKey("-c")) {
	    setComponent(xContext, params.get("-c"));
	}
	else {
	    errors += "Component (-c) is a required parameter\n";
	}

	// Read the LEVEHIST location
	if (params.containsKey("-l")) {
	    setLevelHist(xContext,  params.get("-l"));
	}

	// Read the LEVELHIST web location
	if (params.containsKey("-w")) {
	    setLevelHistWeb(xContext,  params.get("-w"));
	}

	// Read the revision
	if (params.containsKey("-v")) {
	    setRevision( params.get("-v"));
	}

	// Read the last extract event
	setLastExtract(false);
	if (params.containsKey("-last")) {
	    setLastExtract(true);
	}
	return errors;
    }


    protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {
	singleSwitches.add("-y");
	singleSwitches.add("-h");
	singleSwitches.add("-last");
	argSwitches.add("-c");
	argSwitches.add("-db");
	argSwitches.add("-l");
	argSwitches.add("-t");
	argSwitches.add("-v");
	argSwitches.add("-w");
    }


    protected void displayParameters(String dbMode, EdaContext xContext) {
	boolean verboseInd = getVerboseInd(xContext);
	logInfo(xContext, "App         : " + APP_NAME + "  " + APP_VERSION, verboseInd);
	if (getToolKit() != null) {
	    logInfo(xContext, "ToolKit     : " + getToolKit().getToolKit().getDisplayName(), verboseInd);
	}
	else {
	    logInfo(xContext, "ToolKit     : null", verboseInd);
	}
	if (getComponent() != null) {
	    logInfo(xContext, "Component   : " + getComponent().getName(), verboseInd);
	}
	else {
	    logInfo(xContext, "Component   : null", verboseInd);
	}
	if (getRevision() != null) {
	    logInfo(xContext, "Revision    : " + getRevision(), verboseInd);
	}
	else {
	    logInfo(xContext, "Revision    : null", verboseInd);
	}
	logInfo(xContext, "Last extract: " + getLastExtract(), verboseInd);
	if (getLevelHist() != null) {
	    logInfo(xContext, "LevelHist   : " + getLevelHist().getAbsolutePath(), verboseInd);
	}
	else {
	    logInfo(xContext, "LevelHist   : null", verboseInd);
	}
	if (getLevelHistWeb() != null) {
	    logInfo(xContext, "LevelHistWeb  : " + getLevelHistWeb().getAbsolutePath(), verboseInd);
	}
	else {
	    logInfo(xContext, "LevelHistWeb  : null", verboseInd);
	}

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
	usage.append("Writes change information to LEVELHIST file(s) or stdout.\n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " <-t tool_kit> <-c component> <-last or -v revision>\n");
	usage.append("                  [-l levelHist ] [-w levelHistWeb]\n");
	usage.append("                  [-y] [-h] [-db dbMode]\n");
	usage.append("\n");
	usage.append("  tool_kit     = ToolKit name (14.1.1, 14.1.2 ).\n");
	usage.append("  component    = TK component name (hdp, model, einstimer ...)\n");
	usage.append("  -last        = Use change data from LAST extraction event\n");
	usage.append("  revision     = Use change data from extraction events back to this revision\n");
	usage.append("  levelhist    = (optional)Full path to LEVELHIST file\n");
	usage.append("  levelHistWeb = (optional)Full path to web version of LEVELHIST file\n");
	usage.append("  -y           = (optional) Verbose mode (echo messages to screen)\n");
	usage.append("  -h           = Help (shows this information)\n");
	usage.append("  dbMode       = (optional) DEV | PROD (defaults to PROD)\n");
	usage.append("\n");
	usage.append("Notes\n");
	usage.append("------\n");
	usage.append(" 1) Either -last or -v must be specified.\n");
	usage.append(" 2) If -l and -w is not specified LEVELHIST entries will be\n");
	usage.append("    written to stdout.\n");
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
    private String revision;
    private boolean useLastExtract;
    private Component_Version_Db compVersion;
    private Vector<CodeUpdate_Db> codeUpdates;    
    private Vector<String> lhData;
    private Vector<TkCodeUpdate> tkCodeUpdates;
    private IcofFile levelHist;
    private IcofFile levelHistWeb;

    /**
     * Getters.
     */
    public Component_Version_Db getCompVersion() { return compVersion; }
    public String getRevision() { return revision; }
    public boolean getLastExtract() { return useLastExtract; }
    public Vector<CodeUpdate_Db> getCodeUpdates() { return codeUpdates; }
    public Vector <TkCodeUpdate>getTkCodeUpdates() { return tkCodeUpdates; }
    public Vector<String> getLhData() { return lhData; }
    public IcofFile getLevelHist() { return levelHist; }
    public IcofFile getLevelHistWeb() { return levelHistWeb; }
    protected String getAppName() { return APP_NAME; }
    protected String getAppVersion() { return APP_VERSION; }



    /**
     * Setters.
     */
    private void setRevision(String aRev) { revision = aRev;  }
    private void setLevelHist(IcofFile aFile) { levelHist = aFile;  }
    private void setLevelHistWeb(IcofFile aFile) { levelHistWeb = aFile;  }
    private void setLastExtract(boolean aFlag) { useLastExtract = aFlag;  }


    /**
     * Set the LEVELHIST IcofFile object
     * @param xContext      Application context.
     * @param aFileName     Full path to LEVELHIST file
     * @throws IcofException 
     */
    private void setLevelHist(EdaContext xContext, String aFileName) 
    throws IcofException { 
	if (getLevelHist() != null) {
	    levelHist = null;
	}
	if ((aFileName != null) && (! aFileName.equals(""))) {
	    levelHist = new IcofFile(aFileName, false);
	    logInfo(xContext, "LEVELHIST: " + 
	    getLevelHist().getAbsolutePath(), false);
	}
    }


    /**
     * Set the LEVELHIST web IcofFile object
     * @param xContext      Application context.
     * @param aFileName     Full path to LEVELHIST file
     * @throws IcofException 
     */
    private void setLevelHistWeb(EdaContext xContext, String aFileName) 
    throws IcofException { 
	if (getLevelHistWeb() != null) {
	    levelHistWeb = null;
	}
	if ((aFileName != null) && (! aFileName.equals(""))) {
	    levelHistWeb = new IcofFile(aFileName, false);
	    logInfo(xContext, "LEVELHIST web: " + 
	    getLevelHistWeb().getAbsolutePath(), false);
	}
    }


    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
	return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
    }

}

