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
 * Class definition for the CQ record.
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

import java.util.Vector;

import javax.wvcm.PropertyNameList.PropertyName;

import com.ibm.rational.wvcm.stp.StpLocation.Namespace;
import com.ibm.rational.wvcm.stp.cq.CqRecord;

/**
 * @author asuren
 * 
 */
public class cqRecord {

    // Vector of cqField objects.
    private Vector _vcFields = null;

    // Record id.
    private String id = null;

    /**
     * Contractor.
     */
    public cqRecord() {
    }

    /**
     * Contractor.
     * 
     * @param vcFields
     *            Collection of cqField objects.
     */
    public cqRecord(Vector vcFields) {
	this._vcFields = vcFields;
    }

    /**
     * Constructor.
     * 
     * @param id
     */
    public cqRecord(String id) {
	this.id = id;
    }

    /**
     * Returns the record type.
     * 
     * @return Record type such as Problem, tk_patch and etc.
     * @throws Exception
     */
    public String getRecordType() throws Exception {
	String sReturn = null;

	// Iterate thru all the fields to find the record type.
	if (_vcFields != null && _vcFields.size() > 0) {
	    for (int i = 0; i < _vcFields.size(); i++) {
		cqField xCqField = (cqField) _vcFields.get(i);
		if (xCqField.getName().equalsIgnoreCase("record_type")) {
		    sReturn = xCqField.getValue();
		}
	    }
	}

	return sReturn;
    }

    /**
     * Returns the record id.
     * 
     * @return Record id such as TRYIT99999999.
     * @throws Exception
     */
    public String getId() throws Exception {

	if (id == null) {
	    // Iterate thru all the fields to find the record id.
	    if (_vcFields != null && _vcFields.size() > 0) {
		for (int i = 0; i < _vcFields.size(); i++) {
		    cqField xCqField = (cqField) _vcFields.get(i);
		    if (xCqField.getName().equalsIgnoreCase("id")) {
			id = xCqField.getValue();
		    }
		}
	    }
	}

	return id;
    }

    /**
     * @return Collection of cqField objects.
     */
    public Vector getFields() {
	return _vcFields;
    }

    /**
     * Populates values of Rational CqRecord Team API object.
     * This method should not be used anymore, as it populates property
     * values, not field values.
     * 
     * @param cqRecord
     * @throws Exception
     */
    public void populateProperties(CqRecord cqRecord) throws Exception {

	    String sFieldNamespace = Namespace.FIELD_DEFINITION.toNamespaceField();
	    // Iterate thru all the fields and assigned them.
	    if (_vcFields != null && _vcFields.size() > 0) {
	    	for (int i = 0; i < _vcFields.size(); i++) {
	    		cqField xCqField = (cqField) _vcFields.get(i);
	    		String name = xCqField.getName();
	    		String value = xCqField.getValue();
	    		if (!name.equalsIgnoreCase("record_type")) {
		    		cqRecord.setProperty(
				    		new PropertyName(null, name), value);
	    		}
	    	}
	    }
    }

    /**
     * Populates values of Rational CqRecord Team API object.
     * 
     * @param cqRecord
     * @throws Exception
     */
    public void populateFields(CqRecord cqRecord) throws Exception {

	    // Iterate thru all the fields and assigned them.
	    if (_vcFields != null && _vcFields.size() > 0) {

	    	for (int i = 0; i < _vcFields.size(); i++) {
	    		cqField xCqField = (cqField) _vcFields.get(i);
	    		String name = xCqField.getName();
	    		String value = xCqField.getValue();
	    		if (!name.equalsIgnoreCase("record_type")) {
	    			cqRecord.setField(name, value);
	    		}
	    	}
	    }
    }

    /**
     * @return XML representation of the record object.
     * @throws Exception
     */
    public StringBuffer getXmlRepresentation() throws Exception {

	StringBuffer sbReturn = new StringBuffer("    <record>");

	// Iterate thru all the fields.
	if (_vcFields != null && _vcFields.size() > 0) {
	    for (int i = 0; i < _vcFields.size(); i++) {
		cqField xCqField = (cqField) _vcFields.get(i);
		String name = xCqField.getName();
		String value = xCqField.getValue();
		sbReturn.append("\n<" + name + ">" + value + "</" + name + ">");
	    }
	}
	sbReturn.append("\n    </record>\n");
	return sbReturn;
    }
}
