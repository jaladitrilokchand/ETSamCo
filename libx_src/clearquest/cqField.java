/**
 * <pre>
 * =============================================================================
 *  Copyright: (C) IBM Corporation 2009 -- IBM Internal Use Only
 * =============================================================================
 *  CREATOR: Aydin Suren
 * 
 * -PURPOSE---------------------------------------------------------------------
 *  To hold field information
 * -----------------------------------------------------------------------------
 * 
 * -CHANGE LOG------------------------------------------------------------------
 *  06/24/2009 AS  Initial coding.
 * =============================================================================
 * </pre>
 */
package com.ibm.stg.eda.component.clearquest;

/**
 * @author asuren
 * 
 */

public class cqField {

    private String name = null;
    private String operator = null;
    private String value = null;

    /**
     * Constructor.
     * 
     * @param name
     *            Name of the field
     * @param value
     *            Name of the value
     */
    public cqField(String name, String value) {
	super();
	this.name = name;
	this.value = value;
    }

    /**
     * Constructor.
     * 
     * @param name
     *            Name of the field
     * @param value
     *            Name of the value
     */
    public cqField(String name, String operator, String value) {
	super();
	this.name = name;
	this.operator = operator;
	this.value = value;
    }

    /**
     * @return The name of the field.
     */
    public String getName() {
	return name;
    }

    /**
     * @return The operator.
     */
    public String getOperator() {
	return operator;
    }

    /**
     * @return The value of the field.
     */
    public String getValue() {
	return value;
    }

}
