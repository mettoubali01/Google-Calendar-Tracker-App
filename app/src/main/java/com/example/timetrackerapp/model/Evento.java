package com.example.timetrackerapp.model;

import java.math.BigDecimal;

public class Evento {
    //vars
    private String id;
    private String name;
    private String author;
    private String day;
    private String colorId;
    private BigDecimal spentHours;

    public Evento(){}
    public Evento(String name, String colorId, String day, BigDecimal spentHours, String author) {
        this.name = name;
        this.colorId = colorId;
        this.day = day;
        this.spentHours = spentHours;
        this.author = author;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColorId() {
        return colorId;
    }

    public void setColorId(String colorId) {
        this.colorId = colorId;
    }

    public BigDecimal getSpentHours() {
        return spentHours;
    }

    public void setSpentHours(BigDecimal spentHours) {
        this.spentHours = spentHours;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    @Override
    public String toString() {
        return "Evento{" +
                "name='" + name + '\'' +
                ", autor='" + author + '\'' +
                ", day='" + day + '\'' +
                ", colorId='" + colorId + '\'' +
                ", spentHours=" + spentHours +
                '}';
    }
}
