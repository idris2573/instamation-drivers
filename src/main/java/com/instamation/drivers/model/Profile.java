package com.instamation.drivers.model;

import javax.persistence.*;
import java.sql.Date;
import java.util.Random;

@Entity
@Table(name = "profiles")
public class Profile {


    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    @Id
    private Long id;

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "parent_profile")
    private String parentProfile;

    @Column(name = "following")
    private boolean following = false;

    @Column(name = "unfollowed")
    private boolean unfollowed = false;

    @Column(name = "liked")
    private boolean liked = false;

    @Column(name = "commented")
    private boolean commented = false;

    @Column(name = "follow_date")
    private Date followDate;

    @ManyToOne
    @JoinColumn(name="account_id")
    private Account account;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getParentProfile() {
        return parentProfile;
    }

    public void setParentProfile(String parentProfile) {
        this.parentProfile = parentProfile;
    }

    public boolean isFollowing() {
        return following;
    }

    public void setFollowing(boolean following) {
        this.following = following;
    }

    public Date getFollowDate() {
        return followDate;
    }

    public void setFollowDate(Date followDate) {
        this.followDate = followDate;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public boolean isUnfollowed() {
        return unfollowed;
    }

    public void setUnfollowed(boolean unfollowed) {
        this.unfollowed = unfollowed;
    }

    public boolean isUnfollowTime(Setting setting){

        int day = 86400000;
        int buffer = new Random().nextInt(day/2) + 1;
        day = day + buffer;

        Date currentDate = new Date(System.currentTimeMillis() - (setting.getUnfollowAfterNoDays() * day));

        if (currentDate.after(getFollowDate())) {
            return true;
        }
        return false;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public boolean isCommented() {
        return commented;
    }

    public void setCommented(boolean commented) {
        this.commented = commented;
    }
}
