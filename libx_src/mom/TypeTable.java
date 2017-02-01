//=============================================================================
//
// Copyright: (C) IBM Corporation 2000 -- IBM Internal Use Only
//
//=============================================================================
//
//    FILE: TypeTable.java
//
// CREATOR: Karen K. Kellam
//    DEPT: 5ZIA
//    DATE: 10/03/2001
//
//-PURPOSE---------------------------------------------------------------------
// TypeTable class definition file.
//-----------------------------------------------------------------------------
//
//
//-CHANGE LOG------------------------------------------------------------------
// 10/03/2001 KKK  Initial coding.
// 10/06/2006 KKW  Added AppContext as first parameter of each method
// 02/12/2009 AS   Added TypeTable(AppContext anAppContext) constructor.
//=============================================================================

package com.ibm.stg.iipmds.icof.component.mom;
import com.ibm.stg.iipmds.common.Constants;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.icof.component.mom.DBAdd;


public abstract class TypeTable extends IcofObject implements DBAdd {


  //-----------------------------------------------------------------------------
  // Constructor - used to instantiate object from application when only the
  //   name is known.
  //-----------------------------------------------------------------------------
  public TypeTable(AppContext anAppContext
  		             ,String aName) {

    this(anAppContext
    		 ,Constants.EMPTY
         ,aName);

  }


  //-----------------------------------------------------------------------------
  // Constructor - used to instantiate object from application when only the
  //   id is known.
  //-----------------------------------------------------------------------------
  public TypeTable(AppContext anAppContext
  		             ,short anID) {

    this(anAppContext
    		 ,anID
         ,"");

  }


  //-----------------------------------------------------------------------------
  // Constructor - used to instantiate object from database
  //-----------------------------------------------------------------------------
  public TypeTable(AppContext anAppContext
  		             ,short anID
                   ,String aName) {

    super(anAppContext);
    setID(anAppContext, anID);
    setName(anAppContext, aName);
    setMapKey(anAppContext);

  }
  
  //-----------------------------------------------------------------------------
  // Constructor - used to instantiate object from database
  //-----------------------------------------------------------------------------
  public TypeTable(AppContext anAppContext) {
    super(anAppContext);
  }

  //-----------------------------------------------------------------------------
  // Member "getter" functions
  //-----------------------------------------------------------------------------
  public short getID(AppContext anAppContext) { return id; }
  public String getName(AppContext anAppContext) { return name; }


  //-----------------------------------------------------------------------------
  // Get contents of TypeTable as a string
  //-----------------------------------------------------------------------------
  public String asString(AppContext anAppContext) {

    return("ID: "     + String.valueOf(getID(anAppContext)) + ";" +
           "Name: "   + getName(anAppContext));

  }


  //-----------------------------------------------------------------------------
  // Required functions for children classes
  //-----------------------------------------------------------------------------
  public abstract void dbLookUpById(AppContext anAppContext) throws IcofException;
  public abstract void dbLookUpByName(AppContext anAppContext) throws IcofException;


  //-----------------------------------------------------------------------------
  // Data members
  //-----------------------------------------------------------------------------
  private short    id;
  private String   name;


  //-----------------------------------------------------------------------------
  // Member "setter" functions
  //-----------------------------------------------------------------------------
  protected void setMapKey(AppContext anAppContext) {
		setMapKey(anAppContext, getName(anAppContext));
	}

	protected void setID(AppContext anAppContext, short anID) {
		id = anID;
	}

	protected void setName(AppContext anAppContext, String aName) {
		name = aName;
	}

}

//==========================  END OF FILE  ====================================
