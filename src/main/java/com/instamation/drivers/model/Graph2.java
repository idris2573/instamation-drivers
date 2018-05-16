package com.instamation.drivers.model;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Graph2 {


    private Map<LocalDate, Integer> map7 = new TreeMap<>();
    private Map<LocalDate, Integer> map28 = new TreeMap<>();
    private Map<LocalDate, Integer> map90 = new TreeMap<>();

    public Graph2(List<Stats> stats7, List<Stats> stats28, List<Stats> stats90) {

        for(Stats stat : stats7){
            map7.put(new Date(stat.getDate().getTime()).toLocalDate(), stat.getFollowers());
        }

        for(Stats stat : stats28){
            map28.put(new Date(stat.getDate().getTime()).toLocalDate(), stat.getFollowers());
        }

        for(Stats stat : stats90){
            map90.put(new Date(stat.getDate().getTime()).toLocalDate(), stat.getFollowers());
        }
    }

    public Map<LocalDate, Integer> getMap7() {
        return map7;
    }

    public Map<LocalDate, Integer> getMap28() {
        return map28;
    }

    public Map<LocalDate, Integer> getMap90() {
        return map90;
    }
}
