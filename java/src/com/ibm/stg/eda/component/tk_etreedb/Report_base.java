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
 * Generates ChangeRequest reports.
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 07/22/2011 GFS  Initial coding. 
 * 07/11/2012 GFS  Changed the TK column width to accommodate 14.1.build.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.tk_etreedb;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAudit;
import com.ibm.stg.eda.component.tk_etreebase.TkDbUtils;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofStringUtil;

public class Report_base extends TkAudit {

	/**
	 *  Constants
	 */
	private static final long serialVersionUID = 1L;
	
	
	/**
	 * Constructor
	 * 
	 * @param aQuery   DB query to execute
	 */
	public Report_base(String sQuery, String[] xHeaders, Integer[] xWidths) {
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

	
	/**
	 * Getters
	 */
	public String[] getHeaders() { return headers; }
	public Integer[] getWidths() { return widths; }
	public StringBuffer getContent() { return content; }
	

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
	 * 
	 */
	public void setContent(EdaContext xContext) throws IcofException {

		// Create the SQL query.
		setStatement(TkDbUtils.prepStatement(xContext, getQuery()));
		
		// Run the query.
		ResultSet rs = executeQuery(xContext);

		// Format each line for the report.
		int numColumns = getWidths().length;
		content = new StringBuffer();
		try {
			while (rs.next()) {
				
				String[] data = new String[numColumns];
				for (int i = 0; i < numColumns; i++) {
					data[i] = rs.getString(i+1);
				}
				String line =  formatLine(data, widths);

				getContent().append(line);
				
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
	private String formatLine(String[] data, Integer[] widths) {

		// Construct the line
		String line = "";
		for (int i = 0; i < data.length; i++) {
			String text = data[i];
			Integer width = widths[i];
			line += IcofStringUtil.leftJustify(text, " ", width);
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

		StringBuffer header = new StringBuffer();
		header.append(formatLine(getHeaders(), getWidths()));
		
		// Set the dash length
		for (int i = 0; i < getHeaders().length; i++) {
			String dashes = IcofStringUtil.leftJustify("-", "-", widths[i] - 1);
			header.append(IcofStringUtil.leftJustify(dashes, " ", widths[i]));
		}

		return header.toString();

	}

}
