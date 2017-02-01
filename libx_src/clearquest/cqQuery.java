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
 * Class definition for the CQ query.
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

/**
 * @author asuren
 *
 */
public class cqQuery {

	private String queryName = null;
	private Vector vcFilter = null;

	/**
     * Constructor.
     * 
	 * @param queryName    Name of the query.
	 * @param vcFilter           Filters if this query requires any.
	 */
	public cqQuery(String queryName, Vector vcFilter) {
		this.queryName = queryName;
		this.vcFilter = vcFilter;
	}

	/**
     * Retuns collection of the filters.
     * 
	 * @return
	 */
	public Vector getFilter() {
		return vcFilter;
	}

	/**
     * Retuns the query name.
	 * @return
	 */
	public String getQueryName() {
		return queryName;
	}

}
