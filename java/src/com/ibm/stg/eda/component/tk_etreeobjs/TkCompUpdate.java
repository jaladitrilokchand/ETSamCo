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
* ComponentUpdate business object.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 02/07/2011 GFS  Initial coding.
* 08/29/2011 GFS  Updated to use new CodeUpdate_Db constructor.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_etreeobjs;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreedb.CodeUpdate_Db;
import com.ibm.stg.eda.component.tk_etreedb.ComponentUpdate_Db;
import com.ibm.stg.eda.component.tk_etreedb.FunctionalUpdate_Db;
import com.ibm.stg.iipmds.common.IcofException;

public class TkCompUpdate  {

    /**
     * Constructor - takes a DB id
     * 
     * @param  aCompUpdateId  ComponentUpdate database id
     * @throws IcofException 
     */
    public TkCompUpdate(EdaContext xContext, long anId) 
    throws IcofException {
        setCompUpdate(xContext, anId);
        setFuncUpdates(xContext);
    }


    /**
     * Constructor - takes a DB id
     * 
     * @param aCompUpdateId  ComponentUpdate database id
     * @param aFuncUpdateId  FunctionalUpdate database id
     * @throws IcofException 
     */
    public TkCompUpdate(EdaContext xContext, short aCompUpdateId, 
                        short aFuncUpdateId) 
    throws IcofException {
        setCompUpdate(xContext, aCompUpdateId);
        setFuncUpdate(xContext, aFuncUpdateId);
        setFuncUpdates(xContext);
    }


    /**
     * Data Members
     */
    private ComponentUpdate_Db compUpdate;
    private FunctionalUpdate_Db funcUpdate;
    private Hashtable<String,TkFuncUpdate> funcUpdates;
    
    
    /**
     * Getters
     */
    public ComponentUpdate_Db getCompUpdate() { return compUpdate; }
    public FunctionalUpdate_Db getFuncUpdate() { return funcUpdate; }
    public Hashtable<String,TkFuncUpdate>  getFuncUpdates() { return funcUpdates; }


    /**
     * Set the FunctionalUpdate object from the database id
     * 
     * @param xContext   Application context.
     * @param anId       A FunctionalUpdate id
     * @throws IcofException 
     */
    private void setCompUpdate(EdaContext xContext, long anId) 
    throws IcofException {
        if (getCompUpdate() == null) {
            compUpdate = new ComponentUpdate_Db(anId);
            compUpdate.dbLookupById(xContext);
        }            
    }

    /**
     * Set the FunctionalUpdate object from the database id
     * 
     * @param xContext   Application context.
     * @param anId       A FunctionalUpdate id
     * @throws IcofException 
     */
    private void setFuncUpdate(EdaContext xContext, short anId) 
    throws IcofException {
        if (getFuncUpdate() == null) {
            funcUpdate = new FunctionalUpdate_Db(anId);
            funcUpdate.dbLookupById(xContext);
        }            
    }

    
    /**
     * Read the CodeUpdate objects from the database 
     * 
     * @param xContext       Application context.
     * @param aCodeUpdateId  Id of CodeUpdate object to look up.
     * @throws IcofException 
     */
    private void setFuncUpdates(EdaContext xContext) 
    throws IcofException {

    	// Read the CodeUpdates for this ComponentUpdate. At this time
    	// FuncUpdates have the same id as CodeUpdates.
    	
        // Read the CodeUpdate_Db objects for this FunctionalUpdate
        Vector<CodeUpdate_Db> codeUps = new Vector<CodeUpdate_Db>();
        CodeUpdate_Db cu = new CodeUpdate_Db(null, "", "", "", null,
        		                             null);
        codeUps = cu.dbLookupByCompUpdate(xContext);
        
        // Initialize the collection
        if (getFuncUpdates() == null) {
            funcUpdates = new Hashtable<String,TkFuncUpdate> ();
        }
        funcUpdates.clear();
        
        // Convert the CodeUpdate_Db objects into TkFunctUpdate objects.
        Iterator<CodeUpdate_Db> iter = codeUps.iterator();
        while (iter.hasNext()) {
            CodeUpdate_Db myCu = iter.next();
            TkFuncUpdate tkFu = new TkFuncUpdate(xContext, (short)myCu.getId());
            funcUpdates.put(tkFu.getFuncUpdate().getIdKey(xContext), tkFu);
        }
        
    }

    
}
