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
* TkUserRole business object.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 11/11/2010 GFS  Initial coding.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_etreeobjs;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreedb.EdaTkRole_Db;
import com.ibm.stg.eda.component.tk_etreedb.User_Db;
import com.ibm.stg.iipmds.common.IcofException;

public class TkUserRole  {

    /**
     * Constructor - takes User_d and EdaTkRole_Db objects
     * 
     * @param xContext  Application context
     * @param aUser     A User_db object
     * @param aRole     A EdaTkRole_db object
     */
    public TkUserRole(EdaContext xContext, User_Db aUser, EdaTkRole_Db aRole) {
        setUser(aUser);
        setRole(aRole);
    }

    /**
     * Constructor - takes User_Db and EdaTkRole_Db ids
     * 
     * @param xContext  Application context
     * @param userId    A User_db object id
     * @param roleId    A EdaTkRole_db object id
     * @throws IcofException 
     */
    public TkUserRole(EdaContext xContext, short userId, long roleId) 
    throws IcofException {
        setUser(xContext, userId);
        setRole(xContext, roleId);
    }

    
    /**
     * Data Members
     */
    private User_Db user;
    private EdaTkRole_Db role;

    
    /**
     * Getters
     */
    public User_Db getUser() { return user; }
    public EdaTkRole_Db getRole() { return role; }
        

    /**
     * Setters
     */
    private void setUser(User_Db aUser) { user = aUser; }
    private void setRole(EdaTkRole_Db aRole) { role = aRole; }

    
    /**
     * Set the User object from the database id
     * 
     * @param xContext   Application context.
     * @param anId       A User_Db id
     * @throws IcofException 
     */
    public void setUser(EdaContext xContext, short anId) 
    throws IcofException {
        if (getUser() == null) {
            user = new User_Db(anId);
            user.dbLookupById(xContext);
        }            
    }
    

    /**
     * Set the EdaTkRole object from the database id
     * 
     * @param xContext   Application context.
     * @param anId       A EdaTkRole_Db id
     * @throws IcofException 
     */
    public void setRole(EdaContext xContext, long anId) 
    throws IcofException {
        if (getRole() == null) {
            role = new EdaTkRole_Db(anId);
            role.dbLookupById(xContext);
        }            
    }

    
    /**
     * Create a key from the ID.
     * 
     *  @param xContext  Application context object.
     *  @return          A Statement object.
     */
    public String getIdKey(EdaContext xContext) {
        return String.valueOf(getUser().getId()) + "." +
               String.valueOf(getRole().getId());
    }

    
}
