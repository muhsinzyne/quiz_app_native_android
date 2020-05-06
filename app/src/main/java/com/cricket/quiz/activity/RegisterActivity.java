package com.cricket.quiz.activity;

import android.app.AlertDialog;
import android.content.Context;
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

public class RegisterActivity extends AppCompatActivity {
    private EditText phone,state,district,password,name;
    private Button submitButton;
    private String phoneNumber , passWord , districtText,stateText,nameText;
    private AlertDialog alertDialog;
    private AlertDialog.Builder dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        phone=findViewById(R.id.phoneNumber);
        password=findViewById(R.id.password);
        district=findViewById(R.id.district);
        state=findViewById(R.id.state);
        submitButton=findViewById(R.id.submitButton);
        name=findViewById(R.id.name);

        dialog = new AlertDialog.Builder(RegisterActivity.this);
        submitButton.setEnabled(false);
        alertDialog = dialog.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        alertDialog.setCancelable(false);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.progress_dialog,null);
        dialog.setView(dialogView);

        phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length()==0){
                    submitButton.setEnabled(false);
                }else{
                   submitButton.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {


            }
        });

        findViewById(R.id.submitButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("message_register","Clicked");
                phoneNumber=phone.getText().toString();
                passWord=password.getText().toString();
                stateText=state.getText().toString();
                districtText=district.getText().toString();
                nameText=name.getText().toString();
                Log.d("message_register",phoneNumber);
                Log.d("message_register",passWord);
                Log.d("message_register",nameText);
                Log.d("message_register",stateText);
                Log.d("message_register",districtText);

                if(passWord.length()>=5&& phoneNumber.length()==10 && !nameText.isEmpty()&& !stateText.isEmpty() && !districtText.isEmpty()) {
                    doRegister();
                }
                else if(passWord.isEmpty()||phoneNumber.isEmpty()){
                    Toast.makeText(getApplicationContext(),"Empty password or email",Toast.LENGTH_LONG).show();
                }else if(passWord.length()<5){
                    Toast.makeText(getApplicationContext(),"Password should have 5 characters",Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getApplicationContext(),"Invalid details",Toast.LENGTH_LONG).show();
                }

            }
        });

    }
    private void doRegister(){
        submitButton.setText("Please wait....");
        alertDialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.REGISTER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            boolean error = obj.getBoolean("error");
                            String message = obj.getString("message");

                            if (error==false) {
                                alertDialog.dismiss();
                                finish();
                                submitButton.setText("Submit");
                                Log.d("message_register","Register Success");
                            } else {
                                alertDialog.dismiss();
                                submitButton.setText("Submit");
                                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                                Log.d("message_register","Register Failed");
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
                params.put(Constant.registration, "1");
                params.put(Constant.mobile,  phoneNumber);
                params.put(Constant.password, passWord);
                params.put(Constant.name,  nameText);
                params.put(Constant.district, districtText);
                params.put(Constant.state, stateText);
                return params;
            }
        };
        AppController.getInstance().getRequestQueue().getCache().clear();
        AppController.getInstance().addToRequestQueue(stringRequest);
    }
}
