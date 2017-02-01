/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2010 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 * FILE: ChangeRequest_Db.java
 *
 *-PURPOSE---------------------------------------------------------------------
 * ChangeRequest DB class with audit info
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 03/08/2011 GFS  Initial coding.
 * 03/23/2011 GFS  Added dbAddRow(EdaContext, User_Db, boolean).
 * 04/20/2011 GFS  Added dbLookupUpdated().
 * 05/18/2011 GFS  Updated to use base class query and statement members.
 * 06/24/2011 GFS  Reworked dbAdd with autoGen.
 * 09/06/2011 GFS  Added support for Change Request type and severity.
 * 01/11/2012 GFS  Updated dbLookupByCompVerStatus() to limit query by max
 *                 revision.
 * 01/16/2012 GFS  Updated dbLookupByCompVerStatus() to limit query by min 
 *                 revision.
 * 03/06/2012 GFS  Updated SQL statements to use DB time instead of time 
 *                 set from client machine.
 * 03/07/2012 GFS  Fixed bug in setAddRowStatement().
 * 11/29/2012 GFS  Added support for new CUSOMTER_IMPACTED column.
 * 01/23/2013 GFS  Updated getUpdated query to exclude deleted records.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.tk_etreedb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Hashtable;
import java.util.TreeMap;
import java.util.Vector;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAudit;
import com.ibm.stg.iipmds.common.IcofException;

public class ChangeRequest_Db extends TkAudit {

    /**
	 * 
	 */
    private static final long serialVersionUID = 6645768894586172268L;
    /**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.CHANGEREQUEST";
    public static final String ID_COL = "CHANGEREQUEST_ID";
    public static final String DESC_COL = "DESCRIPTION";
    public static final String CQ_COL = "CLEARQUEST_ID";
    public static final String STATUS_ID_COL = "CHANGEREQUEST_STATUS_ID";
    public static final String TYPE_ID_COL = "CHANGEREQUEST_TYPE_ID";
    public static final String SEVERITY_ID_COL = "CHANGEREQUEST_SEVERITY_ID";
    public static final String CUSTOMER_IMPACTED_COL = "CUSTOMER_IMPACTED";
    public static final String COMP_PKG_ID_COL = "COMPONENTPACKAGE_ID";
    public static final String ALL_COLS = ID_COL + "," + DESC_COL + ","
					  + CQ_COL + "," + STATUS_ID_COL + ","
					  + TYPE_ID_COL + ", "
					  + SEVERITY_ID_COL + ","
					  + CUSTOMER_IMPACTED_COL + ","
					  + COMP_PKG_ID_COL + ","
					  + CREATED_BY_COL + ","
					  + CREATED_ON_COL + ","
					  + UPDATED_BY_COL + ","
					  + UPDATED_ON_COL + ","
					  + DELETED_BY_COL + ","
					  + DELETED_ON_COL;


    /**
     * Constructor - takes a DB id
     * 
     * @param anId A database id
     */
    public ChangeRequest_Db(long anId) {

	setId(anId);
    }


    /**
     * Constructor - takes a ClearQuest name
     * 
     * @param aCqName ClearQuest name
     */
    public ChangeRequest_Db(String aCqName) {

	setCqName(aCqName);
    }


    /**
     * Constructor - used to create row
     * 
     * @param aCqName ClearQuest name
     * @param aDescription Change description
     * @param aStatus id ChangeRequestStatus_Db row id
     * @param aType Change request type
     * @param aSeverity Change request severity
     * @param aCustomer Customer impacted by change request
     */
    public ChangeRequest_Db(String aCqName, String aDescricption,
			    ChangeRequestStatus_Db aStatus,
			    ChangeRequestType_Db aType,
			    ChangeRequestSeverity_Db aSeverity, String aCustomer) {

	setCqName(aCqName);
	setDescription(aDescricption);
	setStatus(aStatus);
	setType(aType);
	setSeverity(aSeverity);
	setImpactedCustomer(aCustomer);
    }


    /**
     * Constructor - takes all non-audit members
     * 
     * @param anId Database row id
     * @param aCqName ClearQuest name
     * @param aDescription Change description
     * @param aStatus ChangeRequestStatus_Db object
     * @param aType ChangeRequestType_Db object
     * @param aSeverity ChangeRequestSeverity_Db object
     * @param aCustomer Customer impacted by change request
     */
    public ChangeRequest_Db(long anId, String aCqName, String aDescricption,
			    ChangeRequestStatus_Db aStatus,
			    ChangeRequestType_Db aType,
			    ChangeRequestSeverity_Db aSeverity, String aCustomer) {

	setId(anId);
	setCqName(aCqName);
	setDescription(aDescricption);
	setStatus(aStatus);
	setType(aType);
	setSeverity(aSeverity);
	setImpactedCustomer(aCustomer);
    }


    /**
     * Constructor - takes all non-audit members
     * 
     * @param anId Database row id
     * @param aCqName ClearQuest name
     * @param aDescription Change description
     * @param aStatus ChangeRequestStatus_Db object
     * @param aType ChangeRequestType_Db object
     * @param aSeverity ChangeRequestSeverity_Db object
     * @param aCustomer Customer impacted by change request
     * @param cName Creator intranet id
     * @param cTms Create timestamp
     * @param uName Updator intranet id
     * @param uTms Update timestamp
     * @param dName Deletor intranet id
     * @param dTms Delete timestamp
     */
    public ChangeRequest_Db(long anId, String aCqName, String aDescricption,
			    ChangeRequestStatus_Db aStatus,
			    ChangeRequestType_Db aType,
			    ChangeRequestSeverity_Db aSeverity,
			    String aCustomer, String cName, Timestamp cTms,
			    String uName, Timestamp uTms, String dName,
			    Timestamp dTms) {

	setId(anId);
	setCqName(aCqName);
	setDescription(aDescricption);
	setStatus(aStatus);
	setType(aType);
	setSeverity(aSeverity);
	setCreatedBy(cName);
	setCreatedOn(cTms);
	setUpdatedBy(uName);
	setUpdatedOn(uTms);
	setDeletedBy(dName);
	setDeletedOn(dTms);

    }


    /**
     * Data Members
     */
    private long id;
    private String description;
    private String cqName;
    private String impactedCustomer;
    private ChangeRequestStatus_Db status;
    private ChangeRequestType_Db type;
    private ChangeRequestSeverity_Db severity;


    /**
     * Getters
     * @formatter:off
     */
    public long getId() { return id; }
    public String getDescription() { return description; }
    public String getCqName() { return cqName; }
    public String getImpactedCustomer() { return impactedCustomer; }
    public ChangeRequestStatus_Db getStatus() { return status; }
    public ChangeRequestType_Db getType() { return type; }
    public ChangeRequestSeverity_Db getSeverity() { return severity; }


    /**
     * Setters
     */
    private void setId(long anId) { id = anId; }
    private void setDescription(String aDesc) { description = aDesc; }
    private void setCqName(String aName) { cqName = aName; }
    private void setImpactedCustomer(String aName) { impactedCustomer = aName; }
    private void setStatus(ChangeRequestStatus_Db aStatus) { status = aStatus; }
    private void setStatus(EdaContext xContext, short anId) { 
	status = new ChangeRequestStatus_Db(anId);
    }
    private void setType(ChangeRequestType_Db aType) { type = aType; }
    private void setType(EdaContext xContext, short anId) { 
	type = new ChangeRequestType_Db(anId);
    }
    private void setSeverity(ChangeRequestSeverity_Db aSeverity) { severity = aSeverity; }
    private void setSeverity(EdaContext xContext, short anId) { 
	severity = new ChangeRequestSeverity_Db(anId);
    }
    //	 @formatter:on

    
    /**
     * Create a PreparedStatement to lookup this object by id.
     * 
     * @param xContext Application context.
     * @param bGetAll Get all CRs including deleted
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setLookupIdStatement(EdaContext xContext, boolean bGetAll)
    throws IcofException {

	// Define the query.
	String query = "select " + ALL_COLS + " from " + TABLE_NAME + " where "
		       + ID_COL + " =  ? ";

	if (!bGetAll) {
	    query += " AND " + DELETED_ON_COL + " is NULL";
	}

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup this object by ClearQuest name.
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setLookupNameStatement(EdaContext xContext)
    throws IcofException {

	// Define the query.
	String query = "select " + ALL_COLS + " from " + TABLE_NAME + " where "
		       + CQ_COL + " =  ? " + " AND " + DELETED_ON_COL
		       + " is NULL";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup the next reserved ChangeRequest.
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setLookupNextReservedStatement(EdaContext xContext)
    throws IcofException {

	// select min(clearquest_id) as CLEARQUEST_ID
	// from TK.ChangeRequest
	// where ChangeRequest_Status_ID = 2

	// Define the query.
	String query = "select min(" + CQ_COL + ") as " + CQ_COL + " from "
		       + TABLE_NAME + " where " + STATUS_ID_COL + " =  ? ";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to update this object.
     * 
     * @param xContext Application context.
     * @param creator Person creating this CR (null unless updating reserved CR)
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setUpdateRowStatement(EdaContext xContext, String creatorId)
    throws IcofException {

	// Define the query.
	String query = "update " + TABLE_NAME + " set " + STATUS_ID_COL
		       + " = ? " + ", " + DESC_COL + " = ? " + ", "
		       + TYPE_ID_COL + " = ? " + ", " + SEVERITY_ID_COL
		       + " = ? " + ", " + CUSTOMER_IMPACTED_COL + " = ? "
		       + ", " + UPDATED_BY_COL + " = ? " + ", "
		       + UPDATED_ON_COL + " = " + CURRENT_TMS;

	if ((creatorId != null) && ! creatorId.isEmpty()) {
	    query += ", " + CREATED_BY_COL + " = ? " + ", " + CREATED_ON_COL
		     + " = " + CURRENT_TMS;
	}

	query += " where " + ID_COL + " =  ? ";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }

    
    /**
     * Create a PreparedStatement to update the comp pkg for this object.
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setUpdateCompPkgStatement(EdaContext xContext, long pkgId)
    throws IcofException {

	// Define the query.
	String query = "update " + TABLE_NAME;
	
	if (pkgId != 0)
	    query += " set " + COMP_PKG_ID_COL + " = ?, ";
	else
	    query += " set " + COMP_PKG_ID_COL + " = null, ";
	
	query += UPDATED_BY_COL + " = ?,  " +
                 UPDATED_ON_COL + " = " + CURRENT_TMS + 
                 " where " + ID_COL + " =  ? ";
 	
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
	String query = "insert into " + TABLE_NAME + " ( " + ALL_COLS + " )"
		       + " values( ?, ?, ?, ?, ?, ?, ?, ?" 
		       + " , ?, " + CURRENT_TMS 
		       + " , ?, " + CURRENT_TMS 
		       + " , ?, ? )";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup rows by comp pkg id
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setLookupByCompPkgStatement(EdaContext xContext)
    throws IcofException {

	// Define the query.
	String query = "select " + ID_COL + 
	                " from " + TABLE_NAME + 
	                " where " + COMP_PKG_ID_COL + " =  ? ";

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
     * Create a PreparedStatement to delete this object
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setDeleteStatement(EdaContext xContext)
    throws IcofException {

	// Define the query.
	String query = "update " + TABLE_NAME + " set " + DELETED_BY_COL
		       + " = ? , " + DELETED_ON_COL + " = " + CURRENT_TMS
		       + " where " + ID_COL + " = ? ";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup objects updated between 2 timestamps
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setLookupUpdatedStatement(EdaContext xContext)
    throws IcofException {

	// Define the query.
	String query = "select " + CQ_COL + " from " + TABLE_NAME + " where "
		       + UPDATED_ON_COL + " >= ? " + " and " + UPDATED_ON_COL
		       + " <= ? " + " and " + STATUS_ID_COL + "!= ? " + " and "
		       + DELETED_BY_COL + " is null";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to find CQ information
     * 
     * @param xContext Application context.
     * @param fromDate From Date
     * @param toDate To Date
     * @throws IcofException
     */
    public void setLookupByCompVerLocStatement(EdaContext xContext,
					       String fromDate, String toDate)
    throws IcofException {

	// select cq.CLEARQUEST_ID, cq.DESCRIPTION, cq.CREATED_BY,
	// cq.CREATED_TMSTMP
	// from tk.Component as c,
	// tk.TkRelease as r,
	// tk.Component_TkRelease as cr,
	// tk.Component_TkVersion as cv,
	// tk.TkVersion as v,
	// tk.LOCATIONEVENTNAME as len,
	// tk.LOCATIONEVENT as le,
	// tk.ComponentTkVersion_Location as cvl,
	// tk.Location_ComponentUpdate as lcu,
	// tk.ComponentUpdate as compu,
	// tk.COMPONENT_TKVERSION_X_CHANGEREQUEST as cvcq,
	// tk.CHANGEREQUEST as cq,
	// tk.Code_Update as cu
	// where v.tkversion_id = 1
	// and c.component_id = 124
	// and len.EVENTNAME = 'ADVANCED_TO_PROD'
	// and le.CREATED_TMSTMP BETWEEN '2011-01-01' AND '2012-07-31'
	//
	// and le.LOCATIONEVENTNAME_ID = len.LOCATIONEVENTNAME_ID
	// and cvl.ComponentTkVersion_Location_id =
	// le.COMPONENTTKVERSION_LOCATION_ID
	// and cq.CHANGEREQUEST_ID = cvcq.CHANGEREQUEST_ID
	// and cvcq.component_tkversion_id = cvl.component_tkversion_id
	//
	// and c.Component_id = cr.component_id
	// and cr.Component_TkRelease_id = cv.Component_TkRelease_id
	// and cv.TkVersion_id = v.TkVersion_id
	// and cvl.component_tkversion_id = cv.component_tkversion_id
	// and cvl.component_tkversion_id = cv.component_tkversion_id
	// and lcu.componentTkVersion_Location_id =
	// cvl.ComponentTkVersion_Location_id
	// and compu.ComponentUpdate_id = lcu.ComponentUpdate_id
	// and compu.ComponentUpdate_Id = cu.ComponentUpdate_id
	// order by cast(cq.CHANGEREQUEST_ID as int) asc


	String body = " from " + Component_Db.TABLE_NAME + " as c, "
		      + Release_Db.TABLE_NAME + " as r, "
		      + Component_Release_Db.TABLE_NAME + " as cr, "
		      + Component_Version_Db.TABLE_NAME + " as cv, "
		      + RelVersion_Db.TABLE_NAME + " as v, "
		      + LocationEventName_Db.TABLE_NAME + " as len, "
		      + LocationEvent_Db.TABLE_NAME + " as le, "
		      + Component_Version_Location_Db.TABLE_NAME + " as cvl, "
		      + Location_ComponentUpdate_Db.TABLE_NAME + " as lcu, "
		      + ComponentUpdate_Db.TABLE_NAME + " as compu, "
		      + CompVersion_ChangeRequest_Db.TABLE_NAME + " as cvcq, "
		      + ChangeRequest_Db.TABLE_NAME + " as cq, "
		      + CodeUpdate_Db.TABLE_NAME + " as cu ";

	body += "where v." + RelVersion_Db.ID_COL + " = ?" + " and c."
		+ Component_Db.ID_COL + " = ?";


	body += " and len." + LocationEventName_Db.NAME_COL + " = ?"
		+ " and le." + LocationEventName_Db.CREATED_ON_COL
		+ " BETWEEN " + " '" + fromDate + " '" + " AND " + " '"
		+ toDate + " '" + " and le."
		+ LocationEvent_Db.LOCATION_EVENT_NAME_ID_COL + " = len."
		+ LocationEventName_Db.ID_COL + " and cvl."
		+ Component_Version_Location_Db.ID_COL + " = le."
		+ LocationEvent_Db.COMP_VER_LOCATION_ID_COL + " and cq."
		+ ChangeRequest_Db.ID_COL + " = cvcq."
		+ CompVersion_ChangeRequest_Db.CHANGE_REQUEST_ID_COL
		+ " and cvcq."
		+ CompVersion_ChangeRequest_Db.COMPONENT_VERSION_COL
		+ " = cvl."
		+ Component_Version_Location_Db.COMPONENT_VERSION_ID_COL
		+ " and c." + Component_Db.ID_COL + " = cr."
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
		+ CodeUpdate_Db.COMPONENT_UPDATE_ID_COL;


	String query = "select cq." + CQ_COL + ", cq." + DESC_COL + ", cq."
		       + SEVERITY_ID_COL + ", cq." + STATUS_ID_COL + ", cq."
		       + CREATED_BY_COL + ", cq." + CREATED_ON_COL + body
		       + " order by cast(" + "cq." + ID_COL + " as int) asc";

	// Set and prepare the query and statement.
	setQuery(xContext, query);
    }


    /**
     * Create a PreparedStatement to lookup objects by ComponentVersion and
     * ChangeRequest status
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setLookupByCompVerStatusStatement(EdaContext xContext)
    throws IcofException {

	// select crq.clearquest_id
	// from tk.changerequest as crq,
	// tk.component_tkversion_x_changerequest as cvcrq
	// where cvcrq.component_tkversion_id = 4
	// and cvcrq.changerequest_id = crq.changerequest_id
	// and crq.changerequest_status_id = 6
	// order by crq.clearquest_id

	// Define the query.
	String query = "select crq." + CQ_COL + " from " + TABLE_NAME
		       + " as crq, " + CompVersion_ChangeRequest_Db.TABLE_NAME
		       + " as cvcrq " + " where cvcrq."
		       + CompVersion_ChangeRequest_Db.COMPONENT_VERSION_COL
		       + " = ? " + "   and cvcrq."
		       + CompVersion_ChangeRequest_Db.CHANGE_REQUEST_ID_COL
		       + " = " + "crq." + ID_COL + "   and crq."
		       + STATUS_ID_COL + " = ? " + " order by crq." + CQ_COL;

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup objects by ComponentVersion and
     * ChangeRequest status
     * 
     * @param xContext Application context.
     * @param maxRevision Revision upper limit
     * @param minRevision Revision lower limit
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setLookupByCompVerStatusStatement(EdaContext xContext,
						  String maxRevision,
						  String minRevision)
    throws IcofException {

	// select crq.clearquest_id
	// from tk.changerequest as crq,
	// tk.component_tkversion_x_changerequest as cvcrq,
	// tk.code_update as cu,
	// tk.code_update_x_changerequest as cucr
	// where cvcrq.component_tkversion_id = 11
	// and cvcrq.changerequest_id = crq.changerequest_id
	// and crq.changerequest_status_id = 3
	// and cast (cu.svnrevision as int) <= 395
	// and cast (cu.svnrevision as int) >= 0
	// and cucr.code_update_id = cu.code_update_id
	// and cucr.changerequest_id = crq.changerequest_id
	// order by crq.clearquest_id

	// Define the query.
	String query = "select crq." + CQ_COL + " from " + TABLE_NAME
		       + " as crq, " + CompVersion_ChangeRequest_Db.TABLE_NAME
		       + " as cvcrq, " + CodeUpdate_Db.TABLE_NAME + " as cu, "
		       + CodeUpdate_ChangeRequest_Db.TABLE_NAME + " as cucr "
		       + " where cvcrq."
		       + CompVersion_ChangeRequest_Db.COMPONENT_VERSION_COL
		       + " = ? " + "   and cvcrq."
		       + CompVersion_ChangeRequest_Db.CHANGE_REQUEST_ID_COL
		       + " = crq." + ID_COL + "   and crq." + STATUS_ID_COL
		       + " = ? ";

	if ((maxRevision != null) && (!maxRevision.equals(""))) {
	    query += " and cast (cu." + CodeUpdate_Db.REVISION_COL
		     + " as int) <= ? ";
	}
	if ((minRevision != null) && (!minRevision.equals(""))) {
	    query += "   and cast (cu." + CodeUpdate_Db.REVISION_COL
		     + " as int) >= ? ";
	}

	query += "   and cucr."
		 + CodeUpdate_ChangeRequest_Db.CODE_UPDATE_ID_COL + " = cu."
		 + CodeUpdate_Db.ID_COL + " " + "   and cucr."
		 + CodeUpdate_ChangeRequest_Db.CHANGE_REQUEST_ID_COL
		 + " = crq." + ChangeRequest_Db.ID_COL + " order by crq."
		 + CQ_COL;

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to update the comp package id to null
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setDeleteCompPkgsStatement(EdaContext xContext)
    throws IcofException {

	// Define the query.
	String query = "update " + TABLE_NAME +
	               " set " + COMP_PKG_ID_COL + " = null, " +
	               UPDATED_BY_COL + " = ?, " +
	               UPDATED_ON_COL + " = " + CURRENT_TMS +
	               " where " + COMP_PKG_ID_COL + " = ? " + 
	               " and " + DELETED_BY_COL + " is null";

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

	dbLookupById(xContext, false);

    }


    /**
     * Look up this object by id.
     * 
     * @param xContext An application context object.
     * @throws Trouble querying the database.
     */
    public void dbLookupById(EdaContext xContext, boolean getAll)
    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setLookupIdStatement(xContext, getAll);

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
     * Look up this object by ClearQuest name.
     * 
     * @param xContext An application context object.
     * @throws Trouble querying the database.
     */
    public void dbLookupByName(EdaContext xContext)
    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setLookupNameStatement(xContext);

	try {
	    getStatement().setString(1, getCqName());
	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
						 this.getClass().getName(),
						 "dbLookupByName()",
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
						 "dbLookupByName()",
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
     * Look up the next "reserved" ChangeRequest row.
     * 
     * @param xContext An application context object.
     * @throws Trouble querying the database.
     */
    public void dbLookupNextReserved(EdaContext xContext)
    throws IcofException {

	// Lookup the reserved status object
	ChangeRequestStatus_Db status = new ChangeRequestStatus_Db(
								   ChangeRequestStatus_Db.STATUS_RESERVED);
	status.dbLookupByName(xContext);

	// Create the SQL query in the PreparedStatement.
	setLookupNextReservedStatement(xContext);

	try {
	    getStatement().setShort(1, status.getId());
	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
						 this.getClass().getName(),
						 "dbLookupNextReserved()",
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
	boolean found = false;
	try {
	    while (rs.next() && !found) {
		setCqName(rs.getString(CQ_COL));
	    }

	}
	catch (SQLException ex) {
	    throw new IcofException(this.getClass().getName(),
				    "dbLookupNextReserved()",
				    IcofException.SEVERE,
				    "Error reading DB query results.",
				    ex.getMessage());
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	// Lookup this object by name to fill in all the details.
	dbLookupByName(xContext);

    }


    /**
     * Insert a new row.
     * 
     * @param xContext An application context object.
     * @param editor Person adding this row.
     * @throws Trouble querying the database.
     */
    public void dbAddRow(EdaContext xContext, User_Db editor)
    throws IcofException {

	// Get the next id.
	setNextIdStatement(xContext);
	long id = getNextBigIntId(xContext);
	closeStatement(xContext);

	// Create the SQL query in the PreparedStatement.
	setAddRowStatement(xContext);

	printQuery(xContext);
	
	try {
	    getStatement().setLong(1, id);
	    getStatement().setString(2, getDescription());
	    getStatement().setString(3, getCqName());
	    getStatement().setLong(4, getStatus().getId());
	    getStatement().setLong(5, getType().getId());
	    getStatement().setLong(6, getSeverity().getId());
	    getStatement().setString(7, getImpactedCustomer());
	    getStatement().setString(8, null);
	    getStatement().setString(9, editor.getIntranetId());
	    getStatement().setString(10, editor.getIntranetId());
	    getStatement().setString(11, null);
	    getStatement().setString(12, null);

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
     * Update the status and description for this object.
     * 
     * @param xContext An application context object.
     * @param newCq New ClearQuest id
     * @param newDesc New Description
     * @param newStatus New ChangeRequestStatus
     * @param newType New ChangeRequestType
     * @param newSeverity New ChangeRequestSeverity
     * @param newCustomer New customer impacted by the change request
     * @param creator Person making this update (null unless updating a reserved
     *            CR)
     * @param editor Person making this update.
     * @throws Trouble querying the database.
     */
    public void dbUpdateRow(EdaContext xContext, 
                            String newCq, 
                            String newDesc,
			    ChangeRequestStatus_Db newStatus,
			    ChangeRequestType_Db newType,
			    ChangeRequestSeverity_Db newSeverity,
			    String newCustomer, 
			    User_Db creator, 
			    User_Db editor)
    throws IcofException {

	if (creator != null)
	    dbUpdateRow(xContext, 
	                newCq, newDesc, 
	                newStatus, newType, 
	                newSeverity, newCustomer, 
	                creator.getIntranetId(), editor.getIntranetId());
	else 
	    dbUpdateRow(xContext, 
	                newCq, newDesc, 
	                newStatus, newType, 
	                newSeverity, newCustomer, 
	                null, editor.getIntranetId());

    }

    
    /**
     * Update the status and description for this object.
     * 
     * @param xContext An application context object.
     * @param newCq New ClearQuest id
     * @param newDesc New Description
     * @param newStatus New ChangeRequestStatus
     * @param newType New ChangeRequestType
     * @param newSeverity New ChangeRequestSeverity
     * @param newCustomer New customer impacted by the change request
     * @param creator Person making this update (null unless updating a reserved
     *            CR)
     * @param editor Person making this update.
     * @throws Trouble querying the database.
     */
    public void dbUpdateRow(EdaContext xContext, 
                            String newCq,
                            String newDesc,
			    ChangeRequestStatus_Db newStatus,
			    ChangeRequestType_Db newType,
			    ChangeRequestSeverity_Db newSeverity,
			    String newCustomer, 
			    String creatorId, String editorId)
    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setUpdateRowStatement(xContext, creatorId);

	try {
	    getStatement().setLong(1, newStatus.getId());
	    getStatement().setString(2, newDesc);
	    getStatement().setLong(3, newType.getId());
	    getStatement().setLong(4, newSeverity.getId());
	    getStatement().setString(5, newCustomer);
	    getStatement().setString(6, editorId);

	    if (creatorId != null) {
		getStatement().setString(7, creatorId);
		getStatement().setLong(8, getId());
	    }
	    else {
		getStatement().setLong(7, getId());
	    }
	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
						 this.getClass().getName(),
						 "dbUpdateRow()",
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
						 "dbUpdateRow()",
						 IcofException.SEVERE,
						 "Unable to insert new row.\n",
						 "QUERY: " + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	// Set the new values.
	dbLookupByName(xContext);

    }


    /**
     * Delete (mark as deleted) this object in the database
     * 
     * @param xContext An application context object.
     * @throws Trouble querying the database.
     */
    public void dbDeleteRow(EdaContext xContext, User_Db editor)
    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setDeleteStatement(xContext);
	try {
	    getStatement().setString(1, editor.getIntranetId());
	    getStatement().setLong(2, getId());

	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
						 this.getClass().getName(),
						 "dbDeleteRow()",
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
						 "dbDeleteRow()",
						 IcofException.SEVERE,
						 "Unable to delete row.\n",
						 "QUERY: " + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	// Set the delete info on this object.
	dbLookupById(xContext, true);

    }


    /**
     * Create a list of Strings containing the ChangeRequest name updated
     * between the start and end time stamps.
     * 
     * @param xContext An application context object.
     * @return Collection of FunctionalUpdate_Db objects.
     * @throws Trouble querying the database.
     */
    public Hashtable<String, String> dbLookupUpdated(EdaContext xContext,
						     Timestamp start,
						     Timestamp end)
    throws IcofException {

	// Lookup the reserved status because we don't want to see reserved CRs
	ChangeRequestStatus_Db reserved = new ChangeRequestStatus_Db(
								     ChangeRequestStatus_Db.STATUS_RESERVED);
	reserved.dbLookupByName(xContext);

	// Create the SQL query in the PreparedStatement.
	setLookupUpdatedStatement(xContext);

	try {
	    getStatement().setTimestamp(1, start);
	    getStatement().setTimestamp(2, end);
	    getStatement().setShort(3, reserved.getId());
	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
						 this.getClass().getName(),
						 "dbLookupUpdated()",
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
	Hashtable<String, String> changeReqs = new Hashtable<String, String>();
	try {
	    while (rs.next()) {
		String aCq = rs.getString(CQ_COL);
		changeReqs.put(aCq, aCq);
	    }

	}
	catch (SQLException ex) {
	    throw new IcofException(this.getClass().getName(),
				    "dbLookupUpdated()", IcofException.SEVERE,
				    "Error reading DB query results.",
				    ex.getMessage());
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	return changeReqs;

    }


    /**
     * Create a list of ChangeRequest names in the given state and associated
     * with the given Component_Version.
     * 
     * @param xContext An application context object.
     * @param compVer Desired component_version id
     * @param status Desired status
     * @param maxRev Limit query so CRs don't exceed this revision
     * @param minRev Limit query so CRs aren't less than this revision
     * @return Collection of ChangeRequest_Db objects.
     * @throws Trouble querying the database.
     */
    public Vector<String> dbLookupByCompVerStatus(EdaContext xContext,
						  Component_Version_Db compVer,
						  ChangeRequestStatus_Db status,
						  String maxRev, String minRev)
    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	if ((maxRev != null) && (!maxRev.equals("")) || (minRev != null)
	    && (!minRev.equals("")))
	    setLookupByCompVerStatusStatement(xContext, maxRev, minRev);
	else
	    setLookupByCompVerStatusStatement(xContext);


	try {
	    int index = 1;
	    getStatement().setLong(index++, compVer.getId());
	    // System.out.println("Comp ver id: " + compVer.getId());
	    getStatement().setShort(index++, status.getId());
	    // System.out.println("Status id: " + status.getId());
	    if ((maxRev != null) && (!maxRev.equals(""))) {
		getStatement().setString(index++, maxRev);
		// System.out.println("Revision (max): " + maxRev);
	    }
	    if ((minRev != null) && (!minRev.equals(""))) {
		getStatement().setString(index++, minRev);
		// System.out.println("Revision (min): " + minRev);
	    }

	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
						 this.getClass().getName(),
						 "dbLookupByCompVerStatus()",
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
	Vector<String> changeReqs = new Vector<String>();
	try {
	    while (rs.next()) {
		String aCq = rs.getString(CQ_COL);
		// System.out.println("FOUND: " + aCq);
		if (!changeReqs.contains(aCq)) {
		    changeReqs.add(aCq);
		}
	    }

	}
	catch (SQLException ex) {
	    throw new IcofException(this.getClass().getName(),
				    "dbLookupByCompVerStatus()",
				    IcofException.SEVERE,
				    "Error reading DB query results.",
				    ex.getMessage());
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	return changeReqs;

    }


    /**
     * Create a list of changerequest records for this Component, version and
     * location.
     * 
     * @param xContext An application context object.
     * @param tk A RelVersion_Db object
     * @param compVerLoc Component_Versions_Location_Db object.
     * @param showLatest If true get on the latest revision otherwise get all
     *            revisions
     * @param fromDate From Date to be filtered
     * @param toDate To Date to be filtered
     * @return Collection of ChangeRequest_Db
     * @throws IcofException Trouble querying the database.
     */
    public Collection<ChangeRequest_Db> dbLookupRevsByCompVerLoc(EdaContext xContext,
								 RelVersion_Db tk,
								 Component_Db comp,
								 String locEventName,
								 String fromDate,
								 String toDate)
    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setLookupByCompVerLocStatement(xContext, fromDate, toDate);

//	printQuery(xContext);

	try {
	    getStatement().setShort(1, tk.getId());
	    getStatement().setShort(2, comp.getId());
	    getStatement().setString(3, locEventName);

//	    System.out.println("TK: " + tk.getId());
//	    System.out.println("COMP: " + comp.getId());
//	    System.out.println("LOC: " + locEventName);

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
	TreeMap<String, ChangeRequest_Db> changeRequestsMap = new TreeMap<String, ChangeRequest_Db>();
	try {
	    while (rs.next()) {
		String cqName = rs.getString(CQ_COL);

		if (!changeRequestsMap.containsKey(cqName)) {
		    ChangeRequest_Db changeRequest_Db = new ChangeRequest_Db(
									     cqName);

		    changeRequest_Db.setDescription(rs.getString(DESC_COL));
		    changeRequest_Db.setStatus(xContext,
					       rs.getShort(STATUS_ID_COL));
		    changeRequest_Db.setSeverity(xContext,
						 rs.getShort(SEVERITY_ID_COL));
		    changeRequest_Db.setCreatedBy(rs.getString(CREATED_BY_COL));
		    changeRequest_Db.setCreatedOn(rs.getTimestamp(CREATED_ON_COL));
		    changeRequestsMap.put(cqName, changeRequest_Db);
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

	return changeRequestsMap.values();

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

	super.populate(xContext, rs);
	setId(rs.getInt(ID_COL));
	setDescription(rs.getString(DESC_COL));
	setCqName(rs.getString(CQ_COL));
	setStatus(xContext, rs.getShort(STATUS_ID_COL));
	setType(xContext, rs.getShort(TYPE_ID_COL));
	setSeverity(xContext, rs.getShort(SEVERITY_ID_COL));
	setImpactedCustomer(rs.getString(CUSTOMER_IMPACTED_COL));

	setLoadFromDb(true);

    }


    /**
     * Return the members as a string.
     */
    public String toString(EdaContext xContext) {

	// Get the audit data
	String audit = super.toString(xContext);

	// Get the class specific data
	StringBuffer buffer = new StringBuffer();
	buffer.append("ID: " + getId() + "\n");
	buffer.append("Description: " + getDescription() + "\n");
	buffer.append("CQ Id      : " + getCqName() + "\n");
	buffer.append("Status     : " + getStatus().getName() + "\n");
	buffer.append("Type       : " + getType().getName() + "\n");
	buffer.append("Severity   : " + getSeverity().getName() + "\n");
	buffer.append("Customer   : " + getImpactedCustomer() + "\n");
	buffer.append(audit);

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
     * Update all change requests that are associated with the given it
     * to they are no long associated with it
     *
     * @param xContext  Application context
     * @param pkgId     Component package id to update
     * @param user      User making this update
     * @throws IcofException 
     */
    public void dbDeleteCompPackages(EdaContext xContext, long pkgId, 
                                     User_Db user) throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setDeleteCompPkgsStatement(xContext);

	try {
	    getStatement().setString(1, user.getIntranetId());
	    getStatement().setLong(2, pkgId);
	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
						 this.getClass().getName(),
						 "dbDeleteCompPackages()",
						 IcofException.SEVERE,
						 "Unable to prepare SQL statement.",
						 IcofException.printStackTraceAsString(trap)
						 + "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query .. not testing the return value because there 
	// may be nothing to update
	updateRows(xContext);

	// Close the PreparedStatement.
	closeStatement(xContext);

    }

    
    /**
     * Update the component package id for this row
     *
     * @param xContext Application context
     * @param pkgId    New component package id
     * @param user     Person making the update
     * @throws IcofException 
     */
    public void dbUpdateCompPkg(EdaContext xContext, long pkgId, User_Db user) 
    throws IcofException {
	
	dbUpdateCompPkg(xContext, pkgId, user.getIntranetId());
	
    }

    
    /**
     * Update the component package id for this row
     *
     * @param xContext Application context
     * @param pkgId    New component package id
     * @param user     Person making the update
     * @throws IcofException 
     */
    public void dbUpdateCompPkg(EdaContext xContext, long pkgId, String userId) 
    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setUpdateCompPkgStatement(xContext, pkgId);

	try {
	    if (pkgId != 0) {
		getStatement().setLong(1, pkgId);
		getStatement().setString(2, userId);
		getStatement().setLong(3, getId());
	    }
	    else {
		getStatement().setString(1, userId);
		getStatement().setLong(2, getId());
	    }
	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
	                                         this.getClass().getName(),
	                                         "dbUpdateCompPkg()",
	                                         IcofException.SEVERE,
	                                         "Unable to prepare SQL statement.",
	                                         IcofException.printStackTraceAsString(trap)
	                                         + "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	if (! insertRow(xContext)) {
	    IcofException ie = new IcofException(this.getClass().getName(),
	                                         "dbUpdateCompPkg()",
	                                         IcofException.SEVERE,
	                                         "Unable to insert new row.\n",
	                                         "QUERY: " + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Close the PreparedStatement.
	closeStatement(xContext);
	
    }


    /**
     * Create a collection of CRs associated with the given comp pkg
     *
     * @param xContext  Application
     * @param compPkgId Component package id
     * @return
     * @throws IcofException 
     */
    public Vector<ChangeRequest_Db> dbLookupByCompPkg(EdaContext xContext,
						      long compPkgId) 
						      throws IcofException {

	
	// Create the SQL query in the PreparedStatement.
	setLookupByCompPkgStatement(xContext);

	try {
	    getStatement().setLong(1, compPkgId);
	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
						 this.getClass().getName(),
						 "dbLookupByCompPkg()",
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
	Vector<ChangeRequest_Db> crs = new Vector<ChangeRequest_Db>();
	try {
	    while (rs.next()) {
		long anId= rs.getLong(ID_COL);
		ChangeRequest_Db myCr = new ChangeRequest_Db(anId);
		myCr.dbLookupById(xContext);
		crs.add(myCr);
	    }
	}
	catch (SQLException ex) {
	    throw new IcofException(this.getClass().getName(),
				    "dbLookupByCompPkg()",
				    IcofException.SEVERE,
				    "Error reading DB query results.",
				    ex.getMessage());
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	return crs;
	
    }
    
}
