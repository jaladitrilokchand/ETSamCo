/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2010 -- IBM Internal Use Only
*
*=============================================================================
*
* CREATOR: Gregg Stadtlander
*    DATE: 06/02/2010
*
*-PURPOSE---------------------------------------------------------------------
* FunctionalUpdate business object.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 05/18/2010 GFS  Initial coding.
* 07/25/2010 GFS  Removed newline from LEVEHHIST entry. Updated to use 
*                 PreparedStatements.
* 08/11/2010 GFS  Updated getLevelHistEntry() to remove newlines from 
*                 description and limit description length to 62 chars. 
* 11/11/2010 GFS  Moved db queries to CodeUpdate_FunctionalUpdate_Db object.              
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_etreeobjs;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkConstants;
import com.ibm.stg.eda.component.tk_etreebase.TkLevelHistUtils;
import com.ibm.stg.eda.component.tk_etreedb.CodeUpdate_Db;
import com.ibm.stg.eda.component.tk_etreedb.CodeUpdate_FunctionalUpdate_Db;
import com.ibm.stg.eda.component.tk_etreedb.FunctionalUpdate_Db;
import com.ibm.stg.iipmds.common.IcofException;

public class TkFuncUpdate  {

    /**
     * Constructor - takes a DB id
     * 
     * @param aFuncUpdateId  FunctionalUpdate database id
     * @throws IcofException 
     */
    public TkFuncUpdate(EdaContext xContext, short aFuncUpdateId) 
    throws IcofException {
        setFuncUpdate(xContext, aFuncUpdateId);
        setCodeUpdates(xContext);
    }


    /**
     * Constructor - takes a DB id
     * 
     * @param aFuncUpdateId  FunctionalUpdate database id
     * @param aCodeUpdateId  CodeUpdate database id
     * @throws IcofException 
     */
    public TkFuncUpdate(EdaContext xContext, short aFuncUpdateId, 
                        short aCodeUpdateId) 
    throws IcofException {
        setFuncUpdate(xContext, aFuncUpdateId);
        setCodeUpdate(xContext, aCodeUpdateId);
        setCodeUpdates(xContext);
    }


    /**
     * Data Members
     */
    private FunctionalUpdate_Db funcUpdate;
    private CodeUpdate_Db codeUpdate;
    private Hashtable<String,TkCodeUpdate> codeUpdates;
    
    
    /**
     * Getters
     */
    public FunctionalUpdate_Db getFuncUpdate() { return funcUpdate; }
    public CodeUpdate_Db getCodeUpdate() { return codeUpdate; }
    public Hashtable<String,TkCodeUpdate> getCodeUpdates() { return codeUpdates; }


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
     * Set the CodeUpdate object from the database id
     * 
     * @param xContext   Application context.
     * @param anId       A CodeUpdate id
     * @throws IcofException 
     */
    private void setCodeUpdate(EdaContext xContext, short anId) 
    throws IcofException {
        if (getCodeUpdate() == null) {
            codeUpdate = new CodeUpdate_Db(anId);
            codeUpdate.dbLookupById(xContext);
        }            
    }

    
    /**
     * Read the CodeUpdate objects from the database 
     * 
     * @param xContext       Application context.
     * @param aCodeUpdateId  Id of CodeUpdate object to look up.
     * @throws IcofException 
     */
    private void setCodeUpdates(EdaContext xContext) 
    throws IcofException {

        // Read the CodeUpdate_Db objects for this FunctionalUpdate
        Hashtable<String,CodeUpdate_Db> cus = new Hashtable<String,CodeUpdate_Db>();
        CodeUpdate_FunctionalUpdate_Db cufu = 
            new CodeUpdate_FunctionalUpdate_Db(null, getFuncUpdate());
        cus = cufu.dbLookupCodeUpdates(xContext);
        
        // Initialize the collection
        if (getCodeUpdates() == null) {
            codeUpdates = new Hashtable<String,TkCodeUpdate>();
        }
        codeUpdates.clear();
        
        // Convert the CodeUpdate_Db objects into TkCodeUpdate objects.
        Iterator<CodeUpdate_Db> iter = cus.values().iterator();
        while (iter.hasNext()) {
            CodeUpdate_Db cu =  iter.next();
            TkCodeUpdate tkCu = new TkCodeUpdate(xContext, cu.getId());
            codeUpdates.put(tkCu.getCodeUpdate().getIdKey(xContext), tkCu);
        }
        
    }
    

    /**
     * Return the Functional Update LEVELHIST entry
     *  
     * Like this ...
     * MDCMS000?????: functional_update_description
     * DB_ID: 11
     * 
     * @param xContext       Application context.
     */
    public Vector<String> getLevelHistEntry() {

        Vector<String> entries = new Vector<String>();
       
        // Prepare the functional update description
        String entry = TkConstants.CQ_DUMMY + ": ";
        String desc = getFuncUpdate().getDescription();
        
        // Replace newlines with spaces.
        if (desc.indexOf("\n") > -1)
            desc = desc.replaceAll("\\n"," ");
        
        // Show only the first 62 chars if description is longer than 62 chars.
        if (desc.length() > TkLevelHistUtils.MAX_COMMENT_WIDTH)
            desc = desc.substring(0, TkLevelHistUtils.MAX_COMMENT_WIDTH) + " ...";
        
        entries.add(entry + desc);
        
        
        // Prepare the functional update db id.
        entry = TkConstants.DB_ID + ": " + getFuncUpdate().getId();
        entries.add(entry);
        
        return entries;

    }
    
}
