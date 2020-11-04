package com.example.timetrackerapp.common;

import java.util.ArrayList;

public class Common {
    public static final String[] DAYS = {"MONDAY", "TUESDAY", "WEDNESDAY","THURSDAY","FRIDAY","SATURDAY", "SUNDAY"};
    public static final String[] EVENTSCOLORS = {"#1051f8","#7ae7bf","#dbadff","#ff887c", "#fbd75b","#ffb878","#46d6db", "#e1e1e1", "#5484ed","#51b749","#dc2127"};
    public static ArrayList<String> LABELS = new ArrayList<String>(){
        {
            add("Lu");
            add("Ma");
            add("Mi");
            add("Ju");
            add("Vi");
            add("Sá");
            add("Do");
        }
    };
}
