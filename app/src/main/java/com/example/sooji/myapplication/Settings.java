package com.example.sooji.myapplication;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import android.view.View;
import android.view.View.OnClickListener;

public class Settings extends Activity{

    TextView emailInput;
    Button saveButton;
    Button backButton;
    public static final String SETTINGS = "survey_settings";


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        saveButton = (Button)findViewById(R.id.saveButton);
        emailInput = (EditText) findViewById(R.id.emailInput);
        backButton = (Button)findViewById(R.id.backButton);

        SharedPreferences settings = getSharedPreferences(SETTINGS, MODE_PRIVATE);
        String emailValue = settings.getString("email","");

        if(emailValue != "") {
            emailInput.setText(emailValue);
        }


        saveButton.setOnClickListener(saveListener);
        backButton.setOnClickListener(backListener);

    }

    View.OnClickListener backListener = new View.OnClickListener(){
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
    };

    View.OnClickListener saveListener = new View.OnClickListener() {
        public void onClick(View v) {
            SharedPreferences settings = getSharedPreferences(SETTINGS, MODE_PRIVATE);
            Editor editor  = settings.edit();
            editor.putString("email",  emailInput.getText().toString());
            editor.commit();
            Toast toast = Toast.makeText(getApplicationContext(), "Saved Settings", Toast.LENGTH_SHORT);
            toast.show();
        }
    };
}
