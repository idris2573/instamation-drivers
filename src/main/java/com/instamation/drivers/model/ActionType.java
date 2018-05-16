package com.instamation.drivers.model;

import javax.persistence.*;

@Entity
@Table(name = "action_types")
public class ActionType {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "action_type_id")
    private Long id;

    @Column(name = "type")
    private String type;

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
