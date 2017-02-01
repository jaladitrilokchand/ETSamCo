/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2010 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 * FILE: CodeUpdate.java
 *
 *-PURPOSE---------------------------------------------------------------------
 * CodeUpdate DB class with audit info
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 05/19/2010 GFS  Initial coding.
 * 07/22/2010 GFS  Converted to using PreparedStatements.
 * 08/04/2010 GFS  Converted to using storing CodeUpdateStatus ID instead of 
 *                 CodeUpdateStatusName for current status.
 * 02/03/2011 GFS  Added ComponentUpdate_id and Extracted_on columns/members.
 * 02/09/2011 GFS  Reworked the query in setLookupoNotExtracted() to be more
 *                 efficient and correct.      
 * 03/09/2011 GFS  Added dbLookupCompVerLoc() method.
 * 05/10/2011 GFS  Updated to support new create/update columns. Added
 *                 dbLookupNeedsAuditInfo() method.
 * 06/30/2011 GFS  Added description column. Added dbLookupExtractedSince().
 * 09/29/2011 GFS  Updated dbAdd() to store the description. Update
 *                 dbLookupExtractedSince() to sort the results by svn revision 
 *                 and set the extracted timestamp to the ComponentUpdate created
 *                 timestamp which will be used in the generateLevelHist app.
 * 10/19/2011 GFS  Updated dbAddRow() to handle single quotes.       
 * 11/14/2011 GFS  Fixed a bug in getRevisions() which was returning the wrong 
 *                 CodeUpdate.
 * 11/15/2011 GFS  Updated to setLookupRevsByCompVerLocStatement and 
 *                 dbLookupRevsByCompVerLoc to return SVN revisions instead of
 *                 a collection of CodeUpdate objects.   
 * 11/28/2011 GFS  Updated dbLookupNotExtracted() and dbLookupRevsByCompVerLoc() 
 *                 queries to only return CodeUpdates for tk1*  / trunk.
 * 02/14/2012 GFS  Changed setLookupByCompVerLocStatement() to address the 
 *                 situation where there are no revisions for a tool kit.
 * 08/06/2012 GFS  Added dbLookupIdsByCompVerLoc() method.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.tk_etreedb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.Vector;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAudit;
import com.ibm.stg.iipmds.common.IcofException;

public class CodeUpdate_Db extends TkAudit {

    /**
     * 
     */
    private static final long serialVersionUID = 6698028466158531339L;
    /**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.CODE_UPDATE";
    public static final String ID_COL = "CODE_UPDATE_ID";
    public static final String REVISION_COL = "SVNREVISION";
    public static final String BRANCH_COL = "BRANCH_NAME";
    public static final String DESCRIPTION_COL = "DESCRIPTION";
    public static final String COMP_VERSION_ID_COL = "COMPONENT_TKVERSION_ID";
    public static final String CURRENT_STATUS_ID_COL = "CURRENT_STATUS_ID";
    public static final String COMPONENT_UPDATE_ID_COL = "COMPONENTUPDATE_ID";
    public static final String EXTRACTED_TMS_COL = "EXTRACTED_TMSTMP";
    public static final String ALL_COLS = ID_COL + "," + COMP_VERSION_ID_COL
    + "," + REVISION_COL + ","
    + BRANCH_COL + "," + DESCRIPTION_COL
    + "," + CURRENT_STATUS_ID_COL + ","
    + COMPONENT_UPDATE_ID_COL + ","
    + EXTRACTED_TMS_COL + ","
    + CREATED_BY_COL + ","
    + CREATED_ON_COL + ","
    + UPDATED_BY_COL + ","
    + UPDATED_ON_COL;


    /**
     * Constructor - takes a DB id
     * 
     * @param anId A database id
     */
    public CodeUpdate_Db(long anId) {

	setId(anId);
    }


    public CodeUpdate_Db() {

    }


    /**
     * Constructor - takes a TkComponent object, SVN revision and a branch name.
     * 
     * @param aCompVer Component_TkVersion_Db object
     * @param aRevision The SVN revision number
     * @param aBranch The branch name
     * @param aDescription Commit description
     * @param aCompUpdate The ComponentUpdate object (can be null)
     * @param anExtractTms The extract timestamp (can be null)
     */
    public CodeUpdate_Db(Component_Version_Db aCompVer, String aRevision,
                         String aBranch, String aDescription,
                         ComponentUpdate_Db aCompUpdate, Timestamp anExtractTms) {

	setCompVersion(aCompVer);
	setRevision(aRevision);
	setBranch(aBranch);
	setDescription(aDescription);
	setComponentUpdate(aCompUpdate);
	setExtractedOn(anExtractTms);
    }


    /**
     * Constructor - all members.
     * 
     * @param aCompVer Component_TkVersion_Db object
     * @param aRevision The SVN revision number
     * @param aBranch The branch name
     * @param aDescription Commit description
     * @param aCompUpdate The ComponentUpdate object (can be null)
     * @param anExtractTms The extract timestamp (can be null)
     * @param aCreator Intranet id of person creating this object
     * @param aCreateTms Creation timestamp
     * @param anEditor Intranet id of person updating this object
     * @param anEditTms Update timestamp
     */
    public CodeUpdate_Db(Component_Version_Db aCompVer, String aRevision,
                         String aBranch, String aDescription,
                         ComponentUpdate_Db aCompUpdate,
                         Timestamp anExtractTms, String aCreator,
                         Timestamp aCreateTms, String anEditor,
                         Timestamp anEditTms) {

	setCompVersion(aCompVer);
	setRevision(aRevision);
	setBranch(aBranch);
	setDescription(aDescription);
	setComponentUpdate(aCompUpdate);
	setExtractedOn(anExtractTms);
	setCreatedBy(aCreator);
	setCreatedOn(aCreateTms);
	setUpdatedBy(anEditor);
	setUpdatedOn(anEditTms);
    }

    /**
     * Constructor - all members.
     * 
     * @param aContext    Application context object
     * @param aRelVersion RelVersion object
     * @param aComp       Component object
     * @param aRevision   The SVN revision number
     * @param aBranch     The branch name
     * @param aDescription Commit description
     * @param aCompUpdate The ComponentUpdate object (can be null)
     * @param anExtractTms The extract timestamp (can be null)
     * @param aCreator    Intranet id of person creating this object
     * @param aCreateTms  Creation timestamp
     * @param anEditor    Intranet id of person updating this object
     * @param anEditTms   Update timestamp
     * @throws IcofException 
     */
    public CodeUpdate_Db(EdaContext aContext, RelVersion_Db aRelVersion, 
                         Component_Db aComp, String aRevision,
                         String aBranch, String aDescription,
                         ComponentUpdate_Db aCompUpdate,
                         Timestamp anExtractTms, String aCreator,
                         Timestamp aCreateTms, String anEditor,
                         Timestamp anEditTms) throws IcofException {

	compVersion = new Component_Version_Db(aContext, aRelVersion, aComp);
	compVersion.dbLookupByAll(aContext);
	setRevision(aRevision);
	setBranch(aBranch);
	setDescription(aDescription);
	setComponentUpdate(aCompUpdate);
	setExtractedOn(anExtractTms);
	setCreatedBy(aCreator);
	setCreatedOn(aCreateTms);
	setUpdatedBy(anEditor);
	setUpdatedOn(anEditTms);
    }
    /**
     * Data Members
     */
    private long id;
    private String revision;
    private String branch;
    private String description;
    private Component_Version_Db compVersion;
    private ComponentUpdate_Db compUpdate;
    private Timestamp extractedOn;


    /**
     * Getters
     * @formatter:off
     */
    public long getId() { return id; }
    public String getRevision() { return revision; }
    public String getBranch() { return branch; }
    public String getDescription() { return description; }
    public Component_Version_Db getCompVersion() { return compVersion; }
    public ComponentUpdate_Db getComponentUpdate() { return compUpdate; }
    public Timestamp getExtractedOn() { return extractedOn; }


    /**
     * Setters
     */
    private void setId(long anId) { id = anId; }
    private void setRevision(String aRev) { revision = aRev; }
    private void setBranch(String aBranch) { branch = aBranch; }
    private void setDescription(String aDesc) { description = aDesc; }
    private void setCompVersion(Component_Version_Db aCompVer) { compVersion = aCompVer; }
    private void setComponentUpdate(ComponentUpdate_Db anUpdate) { compUpdate = anUpdate; }
    private void setExtractedOn(Timestamp aTms) { extractedOn = aTms; }
    private void setCompVersion(EdaContext xContext, short compId) { 
	compVersion = new Component_Version_Db(compId);
    }
    private void setComponentUpdate(EdaContext xContext, long anId) { 
	compUpdate = new ComponentUpdate_Db(anId);
    }
    // @formatter:on


    /**
     * Create a PreparedStatement to lookup this object by id.
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setLookupIdStatement(EdaContext xContext)
    throws IcofException {

	// Define the query.
	String query = "select " + ALL_COLS + " from " + TABLE_NAME + " where "
	+ ID_COL + " = ? ";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup the last extracted row.
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setLookupExtractedLastStatement(EdaContext xContext)
    throws IcofException {

	// Define the query.
	String query = "select " + ID_COL + "  from " + TABLE_NAME + " where "
	+ COMP_VERSION_ID_COL + " = ? " + " and "
	+ EXTRACTED_TMS_COL + " = (select max("
	+ EXTRACTED_TMS_COL + ") " + "from " + TABLE_NAME
	+ " where " + COMP_VERSION_ID_COL + " = ? ) "
	+ " order by " + ID_COL + " ASC";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup extracted CodeUpdates from a
     * specific revision onward.
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setLookupExtractedSinceStatement(EdaContext xContext)
    throws IcofException {

	// select Code_Update_Id, SvnRevision, Extracted_TmStmp, Branch_Name,
	// ComponentUpdate_Id
	// from Tk.Code_Update
	// where cast(SvnRevision as int) >= 4
	// and Extracted_TmStmp is not null
	// and ComponentUpdate_ID is not null
	// and Component_TkVersion_Id = 2
	// order by cast(SvnRevision as int)

	// select cu.Code_Update_Id, cu.SvnRevision, cu.Branch_Name,
	// cu.ComponentUpdate_Id, cup.created_tmstmp as "EXTRACTED_TMSTMP"
	// from Tk.Code_Update as cu,
	// tk.componentupdate as cup
	// where cu.SvnRevision >= 250
	// and cu.Extracted_TmStmp is not null
	// and cu.ComponentUpdate_ID is not null
	// and cu.Component_TkVersion_Id = 2
	// and cu.componentupdate_id = cup.componentupdate_id

	// Define the query.
	// String query = "select " + ID_COL + ","
	// + REVISION_COL + ", "
	// + EXTRACTED_TMS_COL + ", "
	// + BRANCH_COL + ", "
	// + COMPONENT_UPDATE_ID_COL +
	// "  from " + TABLE_NAME +
	// " where cast (" + REVISION_COL + " as int) >= ? " +
	// " and " + EXTRACTED_TMS_COL + " is not null " +
	// " and " + COMPONENT_UPDATE_ID_COL + " is not null " +
	// " and " + COMP_VERSION_ID_COL + " = ? " +
	// " order by cast(" + REVISION_COL + " as int)";
	String query = "select cu." + ID_COL + ", " + "cu." + REVISION_COL
	+ ", " + "cup." + ComponentUpdate_Db.CREATED_ON_COL
	+ " as " + EXTRACTED_TMS_COL + ", " + "cu." + BRANCH_COL
	+ ", " + "cu." + COMPONENT_UPDATE_ID_COL + "  from "
	+ TABLE_NAME + " as cu, "
	+ ComponentUpdate_Db.TABLE_NAME + " as cup "
	+ " where cast (cu." + REVISION_COL + " as int) >= ? "
	+ " and cup." + ComponentUpdate_Db.CREATED_ON_COL
	+ " is not null " + " and cu." + COMPONENT_UPDATE_ID_COL
	+ " is not null " + " and cu." + COMP_VERSION_ID_COL
	+ " = ? " + " and cu." + COMPONENT_UPDATE_ID_COL
	+ " = cup." + ComponentUpdate_Db.ID_COL
	+ " order by cast(cu." + REVISION_COL + " as int)";


	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup this object by Component and
     * Revision.
     * 
     * @param xContext Application context.
     * @param bKnowTk  True if the caller knows the TK 
     *                 False if the caller knows only the Component
     * 
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setLookupCompRevStatement(EdaContext xContext, boolean bKnowTk)
    throws IcofException {

	String query = "select cu." + ID_COL;
	
	if (bKnowTk) {
//	    	select cu.code_update_id
//	    	  from tk.Code_Update as cu
//	    	 where cu.component_tkversion_id = ?
//	    	   and cu.svnrevision = ?

	    // Define the query.
	 
	    query += " from " + TABLE_NAME + " as cu " + 
	             " where cu." +  COMP_VERSION_ID_COL + " = ? ";
	             
	}
	else {
//	    select cu.codeupdate_id
//	    from tk.component as c,
//	         tk.tkrelease as r,
//	         tk.component_tkrelease as cr,
//	         tk.component_tkversion as cv,
//	         tk.code_update as cu
//	   where c.component_id = ?
//	     and r.tkrelease_id = ?
//	     and r.tkrelease_id = cr.tkrelease_id
//	     and c.component_id = cr.component_id
//	     and cr.component_tkrelease_id = cv.component_tkrelease_id
//	     and cv.component_tkversion_id = cu.component_tkversion_id
//	     and cu.svnrevision = ?
	    
	    query += " from " + Component_Db.TABLE_NAME + " as c," + 
	             Release_Db.TABLE_NAME + " as r," +
	             Component_Release_Db.TABLE_NAME + " as cr," +
	             Component_Version_Db.TABLE_NAME + " as cv," +
	             TABLE_NAME + " as cu " +
                     " where c." +  Component_Db.ID_COL + " = ? " +
	             " and r." + Release_Db.ID_COL + " = ? " +
	             " and r." + Release_Db.ID_COL + " = cr." + Component_Release_Db.REL_ID_COL +
	             " and c." + Component_Db.ID_COL + " = cr." + Component_Release_Db.COMP_ID_COL +
	             " and cr." + Component_Release_Db.ID_COL + " = cv." + Component_Version_Db.REL_COMP_ID_COL +
	             " and cv." + Component_Version_Db.ID_COL + " = cu." + COMP_VERSION_ID_COL;
	}
	
	query += " and cu." + REVISION_COL + " = ? ";
	
	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }

    /**
     * Create a PreparedStatement to find all rows for the CompVersion
     * 
     * @param xContext Application context.
     * @throws IcofException
     */
    public void setLookupByToolKitCompStatement(EdaContext xContext, 
                                                boolean notExtracted)
                                                throws IcofException {

	// Define the query.
	String query = "select " +ID_COL + 
	" from " + TABLE_NAME + 
	" where " + COMP_VERSION_ID_COL + " = ? ";

	if (notExtracted) 
	    query += " and " + EXTRACTED_TMS_COL + " is NULL";

	query += " order by created_tmstmp";

	//	
	//	               " and (" + BRANCH_COL + " = 'trunk' " + 
	//	               " or " + BRANCH_COL + " like 'tk1%' )";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to find all rows for the CompVersion which and
     * extract timestamp of null.
     * 
     * @param xContext Application context.
     * @throws IcofException
     */
    public void setLookupNotExtractedStatement(EdaContext xContext)
    throws IcofException {

	// Define the query.
	String query = "select " + ALL_COLS + " from " + TABLE_NAME + " where "
	+ COMP_VERSION_ID_COL + " = ? " + 
	" and " + EXTRACTED_TMS_COL + " is NULL" + 
	" and (" + BRANCH_COL + " = 'trunk' " + " " +
	"or " + BRANCH_COL + " like 'tk15.1%' " +
	"or " + BRANCH_COL + " like 'tk14.1%' " +
	"or " + BRANCH_COL + " like '14.1%' )";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to find all rows for the ComponentUpdate.
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setLookupByCompUpdateStatement(EdaContext xContext)
    throws IcofException {

	// Define the query.
	String query = "select " + ALL_COLS + " from " + TABLE_NAME + " where "
	+ COMPONENT_UPDATE_ID_COL + " = ? ";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to find all rows for the
     * ComponentVersionLocation.
     * 
     * @param xContext Application context.
     * @param toolkit A RelVersion_Db object or null if not querying for a
     *            specific tool kit
     * @param showLatest If true query for the latest revision only
     * @throws IcofException
     */
    public void setLookupByCompVerLocStatement(EdaContext xContext,
                                               RelVersion_Db toolKit,
                                               boolean showLatest)
                                               throws IcofException {

	// select cu.svnrevision, cu.code_update_id
	// from tk.Component as c,
	// tk.TkRelease as r,
	// tk.Component_TkRelease as cr,
	// tk.Component_TkVersion as cv,
	// tk.TkVersion as v,
	// tk.location as l,
	// tk.ComponentTkVersion_Location as cvl,
	// tk.Location_ComponentUpdate as lcu,
	// tk.ComponentUpdate as compu,
	// tk.Code_Update as cu
	// where v.tkversion_id = 1
	// and c.component_id = 124
	// and l.name = 'BUILD'
	// and l.Location_id = cvl.Location_id
	// and c.Component_id = cr.component_id
	// and cr.Component_TkRelease_id = cv.Component_TkRelease_id
	// and cv.TkVersion_id = v.TkVersion_id
	// and cvl.component_tkversion_id = cv.component_tkversion_id
	// and lcu.componentTkVersion_Location_id =
	// cvl.ComponentTkVersion_Location_id
	// and compu.ComponentUpdate_id = lcu.ComponentUpdate_id
	// and compu.ComponentUpdate_Id = cu.ComponentUpdate_id
	// order by cast(cu.Code_Update_id as int) asc

	String body = " from " + Component_Db.TABLE_NAME + " as c, "
	+ Release_Db.TABLE_NAME + " as r, "
	+ Component_Release_Db.TABLE_NAME + " as cr, "
	+ Component_Version_Db.TABLE_NAME + " as cv, "
	+ RelVersion_Db.TABLE_NAME + " as v, "
	+ Location_Db.TABLE_NAME + " as l, "
	+ Component_Version_Location_Db.TABLE_NAME + " as cvl, "
	+ Location_ComponentUpdate_Db.TABLE_NAME + " as lcu, "
	+ ComponentUpdate_Db.TABLE_NAME + " as compu, "
	+ CodeUpdate_Db.TABLE_NAME + " as cu ";
	if (toolKit != null) {
	    body += "where v." + RelVersion_Db.ID_COL + " = ?" + " and c."
	    + Component_Db.ID_COL + " = ?";
	}
	else {
	    body += "where c." + Component_Db.ID_COL + " = ?";
	}
	body += " and l." + Location_Db.NAME_COL + " = ?" + " and l."
	+ Location_Db.ID_COL + " = cvl."
	+ Component_Version_Location_Db.LOCATION_ID_COL + " and c."
	+ Component_Db.ID_COL + " = cr."
	+ Component_Release_Db.COMP_ID_COL + " and cr."
	+ Component_Release_Db.ID_COL + " = cv."
	+ Component_Version_Db.REL_COMP_ID_COL + " and cv."
	+ Component_Version_Db.VERSION_ID_COL + " = v."
	+ RelVersion_Db.ID_COL + " and cvl."
	+ Component_Version_Location_Db.COMPONENT_VERSION_ID_COL
	+ " = cv." + Component_Version_Db.ID_COL + " and lcu."
	+ Location_ComponentUpdate_Db.COMPONENT_VERSION_LOCATION_ID_COL
	+ " = cvl." + Component_Version_Location_Db.ID_COL
	+ " and compu." + ComponentUpdate_Db.ID_COL + " = lcu."
	+ Location_ComponentUpdate_Db.COMPONENT_UPDATE_ID_COL
	+ " and compu." + ComponentUpdate_Db.ID_COL + " = cu."
	+ COMPONENT_UPDATE_ID_COL;


	String query;
	if (!showLatest) {
	    query = "select cu." + REVISION_COL + ", cu." + ID_COL + body
	    + " order by cast(" + REVISION_COL + " as int) asc";
	    ;
	}
	else {
	    query = "select max(cast(cu." + REVISION_COL + " as int)) as "
	    + REVISION_COL + " " + body;
	}

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to find all rows for the
     * ComponentVersionLocation.
     * 
     * @param xContext Application context.
     * @param showLatest If true query for the latest revision only
     * @throws IcofException
     */
    public void setLookupByCompVerLocStatement(EdaContext xContext,
                                               boolean showLatest)
                                               throws IcofException {

	// select cu.code_update_id, cu.svnrevision
	// from tk.location as l,
	// tk.ComponentTkVersion_Location as cvl,
	// tk.Location_ComponentUpdate as lcu,
	// tk.ComponentUpdate as compu,
	// tk.Code_Update as cu
	// where l.name = 'PROD'
	// and cvl.component_tkversion_id = 418
	// and l.Location_id = cvl.Location_id
	// and cvl.component_tkversion_id = cu.Component_TkVersion_id
	// and lcu.componentTkVersion_Location_id =
	// cvl.ComponentTkVersion_Location_id
	// and compu.ComponentUpdate_id = lcu.ComponentUpdate_id
	// and compu.ComponentUpdate_Id = cu.ComponentUpdate_id
	// order by cast(cu.Code_Update_id as int) asc

	String body = " from " + Location_Db.TABLE_NAME + " as l, "
	+ Component_Version_Location_Db.TABLE_NAME + " as cvl, "
	+ Location_ComponentUpdate_Db.TABLE_NAME + " as lcu, "
	+ ComponentUpdate_Db.TABLE_NAME + " as compu, "
	+ CodeUpdate_Db.TABLE_NAME + " as cu ";
	body += " where l." + Location_Db.NAME_COL + " = ?" + " and cvl."
	+ Component_Version_Location_Db.COMPONENT_VERSION_ID_COL
	+ " = ? " + " and l." + Location_Db.ID_COL + " = cvl."
	+ Component_Version_Location_Db.LOCATION_ID_COL + " and cvl."
	+ Component_Version_Location_Db.COMPONENT_VERSION_ID_COL
	+ " = cu." + COMP_VERSION_ID_COL + " and lcu."
	+ Location_ComponentUpdate_Db.COMPONENT_VERSION_LOCATION_ID_COL
	+ " = cvl." + Component_Version_Location_Db.ID_COL
	+ " and compu." + ComponentUpdate_Db.ID_COL + " = lcu."
	+ Location_ComponentUpdate_Db.COMPONENT_UPDATE_ID_COL
	+ " and compu." + ComponentUpdate_Db.ID_COL + " = cu."
	+ COMPONENT_UPDATE_ID_COL;

	String query;
	if (!showLatest) {
	    query = "select cu." + REVISION_COL + ", cu." + ID_COL + body
	    + " order by cast(" + REVISION_COL + " as int) asc";
	    ;
	}
	else {
	    query = "select max(cast(cu." + REVISION_COL + " as int)) as "
	    + REVISION_COL + " " + body;
	}

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to find all rows for the
     * ComponentVersionLocation.
     * 
     * @param xContext Application context.
     * @param showLatest If true query for the latest revision only
     * @throws IcofException
     */
    public void setLookupByCompVerLocStatement_old(EdaContext xContext,
                                                   boolean showLatest)
                                                   throws IcofException {

	String filter = " from "
	+ TABLE_NAME
	+ " as cu, "
	+ Location_ComponentUpdate_Db.TABLE_NAME
	+ " as lcu"
	+ " where lcu."
	+ Location_ComponentUpdate_Db.COMPONENT_VERSION_LOCATION_ID_COL
	+ " = ? " + " and lcu."
	+ Location_ComponentUpdate_Db.COMPONENT_UPDATE_ID_COL
	+ " = cu." + COMPONENT_UPDATE_ID_COL + " and ("
	+ BRANCH_COL + " = 'trunk' " + " or " + BRANCH_COL
	+ " like 'tk1%' )";

	String query = "";
	if (!showLatest) {
	    query += "select cu." + CodeUpdate_Db.REVISION_COL + filter
	    + " order by cast(" + REVISION_COL + " as int) asc";
	    ;
	}
	else {
	    query += "select max(cast(cu." + CodeUpdate_Db.REVISION_COL
	    + " as int)) as " + REVISION_COL + " " + filter;
	}

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup this object by id.
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setUpdateCurrStatusStatement(EdaContext xContext)
    throws IcofException {

	// Define the query.
	String query = "update " + TABLE_NAME + " set " + CURRENT_STATUS_ID_COL
	+ " = ?, " + UPDATED_BY_COL + " = ?, " + UPDATED_ON_COL
	+ " = ? " + " where " + ID_COL + " = ? ";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup objects that are missing
     * create/update audit data.
     * 
     * @param xContext Application context.
     * @param xCompVer ComponentTkVersion filter (maybe null)
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setLookupNeedsAuditStatement(EdaContext xContext,
                                             Component_Version_Db xCompVer)
                                             throws IcofException {

	// Define the query.
	String query = "select " + ID_COL + " from " + TABLE_NAME + " where ("
	+ CREATED_BY_COL + " is null " + " or " + CREATED_ON_COL
	+ " is null " + " or " + UPDATED_BY_COL + " is null "
	+ " or " + UPDATED_ON_COL + " is null) ";

	// Set the Component filter if specified
	if (xCompVer != null) {
	    query += " and " + COMP_VERSION_ID_COL + " = ? ";
	}

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to update the ComponentUpdate and ExtractedOn
     * data.
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setUpdateExtractDataStatement(EdaContext xContext)
    throws IcofException {

	// Define the query.
	String query = "update " + TABLE_NAME + " set "
	+ COMPONENT_UPDATE_ID_COL + " = ?, " + EXTRACTED_TMS_COL
	+ " = ?, " + UPDATED_BY_COL + " = ?, " + UPDATED_ON_COL
	+ " = ? " + " where " + ID_COL + " = ? ";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to update the Audit data.
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setUpdateAuditDataStatement(EdaContext xContext)
    throws IcofException {

	// Define the query.
	String query = "update " + TABLE_NAME + " set " + CREATED_BY_COL
	+ " = ?, " + CREATED_ON_COL + " = ?, " + UPDATED_BY_COL
	+ " = ?, " + UPDATED_ON_COL + " = ? " + " where "
	+ ID_COL + " = ? ";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to add a row.
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setAddRowStatement(EdaContext xContext)
    throws IcofException {

	// Define the query.
	String query = "insert into " + TABLE_NAME + " ( " + ID_COL + ","
	+ COMP_VERSION_ID_COL + ", " + REVISION_COL + ", "
	+ BRANCH_COL + ", " + CREATED_BY_COL + ", "
	+ CREATED_ON_COL + ", " + UPDATED_BY_COL + ", "
	+ UPDATED_ON_COL + ", " + DESCRIPTION_COL + " ) "
	+ " values( ?, ?, ?, ?, ?, ?, ?, ?, ? )";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup the next id for this table.
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setNextIdStatement(EdaContext xContext)
    throws IcofException {

	// Define the query.
	String query = TkAudit.getNextIdQuery(xContext, TABLE_NAME, ID_COL);

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Look up this object by id.
     * 
     * @param xContext An application context object.
     * @throws Trouble querying the database.
     */
    public void dbLookupById(EdaContext xContext)
    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setLookupIdStatement(xContext);

	try {
	    getStatement().setLong(1, getId());
	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
	                                         this.getClass().getName(),
	                                         "dbLookupById()",
	                                         IcofException.SEVERE,
	                                         "Unable to prepare SQL statement.",
	                                         IcofException.printStackTraceAsString(trap)
	                                         + "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	if (!selectSingleRow(xContext)) {
	    IcofException ie = new IcofException(
	                                         this.getClass().getName(),
	                                         "dbLookupById()",
	                                         IcofException.SEVERE,
	                                         "Unable to find row for query.\n",
	                                         "QUERY: " + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;

	}

	// Close the PreparedStatement.
	closeStatement(xContext);

    }


    /**
     * Look up this object by Component_Version and Revision
     * 
     * @param xContext An application context object
     * @param ComponentVersion object
     * @param aRevision to lookup
     * @throws Trouble querying the database.
     */
    public boolean dbLookupByCompRev(EdaContext xContext, 
                                  Component_Version_Db aCompVer,
                                  String aRevision)
                                  throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setLookupCompRevStatement(xContext, true);

	try {
	    getStatement().setLong(1, aCompVer.getId());
	    getStatement().setString(2, aRevision);


	    // TODO -- debug
//	    printQuery(xContext);
//	    System.out.println("CompVer: " + aCompVer.getId());
//	    System.out.println("Rev : " + aRevision);


	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
	                                         this.getClass().getName(),
	                                         "dbLookupByCompRev()",
	                                         IcofException.SEVERE,
	                                         "Unable to prepare SQL statement.",
	                                         IcofException.printStackTraceAsString(trap)
	                                         + "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	ResultSet rs = executeQuery(xContext);
		
	// Process the results
	boolean bFound = false;
	try {
	    while (rs.next() && ! bFound) {
		id = rs.getLong(ID_COL);
		dbLookupById(xContext);
		bFound = true;
	    }

	}
	catch (SQLException ex) {
	    throw new IcofException(this.getClass().getName(),
	                            "dbLookupByCompRev()",
	                            IcofException.SEVERE,
	                            "Error reading DB query results.",
	                            ex.getMessage());
	}


	// Close the PreparedStatement.
	closeStatement(xContext);

	return bFound;
	
    }

    
    /**
     * Look up this object by Component_Version and Revision
     * 
     * @param xContext An application context object
     * @param ComponentVersion object
     * @param aRevision to lookup
     * @throws Trouble querying the database.
     */
    public boolean dbLookupByCompRev(EdaContext xContext,
                                     Release_Db aRel,
                                     Component_Db aComp,
                                     String aRevision)
                                     throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setLookupCompRevStatement(xContext, false);

	try {
	    getStatement().setShort(1, aComp.getId());
	    getStatement().setShort(2, aRel.getId());
	    getStatement().setString(3, aRevision);


	    // TODO -- debug
	    printQuery(xContext);
	    System.out.println("Component id: " + aComp.getId());
	    System.out.println("Release id  : " + aRel.getId());
	    System.out.println("Revision    : " + aRevision);


	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
	                                         this.getClass().getName(),
	                                         "dbLookupByCompRev()",
	                                         IcofException.SEVERE,
	                                         "Unable to prepare SQL statement.",
	                                         IcofException.printStackTraceAsString(trap)
	                                         + "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	ResultSet rs = executeQuery(xContext);
		
	// Process the results
	boolean bFound = false;
	try {
	    while (rs.next() && ! bFound) {
		id = rs.getLong(ID_COL);
		dbLookupById(xContext);
		bFound = true;
	    }

	}
	catch (SQLException ex) {
	    throw new IcofException(this.getClass().getName(),
	                            "dbLookupByCompRev()",
	                            IcofException.SEVERE,
	                            "Error reading DB query results.",
	                            ex.getMessage());
	}


	// Close the PreparedStatement.
	closeStatement(xContext);

	return bFound;
	
    }

    
    /**
     * Look up this object by Tool Kit
     * 
     * @param xContext An application context object
     * @param Component object
     * @param aRevision to lookup
     * @throws Trouble querying the database.
     */
    public Vector<CodeUpdate_Db> dbLookupByToolKitComp(EdaContext xContext, 
                                                       Component_Version_Db aCompVer, 
                                                       boolean bNotExtracted)
                                                       throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setLookupByToolKitCompStatement(xContext, bNotExtracted);


	try {
	    getStatement().setLong(1, aCompVer.getId());

	    // TODO -- debug
	    printQuery(xContext);
	    System.out.println("CompVer: " + aCompVer.getId());


	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
	                                         this.getClass().getName(),
	                                         "dbLookupByCompRev()",
	                                         IcofException.SEVERE,
	                                         "Unable to prepare SQL statement.",
	                                         IcofException.printStackTraceAsString(trap)
	                                         + "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	ResultSet rs = executeQuery(xContext);

	Vector<CodeUpdate_Db> codeUpdates = new Vector<CodeUpdate_Db>();
	try {
	    while (rs.next()) {
		long anId = rs.getLong(ID_COL);
		CodeUpdate_Db cu = new CodeUpdate_Db(anId);
		cu.dbLookupById(xContext);

		codeUpdates.add(cu);
	    }

	}
	catch (SQLException ex) {
	    throw new IcofException(this.getClass().getName(),
	                            "dbLookupByToolKitComp()",
	                            IcofException.SEVERE,
	                            "Error reading DB query results.",
	                            ex.getMessage());
	}



	// Close the PreparedStatement.
	closeStatement(xContext);

	return codeUpdates;

    }


    /**
     * Create a list of CodeUpdate objects for this ComponentVersion with a null
     * extracted_on time stamp.
     * 
     * @param xContext An application context object.
     * @return Collection of CodeUpdate objects
     * @throws Trouble querying the database.
     */
    public Vector<CodeUpdate_Db> dbLookupNotExtracted(EdaContext xContext)
    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setLookupNotExtractedStatement(xContext);

	try {
	    getStatement().setLong(1, getCompVersion().getId());
	    
	    printQuery(xContext);
	    System.out.println("Comp version id: " + getCompVersion().getId());
	    
	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
	                                         this.getClass().getName(),
	                                         "dbLookupNotExtracted()",
	                                         IcofException.SEVERE,
	                                         "Unable to prepare SQL statement.",
	                                         IcofException.printStackTraceAsString(trap)
	                                         + "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	ResultSet rs = executeQuery(xContext);

	// Process the results
	Vector<CodeUpdate_Db> codeUpdates = new Vector<CodeUpdate_Db>();
	try {
	    while (rs.next()) {
		long anId = rs.getLong(ID_COL);
		CodeUpdate_Db cu = new CodeUpdate_Db(anId);
		cu.dbLookupById(xContext);

		codeUpdates.add(cu);
	    }

	}
	catch (SQLException ex) {
	    throw new IcofException(this.getClass().getName(),
	                            "dbLookupNotExtracted()",
	                            IcofException.SEVERE,
	                            "Error reading DB query results.",
	                            ex.getMessage());
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	return codeUpdates;

    }


    /**
     * Create a list of CodeUpdate objects for this ComponentVersion which are
     * missing their create/update audit data
     * 
     * @param xContext An application context object.
     * @param xCompVer A ComponentTkVersion object (maybe null)
     * @return Collection of CodeUpdate objects
     * @throws Trouble querying the database.
     */
    public Hashtable<String, CodeUpdate_Db> dbLookupNeedsAuditInfo(EdaContext xContext,
                                                                   Component_Version_Db xCompVer)
                                                                   throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setLookupNeedsAuditStatement(xContext, xCompVer);

	try {
	    if (xCompVer != null) {
		getStatement().setLong(1, xCompVer.getId());
	    }
	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
	                                         this.getClass().getName(),
	                                         "dbLookupNeedsAuditInfo",
	                                         IcofException.SEVERE,
	                                         "Unable to prepare SQL statement.",
	                                         IcofException.printStackTraceAsString(trap)
	                                         + "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	ResultSet rs = executeQuery(xContext);

	// Process the results
	Hashtable<String, CodeUpdate_Db> codeUpdates = new Hashtable<String, CodeUpdate_Db>();
	try {
	    while (rs.next()) {
		long anId = rs.getLong(ID_COL);
		// System.out.println(" ID: " + anId);
		CodeUpdate_Db cu = new CodeUpdate_Db(anId);
		cu.dbLookupById(xContext);

		codeUpdates.put(cu.getIdKey(xContext), cu);
	    }

	}
	catch (SQLException ex) {
	    throw new IcofException(this.getClass().getName(),
	                            "dbLookupNeedsAuditInfo()",
	                            IcofException.SEVERE,
	                            "Error reading DB query results.",
	                            ex.getMessage());
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	return codeUpdates;

    }


    /**
     * Create a list of CodeUpdate objects for this ComponentUpdate.
     * 
     * @param xContext An application context object.
     * @return Collection of CodeUpdate objects
     * @throws IcofException
     * @throws Trouble querying the database.
     */
    public Vector<CodeUpdate_Db> dbLookupByCompUpdate(EdaContext xContext)
    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setLookupByCompUpdateStatement(xContext);

	try {
	    getStatement().setLong(1, getComponentUpdate().getId());
	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
	                                         this.getClass().getName(),
	                                         "dbLookupByCompUpdate()",
	                                         IcofException.SEVERE,
	                                         "Unable to prepare SQL statement.",
	                                         IcofException.printStackTraceAsString(trap)
	                                         + "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	ResultSet rs = executeQuery(xContext);

	// Process the results
	Vector<CodeUpdate_Db> codeUpdates = new Vector<CodeUpdate_Db>();
	try {
	    while (rs.next()) {
		long anId = rs.getLong(ID_COL);
		CodeUpdate_Db cu = new CodeUpdate_Db(anId);
		cu.dbLookupById(xContext);

		codeUpdates.add(cu);
	    }

	}
	catch (SQLException ex) {
	    throw new IcofException(this.getClass().getName(),
	                            "dbLookupByCompUpdate()",
	                            IcofException.SEVERE,
	                            "Error reading DB query results.",
	                            ex.getMessage());
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	return codeUpdates;

    }


    /**
     * Create a list of SvnRevisions for this Component, version and location.
     * 
     * @param xContext An application context object.
     * @param tk A RelVersion_Db object
     * @param compVerLoc Component_Versions_Location_Db object.
     * @param showLatest If true get on the latest revision otherwise get all
     *            revisions
     * @return Collection of Strings
     * @throws IcofException Trouble querying the database.
     */
    public Vector<String> dbLookupRevsByCompVerLoc(EdaContext xContext,
                                                   RelVersion_Db tk,
                                                   Component_Db comp,
                                                   Location_Db loc,
                                                   boolean showLatest)
                                                   throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setLookupByCompVerLocStatement(xContext, tk, showLatest);

	try {

	    int id = 1;
	    if (tk != null) {
		getStatement().setShort(id++, tk.getId());
	    }
	    getStatement().setShort(id++, comp.getId());
	    getStatement().setString(id++, loc.getName());


	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
	                                         this.getClass().getName(),
	                                         "dbLookupRevsByCompVerLoc()",
	                                         IcofException.SEVERE,
	                                         "Unable to prepare SQL statement.",
	                                         IcofException.printStackTraceAsString(trap)
	                                         + "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	ResultSet rs = executeQuery(xContext);

	// Process the results
	Vector<String> revisions = new Vector<String>();
	try {
	    while (rs.next()) {
		String rev = rs.getString(REVISION_COL);
		if (rev != null) {
		    revisions.add(rev);
		}
	    }

	}
	catch (SQLException ex) {
	    throw new IcofException(this.getClass().getName(),
	                            "dbLookupRevsByCompVerLoc()",
	                            IcofException.SEVERE,
	                            "Error reading DB query results.",
	                            ex.getMessage());
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	return revisions;

    }


    /**
     * Create a list of CodeUpdate IDs for this Component_TkVersion and
     * location.
     * 
     * @param xContext An application context object.
     * @param compVer Component_Version_Db object.
     * @param loc Location object.
     * @return Collection of Strings (Ids)
     * @throws IcofException Trouble querying the database.
     */
    public Vector<String> dbLookupIdsByCompVerLoc(EdaContext xContext,
                                                  Component_Version_Db compVer,
                                                  Location_Db loc)
                                                  throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setLookupByCompVerLocStatement(xContext, false);

	try {
	    getStatement().setString(1, loc.getName());
	    getStatement().setLong(2, compVer.getId());
	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
	                                         this.getClass().getName(),
	                                         "dbLookupIdsByCompVerLoc()",
	                                         IcofException.SEVERE,
	                                         "Unable to prepare SQL statement.",
	                                         IcofException.printStackTraceAsString(trap)
	                                         + "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	ResultSet rs = executeQuery(xContext);

	// Process the results
	Vector<String> revisions = new Vector<String>();
	try {
	    while (rs.next()) {
		String rev = rs.getString(ID_COL);
		if (rev != null) {
		    revisions.add(rev);
		}
	    }

	}
	catch (SQLException ex) {
	    throw new IcofException(this.getClass().getName(),
	                            "dbLookupIdsByCompVerLoc()",
	                            IcofException.SEVERE,
	                            "Error reading DB query results.",
	                            ex.getMessage());
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	return revisions;

    }


    /**
     * Create a list of SvnRevisions for this Component, version and location.
     * 
     * @param xContext An application context object.
     * @param compVerLoc Component_Versions_Location_Db object.
     * @param showLatest If true get on the latest revision otherwise get all
     *            revisions
     * @return Collection of Strings
     * @throws IcofException Trouble querying the database.
     */
    public Vector<String> dbLookupRevsByCompVerLoc_old(EdaContext xContext,
                                                       Component_Version_Location_Db compVerLoc,
                                                       boolean showLatest)
                                                       throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setLookupByCompVerLocStatement_old(xContext, showLatest);

	try {
	    getStatement().setLong(1, compVerLoc.getId());
	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
	                                         this.getClass().getName(),
	                                         "dbLookupRevsByCompVerLoc()",
	                                         IcofException.SEVERE,
	                                         "Unable to prepare SQL statement.",
	                                         IcofException.printStackTraceAsString(trap)
	                                         + "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	ResultSet rs = executeQuery(xContext);

	// Process the results
	Vector<String> revisions = new Vector<String>();
	try {
	    while (rs.next()) {
		String rev = rs.getString(REVISION_COL);
		revisions.add(rev);
	    }

	}
	catch (SQLException ex) {
	    throw new IcofException(this.getClass().getName(),
	                            "dbLookupRevsByCompVerLoc()",
	                            IcofException.SEVERE,
	                            "Error reading DB query results.",
	                            ex.getMessage());
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	return revisions;

    }


    /**
     * Sets this object to the latest extracted CodeUpdate object for this
     * ComponentVersion.
     * 
     * @param xContext An application context object.
     * @throws Trouble querying the database.
     */
    public Vector<CodeUpdate_Db> dbLookupExtractedLast(EdaContext xContext)
    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setLookupExtractedLastStatement(xContext);

	try {
	    getStatement().setLong(1, getCompVersion().getId());
	    getStatement().setLong(2, getCompVersion().getId());
	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
	                                         this.getClass().getName(),
	                                         "dbLookupExtractedLast()",
	                                         IcofException.SEVERE,
	                                         "Unable to prepare SQL statement.",
	                                         IcofException.printStackTraceAsString(trap)
	                                         + "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	ResultSet rs = executeQuery(xContext);

	// Process the results
	Vector<CodeUpdate_Db> codeUpdates = new Vector<CodeUpdate_Db>();
	try {
	    while (rs.next()) {
		long anId = rs.getLong(ID_COL);
		CodeUpdate_Db cu = new CodeUpdate_Db(anId);
		cu.dbLookupById(xContext);

		codeUpdates.add(cu);
	    }

	}
	catch (SQLException ex) {
	    throw new IcofException(this.getClass().getName(),
	                            "dbLookupExtractedLast()",
	                            IcofException.SEVERE,
	                            "Error reading DB query results.",
	                            ex.getMessage());
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	return codeUpdates;

    }


    /**
     * Lookup extracted CodeUpdate objects starting with the specified revision.
     * 
     * @param xContext An application context object.
     * @param sRevision A revision.
     * @throws IcofException
     * @throws Trouble querying the database.
     */
    public Vector<CodeUpdate_Db> dbLookupExtractedSince(EdaContext xContext,
                                                        String revision)
                                                        throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setLookupExtractedSinceStatement(xContext);

	try {
	    getStatement().setString(1, revision);
	    getStatement().setLong(2, getCompVersion().getId());
	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
	                                         this.getClass().getName(),
	                                         "dbLookupExtractedSince()",
	                                         IcofException.SEVERE,
	                                         "Unable to prepare SQL statement.",
	                                         IcofException.printStackTraceAsString(trap)
	                                         + "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	ResultSet rs = executeQuery(xContext);

	// Process the results
	Vector<CodeUpdate_Db> codeUpdates = new Vector<CodeUpdate_Db>();
	try {
	    while (rs.next()) {
		long anId = rs.getLong(ID_COL);
		CodeUpdate_Db cu = new CodeUpdate_Db(anId);
		cu.dbLookupById(xContext);
		cu.setCreatedOn(rs.getTimestamp(EXTRACTED_TMS_COL));

		codeUpdates.add(cu);
	    }

	}
	catch (SQLException ex) {
	    throw new IcofException(this.getClass().getName(),
	                            "dbLookupExtractedSince()",
	                            IcofException.SEVERE,
	                            "Error reading DB query results.",
	                            ex.getMessage());
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	return codeUpdates;

    }


    /**
     * Update the extract data for this object.
     * 
     * @param xContext An application context object.
     * @param aCompUpdate A ComponentUpdate object.
     * @param anExtractTms The Extracted Timestamp.
     * @param editor User making this update.
     * 
     * @throws Trouble querying the database.
     */
    public void dbUpdateExtractData(EdaContext xContext,
                                    ComponentUpdate_Db aCompUpdate,
                                    Timestamp anExtractTms, User_Db editor)
                                    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setUpdateExtractDataStatement(xContext);
	Timestamp now = new Timestamp(new java.util.Date().getTime());

	try {
	    getStatement().setLong(1, aCompUpdate.getId());
	    getStatement().setTimestamp(2, anExtractTms);
	    getStatement().setString(3, editor.getIntranetId());
	    getStatement().setTimestamp(4, now);
	    getStatement().setLong(5, getId());

	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
	                                         this.getClass().getName(),
	                                         "dbUpdateExtractData()",
	                                         IcofException.SEVERE,
	                                         "Unable to prepare SQL statement.",
	                                         IcofException.printStackTraceAsString(trap)
	                                         + "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	if (!insertRow(xContext)) {
	    IcofException ie = new IcofException(this.getClass().getName(),
	                                         "dbUpdateExtractData()",
	                                         IcofException.SEVERE,
	                                         "Unable to insert new row.\n",
	                                         "QUERY: " + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	// Set the extract data on this object.
	setComponentUpdate(aCompUpdate);
	setExtractedOn(anExtractTms);
	setLoadFromDb(true);

    }


    /**
     * Update the create/update audit data for this object.
     * 
     * @param xContext An application context object.
     * @param sCreatedBy Intranet id of person who created this object
     * @param xCreatedOn Creation timestamp
     * @param sUpdatedBy Intranet id of person who created this object
     * @param xUpdatedOn Update timestamp
     * 
     * @throws Trouble querying the database.
     */
    public void dbUpdateAuditData(EdaContext xContext, String sCreatedBy,
                                  Timestamp xCreatedOn, String sUpdatedBy,
                                  Timestamp xUpdatedOn)
                                  throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setUpdateAuditDataStatement(xContext);

	try {
	    getStatement().setString(1, sCreatedBy);
	    getStatement().setTimestamp(2, xCreatedOn);
	    getStatement().setString(3, sUpdatedBy);
	    getStatement().setTimestamp(4, xUpdatedOn);
	    getStatement().setLong(5, getId());

	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
	                                         this.getClass().getName(),
	                                         "dbUpdateAuditData()",
	                                         IcofException.SEVERE,
	                                         "Unable to prepare SQL statement.",
	                                         IcofException.printStackTraceAsString(trap)
	                                         + "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	if (!insertRow(xContext)) {
	    IcofException ie = new IcofException(this.getClass().getName(),
	                                         "dbUpdateAuditData()",
	                                         IcofException.SEVERE,
	                                         "Unable to insert new row.\n",
	                                         "QUERY: " + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	// Set the extract data on this object.
	setCreatedBy(sCreatedBy);
	setCreatedOn(xCreatedOn);
	setUpdatedBy(sUpdatedBy);
	setUpdatedOn(xUpdatedOn);
	setLoadFromDb(true);

    }


    /**
     * Insert a new row.
     * 
     * @param xContext An application context object.
     * @throws Trouble querying the database.
     */
    public void dbAddRow(EdaContext xContext)
    throws IcofException {

	// Get the next id for this new row.
	setNextIdStatement(xContext);
	long id = getNextBigIntId(xContext);
	closeStatement(xContext);

	// Create the SQL query in the PreparedStatement.
	setAddRowStatement(xContext);
	try {
	    getStatement().setLong(1, id);
	    getStatement().setLong(2, getCompVersion().getId());
	    getStatement().setString(3, getRevision());
	    getStatement().setString(4, getBranch());
	    getStatement().setString(5, getCreatedBy());
	    getStatement().setTimestamp(6, getCreatedOn());
	    if (getUpdatedBy() != null)
		getStatement().setString(7, getUpdatedBy());
	    else
		getStatement().setString(7, getCreatedBy());
	    if (getUpdatedOn() != null)
		getStatement().setTimestamp(8, getUpdatedOn());
	    else
		getStatement().setTimestamp(8, getCreatedOn());
	    getStatement().setString(9,
	                             formatForInsert(xContext, getDescription()));

	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
	                                         this.getClass().getName(),
	                                         "dbAddRow()",
	                                         IcofException.SEVERE,
	                                         "Unable to prepare SQL statement.",
	                                         IcofException.printStackTraceAsString(trap)
	                                         + "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}


	// Run the query.
	if (!insertRow(xContext)) {
	    IcofException ie = new IcofException(this.getClass().getName(),
	                                         "dbAddRow()",
	                                         IcofException.SEVERE,
	                                         "Unable to insert new row.\n",
	                                         "QUERY: " + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	// Load the data for the new row.
	setId(id);
	dbLookupById(xContext);

    }


    /**
     * Populate this object from the result set.
     * 
     * @param xContext Application context.
     * @param rs A valid result set.
     * @throws IcofException
     * @throws IcofException
     * @throws Trouble retrieving the data.
     */
    protected void populate(EdaContext xContext, ResultSet rs)
    throws SQLException, IcofException {

	setId(rs.getInt(ID_COL));
	setRevision(rs.getString(REVISION_COL));
	setBranch(rs.getString(BRANCH_COL));
	setDescription(rs.getString(DESCRIPTION_COL));
	setCompVersion(xContext, rs.getShort(COMP_VERSION_ID_COL));
	long cuId = rs.getLong(COMPONENT_UPDATE_ID_COL);
	if (cuId != 0) {
	    setComponentUpdate(xContext, cuId);
	}
	else {
	    setComponentUpdate(null);
	}
	setExtractedOn(rs.getTimestamp(EXTRACTED_TMS_COL));

	setCreatedBy(rs.getString(CREATED_BY_COL));
	setCreatedOn(rs.getTimestamp(CREATED_ON_COL));
	setUpdatedBy(rs.getString(UPDATED_BY_COL));
	setUpdatedOn(rs.getTimestamp(UPDATED_ON_COL));
	setLoadFromDb(true);

    }


    /**
     * Return the members as a string.
     */
    public String toString(EdaContext xContext) {

	// Get the class specific data
	StringBuffer buffer = new StringBuffer();
	buffer.append("ID: " + getId() + "\n");
	
	if (getCompVersion() != null)
	    buffer.append("Component Version ID: " + getCompVersion().getId()
	                  + "\n");
	else
	    buffer.append("Component Version ID: NULL\n");

	buffer.append("Revision: " + getRevision() + "\n");
	buffer.append("Branch: " + getBranch() + "\n");
	buffer.append("Description: " + getDescription() + "\n");

	if (getExtractedOn() != null)
	    buffer.append("Extracted on: " + getExtractedOn() + "\n");
	else
	    buffer.append("Extracted on: NULL\n");

	if (getComponentUpdate() != null)
	    buffer.append("ComponentUpdate ID: " + getComponentUpdate().getId()
	                  + "\n");
	else
	    buffer.append("ComponentUpdate ID: NULL\n");

	buffer.append("Created by; " + getCreatedBy() + "\n");
	buffer.append("Created on; " + getCreatedOn() + "\n");
	buffer.append("Updated by; " + getUpdatedBy() + "\n");
	buffer.append("Updated on; " + getUpdatedOn() + "\n");

	return buffer.toString();

    }


    /**
     * Get a key from the ID.
     * 
     * @param xContext Application context.
     */
    public String getIdKey(EdaContext xContext) {

	return String.valueOf(getId());

    }


    /**
     * Sets this object to the latest extracted CodeUpdate object for this
     * ComponentVersion.
     * 
     * @param xContext An application context object.
     * @throws Trouble querying the database.
     */
    public Vector<CodeUpdate_Db> dbLookupCreatedBy(EdaContext xContext,
                                                   String createdBy)
                                                   throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setLookupExtractedCreatedBy(xContext);

	try {
	    getStatement().setString(1, createdBy);
	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
	                                         this.getClass().getName(),
	                                         "dbLookupExtractedLast()",
	                                         IcofException.SEVERE,
	                                         "Unable to prepare SQL statement.",
	                                         IcofException.printStackTraceAsString(trap)
	                                         + "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	ResultSet rs = executeQuery(xContext);

	// Process the results
	Vector<CodeUpdate_Db> codeUpdates = new Vector<CodeUpdate_Db>();
	try {
	    while (rs.next()) {
		long anId = rs.getLong(ID_COL);

		CodeUpdate_Db cu = new CodeUpdate_Db(anId);
		cu.setCreatedOn(rs.getTimestamp(CREATED_ON_COL));
		codeUpdates.add(cu);
	    }

	}
	catch (SQLException ex) {
	    throw new IcofException(this.getClass().getName(),
	                            "dbLookupExtractedLast()",
	                            IcofException.SEVERE,
	                            "Error reading DB query results.",
	                            ex.getMessage());
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	return codeUpdates;

    }


    /**
     * Create a PreparedStatement to lookup the last extracted row.
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setLookupExtractedCreatedBy(EdaContext xContext)
    throws IcofException {

	// Define the query.
	String query = "select " + ID_COL + "," + CREATED_ON_COL + "  from "
	+ TABLE_NAME + " where " + CREATED_BY_COL + " like ? ";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Update the create/update audit data for this object.
     * 
     * @param xContext An application context object.
     * @param sCreatedBy Intranet id of person who created this object
     * @param xCreatedOn Creation timestamp
     * @param sUpdatedBy Intranet id of person who created this object
     * @param xUpdatedOn Update timestamp
     * 
     * @throws Trouble querying the database.
     */
    public void dbUpdateCreatedByData(EdaContext xContext, String sCreatedBy,
                                      long dbID)
                                      throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setUpdateCreatedByStatement(xContext, sCreatedBy, dbID);

	// try {
	// getStatement().setString(1, sCreatedBy);
	// getStatement().setLong(2, dbID);
	// }
	// catch(SQLException trap) {
	// IcofException ie = new IcofException(this.getClass() .getName(),
	// "dbCreatedByData()",
	// IcofException.SEVERE,
	// "Unable to prepare SQL statement.",
	// IcofException.printStackTraceAsString(trap) +
	// "\n" + getQuery());
	// xContext.getSessionLog().log(ie);
	// throw ie;
	// }

	// Run the query.
	if (!insertRow(xContext)) {
	    IcofException ie = new IcofException(this.getClass().getName(),
	                                         "dbCreatedByData()",
	                                         IcofException.SEVERE,
	                                         "Unable to insert new row.\n",
	                                         "QUERY: " + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	System.out.println(getQuery());

    }


    /**
     * Create a PreparedStatement to update the Audit data.
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setUpdateCreatedByStatement(EdaContext xContext,
                                            String sCreatedBy, long dbID)
                                            throws IcofException {

	// Define the query.
	String query = "update " + TABLE_NAME + " set " + CREATED_BY_COL
	+ " = '" + sCreatedBy + "'  where " + ID_COL + " =  '"
	+ dbID + "'";

	// Set and prepare the query and statement.

	setQuery(xContext, query);

    }


}
