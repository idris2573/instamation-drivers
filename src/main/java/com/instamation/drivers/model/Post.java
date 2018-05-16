package com.instamation.drivers.model;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "posts", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"url", "account_id"})
})
public class Post {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    @Id
    private Long id;

    @OneToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(name = "url")
    private String url;

    @Column(name = "posted")
    private Boolean posted = false;

    @Column(name = "post_date")
    private Timestamp postDate;

    public Post() {
    }

    public Post(Account account, String url) {
        this.account = account;
        this.url = url;
    }

    public Long getId() {
        return id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getPosted() {
        return posted;
    }

    public void setPosted(Boolean posted) {
        this.posted = posted;
    }

    public Timestamp getPostDate() {
        return postDate;
    }

    public void setPostDate(Timestamp postDate) {
        this.postDate = postDate;
    }
}
