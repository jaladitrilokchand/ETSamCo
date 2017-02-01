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
* Class for EDA Tool Kit LEVELHIST CMVC track data
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 12/23/2009 GFS  Initial coding.
* 02/09/2010 GFS  Changed files collection from Vector to Hashtable.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_levelhist;

import java.util.Hashtable;

public class CMVC_Track implements java.io.Serializable {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 593887299644162787L;
	/**
     * Constructor
     * 
     * @param aLine
     * @param aName
     * @param anAbstractText
     * @param aChangeType
     * @param aDate
     * @param aLevel
     * @param someFiles
     */
    public CMVC_Track(String aLine,
                      String aName,
                      String anAbstractText,
                      String aChangeType,
                      String aDate,
                      String aLevel,
                      Hashtable<String,CMVC_File> someFiles) {
        super();
        setLine(aLine);
        setName(aName);
        setAbstractText(anAbstractText);
        setChangeType(aChangeType);
        setDate(aDate);
        setLevel(aLevel);
        setFiles(someFiles);
        
    }
    
    
     /*
     * Getters  
     */
    public String getLine() { return line; }
    public String getName() { return name; }
    public String getAbstractText() { return abstractText; }
    public String getChangeType() { return changeType; }
    public Hashtable<String,CMVC_File> getFiles() { return files; }
    public String getDate() { return date; }
    public String getLevel() { return level; }
    
    /*
     * Setters
     */
    private void setLine(String aLine) { line = aLine; }
    private void setName(String aName) { name = aName; }
    private void setAbstractText(String anAbstractText) { abstractText = anAbstractText; }
    private void setChangeType(String aChangeType) { changeType = aChangeType; }
    private void setFiles(Hashtable<String,CMVC_File> someFiles) { files = someFiles; }
    private void setDate(String aDate) { date = aDate; }
    private void setLevel(String aLevel) { level = aLevel; }

    /*
     * Members
     */
    private String line;
    private String name;
    private String abstractText;
    private String changeType;
    private Hashtable<String,CMVC_File> files;
    private String date;
    private String level;
    
}