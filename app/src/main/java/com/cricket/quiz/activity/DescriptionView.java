package com.cricket.quiz.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cricket.quiz.Constant;
import com.cricket.quiz.R;
import com.cricket.quiz.fragment.FragmentPlay;
import com.cricket.quiz.helper.AppController;
import com.cricket.quiz.helper.Utils;
import com.crowdfire.cfalertdialog.CFAlertDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DescriptionView extends AppCompatActivity {

    public ProgressBar prgLoading;
    public WebView mWebView;
    public Activity activity;
    public ImageView back,setting;
    public TextView tvTitle;
    public int LEVEL;

    @SuppressLint({"SetJavaScriptEnabled", "NewApi"})
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description_view);


        prgLoading = (ProgressBar) findViewById(R.id.prgLoading);
        mWebView = (WebView) findViewById(R.id.webView1);
        setting=(ImageView)findViewById(R.id.setting) ;
        back = (ImageView) findViewById(R.id.back);
        tvTitle=(TextView)findViewById(R.id.tvLevel);
        tvTitle.setText("Quiz Description");
        setting.setVisibility(View.GONE);
        try {
            if (Utils.isNetworkAvailable(this)) {
                mWebView.setClickable(true);
                mWebView.setFocusableInTouchMode(true);
                mWebView.getSettings().setJavaScriptEnabled(true);

                GetDescriptionView();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        findViewById(R.id.submitButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences=getSharedPreferences("START", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor =sharedPreferences.edit();
                editor.putString("GAME_START","YES");
                editor.commit();

                launchPlay();
            }
        });


    }

    public void GetDescriptionView() {
        if (!prgLoading.isShown()) {
            prgLoading.setVisibility(View.VISIBLE);
        }


        StringRequest strReq = new StringRequest(Request.Method.POST, Constant.DESCRIPTION_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                prgLoading.setVisibility(View.GONE);
                try {
                    JSONObject obj = new JSONObject(response);
                    boolean error = obj.getBoolean("error");
                    String message = obj.getString("message");
                    String dataStr=obj.getJSONObject("data").getString("content");


                    if (error==false) {

                        if(dataStr.isEmpty()){
                            dataStr ="No specific description";
                        }

                        mWebView.setVerticalScrollBarEnabled(true);
                        mWebView.loadDataWithBaseURL("", dataStr, "text/html", "UTF-8", "");
                        mWebView.setBackgroundColor(getResources().getColor(R.color.white));



                    } else {
                        Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

            }

        }) {
            @Override
            protected Map<String, String> getParams() {

                Map<String, String> params = new HashMap<String, String>();
                params.put(Constant.accessKey, Constant.accessKeyValue);
                params.put(Constant.getLearningDocument, "1");
                params.put(Constant.subCategory, "157");
                params.put(Constant.level, "1");

                return params;

            }
        };

        AppController.getInstance().getRequestQueue().getCache().clear();
        AppController.getInstance().addToRequestQueue(strReq);




    }
        // }
    private void launchPlay(){
       onBackPressed();
    }

    @SuppressLint("NewApi")
    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
    }

    @SuppressLint("NewApi")
    @Override
    protected void onPause() {
        mWebView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {

            case android.R.id.home:
                // app icon in action bar clicked; go home
                this.finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        finish();
        super.onBackPressed();

    }


}

