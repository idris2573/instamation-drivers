package com.instamation.drivers.config;

public enum SpeedSettings {

    SLOW(500, 7, 1, 6),
    NORMAL(800, 5, 2, 9),
    FAST(1300, 3, 3, 12);

    private int actionsPerDay;
    private int unfollowAfterNoDays;
    private int likesPerPage;
    private int hoursWorking;

    SpeedSettings(int actionsPerDay, int unfollowAfterNoDays, int likesPerPage, int hoursWorking) {
        this.actionsPerDay = actionsPerDay;
        this.unfollowAfterNoDays = unfollowAfterNoDays;
        this.likesPerPage = likesPerPage;
        this.hoursWorking = hoursWorking;
    }

    public int getActionsPerDay() {
        return actionsPerDay;
    }

    public int getUnfollowAfterNoDays() {
        return unfollowAfterNoDays;
    }

    public int getLikesPerPage() {
        return likesPerPage;
    }

    public int getHoursWorking() {
        return hoursWorking;
    }
}
