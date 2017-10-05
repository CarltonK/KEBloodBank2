package com.oduolgeorgina.kebloodbank;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Register extends AppCompatActivity {
    TextView btnlogin, btnregister;
    EditText txtname, txtgender, txtdob, txtid, txtphonenumber, txtemail, Texxtpass;
    Spinner spGender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        txtname= (EditText) findViewById(R.id.txtname);
        txtdob= (EditText) findViewById(R.id.txtdob);
        txtemail= (EditText) findViewById(R.id.txtemail);
        txtid= (EditText) findViewById(R.id.txtid);
        txtphonenumber= (EditText) findViewById(R.id.txtphonenumber);
        spGender = (Spinner) findViewById(R.id.sp_gender);
        Texxtpass = (EditText) findViewById(R.id.txtpass);

        //Defines the items to be populated on the spinner
        List<String> gender = new ArrayList<>();
        gender.add("Male");
        gender.add("Female");
        gender.add("Do not wish to disclose");

        //Attaching the defined items to an adapter
        ArrayAdapter<String> data = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,gender);
        data.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGender.setAdapter(data);

        btnregister= (TextView) findViewById(R.id.btnregister);
        btnregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String Name= txtname.getText().toString();
                String Gender= spGender.getSelectedItem().toString();
                String DOB= txtdob.getText().toString();
                String Email= txtemail.getText().toString();
                String ID= txtid.getText().toString();
                String Password = Texxtpass.getText().toString();
                String PhoneNumber= txtphonenumber.getText().toString();

                if (Name.equals("") || Gender.equals("") || Email.equals("") || PhoneNumber.equals("") || DOB.equals("") || ID.equals("") || Password.equals(""))
                {
                    Toast.makeText(Register.this, "Please Enter All your Details!", Toast.LENGTH_LONG).show();
                }else {

                    if (!isOnline(Register.this)){
                        Toast.makeText(getApplicationContext(), "NO INTERNET CONNECTION",Toast.LENGTH_SHORT).show();
                    } else {
                        //start the registration
                        register(Name, Email, PhoneNumber,ID, DOB, Gender, Password);
                    }


                }

            }
        });
        btnlogin=(TextView) findViewById(R.id.btnlogin);
        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register.this, Login.class));
                finish();
            }
        });

    }

    //Method that will store details into DB
    private void register(String Name, String Email, String PhoneNumber, String ID, String DOB, String Gender, String Password) {


        //Dialog starts when the method is called
        final ProgressDialog progressDialog = new ProgressDialog(Register.this);
        progressDialog.setTitle("Just a moment");
        progressDialog.setMessage("Loading....");
        progressDialog.show();


        //Network call to the URL
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestInterface requestInterface = retrofit.create(RequestInterface.class);


        //Set User
        BBUser user = new BBUser();
        user.setName(Name);
        user.setEmail(Email);
        user.setPhone(PhoneNumber);
        user.setNatId(ID);
        user.setDob(DOB);
        user.setGender(Gender);
        user.setPassword(Password);



        //Makes a request to the server based on the operation set
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.REGISTER_OPERATION);
        request.setUser(user);

        //Response from the server side, requestInterface is the response received based on the request made
        Call<ServerResponse> response = requestInterface.operation(request);


        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {

                ServerResponse resp = response.body();
                if (resp.getResult().equals(Constants.SUCCESS)){
                    Toast.makeText(getApplicationContext(), "Registration successful...Welcome", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            gotoNext();
                        }
                    }, 1000);
                } else {
                    Toast.makeText(getApplicationContext(),resp.getMessage(),Toast.LENGTH_SHORT).show();
                    gotoNext();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {


                Log.d(Constants.TAG,t.getLocalizedMessage());
                Toast.makeText(getApplicationContext(),t.getMessage(),Toast.LENGTH_SHORT).show();
                gotoNext();
                progressDialog.dismiss();

            }
        });
    }

    //Checks the internet connection state of the phone
    private boolean isOnline(Context mContext) {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(Register.this, Login.class));
        finish();
    }

    private void gotoNext() {
        startActivity(new Intent(Register.this, Login.class));
        finish();
    }
}
