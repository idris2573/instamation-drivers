package com.instamation.drivers.model;

import javax.persistence.*;

@Entity
@Table(name = "followers", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"username", "account_id"})
})
public class Follower {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "follower_id")
    @Id
    private Long id;

    @OneToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(name = "username")
    private String username;

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
