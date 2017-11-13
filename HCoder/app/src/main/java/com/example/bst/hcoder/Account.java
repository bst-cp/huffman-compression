package com.example.bst.hcoder;

/**
 * Created by Bora on 11.11.2017.
 */

import android.content.Intent;
import android.os.Bundle;
import android.net.Uri;
import android.util.Log;
import android.content.Context;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.math.BigInteger;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView;

import com.example.bst.hcoder.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.FileInputStream;

import cz.msebera.android.httpclient.Header;

public class Account extends AppCompatActivity implements View.OnClickListener {
    private final AppCompatActivity activity = Account.this;

    private NestedScrollView nestedScrollView;

    private AppCompatButton appCompatButtonSelect;
    private AppCompatButton appCompatButtonExecute;
    private AppCompatButton appCompatButtonUser;

    private AppCompatTextView appCompatTextViewUsername;
    private AppCompatTextView appCompatTextViewSelected;
    private AppCompatTextView appCompatTextViewEncoded;
    private AppCompatTextView appCompatTextViewCodebook;
    private AppCompatTextView appCompatTextViewTousername;

    private Spinner spinnerUsers;

    private String username;
    private String[] usersArray;
    private String toUsername;
    private String ip;

    private String content = "";
    private String encoded = "";
    private String codebook = "";

    private static final int READ_REQUEST_CODE = 42;

    private static final String TAG = "Something";

    private HuffmanTree huffmanTree;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        Intent myIntent = getIntent();
        username = myIntent.getStringExtra(MainActivity.EXTRA_USERNAME);
        usersArray = myIntent.getStringArrayExtra(MainActivity.EXTRA_USERS);
        ip = myIntent.getStringExtra(MainActivity.EXTRA_IP);
        int usersSize = usersArray.length;

        appCompatTextViewUsername = (AppCompatTextView) findViewById(R.id.appCompatTextViewUsername);
        username = username + " :  ";
        appCompatTextViewUsername.setText(username);

        List<String> list = new ArrayList<String>();
        for(int i = 0; i < usersSize; i++)
            list.add(usersArray[i]);
        spinnerUsers = (Spinner) findViewById(R.id.user_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUsers.setAdapter(adapter);

        initViews();
        initListeners();
    }

    /**
     * initViews is used to initialize views
     */
    private void initViews() {

        nestedScrollView = (NestedScrollView) findViewById(R.id.nestedScrollView);

        appCompatButtonSelect = (AppCompatButton) findViewById(R.id.appCompatButtonSelect);
        appCompatButtonExecute = (AppCompatButton) findViewById(R.id.appCompatButtonExecute);
        appCompatButtonUser = (AppCompatButton) findViewById(R.id.appCompatButtonUser);

        appCompatTextViewSelected = (AppCompatTextView) findViewById(R.id.appCompatTextViewSelected);
        appCompatTextViewEncoded = (AppCompatTextView) findViewById(R.id.appCompatTextViewEncoded);
        appCompatTextViewCodebook = (AppCompatTextView) findViewById(R.id.appCompatTextViewCodebook);
        appCompatTextViewTousername = (AppCompatTextView) findViewById(R.id.appCompatTextViewTousername);

    }

    /**
     * initListeners is used to initialize listeners
     */
    private void initListeners() {
        appCompatButtonSelect.setOnClickListener(this);
        appCompatButtonExecute.setOnClickListener(this);
        appCompatButtonUser.setOnClickListener(this);
    }

    /**
     * onClick is used to listen click on view
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.appCompatButtonSelect:
                performFileSearch();
                break;
            case R.id.appCompatButtonExecute:
                executeAlgorithm();
                break;
            case R.id.appCompatButtonUser:
                toUsername = String.valueOf(spinnerUsers.getSelectedItem());
                appCompatTextViewTousername.setText(toUsername);
                BigInteger b = new BigInteger("1" + encoded, 2);
                String hexStr = b.toString(16);
                try {
                    String url = "http://" + ip + ":8080";
                    RequestParams rParams = new RequestParams();
                    rParams.put("sender",username);
                    rParams.put("receiver", toUsername);
                    rParams.put("codebook", codebook);
                    Log.w("codebook: ", codebook);
                    rParams.put("message", hexStr);
                    AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
                    asyncHttpClient.post(url, rParams, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            Log.w("success", new String(responseBody));
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Log.w("fail", "fail");
                        }
                    });
                } catch (Exception e){
                    e.printStackTrace();
                }

                /*Bu kısımda elde edlilen hexStr, codebook ile birlikte server'a gönderilecek*/
                /*Ayrıca sender: username, receiver: toUsername stringleri de hazır*/

                /*byte[] bt = new BigInteger(hexStr, 16).toByteArray();
                byte[] bt2 = bt;
                try{
                    FileOutputStream fos = new FileOutputStream("/storage/emulated/0/Download/encoded.txt");
                    fos.write(bt);
                    fos.close();
                } catch (Exception e ){
                    e.printStackTrace();
                }*/
                break;
        }
    }

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    public void performFileSearch() {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("*/*");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    private String readTextFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        return stringBuilder.toString();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        try {
            if (requestCode == READ_REQUEST_CODE && resultCode == activity.RESULT_OK) {
                // The document selected by the user won't be returned in the intent.
                // Instead, a URI to that document will be contained in the return intent
                // provided to this method as a parameter.
                // Pull that URI using resultData.getData().
                Uri uri = null;
                if (resultData != null) {
                    uri = resultData.getData();
                    Log.i(TAG, "Uri: " + uri.toString());
                    content = readTextFromUri(uri);
                }
                appCompatTextViewSelected.setText(content);
                Snackbar.make(nestedScrollView, getString(R.string.successful_file), Snackbar.LENGTH_LONG).show();
            }
        } catch(IOException ie) {
            ie.printStackTrace();
        }
    }

    public void executeAlgorithm(){
        // we will assume that all our characters will have
        // code less than 256, for simplicity
        int[] charFreqs = new int[256];
        // read each character and record the frequencies
        for (char c : content.toCharArray())
            charFreqs[c]++;

        // build tree
        HuffmanTree tree = buildTree(charFreqs);

        StringBuffer encStr = new StringBuffer();
        //create encoded
        for (char c: content.toCharArray()){
            createEncoded(tree, new StringBuffer(), c, encStr);
        }
        encoded = encStr.toString();

        StringBuffer cbStr = new StringBuffer();
        //create codebook
        createCodebook(tree, new StringBuffer(), cbStr);
        codebook = cbStr.toString();

        appCompatTextViewEncoded.setText(encoded);
        appCompatTextViewCodebook.setText(codebook);
        Snackbar.make(nestedScrollView, getString(R.string.successful_compress), Snackbar.LENGTH_LONG).show();
    }

    public static HuffmanTree buildTree(int[] charFreqs){
        PriorityQueue<HuffmanTree> trees = new PriorityQueue<HuffmanTree>();
        // initially, we have a forest of leaves
        // one for each non-empty character
        for (int i = 0; i < charFreqs.length; i++)
            if (charFreqs[i] > 0)
                trees.offer(new HuffmanLeaf(charFreqs[i], (char)i));

        assert trees.size() > 0;
        // loop until there is only one tree left
        while (trees.size() > 1) {
            // two trees with least frequency
            HuffmanTree a = trees.poll();
            HuffmanTree b = trees.poll();

            // put into new node and re-insert into queue
            trees.offer(new HuffmanNode(a, b));
        }
        return trees.poll();
    }

    public static void createEncoded(HuffmanTree tree, StringBuffer prefix, char c, StringBuffer encStr) {
        assert tree != null;
        if (tree instanceof HuffmanLeaf) {
            HuffmanLeaf leaf = (HuffmanLeaf)tree;

            if(leaf.value == c){
                encStr.append(prefix);
            }
            // print out character, frequency, and code for this leaf (which is just the prefix)
            //System.out.println(leaf.value + "\t" + leaf.frequency + "\t" + prefix);

        } else if (tree instanceof HuffmanNode) {
            HuffmanNode node = (HuffmanNode)tree;

            // traverse left
            prefix.append('0');
            createEncoded(node.left, prefix, c, encStr);
            prefix.deleteCharAt(prefix.length()-1);

            // traverse right
            prefix.append('1');
            createEncoded(node.right, prefix, c, encStr);
            prefix.deleteCharAt(prefix.length()-1);
        }
    }

    public static void createCodebook(HuffmanTree tree, StringBuffer prefix, StringBuffer cbStr) {
        assert tree != null;
        if (tree instanceof HuffmanLeaf) {
            HuffmanLeaf leaf = (HuffmanLeaf)tree;

            cbStr.append(leaf.value);
            cbStr.append(' ');
            cbStr.append(prefix);
            cbStr.append("..");
            // print out character, frequency, and code for this leaf (which is just the prefix)
            //System.out.println(leaf.value + "\t" + leaf.frequency + "\t" + prefix);

        } else if (tree instanceof HuffmanNode) {
            HuffmanNode node = (HuffmanNode)tree;

            // traverse left
            prefix.append('0');
            createCodebook(node.left, prefix, cbStr);
            prefix.deleteCharAt(prefix.length()-1);

            // traverse right
            prefix.append('1');
            createCodebook(node.right, prefix, cbStr);
            prefix.deleteCharAt(prefix.length()-1);
        }
    }
}
