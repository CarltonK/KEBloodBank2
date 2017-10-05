package com.oduolgeorgina.kebloodbank;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ForgotPassword extends AppCompatActivity {
    private TextView Timer;
    private EditText InputEmail, InputVerificationCode, InputPassword;
    private LinearLayout LayoutForgot;
    private TextView BtnReset;
    private CountDownTimer countDownTimer;
    private String email;
    private boolean isResetInitiated = false;
    private ProgressDialog dialog;
    private CheckBox ChkPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        Timer = (TextView) findViewById(R.id.timer);
        InputEmail = (EditText) findViewById(R.id.et_email);
        InputVerificationCode = (EditText) findViewById(R.id.et_code);
        InputPassword = (EditText) findViewById(R.id.et_password);
        BtnReset = (TextView) findViewById(R.id.btn_reset);
        LayoutForgot = (LinearLayout) findViewById(R.id.layoutforgot);
        ChkPass = (CheckBox) findViewById(R.id.chkforgot);

        dialog = new ProgressDialog(ForgotPassword.this);
        dialog.setTitle("Just a moment");
        dialog.setMessage("Loading...");


        BtnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isOnline(ForgotPassword.this)){
                    Toast.makeText(getApplicationContext(),"NO INTERNET CONNECTION", Toast.LENGTH_LONG).show();
                } else {
                    ResetProcess();
                }

            }
        });

        ChkPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if ((!isChecked)){
                    InputPassword.setInputType(129);
                } else {
                    InputPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        });


        if (isOnline(getApplicationContext())){
            Toast.makeText(ForgotPassword.this, "NO INTERNET CONNECTION", Toast.LENGTH_SHORT).show();
        }
    }

    private void ResetProcess() {


        if (!isResetInitiated){
            email = InputEmail.getText().toString();

            if (TextUtils.isEmpty(email)){
                Toast.makeText(getApplicationContext(),"Email Address is required",Toast.LENGTH_LONG).show();
                InputEmail.setError("Required");
            } else {
                initiateResetProcess(email);
            }
        } else {
            String code = InputVerificationCode.getText().toString();
            String password = InputPassword.getText().toString();

            if (TextUtils.isEmpty(code)){
                Toast.makeText(getApplicationContext(),"Verification code is required",Toast.LENGTH_LONG).show();
                InputVerificationCode.setError("Required");
            }

            if (TextUtils.isEmpty(password)){
                Toast.makeText(getApplicationContext(),"Password is required",Toast.LENGTH_LONG).show();
                InputPassword.setError("Required");
            }

            else {
                finishresetpassword(email,code,password);
            }
        }

    }


    private void initiateResetProcess(String email) {
        dialog.show();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestInterface requestInterface = retrofit.create(RequestInterface.class);

        BBUser user = new BBUser();
        user.setEmail(email);
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.RESET_PASSWORD_INITIATE);
        request.setUser(user);

        Call<ServerResponse> responseCall = requestInterface.operation(request);
        responseCall.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {

                ServerResponse resp = response.body();

                if(resp.getResult().equals(Constants.SUCCESS)){

                    Toast.makeText(getApplicationContext(),resp.getMessage(),Toast.LENGTH_LONG).show();
                    InputEmail.setVisibility(View.GONE);
                    InputVerificationCode.setVisibility(View.VISIBLE);
                    InputPassword.setVisibility(View.VISIBLE);
                    Timer.setVisibility(View.VISIBLE);
                    ChkPass.setVisibility(View.VISIBLE);
                    BtnReset.setText("Change Password");
                    isResetInitiated = true;
                    startCountdownTimer();

                } else {

                    Toast.makeText(getApplicationContext(),resp.getMessage(),Toast.LENGTH_LONG).show();
                }
                dialog.dismiss();

            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {

                Log.d(Constants.TAG,t.getLocalizedMessage());
                Toast.makeText(getApplicationContext(),"Connection Error. Please check your Internet connection.",Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
    }

    private void finishresetpassword(final String email, String code, String password) {
        dialog.show();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestInterface requestInterface = retrofit.create(RequestInterface.class);

        BBUser user = new BBUser();
        user.setEmail(email);
        user.setCode(code);
        user.setPassword(password);
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.RESET_PASSWORD_FINISH);
        request.setUser(user);
        Call<ServerResponse> response = requestInterface.operation(request);

        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {

                ServerResponse resp = response.body();

                if (resp.getResult().equals(Constants.SUCCESS)) {

                    Toast.makeText(getApplicationContext(),"Password Changed Successfully",Toast.LENGTH_LONG).show();
                    countDownTimer.cancel();
                    isResetInitiated = false;
                    goToLogin();

                } else {
                    Toast.makeText(getApplicationContext(),"Verification code is incorrect",Toast.LENGTH_LONG).show();

                }
                dialog.dismiss();
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                try {
                    Log.d(Constants.TAG,t.getLocalizedMessage());
                    Toast.makeText(getApplicationContext(),"Connection Error. Please check your Internet connection.",Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                } catch (Exception e){
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void startCountdownTimer() {
        countDownTimer = new CountDownTimer(120000, 1000) {
            @Override
            public void onTick(long l) {
                Timer.setText("Time remaining : " + l/1000 + " seconds");
            }

            @Override
            public void onFinish() {
                Toast.makeText(getApplicationContext(),"Time out! Request again to reset password", Toast.LENGTH_LONG).show();
                startActivity(new Intent(ForgotPassword.this,Login.class));
                finish();
            }
        }.start();
    }

    private boolean isOnline(Context mContext) {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo == null || !netInfo.isConnectedOrConnecting();
    }

    private void goToLogin() {
        startActivity(new Intent(ForgotPassword.this,Login.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goToLogin();
    }
}

