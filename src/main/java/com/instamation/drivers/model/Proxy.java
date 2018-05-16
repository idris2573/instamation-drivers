package com.instamation.drivers.model;

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

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
