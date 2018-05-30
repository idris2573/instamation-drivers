package com.instamation.drivers.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;

@Entity
@Table(name = "proxies")
public class Proxy {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "proxy_id")
    private Long id;

    @Column(name = "ip", unique = true)
    private String ip;

    public Long getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

}
