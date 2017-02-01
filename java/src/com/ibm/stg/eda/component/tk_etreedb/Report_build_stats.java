/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2013 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 * CREATOR: Gregg Stadtlander
 *
 *-PURPOSE---------------------------------------------------------------------
 * Generates build statistics reports.
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 01/22/2013 GFS  Initial coding. 
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.tk_etreedb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAudit;
import com.ibm.stg.eda.component.tk_etreebase.TkDbUtils;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofStringUtil;

public class Report_build_stats extends TkAudit {

	/**
	 * Constants
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * Constructor
	 * 
	 * @param aQuery   DB query to execute
	 */
	public Report_build_stats(String sQuery, String[] xHeaders, Integer[] xWidths) {
		setQuery(sQuery);
		setHeaders(xHeaders);
		setWidths(xWidths);
	}

	
	/**
	 * Data Members
	 */
	private String[] headers;
	private Integer[] widths;
	private StringBuffer content;
	private Vector<String> rawContent;

	
	/**
	 * Getters
	 */
	public String[] getHeaders() { return headers; }
	public Integer[] getWidths() { return widths; }
	public StringBuffer getContent() { return content; }
	public Vector<String> getRawContent() { return rawContent; }
	

	/**
	 * Setters
	 */
	private void setHeaders(String[] arr) { headers = arr; }
	private void setWidths(Integer[] arr) { widths = arr; }
	
	
	/**
	 * Generate the ChanegRequest report.
	 * 
	 * @param xContext   Application context.
	 * @param widths     Collection of column widths
	 * @throws IcofException 
	 * @throws SQLException 
	 * 
	 */
	public void setContent(EdaContext xContext) throws IcofException, SQLException {

		// Create the SQL query.
		setStatement(TkDbUtils.prepStatement(xContext, getQuery()));
		
		// Run the query.
		ResultSet rs = executeQuery(xContext);

		// Format each line for the report.
		int numColumns = getWidths().length;
		content = new StringBuffer();
		rawContent = new Vector<String>();
		try {
		
			while (rs.next()) {
				
				String[] data = new String[numColumns];
				
				data[0] = rs.getString(1);
				data[1] = rs.getString(2);
				data[2] = rs.getString(3);
				Timestamp ts = rs.getTimestamp(4);
				data[3] = ts.toString();
				String line =  formatLine(data, widths);

				content.append(line);
				rawContent.add(data[0] + " " +
				               data[1] + " " +
				               data[2] + " " +
				               data[3]);
				
			}

		}
		catch(SQLException ex) {
			throw new IcofException(this.getClass().getName(), "setContent()",
			                        IcofException.SEVERE, 
			                        "Error reading DB query results.",
			                        ex.getMessage());
		}
		
		// Close the PreparedStatement.
		closeStatement(xContext);
	
	}


	/**
	 * Format the display line
	 * 
	 * @param data   Collection of data to be displayed
	 * @param widths Collection of column widths
	 * @return String formatted for display
	 */
	public String formatLine(String[] data, Integer[] widths) {

		// Construct the line
		String line = "";
		for (int i = 0; i < data.length; i++) {
			String text = data[i];
			Integer width = widths[i];
			if (i == 0) {
				line += IcofStringUtil.leftJustify(text, " ", width);
			}
			else {
				line += IcofStringUtil.center(text, " ", width);
			}
		
		}
		line += "\n";

		return line;

	}


	/**
	 * Generate the report header
	 * 
	 * @param headers  Collection of column header text
	 * @param widths   Collection of column widths
	 * @return Report header
	 */
	public String getHeader() {
		return getHeader(getHeaders(), getWidths());
	}
	
	
	/**
	 * Generate the report header
	 * 
	 * @param headers  Collection of column header text
	 * @param widths   Collection of column widths
	 * @return Report header
	 */
	public String getHeader(String[] sHeaders, Integer[] sWidths) {

		StringBuffer header = new StringBuffer();
		header.append(formatLine(sHeaders, sWidths));
		
		// Set the dash length
		for (int i = 0; i < sHeaders.length; i++) {
			String dashes = IcofStringUtil.leftJustify("-", "-", sWidths[i] - 1);
			header.append(IcofStringUtil.leftJustify(dashes, " ", sWidths[i]));
		}

		return header.toString();

	}

}
