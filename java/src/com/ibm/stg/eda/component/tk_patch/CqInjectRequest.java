/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2014 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 11/07/2014 GFS  Initial coding.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.tk_patch;

import com.ibm.stg.iipmds.common.IcofException;


public class CqInjectRequest implements java.io.Serializable {

    private static final long serialVersionUID = -6053488863457230346L;


    /**
     * Constructor
     * 
     * @param anId         ClearQuest record id
     * @param aTk          Tool kit name
     * @param aComp        Component/tool name
     * @param aDeveloper   Developer's name
     * @param aState       CQ record state
     * @param anEmergency  Is this an emergency inject request
     * @throws IcofException
     */
    public CqInjectRequest(String anId, String aTk,
			   String aComp, String aDeveloper,
			   String aState, boolean anEmergency) {

	setId(anId);
	setToolKit(aTk);
	setComponent(aComp);
	setDeveloper(aDeveloper);
	setState(aState);
	setEmergency(anEmergency);

    }
    

    /**
     * Show the objects contents
     */
    public String toString() {
	
	StringBuffer contents = new StringBuffer();
	contents.append("ID: " + getId() + "\n");
	contents.append("TK: " + getToolKit() + "\n");
	contents.append("Comp: " + getComponent() + "\n");
	contents.append("Dev: " + getDeveloper() + "\n");
	contents.append("State: " + getState() + "\n");
	contents.append("Emer: " + getEmergency() + "\n");
	
	return contents.toString();
	
    }
    
    
    /*
     * Members
     * @formatter:off
     */
    private String cqId;
    private String toolKit;
    private String component;
    private String developer;
    private String state;
    private boolean emergency;

    
    /*
     * Getters
     */
    public String getId() { return cqId; }
    public String getToolKit() { return toolKit; }
    public String getComponent() { return component; }
    public String getDeveloper() { return developer; }
    public String getState() { return state; }
    public boolean getEmergency() { return emergency; }
    

    /*
     * Setters
     */
    private void setId(String anId) { cqId = anId; }
    private void setToolKit(String aTk) { toolKit = aTk; }
    private void setComponent(String aComp) { component = aComp; }
    private void setDeveloper(String aName) { developer = aName; }
    private void setState(String aState) { state = aState; }
    private void setEmergency(boolean aFlag) { emergency = aFlag; }
    // @formatter:on

    
}
