package com.showoff.tech.test;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.squareup.picasso.Picasso;

public class UserInfoActivity extends AppCompatActivity {

    private TextView txtUserName;
    private TextView txtMediaInfo;
    private TextView txtMediaTimeStamp;
    private ImageView imgUserProfile;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        txtUserName = (TextView) findViewById(R.id.txtUserName);
        txtMediaInfo = (TextView) findViewById(R.id.txtMediaInfo);
        txtMediaTimeStamp = (TextView) findViewById(R.id.txtTimeStamp);
        imgUserProfile = (ImageView) findViewById(R.id.imageView);
        btnBack = (Button) findViewById(R.id.btnGoBack);
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getStringExtra("username") != null) {
                txtUserName.setText("User Name : " + intent.getStringExtra("username"));
            }
            if (intent.getStringExtra("mediaType") != null) {
                txtMediaTimeStamp.setText("Timestamp : " + intent.getStringExtra("timestamp"));
            }
            if (intent.getStringExtra("timestamp") != null) {
                txtMediaInfo.setText("Media Type : " + intent.getStringExtra("mediaType"));
            }
            if (intent.getStringExtra("mediaUrl") != null) {
                Picasso.get().load(intent.getStringExtra("mediaUrl")).into(imgUserProfile);
            }
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 finish();
            }
        });

    }


}