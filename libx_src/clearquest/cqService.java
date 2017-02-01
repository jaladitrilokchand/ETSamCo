/**
 * <pre>
 * =============================================================================
 *
 *  IBM Internal Use Only
 *
 * =============================================================================
 *
 *  CREATOR: Aydin Suren
 *     DEPT: AW0V
 *     DATE: 03/01/2008
 *     
 *-PURPOSE----------------------------------------------------------------------------------------
 * Generic class definition for the CQ services.
 *------------------------------------------------------------------------------------------------------
 *
 * =============================================================================
 *
 * -CHANGE LOG------------------------------------------------------------------
 * 03/01/2008 AS  Initial coding.
 *
 * =============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.clearquest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import javax.wvcm.Feedback;
import javax.wvcm.PropertyNameList;
import javax.wvcm.PropertyRequestItem;
import javax.wvcm.ProviderFactory;
import javax.wvcm.Resource;
import javax.wvcm.ResourceList;
import javax.wvcm.ProviderFactory.Callback;

import com.ibm.md.mdException;
import com.ibm.rational.wvcm.stp.StpResource;
import com.ibm.rational.wvcm.stp.cq.CqDbSet;
import com.ibm.rational.wvcm.stp.cq.CqProvider;
import com.ibm.rational.wvcm.stp.cq.CqRecordType;
import com.ibm.rational.wvcm.stp.cq.CqUserDb;

/**
 * @author asuren
 * 
 */
public class cqService {

	/* Format for converting CQRowData Date objects into xml */
    public static final String F$sDateFormat = "yyyy-MM-dd HH:mm:ss";

    /* Data formatter object */
    private static SimpleDateFormat S$sdfFormat = null;

    /**
     * Provides a ClearQuest connection object.
     * 
     * @param sUN
     *            Username.
     * @param sPW
     *            User's password.
     * @return
     * @throws Exception
     */
    protected CqProvider getProvider(String sUN, String sPW)
	    throws Exception {

	Callback callback = null;

	try {
	    callback = new cqProviderCallback(sUN, sPW);

	    // Instantiate a Provider
	    return (CqProvider) ProviderFactory.createProvider(
		    CqProvider.CQ_ONLY_PROVIDER_CLASS, callback);

	} catch (Exception e) {
	    throw (e);
	} finally {
	    callback = null;
	}
    }

    
    /**
     * Use the provider to get the requested database, then the requested 
     * record type as a CqRecordType object with required properties already
     * read.
     * 
     * @param provider  The CqProvider object.
     * @param sSchema The schema for the database.
     * @param sDatabase The Cq database name.
     * @param sRecordType The requested record type as a String.
     * @return            The CqRecordType object requested.
     */
    protected CqRecordType getCqRecordType(CqProvider provider, String sSchema, String sDatabase, 
    									String sRecordType) {

	Feedback feedback = null;
	CqUserDb xUserDb = null;
	ResourceList xTypes = null;
	Iterator itTypes = null;
	CqRecordType xRecordType = null;
	
	try {

	    if (provider == null) {
	    	throw new Exception("Provider is null.");
	    }
	    if (sSchema == null) {
	    	throw new Exception("Schema is null.");
	    }
	    if (sDatabase == null) {
	    	throw new Exception("Database is null.");
	    }
	    if (sRecordType == null) {
	    	throw new Exception("Record type is null.");
	    }

	    // get the database
	    xUserDb = getCqUserDb(provider, sSchema, sDatabase);

	    // get the requested type from the types provided in the database
	    // proxy
	    xTypes = xUserDb.getRecordTypeSet();
	    itTypes = xTypes.iterator();
	    boolean bFound = false;
	    feedback = new PropertyRequestItem.PropertyRequest(
			    new PropertyNameList.PropertyName[] {
				    Resource.DISPLAY_NAME, StpResource.STABLE_LOCATION, 
				    StpResource.USER_FRIENDLY_LOCATION});
	    while (!bFound && itTypes.hasNext()) {
	    	xRecordType = (CqRecordType) itTypes.next();
    		xRecordType = (CqRecordType) xRecordType.doReadProperties(feedback);
	    	bFound = (sRecordType.equals(xRecordType.getDisplayName()));
	    }

	} catch (Exception e) {
	    StringWriter sw = new StringWriter();
	    e.printStackTrace(new PrintWriter(sw, true));
		String sError = null;
	    try {
	    	sError = cqUtil.filterXMLReservedChars(sw.toString());
	    } catch (Exception ignore) {
	    	sError = sw.toString();
	    } finally {
	    	System.err.println("Error in cqService.getCqRecordType: " 
	    						+ sError);
	    }
	} finally {
			feedback = null;
			xUserDb = null;
			xTypes = null;
			itTypes = null;
	}
	return xRecordType;
    }


    /**
     * Use the provider to get the requested database, then the requested 
     * record type as a CqRecordType object with required properties already
     * read.  This includes the display name and the record type set, which 
     * can be used to find the requested CqRecordType object in getCqRecordType.
     * 
     * @param provider  The CqProvider object.
     * @param sDatabase The Cq database name.
     * @param sRecordType The requested record type as a String.
     * @return            The CqRecordType object requested.
     */
    protected CqUserDb getCqUserDb(CqProvider provider, String sSchema, String sDatabase) 
    throws Exception {

	Feedback feedback = null;
	CqDbSet xSet = null;
	CqUserDb xUserDb = null;
	ResourceList xList = null;
	Iterator itList = null;
	ResourceList xListDb = null;
	Iterator itListDb = null;
	
	try {

	    if (provider == null) {
	    	throw new Exception("Provider is null.");
	    }
	    if (sSchema == null) {
	    	throw new Exception("Schema is null.");
	    }
	    if (sDatabase == null) {
	    	throw new Exception("Database is null.");
	    }

	    // get the database directly, without looping through all available
	    String sDbLocation = "cq.userdb:" + sSchema + "/" + sDatabase;
	    xUserDb = (CqUserDb) provider.resource(provider.location(sDbLocation));
	    feedback = new PropertyRequestItem.PropertyRequest(
			    new PropertyNameList.PropertyName[] {
				    CqUserDb.RECORD_TYPE_SET, StpResource.REPOSITORY});
		xUserDb = (CqUserDb) xUserDb.doReadProperties(feedback);
	    
	    /*
	     * This is the tutorial's method for making a connection, looping through
	     * all connections and matching schema and db name.  It doesn't work, however,
	     * because getAccessibleDatabases() or getUserDatabases() do 
	     * not return all of the databases that actually are accessible
	    feedback = new PropertyRequestItem.PropertyRequest(
			    new PropertyNameList.PropertyName[] {
				    CqDbSet.USER_DATABASES});
	    xList = provider.doGetDbSetList(feedback);
	    itList = xList.iterator();
	    boolean bFound = false;
	    while (itList.hasNext()) {
	    	xSet = (CqDbSet) itList.next();
	    	xListDb = xSet.getUserDatabases();
	    	itListDb = xListDb.iterator();
		    feedback = new PropertyRequestItem.PropertyRequest(
				    new PropertyNameList.PropertyName[] {
					    CqUserDb.RECORD_TYPE_SET, StpResource.REPOSITORY});
	    	while (!bFound && itListDb.hasNext()) {
	    		xUserDb = (CqUserDb) itListDb.next();
	    		xUserDb = (CqUserDb) xUserDb.doReadProperties(feedback);
	    		System.err.println("database location: " + xUserDb.location().string());
	    		String sRepository = xUserDb.getRepository().toString();
	    		System.err.println("cqService: user db: " + sRepository);
	    		bFound = sRepository.endsWith(":" + sSchema + "/" + sDatabase);
	    	}
	    }
	    
	    if (!bFound) {
	    	xUserDb = null;
	    	throw new Exception("Database " + sSchema + "/" + sDatabase + " not found.");
	    }
	    */
	    
	    if (xUserDb == null) {
	    	throw new Exception("Database " + sDbLocation + " was not found.");
	    } 
	    
	} catch (Exception e) {
	    StringWriter sw = new StringWriter();
	    e.printStackTrace(new PrintWriter(sw, true));
		String sError = null;
	    try {
	    	sError = cqUtil.filterXMLReservedChars(sw.toString());
	    } catch (Exception ignore) {
	    	sError = sw.toString();
	    }
    	String sMessage = "Error in cqService.getCqUserDb: " 
			+ sError;
    	System.err.println(sMessage);
    	throw new Exception(sMessage);

	} finally {
		feedback = null;
		xSet = null;
		xList = null;
		itList = null;
		xListDb = null;
		itListDb = null;
	}
	return xUserDb;
    }

    /**
     * Use the specified simple date format to format the specified
     * date-time and return the result as a string.
     *
     * @param       dtDate   Date object to format.
     * @param       sFormat     Format to apply.
     * @exception   mdException Wrapper around any Simple Date
     *                          Format exception.
     * @return                  Formatted date-time string.
     */
     public static String dateToString(Date dtDate)
     throws mdException
     {

         String sDateString = "";

         try {
             if (dtDate != null) {
            	 if (S$sdfFormat == null) {
            		 S$sdfFormat = new SimpleDateFormat(F$sDateFormat);
                     S$sdfFormat.setTimeZone(TimeZone.getDefault());
            	 }
                 sDateString = S$sdfFormat.format(dtDate);
             }

         } catch (Exception e) {
     	    StringWriter sw = new StringWriter();
    	    e.printStackTrace(new PrintWriter(sw, true));
         }

         return sDateString;
     }



}
