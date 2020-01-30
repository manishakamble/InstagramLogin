package com.showoff.tech.test;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.HashMap;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private InstagramApp mApp;
    private Button btnConnect, btnViewInfo;
    private ProgressDialog mProgress;
    Context context;
    private HashMap<String, String> userInfoHashmap = new HashMap<String, String>();
    private HashMap<String, String> mediaDetailHashmap = new HashMap<String, String>();
    private ArrayList<String> mediaList = new ArrayList<>();
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == InstagramApp.WHAT_FINALIZE) {
                userInfoHashmap = mApp.getUserInfo();
            } else if (msg.what == InstagramApp.WHAT_FINALIZE) {
                Toast.makeText(LoginActivity.this, "Check your network.",
                        Toast.LENGTH_SHORT).show();
            }
            if (msg.what == InstagramApp.WHAT_MEDIA) {
                mediaList = mApp.getUserMediaList();
                if (mediaList != null && mediaList.size() != 0) {
                    mApp.fetchUserMediaDetails(handler, mediaList.get(0));
                }
            }
            if (msg.what == InstagramApp.WHAT_MEDIA_DETAILS) {
                mediaDetailHashmap = mApp.getUserMediaDetail();
                if (mediaDetailHashmap != null) {
                    launchApp();
                }
            }
            return false;
        }
    });

    private void launchApp() {
        Intent intent = new Intent(this, UserInfoActivity.class);
        intent.putExtra("id", userInfoHashmap.get(InstagramApp.TAG_ID));
        intent.putExtra("username", userInfoHashmap.get(InstagramApp.TAG_USERNAME));
        intent.putExtra("mediaId", mediaDetailHashmap.get(InstagramApp.TAG_ID));
        intent.putExtra("mediaType", mediaDetailHashmap.get(InstagramApp.TAG_MEDIA_TYPE));
        intent.putExtra("mediaUrl", mediaDetailHashmap.get(InstagramApp.TAG_MEDIA_URL));
        intent.putExtra("timestamp", mediaDetailHashmap.get(InstagramApp.TAG_TIMESTAMP));
        startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setWidgetReference();
        bindEventHandlers();
        this.context = getBaseContext();
        mApp = new InstagramApp(this, Utils.APP_ID,
                Utils.CLIENT_SECRET_KEY, Utils.REDIRECT_URI);
        mApp.setListener(new InstagramApp.OAuthAuthenticationListener() {
            @Override
            public void onSuccess() {
                btnConnect.setText("Logout");
                // btnViewInfo.setEnabled(true);
                mApp.fetchUserName(handler);
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
        if (mApp.hasAccessToken()) {
            btnConnect.setText("Logout");
            // btnViewInfo.setEnabled(true);
            mApp.fetchUserName(handler);
        }

    }

    private void bindEventHandlers() {
        btnConnect.setOnClickListener(this);
        btnViewInfo.setOnClickListener(this);
    }

    private void setWidgetReference() {
        btnConnect = (Button) findViewById(R.id.btn_login);
        btnViewInfo = (Button) findViewById(R.id.btnViewInfo);
        //btnViewInfo.setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        if (v == btnConnect) {
            connectOrDisconnectUser();
        } else if (v == btnViewInfo) {
            if (!mApp.hasAccessToken()) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setMessage("Please login first to Instagram")
                        .setCancelable(false)
                        .setNegativeButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                final AlertDialog alert = builder.create();
                alert.show();
            } else {
                reDirectToUserProfile();
            }
        }
    }

    private void connectOrDisconnectUser() {
        if (mApp.hasAccessToken()) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            builder.setMessage("Logout from Instagram?")
                    .setCancelable(false)
                    .setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    mApp.resetAccessToken();
                                    btnConnect.setText("Login");
                                    //btnViewInfo.setEnabled(false);
                                }
                            })
                    .setNegativeButton("No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    dialog.cancel();
                                }
                            });
            final AlertDialog alert = builder.create();
            alert.show();
        } else {
            mApp.authorize();
        }
    }

    private void reDirectToUserProfile() {
        mApp.fetchUserMedia(handler);
    }

}
