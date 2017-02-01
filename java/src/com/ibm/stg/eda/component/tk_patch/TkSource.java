/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2009 -- IBM Internal Use Only
*
*=============================================================================
*
* CREATOR: Gregg Stadtlander
*    DATE: 12/30/2009
*
*-PURPOSE---------------------------------------------------------------------
* Class for EDA Tool Kit patch source data
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 12/30/2009 GFS  Initial coding.
* 02/17/2010 GFS  Added support for extracting a track from CMVC.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_patch;

import java.io.File;
import java.util.Vector;

import com.ibm.stg.iipmds.common.IcofCollectionsUtil;
import com.ibm.stg.iipmds.common.IcofException;


public class TkSource implements java.io.Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 8909850144032051871L;


	/**
     * Constructor
     * 
     * @param aName
     * @param aFullPath
     * @param aShortCut
     * @param bIsActive
     * @throws IcofException 
     */
    public TkSource(String aName, String aFullPath, 
                    boolean bIsActive) throws IcofException {
        setName(aName);
        setFullPath(aFullPath);
        setIsActive(bIsActive);
        setIsPathValid();
        
    }

    
    /**
     * Sets the repository based on the full path.
     * @throws IcofException 
     */
    private void setRepository() throws IcofException {
        
        // From CMVC?
        if (getFullPath().equals(TkInjectUtils.CMVC_FILE_EXT)) {
            setRepository(TkInjectUtils.CMVC_FILE_EXT);
        }
        if (getFullPath().equals(TkInjectUtils.CMVC_TRACK_EXT)) {
            setRepository(TkInjectUtils.CMVC_TRACK_EXT);
        }
        else if (getFullPath().indexOf(TkInjectUtils.BUILD_DIR) > -1) {
            setRepository(getFullPath());
        }
        else if (getFullPath().indexOf(TkInjectUtils.DEV_DIR) > -1) {
            setRepository(getFullPath());
        }
        else if (getFullPath().indexOf(TkInjectUtils.PROD_DIR) > -1) {
            setRepository(getFullPath());
        }
        else if (getFullPath().indexOf(TkInjectUtils.TKB_DIR) > -1) {
        	setRepository(getFullPath());
        }
        else if (getFullPath().length() > 0) {
            setRepository(TkInjectUtils.OTHER);
        }
        else {
            setRepository("EMPTY");
        }
        
    }


    
    
    /**
     * Constructor
     * 
     * @param aName
     * @param aFullPath
     * @param aRepository
     * @param aShortCut
     * @param bIsActive
     * @throws IcofException 
     */
    public TkSource(String aName, String aFullPath, String aRepository, 
                    boolean bIsActive) throws IcofException {
        setName(aName);
        setFullPath(aFullPath);
        setRepository(aRepository);
        setIsActive(bIsActive);
        setIsPathValid();
        
        
    }
    
    /*
     * Constants
     */
    public static String CLASS_NAME = "TkSource";
    
    
    /*
     * Getters
     */
    public String getName() { return name; }
    public String getFullPath() { return fullPath;  }
    public String getRepository() { return repository;  }
    public boolean isActive() { return isActive; }
    public boolean isPathValid() { return isPathValid; }
    
    
    /*
     * Setters
     */
    private void setName(String aName) { name = aName; }
    public void setFullPath(String aPath) throws IcofException { 
        fullPath = aPath;
        setRepository();
    }
    public void setRepository(String aPath) throws IcofException {
        
        if (aPath.equals(TkInjectUtils.OTHER) || 
            aPath.equals(TkInjectUtils.CMVC_FILE_EXT) ||
            aPath.equals(TkInjectUtils.CMVC_TRACK_EXT) || aPath.equals("EMPTY")) {
            repository = aPath;
        }
        else {
        
            // Otherwise create the repository entry from the full path.  The repository 
            // should be the /afs/eda/build/hdp/13.1 part of the full path 
            // (ie, first 5 sub dirs).
            Vector<String> tokens = new Vector<String>();
            IcofCollectionsUtil.parseString(aPath, File.separator, tokens, true);
            repository = "";
            for (int i = 1; (i < tokens.size()) && (i < 6); i++) {
                repository += File.separator + (String) tokens.get(i);
            }

        }
        
    }
    public void setIsActive(boolean aState) { isActive = aState; }
    public void setIsPathValid(boolean aState) { isPathValid = aState; }
    public void setIsPathValid() { 
        if (TkInjectUtils.getFullLocationPath(name, fullPath) == null) {
            isPathValid = false;
        }
        else {
            isPathValid = true;
        }
    }
    
    
    /*
     * Members
     */
    private String name;
    private String fullPath;
    private String repository;
    private boolean isActive  = true;
    private boolean isPathValid = false;
   
}
