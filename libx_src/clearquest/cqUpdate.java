/**
 * <pre>
 * =============================================================================
 * 
 *  CREATOR: Aydin Suren
 *     DEPT: AW0V
 *         
 *-PURPOSE----------------------------------------------------------------------------------------
 * Class definition for the CQ Update.
 *------------------------------------------------------------------------------------------------------
 *
 * =============================================================================
 *
 * -CHANGE LOG------------------------------------------------------------------
 * 06/24/2009 AS  Initial coding.
 *
 * =============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.clearquest;

import java.util.Vector;

/**
 * @author asuren
 * 
 */
public class cqUpdate {

    private String record_type = null;
    private String record_id = null;
    private String action = null;

    /** Collection of cqField objects. */
    private Vector vcField = null;

    /**
     * Constructor.
     * 
     * @param record_type
     *            Record type.
     * @param record_id
     *            Record id.
     * @param action
     *            Action name.
     * @param vcField
     *            Collection of cqField objects if this action requires any.
     */
    public cqUpdate(String record_type, String record_id, String action,
	    Vector vcField) {
	super();
	this.record_type = record_type;
	this.record_id = record_id;
	this.action = action;
	this.vcField = vcField;
    }

    /**
     * Returns collection of the collection of cqField objects.
     * 
     * @return
     */
    public Vector getFields() {
	return vcField;
    }

    /**
     * Returns the record type.
     * 
     * @return record_type value
     */
    public String getRecordType() {
	return record_type;
    }

    /**
     * Returns the record id.
     * 
     * @return record_id value
     */
    public String getRecordID() {
	return record_id;
    }

    /**
     * Returns the action name.
     * 
     * @return action value
     */
    public String getAction() {
	return action;
    }

}
