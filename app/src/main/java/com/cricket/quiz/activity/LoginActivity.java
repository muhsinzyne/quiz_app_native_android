package com.cricket.quiz.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cricket.quiz.Constant;
import com.cricket.quiz.R;
import com.cricket.quiz.helper.AppController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {


    private EditText phone;
    private EditText password;
    private Button loginButton;
    private String phoneNumber;
    private String passWord;
    AlertDialog alertDialog;
    AlertDialog.Builder dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dialog = new AlertDialog.Builder(LoginActivity.this);
        phone=findViewById(R.id.phoneNumber);
        password=findViewById(R.id.password);
        loginButton=findViewById(R.id.loginButton);

        loginButton.setEnabled(false);
        alertDialog = dialog.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        alertDialog.setCancelable(false);





        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.progress_dialog,null);
        dialog.setView(dialogView);

        findViewById(R.id.regText).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
            }
        });
        phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if(charSequence.length()==0){
                        loginButton.setEnabled(false);
                    }else{
                        loginButton.setEnabled(true);
                    }
            }

            @Override
            public void afterTextChanged(Editable editable) {


            }
        });

        findViewById(R.id.loginButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("message_register","Clicked");
                phoneNumber=phone.getText().toString();
                passWord=password.getText().toString();
                Log.d("message_register",phoneNumber);
                Log.d("message_register",passWord);
                if(passWord.length()>=5&& phoneNumber.length()==10) {
                    doLogin();
                }
                else if(passWord.isEmpty()||phoneNumber.isEmpty()){
                    Toast.makeText(getApplicationContext(),"Empty password or Phone Number",Toast.LENGTH_LONG).show();
                }else if(passWord.length()<5){
                    Toast.makeText(getApplicationContext(),"Password should have 8 letters",Toast.LENGTH_LONG).show();
                }
                    else{
                    Toast.makeText(getApplicationContext(),"Invalid details",Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    private void doLogin(){
        loginButton.setText("Please wait....");
        alertDialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.LOGIN_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            boolean error = obj.getBoolean("error");
                            String message = obj.getString("message");

                            if (error==false) {
                                alertDialog.dismiss();
                                loginButton.setText("Log in");
                                Log.d("message_register","Login Success");
                                SharedPreferences sharedPreferences=getSharedPreferences("User.pref",MODE_PRIVATE);
                                SharedPreferences.Editor editor=sharedPreferences.edit();
                                editor.putBoolean("Logedin",true);
                                editor.putString("Phone",phoneNumber);
                                editor.commit();

                                finish();
                                startActivity(new Intent(LoginActivity.this,MainActivity.class));
                            } else {
                                alertDialog.dismiss();
                                loginButton.setText("Log in");
                                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                                Log.d("message_register","Login Failed");
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(Constant.accessKey, Constant.accessKeyValue);
                params.put(Constant.login, "1");
                params.put(Constant.mobile,  phoneNumber);
                params.put(Constant.password, passWord);
                return params;
            }
        };
        AppController.getInstance().getRequestQueue().getCache().clear();
        AppController.getInstance().addToRequestQueue(stringRequest);
    }
}
