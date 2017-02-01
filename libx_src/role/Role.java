/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2001 -- IBM Internal Use Only
*
*=============================================================================
*
*    FILE: Role.java
*
* CREATOR: Karen K. Kellam
*    DEPT: 5ZIA
*    DATE: 01/04/2001
*
*-PURPOSE---------------------------------------------------------------------
* Role class definition file.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 01/04/2001 KKK  Initial coding.
* 04/09/2001 GFS  Switched class from abstract to non-abstract.
* 10/17/2001 KKK  Modified class to inherit from base class.
* 10/24/2001 KKK  Converted this class to work with the Role table in the
*                 database.  Formerly, there was no actual Role table.  Class
*                 now inherits from TypeTable, instead of IcofObject.
* 03/20/2002 KKK  Added AppContext to constructors for all objects.  Added
*                 DefaultContext and AuditUserid to all DBInterface constructors.
*                 Convert to Java 1.2.2.
* 07/29/2005 KW   Added javadoc.
* 12/15/2005 KKW  Modified due to splitting of Constants.java into several
*                 *Util classes.
* 12/05/2006 KKW  Added AppContext as first parameter of each method.
*=============================================================================
* </pre>
*/


package com.ibm.stg.iipmds.icof.component.role;
import com.ibm.stg.iipmds.common.Constants;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.icof.component.db.RoleDBInterface;
import com.ibm.stg.iipmds.icof.component.mom.AppContext;
import com.ibm.stg.iipmds.icof.component.mom.TypeTable;


public class Role extends TypeTable {


  //-----------------------------------------------------------------------------
  /**
   * Constructor - used to instantiate object from application when only the
   *   role name is known.
   *
   * This constructor will do an automatic database lookup to populate all
   *   fields.
   *
   * @param     aName             the role name
   * @param     anAppContext      the db2 context and user information
   *
   * @exception IcofException     role name does not exist in database
   */
  //-----------------------------------------------------------------------------
  public Role(AppContext anAppContext
              ,String aName) throws IcofException {

    super(anAppContext, aName);
    dbLookUpByName(anAppContext);

  }


  //-----------------------------------------------------------------------------
  /**
   * Constructor - used to instantiate object from application when only the
   *   role id is known.
   *
   * This constructor will do an automatic database lookup to populate all
   *   fields.
   *
   * @param     anID              the numeric identifier for the role
   * @param     anAppContext      the db2 context and user information
   *
   * @exception IcofException     role name does not exist in database
   */
  //-----------------------------------------------------------------------------
  public Role(AppContext anAppContext
              ,short anID) throws IcofException {

    super(anAppContext, anID);
    dbLookUpById(anAppContext);

  }


  //-----------------------------------------------------------------------------
  /**
   * Constructor - used to instantiate object from database
   *
   * This constructor can also be used when creating an object to be added
   *   to the database.  Use EMPTY for the value of ID.
   *
   * @param     anID              the numeric identifier for the role
   * @param     aName             the role name
   * @param     anAppContext      the db2 context and user information
   *
   * @exception IcofException     role name does not exist in database
   */
  //-----------------------------------------------------------------------------
  public Role(AppContext anAppContext
              ,short anID
              ,String aName) {

    super(anAppContext
          ,anID
          ,aName);

  }


  //-----------------------------------------------------------------------------
  /**
   * Lookup a Role in the database, using the role name.  Populate the data
   *   members in this object with the values from the database.
   *
   * @exception IcofException     role name does not exist in database
   */
  //-----------------------------------------------------------------------------
  public void dbLookUpByName(AppContext anAppContext) throws IcofException {

    setFuncName(anAppContext, "dbLookUpByName(AppContext)");

    RoleDBInterface dba =
        new RoleDBInterface(anAppContext);
    if (!dba.selectRow(anAppContext, getName(anAppContext))) {
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,getFuncName(anAppContext)
                                           ,IcofException.SEVERE
                                           ,"Could not find roleName in Role table"
                                           ,getName(anAppContext));
      anAppContext.getSessionLog().log(ie);
      throw(ie);
    }

    populate(anAppContext, dba);

  }


  //-----------------------------------------------------------------------------
  /**
   * Lookup a Role in the database, using the role ID.  Populate the data
   *   members in this object with the values from the database.
   *
   * @exception IcofException     role ID does not exist in database
   */
  //-----------------------------------------------------------------------------
  public void dbLookUpById(AppContext anAppContext) throws IcofException {

    setFuncName(anAppContext, "dbLookUpById(AppContext)");

    RoleDBInterface dba =
        new RoleDBInterface(anAppContext);
    if (!dba.selectRow(anAppContext, getID(anAppContext))) {
      String msg = new String("Could not find roleID in Role table");
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,getFuncName(anAppContext)
                                           ,IcofException.SEVERE
                                           ,msg
                                           ,String.valueOf(getID(anAppContext)));
      anAppContext.getSessionLog().log(ie);
      throw(ie);
    }

    populate(anAppContext, dba);

  }


  //-----------------------------------------------------------------------------
  /**
   * Determine whether to add this Role to the database.  If it is not already
   *   there, add it.  If it is, just indicate that it is already in the database.
   *
   * @return                      Constants.IS_ACTIVE, if already in database;
   *                              Constants.ADDED, if added to database.
   * @exception                   Unable to communicate with database.
   */
  //-----------------------------------------------------------------------------
  public String addOrReinstate(AppContext anAppContext) throws IcofException {

    RoleDBInterface dba =
        new RoleDBInterface(anAppContext);
    if (dba.selectRow(anAppContext, getName(anAppContext))) {
      populate(anAppContext, dba);
      return Constants.IS_ACTIVE;
    }

    //
    // If we get here, it means this Role was not in the
    //   database, at all, so we need to add it.
    //
    dbAdd(anAppContext);
    return Constants.ADDED;

  }


  //-----------------------------------------------------------------------------
  /**
   * Add a Role to the database -- this function is PROTECTED because
   *   all applications should use addOrReinstate()
   *
   * @exception IcofException     Unable to add role to database
   */
  //-----------------------------------------------------------------------------
  public void dbAdd(AppContext anAppContext) throws IcofException {

    RoleDBInterface dba =
        new RoleDBInterface(anAppContext);
    setID(anAppContext, dba.getNextID(anAppContext));
    dba.insertRow(anAppContext, this);

  }


  //-----------------------------------------------------------------------------
  /**
   * Populate this object from its corresponding database object.
   *
   * @param     dba               the data from a db2 "select *" query on the
   *                              role table
   */
  //-----------------------------------------------------------------------------
  private void populate(AppContext anAppContext, RoleDBInterface dba) {

    setID(anAppContext, dba.getID(anAppContext));
    setName(anAppContext, dba.getRoleName(anAppContext));
    setMapKey(anAppContext);

  }

}


//==========================  END OF FILE  ====================================
