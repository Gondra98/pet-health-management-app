package com.example.pet_care3.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AlarmDataModel {

    private String alarmName;
    private String alarmTime;

    public AlarmDataModel(String alarmName, String alarmTime) {
        this.alarmName = alarmName;
        this.alarmTime = alarmTime;
    }

    public String getAlarmName() {
        return alarmName;
    }

    public String getAlarmTime() {
        return alarmTime;
    }
}




