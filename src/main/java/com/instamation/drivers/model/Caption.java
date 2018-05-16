package com.instamation.drivers.model;

import javax.persistence.*;
import java.sql.Date;

@Entity
@Table(name = "captions")

public class Caption {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "caption_id")
    @Id
    private Long id;


    @Column(name = "caption")
    private String caption;

    @Column(name = "category")
    private String category;

    public String getCaption() {
        return caption;
    }

    public String getCategory() {
        return category;
    }
}
