package com.example.timetrackerapp.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.view.*;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.timetrackerapp.R;
import com.example.timetrackerapp.common.Common;
import com.example.timetrackerapp.model.*;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import java.math.BigDecimal;
import java.util.*;

public class RecyclerViewEventsAdapter extends RecyclerView.Adapter<RecyclerViewEventsAdapter.ViewHolder> {
    private HashMap<String, EventsWeek> data;
    private Activity activity;

    public RecyclerViewEventsAdapter(ArrayList data, Activity activity){
        this.data = (HashMap<String, EventsWeek>) data.get(2);
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Object[] keys = data.keySet().toArray();
        ArrayList<BarEntry> entries = new ArrayList<>();
        final EventsWeek eventsWeek = (EventsWeek) data.get(keys[position]);
        final ArrayList<Evento> eventos = eventsWeek.getEvents();
        BigDecimal totalHours = new BigDecimal(0);
        BarDataSet bardataset = new BarDataSet(entries, "");
        BarData data = new BarData(Common.LABELS, bardataset);
        int eventColor = 0;

        for (Evento evento: eventos) {
            totalHours = totalHours.add(evento.getSpentHours());
            entries.add(new BarEntry(evento.getSpentHours().floatValue(), Arrays.asList(Common.DAYS).indexOf(evento.getDay())));
            eventColor = Color.parseColor(Common.EVENTSCOLORS[Integer.parseInt(evento.getColorId())-1]);
        }

        holder.eventTitle.setText(eventsWeek.getName());
        holder.eventTitle.setTextColor(Color.parseColor("#FFFFFF"));
        holder.eventTitle.setBackgroundColor(Color.parseColor(Common.EVENTSCOLORS[Integer.parseInt(eventsWeek.getEvents().get(0).getColorId())-1]));
        holder.eventSpentHours.setText("Planned:\n " + totalHours + "h");
        holder.eventBarChart.setData(data); // set the data and list of labels into chart
        holder.eventBarChart.getXAxis().setTextColor(Color.parseColor("#FFFFFF"));
        bardataset.setColors(Collections.singletonList(eventColor));
        bardataset.setBarSpacePercent(50f);

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                ViewGroup viewGroup = v.findViewById(android.R.id.content);
                View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.custom_dialog, viewGroup, false);

                Button dialogCancelBtn = dialogView.findViewById(R.id.dialog_cancel_button);
                TextView dialogTitle = dialogView.findViewById(R.id.dialog_eventWeek_title);
                TextView dialogEventsContent = dialogView.findViewById(R.id.dialog_eventweek_content);

                dialogTitle.setText(eventsWeek.getName());
                dialogTitle.setBackgroundColor(Color.parseColor(Common.EVENTSCOLORS[Integer.parseInt(eventsWeek.getEvents().get(0).getColorId())-1]));

                for (Evento evento:eventos) {
                    dialogEventsContent.append(evento.getDay() + ": You will spend " + evento.getSpentHours() + "h.\n\n");
                }
                builder.setView(dialogView);
                final AlertDialog alertDialog = builder.create();

                dialogCancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return  data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView eventTitle, eventSpentHours;
        private BarChart eventBarChart;
        private XAxis xAxis;
        private LinearLayout cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventTitle = itemView.findViewById(R.id.event_name);
            eventSpentHours = itemView.findViewById(R.id.event_hours);
            eventBarChart = itemView.findViewById(R.id.event_barChart);
            cardView = itemView.findViewById(R.id.event_item);

            eventBarChart.getAxisLeft().setDrawLabels(false);
            eventBarChart.getAxisRight().setDrawLabels(false);
            eventBarChart.getXAxis().setDrawLabels(true);
            eventBarChart.getLegend().setEnabled(false);
            eventBarChart.getAxisLeft().setDrawGridLines(false);
            eventBarChart.getAxisRight().setDrawGridLines(false);
            eventBarChart.getContentDescription();
            xAxis = eventBarChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            eventBarChart.getAxisLeft().setAxisMinValue(0);
            eventBarChart.getAxisRight().setAxisMinValue(0);
            eventBarChart.setDescription("");
            eventBarChart.animateY(5000);
        }
    }
}
