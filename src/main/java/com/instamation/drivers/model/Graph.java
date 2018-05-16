package com.instamation.drivers.model;

import java.sql.Date;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Graph {

    // preloaded maps
    private Map<Date,Integer> sevenDays = newBackDate(7);
    private Map<Date,Integer> twentyEightDays = newBackDate(28);
    private Map<Date,Integer> ninetyDays = newBackDate(90);

    private List<Stats> stats;

    public Graph(List<Stats> stats) {
        this.stats = stats;

        countStats(sevenDays);
        countStats(twentyEightDays);
        countStats(ninetyDays);


        System.out.println();
    }

    // dateBack
    // 0 = today
    // 1 = yesterday
    // 7 = last week
    private Date getDateBack(int dateBack) {
        // Create a calendar object with today date. Calendar is in java.util pakage.
        Calendar calendar = Calendar.getInstance();

        // Move calendar to yesterday
        calendar.add(Calendar.DATE, -dateBack);

        // Get current date of calendar which point to the yesterday now
        java.util.Date yesterday = calendar.getTime();

        return new Date(yesterday.getTime());
    }

    private Map<Date, Integer> newBackDate(int backDate){
        Map<Date, Integer> days = new TreeMap<>();
        for(int i = 0; i < backDate; i++){
            days.put(getDateBack(i), 0);
        }
        return days;
    }

    private void countStats(Map<Date,Integer> statsDays){
        int dateBack = 0;

        for(int i = 0; i < statsDays.size(); i++) {
            for (Stats stats : this.stats) {

                Date statsDate = new Date(stats.getDate().getTime());

                // if stats date is in between
                if (statsDate.before(getDateBack(dateBack)) && stats.getDate().after(getDateBack(dateBack + 1))) {
                    for (Map.Entry entry : statsDays.entrySet()) {
                        Date dayDate = (Date) entry.getKey();
                        if (dayDate.before(getDateBack(dateBack)) && dayDate.after(getDateBack(dateBack + 1))) {
                            statsDays.put(dayDate, stats.getFollowers());
                        }
                    }

                }
            }
            dateBack++;
        }

        Date previousDate = null;
        for(Map.Entry entry : statsDays.entrySet()){
            if((Integer)entry.getValue() == 0 && previousDate != null){
                statsDays.put((Date)entry.getKey(), statsDays.get(previousDate));

            }
            previousDate = (Date)entry.getKey();
        }
    }



    ///////////////////////GETTERS/////////////////////
    public Map<Date, Integer> getSevenDays() {
        return sevenDays;
    }

    public Map<Date, Integer> getTwentyEightDays() {
        return twentyEightDays;
    }

    public Map<Date, Integer> getNinetyDays() {
        return ninetyDays;
    }

}
