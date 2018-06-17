package com.instamation.drivers.config;

public enum AccountType {

    TRIAL(3),
    WEEKLY(7),
    MONTHLY(30),
    EXIPIRED(0);

    private int days;

    AccountType(int days) {
        this.days = days;
    }

    public int getDays() {
        return days;
    }
}
