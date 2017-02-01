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
 * =============================================================================
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
 */
public abstract class cqInput {

    /**
     * Transforms the memory representation of an object to a data format such
     * as XML.
     * 
     * @return
     * @throws Exception
     */
    protected abstract String marshall() throws Exception;
}
