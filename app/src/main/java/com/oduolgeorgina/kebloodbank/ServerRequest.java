package com.oduolgeorgina.kebloodbank;

/**
 * Created by Edward on 23/01/2017.
 */

@SuppressWarnings("ALL")
class ServerRequest {

    //Defines the operation to be done on the DB
    private String operation;
    private BBUser user;

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public void setUser(BBUser user) {
        this.user = user;
    }
}
