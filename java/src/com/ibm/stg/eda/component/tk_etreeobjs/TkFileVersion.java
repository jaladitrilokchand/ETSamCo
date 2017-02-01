/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2010 -- IBM Internal Use Only
*
*=============================================================================
*
* CREATOR: Gregg Stadtlander
*    DATE: 06/18/2010
*
*-PURPOSE---------------------------------------------------------------------
* TkFileVersion business object.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 06/18/2010 GFS  Initial coding.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_etreeobjs;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreedb.FileActionName_Db;
import com.ibm.stg.eda.component.tk_etreedb.FileName_Db;
import com.ibm.stg.iipmds.common.IcofException;

public class TkFileVersion  {

    /**
     * Constants.
     */

    
    /**
     * Constructor - takes objects
     * 
     * @param xContext  Application context
     * @param aFile     A FileName object
     * @param anAction  A FileActionName object
     */
    public TkFileVersion(EdaContext xContext, FileName_Db aFile,
                         FileActionName_Db anAction) {
        setFile(aFile);
        setAction(anAction);
        
    }

    
    /**
     * Constructor - takes IDs
     * 
     * @param xContext  Application context
     * @param aFileId     A FileName object id
     * @param anActionId  A FileActionName object id
     * @throws IcofException 
     */
    public TkFileVersion(EdaContext xContext, long aFileId, 
                         short anActionId) 
    throws IcofException {
        setFile(xContext, aFileId);
        setAction(xContext, anActionId);
        
    }

    
    /**
     * Data Members
     */
    private FileName_Db file;
    private FileActionName_Db action;
    
    
    /**
     * Getters
     */
    public FileName_Db getFile() { return file; }
    public FileActionName_Db getAction() { return action; }


    /**
     * Setters
     */
    private void setFile(FileName_Db aFile) { file = aFile; }
    private void setAction(FileActionName_Db anAction) { action = anAction; }

    
    /**
     * Set the FileName object from the database id
     * 
     * @param xContext   Application context.
     * @param anId       A FileName id
     * @throws IcofException 
     */
    private void setFile(EdaContext xContext, long anId) 
    throws IcofException {
        if (getFile() == null) {
            file = new FileName_Db(anId);
            file.dbLookupById(xContext);
        }            
    }

    
    /**
     * Set the FileActionName object from the database id
     * 
     * @param xContext   Application context.
     * @param anId       A FileActionName id
     * @throws IcofException 
     */
    private void setAction(EdaContext xContext, short anId) 
    throws IcofException {
        if (getAction() == null) {
            action = new FileActionName_Db(anId);
            action.dbLookupById(xContext);
        }            
    }
    
    /**
     * Create a key from the ID.
     * 
     *  @param xContext  Application context object.
     *  @return          A Statement object.
     */
    public String getIdKey(EdaContext xContext) {
        return String.valueOf(getFile().getId());
    }

}
