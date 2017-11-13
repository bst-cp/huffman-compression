package com.example.bst.hcoder;

import android.content.Intent;
import android.os.Environment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.math.BigInteger;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class MessageActivity extends AppCompatActivity  implements View.OnClickListener{
    private final AppCompatActivity activity = MessageActivity.this;

    private NestedScrollView nestedScrollView;
    private AppCompatTextView textMessageStatus;
    private AppCompatTextView textUsername;
    private AppCompatTextView textmessage;
    private AppCompatTextView textLocation;
    private AppCompatButton appCompatButtonAccept;
    private AppCompatButton appCompatButtonDecline;

    private HuffmanDecode huffmanDecode;

    private Handler h;
    private int delay = 10000; //10 seconds
    private Runnable runnable;

    private String username;
    private String ip;
    private String codebook;
    private String message;
    private String sender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        initObjects();
        initViews();
        initListeners();

    }
    private void initViews() {
        nestedScrollView = (NestedScrollView) findViewById(R.id.nestedScrollView);

        textUsername = (AppCompatTextView) findViewById(R.id.textUsername);
        textmessage = (AppCompatTextView) findViewById(R.id.textMessage);
        textMessageStatus = (AppCompatTextView) findViewById(R.id.textMessageStatus);
        textLocation = (AppCompatTextView) findViewById(R.id.textLocation);

        appCompatButtonAccept = (AppCompatButton) findViewById(R.id.appCompatButtonAccept);
        appCompatButtonDecline = (AppCompatButton) findViewById(R.id.appCompatButtonDecline);

    }
    private void initListeners() {
        appCompatButtonAccept.setOnClickListener(this);
        appCompatButtonDecline.setOnClickListener(this);
    }
    private void initObjects() {
        huffmanDecode = new HuffmanDecode();
        h = new Handler();
        Intent myIntent = getIntent();
        ip = myIntent.getStringExtra(MainActivity.EXTRA_IP);
        username = myIntent.getStringExtra(MainActivity.EXTRA_USERNAME);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.appCompatButtonAccept:
                textMessageStatus.setText(getString(R.string.text_have_not_message));
                textLocation.setText(Environment.getExternalStorageDirectory() + "/Documents/" + sender+ ".txt");
                appCompatButtonAccept.setVisibility(View.INVISIBLE);
                appCompatButtonDecline.setVisibility(View.INVISIBLE);
                getMessage(codebook, message, sender);
                break;
            case R.id.appCompatButtonDecline:
                textMessageStatus.setText(getString(R.string.text_have_not_message));
                appCompatButtonAccept.setVisibility(View.INVISIBLE);
                appCompatButtonDecline.setVisibility(View.INVISIBLE);
                break;
        }
    }

    @Override
    protected void onResume() {
        h.postDelayed(new Runnable() {
            public void run() {
                try {
                    String url = "http://" + ip + ":8080";
                    RequestParams rParams = new RequestParams();
                    rParams.put("checkmessage","isMessage");
                    rParams.put("receivername", username);
                    AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
                    asyncHttpClient.post(url, rParams, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            try {
                                String response = new String(responseBody);
                                boolean noMessage = new JSONObject(response).isNull("nomessage");
                                if(noMessage == true){
                                    appCompatButtonAccept.setVisibility(View.VISIBLE);
                                    appCompatButtonDecline.setVisibility(View.VISIBLE);
                                    textMessageStatus.setText(getString(R.string.text_have_message));

                                    sender = new JSONObject(response).getString("sender");
                                    codebook = new JSONObject(response).getString("codebook");
                                    message = new JSONObject(response).getString("message");
                                    textUsername.setText(sender);
                                }
                                else{
                                    appCompatButtonAccept.setVisibility(View.INVISIBLE);
                                    appCompatButtonDecline.setVisibility(View.INVISIBLE);
                                    textMessageStatus.setText(getString(R.string.text_have_not_message));
                                }
                            }catch (Exception e1) {
                                e1.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Log.w("fail", "error");
                        }
                    });
                } catch (Exception e){
                    e.printStackTrace();
                }

                runnable=this;

                h.postDelayed(runnable, delay);
            }
        }, delay);
        super.onResume();
    }

    @Override
    protected void onPause() {
        h.removeCallbacks(runnable); //stop handler when activity not visible
        super.onPause();
    }


    private void getMessage(String codebook, String encoded, String sender){
        huffmanDecode.generateTree(codebook);
        encoded = (new BigInteger(encoded, 16)).toString(2).substring(1);
        textmessage.setText(huffmanDecode.decode(encoded, sender));

    }
}
