package com.instamation.drivers.model;

public class Alert {

    private String message;

    private Status status;

    public enum Status {
        PRIMARY("alert-primary"),
        SECONDARY("alert-secondary"),
        SUCCESS("alert-success"),
        DANGER("alert-danger"),
        WARNING("alert-warning"),
        INFO("alert-info"),
        LIGHT("alert-light"),
        DARK("alert-dark");

        private String type;

        Status(String type){
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

    public Alert(Status status, String message) {
        this.message = message;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
