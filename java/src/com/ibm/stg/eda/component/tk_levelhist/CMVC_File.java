/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2009 -- IBM Internal Use Only
*
*=============================================================================
*
* CREATOR: Gregg Stadtlander
*    DATE: 12/23/2009
*
*-PURPOSE---------------------------------------------------------------------
* Class for EDA Tool Kit LEVELHIST CMVC file data
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 12/23/2009 GFS  Initial coding.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_levelhist;

import java.util.Vector;

public class CMVC_File implements java.io.Serializable {

    
    /**
	 * 
	 */
	private static final long serialVersionUID = -8875754059659891652L;
	/**
     * Constructor 

     * @param aLine
     * @param aPath
     * @param aSid
     * @param aType
     * @param aDeveloper
     * @param aDescription
     */
    public CMVC_File(String aLine,
                     String aPath,
                     String aSid,
                     String aType,
                     String aDeveloper,
                     Vector<String>  aDescription) {
        super();
        setLine(aLine);
        setPath(aPath);
        setSid(aSid);
        setType(aType);
        setDeveloper(aDeveloper);
        setDescription(aDescription);

    }

    
    /*
     * Getters
     */
    public String getLine() { return line; }
    public String getSid() { return sid; }
    public String getPath() { return path; }
    public String getType() { return type; }
    public String getDeveloper() { return developer; }
    public Vector<String>  getDescription() { return description; }
    
    
    /*
     * Setters
     */
    private void setLine(String aLine) { line = aLine; }
    private void setSid(String aSid) { sid = aSid; }
    private void setPath(String aPath) { path = aPath; }
    private void setType(String aType) { type = aType; }
    private void setDeveloper(String aDeveloper) { developer = aDeveloper; }
    private void setDescription(Vector<String>  descriptions) { description = descriptions; }
    
    
    /*
     * Members
     */
    private String line;
    private String sid;
    private String path;
    private String type;
    private String developer;
    private Vector<String>  description;

    
    
}