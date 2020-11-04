package com.example.timetrackerapp.adapters;

import android.graphics.Color;
import android.os.Build;
import com.google.api.services.calendar.model.Events;
import androidx.annotation.RequiresApi;
import com.example.timetrackerapp.common.Common;
import com.example.timetrackerapp.model.*;
import com.github.mikephil.charting.data.BarEntry;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import java.math.*;
import java.text.DecimalFormat;
import java.time.*;
import java.util.*;

public class EventAdapter {

    //calculate hours by the giving start time and the end time
    public BigDecimal getElapsedHours(DateTime start, DateTime end){
        DecimalFormat df = new DecimalFormat("#.0");
        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        double minutes = (((end.getValue() - start.getValue()) / minutesInMilli) % 60);
        BigDecimal bigDecimal = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        return  new BigDecimal((df.format((end.getValue() - start.getValue()) / hoursInMilli))).add(bigDecimal);
    }

    //giving a DateTime we extract the weekly day
    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getDayOfWeek(DateTime dateTime){
        String dateTimeString = dateTime.toStringRfc3339();
        if (dateTime.toStringRfc3339().contains("T"))
            dateTimeString = dateTime.toStringRfc3339().substring(0, dateTime.toStringRfc3339().indexOf("T"));
        String splittedDate[] = dateTimeString.split("-");
        LocalDate localDate = LocalDate.of(Integer.parseInt(splittedDate[0]), Integer.parseInt(splittedDate[1]), Integer.parseInt(splittedDate[2]) );
        DayOfWeek dayOfWeek = DayOfWeek.from(localDate);

        return dayOfWeek.name();
    }

    //arrayList to float[]
    public float[] arrayListToFloatArray(ArrayList<Float> arrayList){

        float converted[] = new float[arrayList.size()];
        int index = 0;
        for (Object object: arrayList) {
            converted[index++] = (Float) object;
        }
        return converted;
    }

    //convert String[] into int[]
    public static int[] getColorFromString(String [] colors){
        int[] intColors = new int[colors.length];
        int j = 0;

        for (String stringColor : colors) {
            intColors[j] = Color.parseColor(stringColor);
            j++;
        }
        return intColors;
    }

    //Get events and put them in a map
    @RequiresApi(api = Build.VERSION_CODES.O)
    public HashMap<String, EventsWeek> getEventsInMap(Events events){
        List<Event> items = events.getItems();
        HashMap<String, EventsWeek> map = new HashMap();

        for (Event event : items) {
            if (event.getColorId() == null) {
                event.setColorId("1");
            }
            EventsWeek eventsWeek = new EventsWeek();

            DateTime start = event.getStart().getDateTime();
            DateTime end = event.getEnd().getDateTime();
            if (start == null || end == null) {
                start = event.getStart().getDate();
                end = event.getEnd().getDate();
            }
            BigDecimal elapsedHours = getElapsedHours(start, end);
            Evento eventObject = new Evento(event.getSummary(), event.getColorId(), getDayOfWeek(start), elapsedHours, event.getCreator().getEmail());
            if (!map.containsKey(eventObject.getName())) {
                eventsWeek.setName(eventObject.getName());
                if (eventsWeek.getName().equals(eventObject.getName())) {
                    eventsWeek.addEvent(eventObject);
                    map.put(eventObject.getName(), eventsWeek);
                }
            } else {
                EventsWeek eventsWeek1 = (EventsWeek) map.get(eventObject.getName());
                eventsWeek1.addEvent(eventObject);
                map.put(eventObject.getName(), eventsWeek1);
            }
        }

        return map;
    }

    // get all events from HashMap with their color and spent hour in week
    // to print the home barChart
    public ArrayList getDailyEventHoursSpent(HashMap<String, EventsWeek> map){
        List<String> colorIdEvents = new ArrayList<>();
        ArrayList<BarEntry> barEntryValues = new ArrayList<>();
        ArrayList results = new ArrayList<>();

        for (int t = 0; t < Common.DAYS.length; t++) {
            ArrayList<Float> count = new ArrayList<Float>();
            String day = null;

            for (Map.Entry<String, EventsWeek> mapp : map.entrySet()) {
                ArrayList<Evento> eventos = mapp.getValue().getEvents();
                if (eventos.size() > 1) {
                    for (Evento evento : eventos) {
                        if (evento.getDay().equals(Common.DAYS[t])){
                            count.add(evento.getSpentHours().floatValue());
                            day = evento.getDay();
                            if (evento.getColorId() == null) {
                                colorIdEvents.add(Common.EVENTSCOLORS[0]);
                            }else {
                                colorIdEvents.add(Common.EVENTSCOLORS[Integer.parseInt(evento.getColorId())-1]);
                            }
                        }
                    }
                }else{
                    Evento evento1 = mapp.getValue().getEvents().get(0);
                    if (evento1.getDay().equals(Common.DAYS[t])) {
                        count.add(evento1.getSpentHours().floatValue());
                        day = evento1.getDay();
                        if (evento1.getColorId() == null) {
                            colorIdEvents.add(Common.EVENTSCOLORS[0]);
                        }else {
                            colorIdEvents.add(Common.EVENTSCOLORS[Integer.parseInt(evento1.getColorId())-1]);
                        }
                    }
                }
            }
            if (!Collections.singletonList(count).get(0).isEmpty()) {
                barEntryValues.add(new BarEntry(arrayListToFloatArray(count), Arrays.asList(Common.DAYS).indexOf(day)));
            }
        }

        String[] arrayToconvert = new String[colorIdEvents.size()];
        results.add(barEntryValues);
        results.add(EventAdapter.getColorFromString(colorIdEvents.toArray(arrayToconvert)));

        return results;
    }
}