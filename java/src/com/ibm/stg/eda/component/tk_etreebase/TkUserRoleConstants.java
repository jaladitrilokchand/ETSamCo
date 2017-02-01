package com.ibm.stg.eda.component.tk_etreebase;

public enum TkUserRoleConstants {

    CCB_APPROVER ("CCB_approver");

    String userRole;

    TkUserRoleConstants (String userRole){
	this.userRole = userRole;
    }

    public String getUserRole() {
	return userRole;
    }

}
