package com.oduolgeorgina.kebloodbank;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.HashMap;

/**
 * Created by Mark Carlton on 08/09/2017.
 */

public class SessionManager {

    // Shared Preferences
    private final SharedPreferences pref;

    // Editor for Shared preferences
    private final SharedPreferences.Editor editor;

    // Context
    private final Context _context;

    // Shared pref mode
    private final int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "BloodBankKE";


    // All Shared Preferences Keys
    private static final String IS_LOGIN = Constants.IS_LOGGED_IN;

    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_GENDER = "gender";
    public static final String KEY_DOB = "dob";
    public static  final String KEY_NATID = "natId";
    public static final String KEY_CAPACITY = "capacity";



    // Constructor
    public SessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void createLoginSession(String name, String phone, String email, String gender, String dob, String natid, String capacity){
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);
        // Storing name in pref
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_PHONE, phone);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_GENDER,dob);
        editor.putString(KEY_DOB,natid);
        editor.putString(KEY_NATID, gender);
        editor.putString(KEY_CAPACITY, capacity);
        // commit changes
        editor.commit();
    }

    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<>();
        user.put(KEY_NAME, pref.getString(KEY_NAME, null));
        user.put(KEY_NATID, pref.getString(KEY_NATID, null));
        user.put(KEY_PHONE, pref.getString(KEY_PHONE, null));
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));
        user.put(KEY_GENDER, pref.getString(KEY_GENDER,null));
        user.put(KEY_CAPACITY, pref.getString(KEY_CAPACITY, null));
        user.put(KEY_DOB, pref.getString(KEY_DOB,null));


        // return user
        return user;
    }

    public void updatecapacity(String capacity){

        editor.putString(KEY_CAPACITY, capacity);
        editor.commit();
    }

    public void checkLogin(){
        // Check login status
        if(!this.isLoggedIn()){
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, Login.class);
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            _context.startActivity(i);
        }

    }

    /**
     * Clear session details
     * */
    public void logoutUser(){
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

        // After logout redirect user to Loing Activity
        Intent i = new Intent(_context, Login.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        _context.startActivity(i);
    }

    /**
     * Quick check for login
     * **/
    // Get Login State
    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }
}
