/**
 * <pre>
 * =============================================================================
 *
 *  IBM Internal Use Only
 *
 * =============================================================================
 *
 *  CREATOR: Aydin Suren
 *     DATE: 02/04/2009
 *     
 *-PURPOSE---------------------------------------------------------------------
 * Class definition for the CQ sys_application_usage.
 *-----------------------------------------------------------------------------
 *
 * =============================================================================
 *
 * -CHANGE LOG------------------------------------------------------------------
 * 02/04/2009 AS  Initial coding.
 *
 * =============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.clearquest;

import com.ibm.rational.wvcm.stp.StpLocation;
import com.ibm.rational.wvcm.stp.cq.CqProvider;
import com.ibm.rational.wvcm.stp.cq.*;
import javax.wvcm.PropertyRequestItem;
import javax.wvcm.PropertyRequestItem.PropertyRequest;

/**
 * @author asuren
 * 
 */
public class cqSysAppUsage {

    private final String CREATE_ACTION = "cq.record:sys_application_usage/new@";

    /** Required CQ database sys_application_usage table attributes. */
    private String sys_cq_service = null;
    private String service_requester_name = null;
    private String userlogin = null;

    /**
     * Constructor.
     */
    public cqSysAppUsage(String sys_cq_service, String service_requester_name,
	    String userlogin) {
    	this.sys_cq_service = sys_cq_service;
    	this.service_requester_name = service_requester_name;
    	this.userlogin = userlogin;
    }

    /**
     * Populates values of Rational CqRecord Team API object.
     * 
     * @param cqRecord
     * @throws Exception
     */
    private void populateProperties(CqRecord cqRecord) throws Exception {

    	 cqRecord.setField(cqUtil.SYS_CQ_SERVICE, sys_cq_service);
    	 cqRecord.setField(cqUtil.SERVICE_REQUESTER_NAME, service_requester_name);
    	 cqRecord.setField(cqUtil.USERLOGIN, userlogin);
    }

    /**
     * Prepares a StpLocation team API object based on the action type.
     * 
     * @param provider
     *            CqProvider team API object.
     * @param sDatabase
     *            CQ database that user is trying to connect.
     * @return
     * @throws Exception
     */
    private StpLocation getCqLocation(CqProvider provider, String sSchema,
	    String sDatabase) throws Exception {

    	String sLocation = CREATE_ACTION + sSchema + "/" + sDatabase;
    	StpLocation stpLocation = (StpLocation) provider.location(sLocation);

    	return stpLocation;
    }

    protected void recordUsageToCQ(CqProvider provider, String sSchema,
	    String sDatabase) 
    throws Exception {
    	
    	StpLocation stpLocation = null;
    	CqRecord newTeamCqRecord = null;
    	PropertyRequest request = null;

    	try {
    		stpLocation = this.getCqLocation(provider, sSchema,
    				sDatabase);
    		newTeamCqRecord = provider.cqRecord(stpLocation);

    		// Commit the record.
    		request = new PropertyRequest(
    				new PropertyRequestItem[] {CqRecord.ALL_PROPERTIES});
			
    		populateProperties(newTeamCqRecord);

    		newTeamCqRecord.doCreateRecord(request, CqProvider.DELIVER_ALL);
    		
    	} catch (Exception e) {
		    e.printStackTrace();
		}
    }

}
