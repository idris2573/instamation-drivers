package com.instamation.drivers.model;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "actions")
public class Action {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "action_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne
    @JoinColumn(name = "action_type_id")
    private ActionType actionType;

    @Column(name = "date", nullable = false, updatable = false)
    @CreationTimestamp
    private Timestamp date;

    @Column(name = "description")
    private String description;

    public Long getId() {
        return id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public Timestamp getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
