package com.cricket.quiz.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cricket.quiz.Constant;
import com.cricket.quiz.R;
import com.cricket.quiz.fragment.FragmentPlay;
import com.cricket.quiz.helper.AppController;
import com.cricket.quiz.helper.SettingsPreferences;
import com.cricket.quiz.helper.Utils;

import static com.cricket.quiz.helper.AppController.StopSound;
import static com.cricket.quiz.helper.AppController.getAppContext;

public class SettingActivity extends AppCompatActivity {
    private Context mContext;
    private Dialog mCustomDialog;
    private SwitchCompat mSoundCheckBox, mVibrationCheckBox, mMusicCheckBox;
    private TextView ok_btn;
    private boolean isSoundOn;
    private boolean isVibrationOn;
    private boolean isMusicOn;
    RelativeLayout fontLayout;
    public static SharedPreferences sharedPreferences;
    public static SharedPreferences.Editor editor;
    public static AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(0));
        setContentView(R.layout.activity_setting);

        mContext = SettingActivity.this;
        AppController.currentActivity = this;
        initViews();
        fontLayout = (RelativeLayout) findViewById(R.id.font_layout);
        fontLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    fontSizeDialog();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }

            }
        });
    }


    private void initViews() {
        mSoundCheckBox = (SwitchCompat) findViewById(R.id.sound_checkbox);
        mVibrationCheckBox = (SwitchCompat) findViewById(R.id.vibration_checkbox);
        mMusicCheckBox = (SwitchCompat) findViewById(R.id.show_music_checkbox);
        ok_btn = (TextView) findViewById(R.id.ok);
        populateSoundContents();
        populateVibrationContents();
        populateMusicEnableContents();
        ok_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                overridePendingTransition(R.anim.open_next, R.anim.close_next);
                finish();
            }
        });
    }

    private void moreAppClicked() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("")));
        } catch (ActivityNotFoundException anfe) {
            startActivity(new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(String.format("https://play.google.com/store/apps/developer?id=%s&hl=en", ""))));
        }
    }

    private void switchSoundCheckbox() {
        isSoundOn = !isSoundOn;
        SettingsPreferences.setSoundEnableDisable(mContext, isSoundOn);
        populateSoundContents();
    }

    private void switchVibrationCheckbox() {
        isVibrationOn = !isVibrationOn;
        SettingsPreferences.setVibration(mContext, isVibrationOn);
        populateVibrationContents();
    }

    private void switchMusicEnableCheckbox() {
        isMusicOn = !isMusicOn;
        if (isMusicOn) {
            SettingsPreferences.setMusicEnableDisable(mContext, true);
            AppController.playSound();

        } else {
            SettingsPreferences.setMusicEnableDisable(mContext, false);
            StopSound();
        }
        populateMusicEnableContents();
    }

    protected void populateSoundContents() {
        if (SettingsPreferences.getSoundEnableDisable(mContext)) {
            mSoundCheckBox.setChecked(true);
        } else {
            mSoundCheckBox.setChecked(false);
        }
        isSoundOn = SettingsPreferences.getSoundEnableDisable(mContext);
    }

    protected void populateVibrationContents() {
        if (SettingsPreferences.getVibration(mContext)) {
            mVibrationCheckBox.setChecked(true);
        } else {
            mVibrationCheckBox.setChecked(false);
        }
        isVibrationOn = SettingsPreferences.getVibration(mContext);
    }

    protected void populateMusicEnableContents() {
        if (SettingsPreferences.getMusicEnableDisable(mContext)) {
            AppController.playSound();
            mMusicCheckBox.setChecked(true);
        } else {
            StopSound();
            mMusicCheckBox.setChecked(false);
        }
        isMusicOn = SettingsPreferences.getMusicEnableDisable(mContext);
    }

    private void updateClicked() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id="
                            + mContext.getPackageName())));
        } catch (ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id="
                            + mContext.getPackageName())));
        }
    }

    private void shareClicked(String subject, String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + getPackageName());
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);

        startActivity(Intent.createChooser(intent,
                getString(R.string.share_via)));
    }

    public void viewClickHandler(View view) {
        switch (view.getId()) {
            case R.id.share_layout:
                shareClicked(getString(R.string.share_subject), AppController.getAppUrl());
                break;
            case R.id.sound_layout:
                switchSoundCheckbox();
                break;
            case R.id.sound_checkbox:
                switchSoundCheckbox();
                break;


            case R.id.vibration_layout:
                switchVibrationCheckbox();
                break;
            case R.id.vibration_checkbox:
                switchVibrationCheckbox();
                break;
            case R.id.show_hint_layout:
                switchMusicEnableCheckbox();
                break;
            case R.id.show_music_checkbox:
                String[] LOCATION_PERMS = {android.Manifest.permission.READ_PHONE_STATE
                };

                if (ContextCompat.checkSelfPermission(SettingActivity.this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(SettingActivity.this, LOCATION_PERMS, 0);
                } else {

                    setTelephoneListener();

                }

                switchMusicEnableCheckbox();
                break;
            case R.id.moreapp_layout:
                moreAppClicked();
                overridePendingTransition(R.anim.open_next, R.anim.close_next);
                break;
            case R.id.rate_layout:
                updateClicked();
                overridePendingTransition(R.anim.open_next, R.anim.close_next);
                break;
            case R.id.ok:
                onBackPressed();
                break;
            case R.id.logout_layout:
                finishAffinity();
                SharedPreferences sharedPreferences=getSharedPreferences("User.pref",MODE_PRIVATE);
                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.putBoolean("Logedin",false);
                editor.commit();
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                break;

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:

                setTelephoneListener();

                break;
        }
    }

    private void setTelephoneListener() {
        PhoneStateListener phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    StopSound();
                } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    StopSound();
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };

        TelephonyManager telephoneManager = (TelephonyManager) getAppContext().getSystemService(Context.TELEPHONY_SERVICE);
        if (telephoneManager != null) {
            telephoneManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }


    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        overridePendingTransition(R.anim.close_next, R.anim.open_next);
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {

        if (mContext != null) {
            if (mCustomDialog != null) {
                mCustomDialog.dismiss();
                mCustomDialog = null;
            }
            mVibrationCheckBox = null;
            mMusicCheckBox = null;
            mSoundCheckBox = null;
            mContext = null;
            super.onDestroy();
        }
    }

    public void fontSizeDialog() {


        String changedFontSize;

        changedFontSize = SettingsPreferences.getSavedTextSize(getApplicationContext());

        final AlertDialog.Builder dialog = new AlertDialog.Builder(SettingActivity.this);
        dialog.setTitle("Change font Size");

        LayoutInflater inflater1 = (LayoutInflater) SettingActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater1.inflate(R.layout.dialog_font_size, null);
        dialog.setView(dialogView);

        alertDialog = dialog.create();
        Button btnOk = (Button) dialogView.findViewById(R.id.btnYes);
        final EditText edt_font_size_value = (EditText) dialogView.findViewById(R.id.edt_font_size_value);
        final SeekBar skBar_value = (SeekBar) dialogView.findViewById(R.id.skBar_value);

        skBar_value.setMax(14);
        skBar_value.setProgress(Integer.parseInt(changedFontSize) - 16);
        edt_font_size_value.setText(changedFontSize);
        edt_font_size_value.setSelection(edt_font_size_value.getText().toString().length());

        skBar_value.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                edt_font_size_value.setText(Integer.toString(progress + 16));
                edt_font_size_value.setSelection(edt_font_size_value.getText().toString().length());
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {

                System.out.println("----edit size  " + edt_font_size_value.getText().toString().trim());
                if (Integer.parseInt(edt_font_size_value.getText().toString().trim()) >= 30) {
                    edt_font_size_value.setText(Constant.TEXTSIZE_MAX);
                    SettingsPreferences.saveTextSize(getApplicationContext(), Constant.TEXTSIZE_MAX);

                } else if (Integer.parseInt(edt_font_size_value.getText().toString().trim()) < 16) {
                    edt_font_size_value.setText(Constant.TEXTSIZE_MIN);
                    SettingsPreferences.saveTextSize(getApplicationContext(), Constant.TEXTSIZE_MIN);

                } else {
                    SettingsPreferences.saveTextSize(getApplicationContext(), edt_font_size_value.getText().toString().trim());

                }

                FragmentPlay.ChangeTextSize(Integer.parseInt(SettingsPreferences.getSavedTextSize(getApplicationContext())));
            }

        });

        edt_font_size_value.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String currentProgress = edt_font_size_value.getText().toString().trim();
                if (!currentProgress.equals("")) {
                    skBar_value.setProgress(Integer.parseInt(currentProgress) - 16);
                    edt_font_size_value.setSelection(edt_font_size_value.getText().toString().length());
                }
            }
        });
        btnOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }
}
