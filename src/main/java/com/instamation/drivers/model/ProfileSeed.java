package com.instamation.drivers.model;

import javax.persistence.*;

@Entity
@Table(name = "profile_seeds")
public class ProfileSeed {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "profile_seed_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(name = "type")
    private String type;

    @Column(name = "name")
    private String name;

    @Column(name = "used")
    private Boolean used = false;

    public Long getId() {
        return id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getUsed() {
        return used;
    }

    public void setUsed(Boolean used) {
        this.used = used;
    }
}
