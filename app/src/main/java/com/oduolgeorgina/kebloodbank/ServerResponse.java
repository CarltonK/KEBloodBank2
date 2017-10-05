package com.oduolgeorgina.kebloodbank;

import java.util.List;

/**
 * Created by Edward on 23/01/2017.
 */

@SuppressWarnings("ALL")
class ServerResponse {
    private String result;
    private String message;
    private BBUser user;
    private List<BBUser> userslocation;

    public List<BBUser> getUsers() {
        return userslocation;
    }

    public String getMessage() {
        return message;
    }

    public BBUser getUser() {
        return user;
    }

    public String getResult() {
        return result;
    }
}
