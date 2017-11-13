package com.example.bst.hcoder;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.os.StrictMode;

import java.io.UnsupportedEncodingException;

import com.example.bst.hcoder.R;

import com.loopj.android.http.*;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final AppCompatActivity activity = MainActivity.this;

    private NestedScrollView nestedScrollView;

    private TextInputLayout textInputLayoutUsername;
    private TextInputLayout textInputLayoutIP;

    private TextInputEditText textInputEditTextUsername;
    private TextInputEditText textInputEditTextIP;

    private AppCompatButton appCompatButtonEncode;
    private AppCompatButton appCompatButtonDecode;

    private AppCompatTextView appCompatTextViewResponse;

    public final static String EXTRA_USERNAME = "USERNAME";
    public final static String EXTRA_USERS = "USERS";
    public final static String EXTRA_IP = "IP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        initViews();
        initListeners();
    }

    /**
     * initViews is used to initialize views
     */
    private void initViews() {

        nestedScrollView = (NestedScrollView) findViewById(R.id.nestedScrollView);

        textInputLayoutUsername = (TextInputLayout) findViewById(R.id.textInputLayoutUsername);
        textInputLayoutIP = (TextInputLayout) findViewById(R.id.textInputLayoutIP);

        textInputEditTextUsername = (TextInputEditText) findViewById(R.id.textInputEditTextUsername);
        textInputEditTextIP = (TextInputEditText) findViewById(R.id.textInputEditTextIP);

        appCompatButtonEncode = (AppCompatButton) findViewById(R.id.appCompatButtonEncode);
        appCompatButtonDecode = (AppCompatButton) findViewById(R.id.appCompatButtonDecode);

        appCompatTextViewResponse = (AppCompatTextView) findViewById(R.id.appCompatTextViewResponse);

    }

    /**
     * initListeners is used to initialize listeners
     */
    private void initListeners() {
        appCompatButtonEncode.setOnClickListener(this);
        appCompatButtonDecode.setOnClickListener(this);
    }

    /**
     * onClick is used to listen click on view
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.appCompatButtonEncode:
                try{
                    verifyLogin(true);
                }catch (Exception e){
                    Snackbar.make(nestedScrollView, getString(R.string.error_connection), Snackbar.LENGTH_LONG).show();
                }
                break;
            case R.id.appCompatButtonDecode:
                try{
                    verifyLogin(false);
                }catch (Exception e){
                    Snackbar.make(nestedScrollView, getString(R.string.error_connection), Snackbar.LENGTH_LONG).show();
                }
                break;
        }
    }

    /**
     * verifyFromSQLite is used to control text fields and user is saved on database
     */
    private void verifyLogin(boolean choice) throws UnsupportedEncodingException {

        String username = textInputEditTextUsername.getText().toString().trim();
        final String ip = textInputEditTextIP.getText().toString().trim();
        final boolean fChoice = choice;

        try{
            String url = "http://" + ip + ":8080";
            RequestParams rParams = new RequestParams();
            rParams.put("username",username);
            AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
            asyncHttpClient.post(url, rParams, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    if(fChoice == true){
                        String successStr = new String(responseBody);
                        appCompatTextViewResponse.setText(successStr);
                        String[] userArrays = successStr.trim().split("\\s+");
                        Intent accountIntent = new Intent(activity, Account.class);
                        accountIntent.putExtra(EXTRA_USERNAME, textInputEditTextUsername.getText().toString().trim());
                        accountIntent.putExtra(EXTRA_USERS, userArrays);
                        accountIntent.putExtra(EXTRA_IP, ip);
                        emptyInputEditText();
                        startActivity(accountIntent);
                    }
                    else{
                        Intent messageIntent = new Intent(activity, MessageActivity.class);
                        messageIntent.putExtra(EXTRA_USERNAME, textInputEditTextUsername.getText().toString().trim());
                        messageIntent.putExtra(EXTRA_IP, ip);
                        emptyInputEditText();
                        startActivity(messageIntent);
                    }

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    String failStr = new String(responseBody);
                    appCompatTextViewResponse.setText(failStr);
                    error.printStackTrace();
                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * emptyInputEditText is used to empty all input edit text
     */
    private void emptyInputEditText() {
        textInputEditTextUsername.setText(null);
    }
}

