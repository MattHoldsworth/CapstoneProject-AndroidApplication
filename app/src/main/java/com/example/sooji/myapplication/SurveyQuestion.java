package com.example.sooji.myapplication;

import java.util.List;

public class SurveyQuestion {

    public static final int ALL = -1;
    public static final int ONE = 1;

    protected String questionTitle;
    protected String[] questionOptions;
    protected int checkLimit;
    protected Boolean[] checked;

    public SurveyQuestion(String questionTitle, String[] questionOptions, int checkLimit)
    {
        this.questionTitle = questionTitle;
        this.questionOptions = questionOptions;
        this.checkLimit = checkLimit;

        if(checkLimit == ALL) {
            checkLimit = questionOptions.length;
        }
    }

    public String getTitle()
    {
        return questionTitle;
    }

    public String[] getQuestionOptions()
    {
        return questionOptions;
    }

    public int getCheckLimit()
    {
        return checkLimit;
    }

    public Boolean[] getChecked()
    {
        return checked;
    }

    public void setChecked(Boolean[] checked)
    {
        this.checked = checked;
    }
}
