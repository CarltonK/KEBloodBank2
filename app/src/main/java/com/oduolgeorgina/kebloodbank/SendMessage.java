package com.oduolgeorgina.kebloodbank;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SendMessage extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {
    private EditText MsgTitle, MsgBody;
    private TextView SendMsg;
    private RadioGroup RadGroup;
    private String capacity, title, message;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sessionManager = new SessionManager(SendMessage.this);

        MsgTitle = (EditText) findViewById(R.id.txtmsgtitle);
        MsgBody = (EditText) findViewById(R.id.txtmsg);
        SendMsg = (TextView) findViewById(R.id.btnsendmsg);
        RadGroup = (RadioGroup) findViewById(R.id.radioGroup);

        SendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                title = MsgTitle.getText().toString();
                message = MsgBody.getText().toString();

                if (TextUtils.isEmpty(title) || TextUtils.isEmpty(message) || TextUtils.isEmpty(capacity)){
                    Toast.makeText(getApplicationContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                } else {

                    HashMap<String, String> use = sessionManager.getUserDetails();
                    String email = use.get(SessionManager.KEY_EMAIL);


                    Send(email, capacity, title, message);
                }
            }
        });

        RadGroup.setOnCheckedChangeListener(this);
    }

    private void Send(String email, String capacity, String title, String message) {

        final ProgressDialog progressDialog = new ProgressDialog(SendMessage.this);
        progressDialog.setTitle("Just a moment");
        progressDialog.setMessage("Sending Message......");
        progressDialog.show();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.PUSH_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        PushInterface requestInterface = retrofit.create(PushInterface.class);

        NotificationObject object = new NotificationObject();
        object.setEmail(email);
        object.setCapacity(capacity);
        object.setMessage(message);
        object.setTitle(title);

        ServerRequest request = new ServerRequest();
        request.setData(object);

        Gson gson = new Gson();
        String maneno = gson.toJson(request);
        Log.i(Constants.TAG, maneno);

        Call<ServerResponse> response = requestInterface.operation(request);

        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {

                try {

                    ServerResponse resp = response.body();

                    //Fetch result from db and save to Shared Preferences
                    if(resp.getResult().equals(Constants.SUCCESS)) {

                        Toast.makeText(getApplicationContext(), "Message sent successfully", Toast.LENGTH_SHORT).show();
                        onBackPressed();

                    } else {
                        Toast.makeText(getApplicationContext(),"Message not sent", Toast.LENGTH_SHORT ).show();
                    }
                } catch (Exception e){
                    Log.e(Constants.TAG, e.getLocalizedMessage());
                    Toast.makeText(getApplicationContext(),"Message sent successfully", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
                progressDialog.dismiss();

            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {

                Log.d(Constants.TAG,t.getMessage());
                Toast.makeText(getApplicationContext(),"Connection Error.",Toast.LENGTH_SHORT).show();
                onBackPressed();
                progressDialog.dismiss();
            }
        });

    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        switch (radioGroup.getCheckedRadioButtonId()) {
            case R.id.radioButtonrecipients:
                capacity = "Recipient";
                break;

            case R.id.radioButtondonors:
                capacity = "Donor";
                break;

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
