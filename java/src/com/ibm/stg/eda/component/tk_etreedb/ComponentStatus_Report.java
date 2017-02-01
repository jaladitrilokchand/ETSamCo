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
 * Generates Component status reports.
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 08/09/2011 GFS  Initial coding. 
 * 09/19/2011 GFS  Changed active to default.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.tk_etreedb;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAudit;
import com.ibm.stg.eda.component.tk_etreebase.TkDbUtils;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequestStatus;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofStringUtil;

public class ComponentStatus_Report extends TkAudit {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5841467879333022900L;
	// Constants
	private static int MAX_DESC = 35;
	private static int TK_WIDTH = 9;
	private static int COMP_WIDTH = 11;
	private static int CREATOR_WIDTH = 22;
	private static int CR_WIDTH = 15;
	private static int STATE_WIDTH = 14;
	//private static int DEFAULT_WIDTH = 9;

	
	/**
	 * Constructor
	 * 
	 * @param xContext     Application context
	 * @param aUser        A User_Db object
	 * @param aTk          A ToolKit object
	 * @param aComp        A Component object
	 * @param aStatus      A ChangeRequestStatus object
	 * @param defaultOnlyFlag  If true show only default ChangeRequests
	 * @throws IcofException 
	 */
	public ComponentStatus_Report(EdaContext xContext, User_Db aUser, 
	                              ToolKit aTk, Component aComp,
	                              ChangeRequestStatus aStatus,
	                              boolean defaultOnlyFlag) {
		setUser(aUser);
		setToolKit(aTk);
		setComponent(aComp);
		setStatus(aStatus);
		setDefaultOnly(defaultOnlyFlag);

	}

	
	/**
	 * Data Members
	 */
	private User_Db user;
	private ToolKit toolKit;
	private Component component;
	private ChangeRequestStatus status;
	private boolean defaultOnly = false;
	private StringBuffer content;
	private String query;
	private Component_Version_Db compVersion;

	/**
	 * Getters
	 */
	public User_Db getUser() { return user; }
	public ToolKit getToolKit() { return toolKit; }
	public Component getComponent() { return component; }
	public ChangeRequestStatus getStatus() { return status; }
	public boolean isDefaultOnly() { return defaultOnly; }
	public StringBuffer getContent() { return content; }
	public String getQuery() { return query; }
	public Component_Version_Db getCompVersion() { return compVersion; }
	

	/**
	 * Setters
	 */
	private void setUser(User_Db aUser) { user = aUser; }
	private void setToolKit(ToolKit aTk) { toolKit = aTk; }
	private void setComponent(Component aComp) { component = aComp; }
	private void setStatus(ChangeRequestStatus aStatus) { status = aStatus; }
	private void setDefaultOnly(boolean aFlag) { defaultOnly = aFlag; }


	/**
	 * Create a PreparedStatement
	 * 
	 * @param xContext  Application context.
	 * @throws IcofException 
	 */
	public void setBdpStatement(EdaContext xContext) throws IcofException {

//		  select crq.changerequest_id, 
//		         crq.clearquest_id, 
//		         crq.created_by
//		         crs.changerequest_status, 
//			     r.tkrelease_name, 
//		         v.tkversion_name,
//		         c.component_name, 
//			     substring(crq.description, 1, 35, CODEUNITS32) as description    <-- truncate description
//			 from tk.changerequest as crq,
//			      tk.component_tkversion_x_changerequest as cvcrq,
//			      tk.activechangerequest as acr,  <-- add only if looking for default
//			      tk.changerequest_status as crs,
//			      tk.component as c,
//			      tk.component_tkrelease as cr,
//			      tk.component_tkversion as cv,
//			      tk.tkrelease as r,
//			      tk.tkversion as v
//			where cvcrq.changerequest_id = crq.changerequest_id
//			  and crs.changerequest_status_id = crq.changerequest_status_id
//			  and c.component_id = cr.component_id
//			  and cr.component_tkrelease_id = cv.component_tkrelease_id
//			  and cv.component_tkversion_id = cvcrq.component_tkversion_id
//			  and cr.tkrelease_id = r.tkrelease_id
//			  and cv.tkversion_id = v.tkversion_id
//			  and cvcrq.component_tkversion_id in (
//			        select component_tkversion_id   <-- Replace with one of the "Get Component_TkVersion id" 
//			          from tk.component_tkversion    <-- calls or the Component_TKVersion id if filtering
//			         where tkversion_id = 1          <--  by Tk and Comp
//			  )
//			  and crq.changerequest_status_id = 2             <-- Add if filtering by state
//			  and crq.changerequest_id = acr.changerequest_id <-- Add if looking for default CRs only
//			  order by clearquest_id asc

		// Define the query.
		query = "select " +
 		        " crq." + ChangeRequest_Db.ID_COL + ", " +
 		        " crq." + ChangeRequest_Db.CQ_COL + ", " +
 		        " crq." + ChangeRequest_Db.CREATED_BY_COL + ", " +
 		        " crs." + ChangeRequestStatus_Db.NAME_COL + ", " + 
 		        " r." + Release_Db.NAME_COL + ", " +
 		        " v." + RelVersion_Db.NAME_COL + ", " +
 		        " c." + Component_Db.NAME_COL + ", " +
 		        " substring(crq." + ChangeRequest_Db.DESC_COL + ", 1, " + MAX_DESC + ", CODEUNITS32) " + 
 		        "from " + ChangeRequest_Db.TABLE_NAME + " as crq, " +
 		        CompVersion_ChangeRequest_Db.TABLE_NAME + " as cvcrq, ";
			if (isDefaultOnly())
 		        query += ChangeRequestActive_Db.TABLE_NAME + " as acr, ";
			query += ChangeRequestStatus_Db.TABLE_NAME + " as crs, " +
 		        Component_Db.TABLE_NAME + " as c, " +
 		        Component_Release_Db.TABLE_NAME + " as cr, " +
 		        Component_Version_Db.TABLE_NAME + " as cv, " +
 		        Release_Db.TABLE_NAME + " as r, " +
 		        RelVersion_Db.TABLE_NAME + " as v " +
 		        "where cvcrq." + CompVersion_ChangeRequest_Db.CHANGE_REQUEST_ID_COL +
 		        " = crq." + ChangeRequest_Db.ID_COL +
 		        " and crs." + ChangeRequestStatus_Db.ID_COL +
 		        " = crq." + ChangeRequest_Db.STATUS_ID_COL +
 		        " and c." + Component_Db.ID_COL +
 		        " = cr." + Component_Release_Db.COMP_ID_COL + 
 		        " and cr." + Component_Release_Db.ID_COL +
 		        " = cv." + Component_Version_Db.REL_COMP_ID_COL +
 		        " and cv." + Component_Version_Db.ID_COL +
 		        " = cvcrq." + CompVersion_ChangeRequest_Db.COMPONENT_VERSION_COL +
 		        " and cr." + Component_Release_Db.REL_ID_COL +
 		        " = r." + Release_Db.ID_COL +
 		        " and cv." + Component_Version_Db.VERSION_ID_COL +
 		        " = v." + RelVersion_Db.ID_COL +
 		        getTkCompFilter(xContext);

		if (getStatus() != null) {
			query += getStatusFilter(xContext);
		}
		if (isDefaultOnly()) {
			query += getDefaultFilter(xContext);
		}
		query += " order by " + ChangeRequest_Db.CQ_COL + " asc ";

		// Set and prepare the query and statement.
		setStatement(TkDbUtils.prepStatement(xContext, getQuery()));
		
	}

	
	/**
	 * Returns the SQL line to filter ChangeRequests by Tk and/or component
	 * @param xContext  Application context
	 * @return
	 * @throws IcofException 
	 */
	private String getTkCompFilter(EdaContext xContext) throws IcofException {
			
		String line;
		
		// If TK and Component known
		if ((getToolKit() != null) && (getComponent() != null)) {
			setCompVersion(xContext);
			line = " and cvcrq." + CompVersion_ChangeRequest_Db.COMPONENT_VERSION_COL +
			       " = ? ";
			return line;
		}

		// If TK and Component are NOT known
		if ((getToolKit() == null) && (getComponent() == null)) {
			return "";
		}
			
		line = " and cvcrq." + CompVersion_ChangeRequest_Db.COMPONENT_VERSION_COL +
		" in ( ";

		// If TK known
		if (getComponent() == null) {
//				select component_tkversion_id
//				 from tk.component_tkversion as cv
//				where cv.tkversion_id = 1
			line += " select " + Component_Version_Db.ID_COL +
			" from " + Component_Version_Db.TABLE_NAME + 
			" where " + Component_Version_Db.VERSION_ID_COL + " = ? ) ";
		}

		// If Component known
		else {
//				select cv2.component_tkversion_id
//                from tk.component_tkversion as cv2,
//                     tk.component_tkrelease as cr2
//               where cr2.component_id = 4
//                 AND cr2.component_tkrelease_id = cv2.component_tkrelease_id
			line += " select cv2." + Component_Version_Db.ID_COL +
			" from " +  Component_Version_Db.TABLE_NAME + " as cv2, " + 
			Component_Release_Db.TABLE_NAME + " as cr2 " +
			" where cr2." + Component_Release_Db.COMP_ID_COL + " = ? " +
			" and cr2." + Component_Release_Db.ID_COL + 
			" = cv2." + Component_Version_Db.REL_COMP_ID_COL + " ) ";
		}

		return line;
		
	}
	
	
	/**
	 * Lookup the ComponentVersion object
	 * @param xContext  Application context
	 */
	private void setCompVersion(EdaContext xContext) throws IcofException {
		
		compVersion = 
			new Component_Version_Db(xContext, getToolKit().getToolKit(), 
			                         getComponent().getComponent());
		compVersion.dbLookupByCompRelVersion(xContext);
		
	}
	
	
	/**
	 * Returns the SQL line to filter ChangeRequests by status
	 * @param xContext  Application context
	 * @return
	 */
	private String getStatusFilter(EdaContext xContext) {
		String line = " and crq." + ChangeRequest_Db.STATUS_ID_COL + " = ? ";		
		return line;
	}
	
	
	/**
	 * Returns the SQL line to filter for default CR only
	 * @param xContext  Application context
	 * @return
	 */
	private String getDefaultFilter(EdaContext xContext) {
		String line = " and crq." + ChangeRequest_Db.ID_COL + 
		              " = acr." + ChangeRequestActive_Db.CHANGE_REQUEST_ID_COL + " ";		
		return line;
	}
	
	
	/**
	 * Generate the ChanegRequest report.
	 * @param xContext   Application context.
	 * @throws IcofException 
	 * 
	 */
	public void setContent(EdaContext xContext) throws IcofException {

		// Create the SQL query.
		setBdpStatement(xContext);
		
		// Update the query with real data.
		try {
			
			// Set the ToolKit and/or Component ids
			int index = 1;
			if ((getToolKit() != null) && (getComponent() != null)) {
				getStatement().setLong(index++, getCompVersion().getId());
			}
			else if ((getComponent() == null) && (getToolKit() != null)) {
				getStatement().setShort(index++, getToolKit().getToolKit().getId());
			}
			else if ((getToolKit() == null) && (getComponent() != null)) {
				getStatement().setShort(index++, getComponent().getComponent().getId());
			}
			else {
				// nothing to add to prepared statement
			}

			// Set the Status id
			if (getStatus() != null) {
				getStatement().setShort(index++, getStatus().getStatus().getId());
			}

		}
		catch(SQLException trap) {
			IcofException ie = new IcofException(this.getClass().getName(),
			                                     "setReport()",
			                                     IcofException.SEVERE,
			                                     "Unable to prepare SQL statement.",
			                                     IcofException.printStackTraceAsString(trap) + 
			                                     "\n" + getQuery());
			xContext.getSessionLog().log(ie);
			throw ie;
		}
		
		
		// Run the query.
		ResultSet rs = executeQuery(xContext);
		
		// Format each line for the report.
		content = new StringBuffer();
		String defaultText = "?";
		if (isDefaultOnly()) 
			defaultText = "Y";
		try {
			while (rs.next()) {
				String cqName =  rs.getString(2);
				String creator = rs.getString(3);
				String stateName = rs.getString(4);
				String relName = rs.getString(5);
				String verName = rs.getString(6);
				String compName = rs.getString(7);
				String description = rs.getString(8);
				String tkName = relName + "." + verName;
				
				String line =  formatLine(tkName, compName, cqName, creator, 
				                          stateName, defaultText, description);

				getContent().append(line);
				
			}
			
		}
		catch(SQLException ex) {
			throw new IcofException(this.getClass().getName(), "setReport()",
			                        IcofException.SEVERE, 
			                        "Error reading DB query results.",
			                        ex.getMessage());
		}

		// Close the PreparedStatement.
		closeStatement(xContext);

	}


	/**
	 * Format the display line
	 * @param tkText      Tool Kit text
	 * @param compText    Component text
	 * @param crText      Change Request text
	 * @param createText  Change Request creator text
	 * @param stateText   Change Request state text
	 * @param defaultText  Default Change Request text
	 * @param description Change Request description
	 * @return String formatted for display
	 */
	private String formatLine(String tkText, String compText, String crText,
	                          String createText,  String stateText, String defaultText,
	                          String description) {

		// Construct the line
		String line = IcofStringUtil.leftJustify(tkText, " ", TK_WIDTH) + 
		              IcofStringUtil.leftJustify(compText, " ", COMP_WIDTH) +
		              IcofStringUtil.leftJustify(crText, " ", CR_WIDTH) +
		              IcofStringUtil.leftJustify(stateText, " ", STATE_WIDTH) +
		              //IcofStringUtil.center(defaultText, _WIDTH) +
		              IcofStringUtil.leftJustify(createText, " ", CREATOR_WIDTH) +
		              IcofStringUtil.leftJustify(description, " ", MAX_DESC) + 
		              "\n";

		return line;

	}


	/**
	 * Generate the report header
	 * 
	 * @return Report header
	 */
	public String getHeader() {

		StringBuffer header = new StringBuffer();
		
//		header.append("ChangeRequests for " + getUser().getAfsId() + "\n");
		header.append("\n");
		header.append(formatLine("ToolKit", "Component", "ChangeRequest", 
		                         "Created by", "State", "Default", 
		                         "Description (first " + MAX_DESC + " chars)"));
		header.append(formatLine("-------", "---------", "-------------", 
		                         "----------", "-----", "------", 
		                         "----------------------------------------"));

		return header.toString();

	}

}
