/**
 * <pre>
 * 
 *  CREATOR: Aydin Suren
 *     
 *-PURPOSE----------------------------------------------------------------------------------------
 * Class definition for the CQ user.
 *------------------------------------------------------------------------------------------------------
 *
 * -CHANGE LOG------------------------------------------------------------------
 * 03/01/2008 AS  Initial coding.
 *
 * =============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.clearquest;

/**
 * @author asuren
 * 
 * 9/23/2010, all schema and database names are uppercase in ClearQuest.
 * Users should be allowed to enter lower case, however, and they will
 * be uppercased there - Tom Peterson
 * 
 */
public class cqUser {

    private String username = null;
    private String password = null;
    private String database = null;
    private String schema = null;

    /**
     * Constructor.
     */
    public cqUser() {
    }

    /**
     * Constructor.
     * 
     * @param username
     * @param password
     * @param database
     * @param scheam
     */
    public cqUser(String username, String password, String database,
	    String schema) {
	this.username = username;
	this.password = password;
	this.database = database;
	if (this.database != null) this.database = this.database.toUpperCase();
	this.schema = schema;
	if (this.schema != null) this.schema = this.schema.toUpperCase();
    }

    /**
     * Returns the value.
     * 
     * @return
     */
    public String getDatabase() {
	return database;
    }

    /**
     * Returns the value.
     * 
     * @return
     */
    public String getPassword() {
	return password;
    }

    /**
     * Returns the value.
     * 
     * @return
     */
    public String getSchema() {
	return schema;
    }

    /**
     * Returns the value.
     * 
     * @return
     */
    public String getUsername() {
	return username;
    }

}
