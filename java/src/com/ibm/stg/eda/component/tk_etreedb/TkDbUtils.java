/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2010 -- IBM Internal Use Only
*
*=============================================================================
*
* CREATOR: Gregg Stadtlander
*    DATE: 05/25/2010
*
*-PURPOSE---------------------------------------------------------------------
* DB Utility class
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 05/25/2010 GFS  Initial coding.
* 08/19/2010 GFS  Removed some debug statements.
* 07/23/2010 GFS  Converted to using PreparedStatements.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_etreedb;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.iipmds.common.IcofException;

public class TkDbUtils implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -7924494448737687237L;
	/**
     * Constants.
     */
    public static final long EMPTY = -1;
    public static final int START_ID = 1;
   
    /**
     * Execute the specified SQL query.
     * 
     *  @param xContext   Application context object.
     *  @param aStatement A prepared SQL query.
     *  @return           A ResultSet containing the query results
     *  @throws           IcofException
     */
    public static ResultSet execute(EdaContext xContext, 
    		                        PreparedStatement aStatement)
    throws IcofException {
    
        // Log the query
//        xContext.getSessionLog().log(SessionLog.INFO, aStatement.toString());

        // Execute the query.
        ResultSet results = null;
        try {
        	boolean success = aStatement.execute();
        	if (success) {
        		results = aStatement.getResultSet();
        	}
        }
        catch (Exception e) {
            IcofException ie = new IcofException("TkDbUtils",
                                                 "executeQuery()",
                                                 IcofException.SEVERE,
                                                 e.getMessage(), aStatement.toString());
            xContext.getSessionLog().log(ie);
            throw ie;
        }

        return results;
        
    }

    

    /**
     * Create a PreparedStatement for the specified query.
     *  
     * @param   xContext  Application context
     * @param   aQuery    A database SQL query.
     * @return  PreparedStatement
     * @throws IcofException 
     */
    public static PreparedStatement prepStatement(EdaContext xContext, String sQuery)
    throws IcofException {

        // Create a statement object and return it.
        try {
        	
            PreparedStatement stmt = 
                xContext.getConnection().prepareStatement(sQuery);
            return stmt;
            
        }
        catch (Exception e) {
            IcofException ie = new IcofException("EdaContext",
                                                 "prepStatement()",
                                                 IcofException.SEVERE,
                                                 e.getMessage(), sQuery);
            xContext.getSessionLog().log(ie);
            throw ie;
        }

    }
    
}
