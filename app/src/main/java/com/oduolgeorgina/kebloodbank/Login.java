package com.oduolgeorgina.kebloodbank;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Login extends AppCompatActivity {
    TextView btnlogin1, TVlogin;
    EditText txtpass, txtemail1;
    String Email,Password;
    private SessionManager sessionManager;
    private TextView ForgotPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(getApplicationContext());

        btnlogin1 = (TextView) findViewById(R.id.btnlogin1);
        TVlogin = (TextView) findViewById(R.id.tvlogin);
        ForgotPass = (TextView) findViewById(R.id.tvforgotpassword);
        btnlogin1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                txtemail1 = (EditText) findViewById(R.id.txtemail1);
                Email = txtemail1.getText().toString();
                txtpass = (EditText) findViewById(R.id.txtpass);
                Password = txtpass.getText().toString();

                if (Email.equals("") || Password.equals("")) {
                    Toast.makeText(Login.this, "Please Enter your Email and Password" + Email + "" + Password, Toast.LENGTH_LONG).show();

                } else {

                    if (!isOnline(Login.this)) {
                        Toast.makeText(getApplicationContext(), "NO INTERNET CONNECTION", Toast.LENGTH_SHORT).show();
                    } else {
                        loginprocess(Email, Password);
                    }

                }

            }
        });

        ForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, ForgotPassword.class));
                finish();
            }
        });

        TVlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, Register.class));
                finish();
            }
        });
    }

    private void loginprocess(String Email, String Password) {

        final ProgressDialog progressDialog = new ProgressDialog(Login.this);
        progressDialog.setTitle("Just a moment");
        progressDialog.setMessage("Loading....");
        progressDialog.show();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestInterface requestInterface = retrofit.create(RequestInterface.class);

        BBUser user = new BBUser();
        user.setEmail(Email);
        user.setPassword(Password);

        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.LOGIN_OPERATION);
        request.setUser(user);

        Call<ServerResponse> response = requestInterface.operation(request);

        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {

                try {

                    ServerResponse resp = response.body();

                    //Fetch result from db and save to Shared Preferences
                    if(resp.getResult().equals(Constants.SUCCESS)) {
                        sessionManager.createLoginSession(
                                resp.getUser().getName(),
                                resp.getUser().getPhone(),
                                resp.getUser().getEmail(),
                                resp.getUser().getNatId(),
                                resp.getUser().getGender(),
                                resp.getUser().getDob(),
                                resp.getUser().getCapacity());


                        Log.d(Constants.TAG, String.valueOf(sessionManager.getUserDetails()));

                        Toast.makeText(getApplicationContext(), "Welcome ....", Toast.LENGTH_SHORT).show();
                        goToProfile();

                    } else {
                        Toast.makeText(getApplicationContext(),"Username or password incorrect. Please try again! ", Toast.LENGTH_SHORT ).show();
                    }
                } catch (Exception e){
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();

            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {

                Log.d(Constants.TAG,t.getLocalizedMessage());
                Toast.makeText(getApplicationContext(),"Connection Error. Sit tight, we're handling it",Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    private void goToProfile() {
        startActivity(new Intent(Login.this, MapsActivity.class));
        finish();
    }


    private boolean isOnline(Context mContext) {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

}
