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
* CodeUpdate business object.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 05/18/2010 GFS  Initial coding.
* 07/25/2010 GFS  Removed newline from LEVEHHIST entry. Updated to use 
*                 PreparedStatements.
* 11/11/2010 GFS  Moved db queries to DeliverableUpdate_CodeUpdate_Db object.
* 06/27/2011 GFS  Updated setSourceFiles to fix a bug when more than 1 source
*                 files is associated with the revision.
* 08/29/2011 GFS  Update getLevelHistEntry to construct the Code Update header
*                 which was previously created in the TkFuncUpdate object.
* 09/09/2011 GFS  Added hasChangeReqs() and updated getLevelHistEntry() to 
*                 ignore the dummy MDCMS entry if this Code Update has a CR.                
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
import com.ibm.stg.eda.component.tk_etreedb.ChangeRequest_Db;
import com.ibm.stg.eda.component.tk_etreedb.CodeUpdate_ChangeRequest_Db;
import com.ibm.stg.eda.component.tk_etreedb.CodeUpdate_Db;
import com.ibm.stg.eda.component.tk_etreedb.FileVersion_Db;
import com.ibm.stg.iipmds.common.IcofException;

public class TkCodeUpdate  {

    /**
     * Constructor - takes a DB id
     * 
     * @param aCodeUpdateId  CodeUpdate database id
     * @throws IcofException 
     */
    public TkCodeUpdate(EdaContext xContext, long aCodeUpdateId) 
    throws IcofException {
        setCodeUpdate(xContext, aCodeUpdateId);
        setSourceFiles(xContext);
    }

    
    /**
     * Data Members
     */
    private CodeUpdate_Db codeUpdate;
    private Hashtable<String,TkFileVersion> sourceFiles;
    
    
    /**
     * Getters
     */
    public CodeUpdate_Db getCodeUpdate() { return codeUpdate; }
    public Hashtable <String,TkFileVersion>getSourceFiles(EdaContext xContext) throws IcofException { 
        if ((sourceFiles == null) || (sourceFiles.size() < 1)) {
            setSourceFiles(xContext);
        }
        return sourceFiles; 
    }

    /**
     * Setters
     */

   
    /**
     * Set the CodeUpdate object from the database id
     * 
     * @param xContext   Application context.
     * @param anId       A FunctionalUpdate id
     * @throws IcofException 
     */
    private void setCodeUpdate(EdaContext xContext, long anId) 
    throws IcofException {
        if (codeUpdate == null) {
            codeUpdate = new CodeUpdate_Db(anId);
            codeUpdate.dbLookupById(xContext);
        }            
    }

    
    /**
     * Read the FileName objects from the database for this CodeUpdate. 
     * 
     * @param xContext   Application context.
     * @throws IcofException 
     */
    private void setSourceFiles(EdaContext xContext) 
    throws IcofException {

        // Initialize the collection
        if (sourceFiles == null) {
            sourceFiles = new Hashtable<String,TkFileVersion>();
        }
        sourceFiles.clear();
        

        // Query the database.
        Hashtable<String,FileVersion_Db> myFiles = new Hashtable<String,FileVersion_Db>();
        FileVersion_Db files = new FileVersion_Db(getCodeUpdate(), null, null);
        myFiles = files.dbLookupFiles(xContext);
        
        // Convert the TkFileVersion_db objects to tkFileVersion objects.
        Iterator<FileVersion_Db> iter = myFiles.values().iterator();
        while (iter.hasNext()) {
            FileVersion_Db myFile =  iter.next();
            TkFileVersion file = new TkFileVersion(xContext, 
                                                   myFile.getFileName(),
                                                   myFile.getFileAction());
            sourceFiles.put(file.getFile().getName() + ":" + file.getAction().getName(), file);
            
        }
        
    }


    /**
     * Return the Code Update LEVELHIST entry 
     * 
     * Like ...
     *  MDCMS000?????: functional_update_description
     *  DB_ID: 11
     *  15 export.includes U amagnan@us.ibm.com
     *  15 sub_dir/file_A A amagnan@us.ibm.com

     * 
     * @param developer A developer's name
     * @throws IcofException 
     */
    public Vector<String> getLevelHistEntry(EdaContext xContext) throws IcofException {

        Vector<String> entries = new Vector<String>();
        
        /*
         * Create the Code Update header
         */
        String desc = getCodeUpdate().getDescription();
        String entry = "";
        if (! hasChangeRequest(xContext)) {
        	entry = TkConstants.CQ_DUMMY + ": ";
        }
        
        // Replace newlines with spaces.
        if (desc.indexOf("\n") > -1)
            desc = desc.replaceAll("\\n"," ");
        
        // Show only the first 62 chars if description is longer than 62 chars.
        if (desc.length() > TkLevelHistUtils.MAX_COMMENT_WIDTH)
            desc = desc.substring(0, TkLevelHistUtils.MAX_COMMENT_WIDTH) + " ...";
        
        entries.add(entry + desc);
        
        
        // Prepare the Code Update database id.
        //entry = TkConstants.DB_ID + ": " + getCodeUpdate().getId();
        //entries.add(entry);
        
        //  Prepare the source files
        Iterator<TkFileVersion> iter = getSourceFiles(xContext).values().iterator();
        while (iter.hasNext()) {
            TkFileVersion file =  iter.next();
            entries.add(getCodeUpdate().getRevision() + TkConstants.LH_DELIM +
                         file.getFile().getName() + TkConstants.LH_DELIM + 
                         file.getAction().getSvnName() + TkConstants.LH_DELIM + 
                         getCodeUpdate().getCreatedBy());
            
        }
        
        return entries;

    }
    
    
    /**
     * Determine if this CodeUpdate is associated with a ChangeRequest
     * @param xContext Application context
     * @return True if this Code Update is associated with a CR otherwise false
     * @throws IcofException 
     */
	private boolean hasChangeRequest(EdaContext xContext) throws IcofException {

		CodeUpdate_ChangeRequest_Db cucr = 
			new CodeUpdate_ChangeRequest_Db(getCodeUpdate(), null);
		Hashtable<String,ChangeRequest_Db> changeReqs = cucr.dbLookupChangeRequests(xContext);
		
		boolean result = false;
		if ((changeReqs != null) && (changeReqs.size() > 0)) {
			result = true;
		}
		
		return result;
		
	}
    
}
