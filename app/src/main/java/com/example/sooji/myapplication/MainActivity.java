package com.example.sooji.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.content.res.Resources;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.util.Log;


import java.util.List;
import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {

    private final static String TAG = "MAIN ACTIVITY";

    List<SurveyQuestion> currentItems = new ArrayList<SurveyQuestion>();
    protected int currentQuestion = 0;

    protected CheckboxAdapter adapter;

    // UI
    TextView title;
    ListView checkboxView;
    Button nextButton;
    ArrayList<String> persistentData;
    TextView finishedMessage;
    Button cameraButton;
    String resultString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set/Reference our UI
        title = (TextView)findViewById(R.id.questionTitle);
        checkboxView = (ListView)findViewById(R.id.questionList);
        nextButton = (Button)findViewById(R.id.nextQuestionButton);
        cameraButton = (Button)findViewById(R.id.cameraButton);
        finishedMessage = (TextView) findViewById(R.id.surveyFinished);

        // Set up questions
        Resources r = getResources();

        // Questions are stored in here
        String[] questionOptions = r.getStringArray(R.array.question1);

        // Add our questions
        currentItems.add(new SurveyQuestion("Please tick any number of options that describe the lighting in your workspace", r.getStringArray(R.array.question1), SurveyQuestion.ALL));
        currentItems.add(new SurveyQuestion("How would you describe your exterior window view?", r.getStringArray(R.array.question2), 1));
        currentItems.add(new SurveyQuestion("Approximately how long have you worked under these lighting conditions?", r.getStringArray(R.array.question3), 1));
        currentItems.add(new SurveyQuestion("Are you wearing corrective eye-wear right now?", r.getStringArray(R.array.question4), 1));
        currentItems.add(new SurveyQuestion("What is your Age?", r.getStringArray(R.array.question5) , 1));
        currentItems.add(new SurveyQuestion("Does your working day consist of predominantly screen based tasks?", r.getStringArray(R.array.question6) , 1));

        // Set up first question
        SurveyQuestion firstQuestion = currentItems.get(0);
        // Question title
        title.setText(firstQuestion.getTitle());

        // Options
        //persistentData = new ArrayList<String>(Arrays.asList(firstQuestion.getQuestionOptions()));
        adapter = new CheckboxAdapter(this, R.layout.row, currentItems);
        checkboxView.setAdapter(adapter);

     //  Log.v(this, adapter.items.size(), Toast.LENGTH_LONG);


         // Setup main activities events here
        nextButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (adapter.canNext()) {
                    adapter.clear();
                    adapter.nextSurveyQuestion();
                    title.setText(adapter.getCurrentSurveyQuestion().getTitle());
                } else {
                    checkboxView.setVisibility(View.GONE);
                    nextButton.setVisibility(View.GONE);
                    title.setVisibility(View.GONE);
                    finishedMessage.setVisibility(View.VISIBLE);
                    cameraButton.setVisibility(View.VISIBLE);

                    // Allow them to take a picture with the camera activity (perhaps just get a result and handle using this page?)
                    if(hasCamera(getApplication().getApplicationContext())) {
                        // Display camera button
                        finishedMessage.setText("You may proceed to take a picture");
                        // button.setVisibility(View.VISIBLE);
                    } else {
                        finishedMessage.setText("Your device does not support a camera");
                    }

                    // MUST SAVE QUESTION
                    adapter.saveSurveyQuestion();
                    // Lets display our survey results in this text field
                    ArrayList<Boolean[]> surveyResults = adapter.returnResults();

                    resultString += "\n\n";
                    // Prepare results
                    for (int i = 0; i < surveyResults.size(); i++) {
                        Boolean[] result = surveyResults.get(i);
                        SurveyQuestion currentSurveyQuestion = currentItems.get(i);
                        resultString += "[Q" + ( i + 1 ) + "]" + " " + currentSurveyQuestion.getTitle();

                        // PARSE QUESTION RESULTS
                        int checkLimit = currentSurveyQuestion.getCheckLimit();

                        // Only allowed to include check 1 question
                        if (checkLimit == SurveyQuestion.ONE) {
                            resultString += " checked: ";
                            // Therefore, find the one checkbox
                            int position = 0;
                            for (Boolean userSelection : currentSurveyQuestion.getChecked()) {

                                // If it is checked, this is the 'answer'
                                if (userSelection) resultString += " " + currentSurveyQuestion.getQuestionOptions()[position] + " ";
                                position++;
                            }
                            // Can choose more than 1 or All of them
                        } else {
                            resultString += " checked: ";
                            int position = 0;
                            for(Boolean userSelection : currentSurveyQuestion.getChecked()) {

                                if(userSelection) resultString += " " + currentSurveyQuestion.getQuestionOptions()[position] + " ";
                                position++;
                            }
                        }
                        resultString += "\n\n";
                    }
                    //finishedMessage.setText(resultString);
                    Log.d(TAG, resultString);
                }
            }
        });// Button Click

        cameraButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(getApplication().getApplicationContext(), AndroidCameraExample.class);
                intent.putExtra("resultString", resultString);
                startActivity(intent);
            }
        });

    }


    /* Is there a camera? */
    private boolean hasCamera(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    private boolean hasExposure() {
       return true;
    }

    private void displayNextQuestion() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, Settings.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
