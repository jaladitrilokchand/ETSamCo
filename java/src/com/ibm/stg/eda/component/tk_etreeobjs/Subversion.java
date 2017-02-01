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
 * Subversion objject
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 07/29/2011 GFS  Initial coding. 
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.tk_etreeobjs;

import java.util.Iterator;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkConstants;
import com.ibm.stg.eda.component.tk_etreebase.TkSystem;
import com.ibm.stg.iipmds.common.IcofCollectionsUtil;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofStringUtil;

public class Subversion extends TkSystem {

    /**
     * Constants
     */
    public static final String EDA_SVN = "/afs/eda/tools/bin/svn";
    public static final String BTV_SVN = "/afs/btv/data/subversion/bin/svn";
    public static final String URL = "URL: ";
    public static final String ROOT = "Repository Root: ";
    public static final String BTV = "btv";

    /**
     * Constructor
     * 
     * @param xContext     Application context
     * @throws IcofException 
     */
    public Subversion(EdaContext xContext) throws IcofException {
	super(xContext, "");
	setEdaCell();
    }


    /**
     * Constructor - takes a command
     * 
     * @param xContext     Application context
     * @param aCommand     Subverison command to run
     * @throws IcofException 
     */
    public Subversion(EdaContext xContext, String aCommand) 
    throws IcofException {
	super(xContext, aCommand);
	setEdaCell();
    }


    /**
     * Data Members
     */
    private String url;
    private String root;
    private String trunk;
    private String branch;
    private String component;
    private boolean edaCell = true;


    /**
     * Getters
     */
    public String getUrl() { return url; }
    public String getRoot() { return root; }
    public String getBranch() { return branch; }
    public String getTrunk() { return trunk; }
    public String getComponent() { return component; }
    public boolean isEdaCell() { return edaCell; }
    public String getInfoCommand(String directory) {
	return getSvnExec() + " info " + directory;
    }
    public String getCommitCommand(String args) {
	return getSvnExec() + " commit " + args;
    }
    public String getSvnExec() { 
	if (isEdaCell())
	    return EDA_SVN;
	else
	    return BTV_SVN;
    }


    /**
     * Setters
     */
    private void setRoot(String aName) { root = aName; }
    private void setUrl(String aName) { url = aName; }
    private void setBranch(String aName) { branch = aName; }
    private void setTrunk(String aName) { trunk = aName; }
    private void setComponent(String aName) { component = aName; }
    private void setEdaCell() {
	setHostName();
	if (getHostName().indexOf(BTV) > -1)
	    edaCell = false;
    }


    /**
     * Read the repository information
     *
     *@param xContext  Application context
     */
    public boolean svnInfo(EdaContext xContext, String directory) 
    throws IcofException { 

	setCommand(getInfoCommand(directory));
	execute(xContext);
	if (getReturnCode() == 1) {
	    return false;
	}

	setRootAndUrl(xContext);
	setTrunkAndBranch(xContext);

	return true;

    }


    /**
     * Read the trunk or branch data from the ROOT and URL.
     * @param xContext  Application context
     */
    private void setTrunkAndBranch(EdaContext xContext) {

	int rootLength = getRoot().length() + 1;

	String subDir = getUrl().substring(rootLength);
	if (subDir.startsWith(TkConstants.TRUNK)) {
	    setTrunk(TkConstants.TRUNK);
	}
	else {
	    int firstSlash = subDir.indexOf("/");
	    if (firstSlash < 0) {
		setBranch(subDir);
	    }
	    else  if (subDir.startsWith(TkConstants.BRANCHES)) {
		setBranch(subDir.substring(0, firstSlash));
	    }
	    else {
		int nextSlash = subDir.indexOf("/", firstSlash);
		if (nextSlash < 0) {
		    setBranch(subDir.substring(firstSlash));
		}
		else {
		    setBranch(subDir.substring(firstSlash, nextSlash));
		}
	    }

	}
    }


    /**
     * Parse the URL from the svn info commmand results.
     * 
     * @param xContext
     * @throws IcofException 
     */
    private boolean setRootAndUrl(EdaContext xContext) throws IcofException {

	if (getResults() == null)
	    return false;

	// Find the ROOT and URL lines
	boolean foundUrl = false; 
	boolean foundRoot = false;
	Iterator<String> iter = getResults().iterator();
	while (iter.hasNext()) {
	    String line =  iter.next();
	    if (! foundUrl && line.startsWith(URL)) {
		setUrl(line.substring(URL.length(), line.length()));
		//System.out.println("URL: " + getUrl());
		foundUrl = true;
	    }
	    else if (! foundRoot && line.startsWith(ROOT)) {
		setRoot(line.substring(ROOT.length(), line.length()));
		//System.out.println("ROOT: " + getRoot());
		foundRoot = true;
	    }

	}

	// Set the component if ROOT found
	if (foundRoot) {

	    int index = 5;
	    int slashCount = IcofStringUtil.occurrencesOf(getRoot(), "/");
	    if (slashCount >= index) {
		Vector<String>  tokens = new Vector<String> ();
		IcofCollectionsUtil.parseString(getRoot(), "/", tokens, false);
		if (tokens.size() >= index+1) {
		    setComponent( tokens.get(5));
		}
	    }
	}

	return foundRoot && foundUrl;

    }


    /**
     * Execute the commit command
     * 
     * @param xContext  Application context
     * @param args      Command line args
     * @throws IcofException 
     */
    public boolean svnCommit(EdaContext xContext, String args) 
    throws IcofException {

	setCommand(getCommitCommand(args));

	System.out.println("Executing ...");
	System.out.println(" --> " + getCommand() + "\n");

	//initialize();
	boolean success = true;
	execute(xContext);
	if (getReturnCode() == 1) {
	    System.out.println(getErrorMsg().toString());
	    success = false;
	}
	else {
	    System.out.println(IcofCollectionsUtil.getVectorAsString(getResults(), "\n"));
	}

	return success;

    }

}
