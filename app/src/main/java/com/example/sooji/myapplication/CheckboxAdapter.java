package com.example.sooji.myapplication;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import android.util.Log;

public class CheckboxAdapter extends ArrayAdapter<String> {

    private static final int FIRST_QUESTION = 0;
    private static final String TAG = "CheckboxAdapter";

    Context context;

    private List<SurveyQuestion> surveyQuestions;
    private int numSurveys = 0; // How many surveys we have
    private int currentSurvey = 0; // The current Survey
    private CheckBox lastCheckbox;
    private Boolean[] currentlyChecked; // Boolean
    private int numChecked = 0; // How many questions on the current survey, we have checked

    // List of items currently showing in the checkbox
    ArrayList<String> items;

    @Override
    public void clear() {
        super.clear();
        items.clear();
    }

    @Override
    public void add(String object) {
        super.add(object);
        items.add(object);
    }

    @Override
    public void addAll(Collection<? extends String> collection) {
        super.addAll(collection);
        items.addAll(collection);
    }

    // Can we actually go?
    public Boolean canNext()
    {
        // Not the last survey, nope, don't want that to happen
        return currentSurvey != ( numSurveys - 1 );
    }

    public SurveyQuestion getCurrentSurveyQuestion()
    {
        return surveyQuestions.get(currentSurvey);
    }

    private int numChecked() {
        int numChecked = 0;
        for(Boolean isChecked : currentlyChecked) {
            if(isChecked) numChecked++;
        }
        return numChecked;
    }


    /* ------------------------
       ADAPTER HANDLER FUNCTIONS
    * -----------------------*/

    // Let's go to the next Survey Question
    public void nextSurveyQuestion()
    {
        // Now let's just output the current SurveyCount!
        Log.d(TAG, "The current survey was " + currentSurvey);


        if(canNext()) {
            // Before going next, save the list of 'checked items'
            // inside it's surveyQuestion object
            //surveyQuestions.get(currentSurvey++).setChecked(currentlyChecked); // the ++ only applies after that one usage
            saveSurveyQuestion(); //
            currentSurvey++;
            // Since we are on the next survey, let's store it
            SurveyQuestion nextSurvey = surveyQuestions.get(currentSurvey);

            //let's remove all the values
            currentlyChecked = new Boolean[nextSurvey.getQuestionOptions().length];
            Arrays.fill(currentlyChecked, false);
            numChecked = 0;
            lastCheckbox = null;

            // Welp, let's go to the next one shall we?
            addAll(new ArrayList<>(Arrays.asList(nextSurvey.getQuestionOptions())));


            notifyDataSetChanged();
        } else {
            Log.d(TAG, "Cannot canNext(); We are on the last index");
        }
    }


    // This saves the current question
    public void saveSurveyQuestion() {
        surveyQuestions.get(currentSurvey).setChecked(currentlyChecked);
        Log.v(TAG, "Saving checked list For Survey Question #" + currentSurvey + ".");
    }

    public ArrayList<Boolean[]> returnResults() {
        int position; // the current survey question of interation
        ArrayList<Boolean[]> surveyResults = new ArrayList<>();
        for( position = 0; position < numSurveys; position++ ) {
            SurveyQuestion currentSurveyQuestion = surveyQuestions.get(position);
            Boolean[] surveyResult = currentSurveyQuestion.getChecked();
            surveyResults.add(surveyResult);
        }
        return surveyResults;
    }

    public CheckboxAdapter(Context context, int resource, List<SurveyQuestion> surveyQuestions)
    {

        //new ArrayList(Arrays.asList(surveyQuestions.get(0).getQuestionOptions()));
        super(context, resource, new ArrayList<>(Arrays.asList(surveyQuestions.get(0).getQuestionOptions())));
        this.context = context;
        this.items = new ArrayList<>(Arrays.asList(surveyQuestions.get(0).getQuestionOptions()));
        this.surveyQuestions = surveyQuestions;
        numSurveys = surveyQuestions.size();
        currentlyChecked = new Boolean[this.items.size()];
        Arrays.fill(currentlyChecked, false);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        convertView = inflater.inflate(R.layout.row, parent, false);
        CheckBox cb = (CheckBox) convertView.findViewById(R.id.questionCheckbox);

        String checkboxText = "(this shouldn't display)";

        if(position < items.size()) {
            checkboxText = items.get(position);
        }

        cb.setText(checkboxText);
        cb.setTag(position);

        cb.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view)
            {
                CheckBox cb = (CheckBox)view;
                int position = (Integer) cb.getTag();

                int checkLimit = surveyQuestions.get(currentSurvey).getCheckLimit();

                // Handle survey questions limited to one result (answer)
                if (checkLimit == SurveyQuestion.ONE) {
                    // ONE checkbox IS selected
                    if (numChecked == 1) {
                        // Not the current one!!
                        if(lastCheckbox != cb) {
                            lastCheckbox.setChecked(false);
                            currentlyChecked[(Integer)lastCheckbox.getTag()] = false;
                            currentlyChecked[position] = true;
                            numChecked = numChecked; // Stays the same. We are not iterating, just switching
                        } else {
                            // If we have clicked the current one
                            // uncheck it as per normal
                            currentlyChecked[position] = false;
                            numChecked--;
                        }
                    // All are Unselected
                    } else {
                        currentlyChecked[position] = true;
                        numChecked++;
                    }
                    // Store the new checkbox
                    lastCheckbox = cb;
                } else if(checkLimit == SurveyQuestion.ALL) { // Can select > 1 or all of them
//                    if( numChecked !=  checkLimit) {
//                        numChecked++;
//                    }
                    currentlyChecked[position] = !currentlyChecked[position];

                    if (!currentlyChecked[position]){
                        numChecked--;
                    } else {
                        numChecked++;
                    }
                }
            }
        });
        return convertView;
    }

    private void unCheck(int item) {

    }

}
