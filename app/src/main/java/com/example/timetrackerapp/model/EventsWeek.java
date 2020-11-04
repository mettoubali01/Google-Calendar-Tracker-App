package com.example.timetrackerapp.model;

import android.app.usage.UsageEvents;

import com.github.mikephil.charting.data.BarEntry;
import com.google.api.client.util.DateTime;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.ArrayList;

public class EventsWeek {
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("events")
    @Expose
    private ArrayList<Evento> events;

    public EventsWeek() {
        this.events = new ArrayList<>();
    }

    public EventsWeek(String name, ArrayList<Evento> events) {
        this.name = name;
        this.events = events;
    }

    public EventsWeek(String name) {
        this.name = name;
    }

    public ArrayList<Evento> getEvents() {
        return events;
    }

    public void setEvents(ArrayList<Evento> events) {
        this.events = events;
    }

    //adding events
    public void addEvent(Evento evento){
        this.events.add(evento);
    }

    //check if exists
    public boolean exist(String eventName){
        boolean flag = false;
        for (Evento event : this.getEvents()) {
            if (event.getName().equals(eventName))
                flag = true;
        }
        return flag;
    }

    //remove elemntos from the lis
    public void removeElementByValue(String name){
        for (Evento evento : this.getEvents()) {
            if (evento.getName().equals(name)){
                this.getEvents().remove(evento);
            }
        }
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "EventsWeek{" +
                "name='" + name + '\'' +
                ", events=" + events +
                '}';
    }
}