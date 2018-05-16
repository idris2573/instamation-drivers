package com.instamation.drivers.model;

import com.instamation.drivers.repository.StatsRepository;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "accounts")
public class Account {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "email")
    private String email;

    @Column(name = "post_count")
    private Integer postCount;

    @Column(name = "followers")
    private Integer followers;

    @Column(name = "following")
    private Integer following;

    @Column(name = "bio")
    private String bio;

    @Column(name = "website")
    private String website;

    @Column(name = "private_account")
    private Boolean privateAccount;

    @Column(name = "profile_pic")
    private String profilePic;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "running")
    private Boolean running = false;

    @Column(name = "actions")
    private Integer actions = 0;

    @Column(name = "followers_gained")
    private Integer followersGained = 0;

    @Column(name = "enabled")
    private boolean enabled = true;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "settings_id")
    private Setting setting;

    @OneToOne
    @JoinColumn(name = "proxy_id")
    private Proxy proxy;

    @Column(name = "register_date", nullable = false, updatable = false)
    @CreationTimestamp
    private Timestamp registerDate;

    @Column(name = "expiry_date")
    private Date expiryDate;

    @Column(name = "pending_upgrade")
    private boolean pendingUpgrade = false;

    @Column(name = "automation_lock")
    private boolean automationLock = false;

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getPostCount() {
        return postCount;
    }

    public void setPostCount(Integer postCount) {
        this.postCount = postCount;
    }

    public Integer getFollowers() {
        return followers;
    }

    public void setFollowers(Integer followers) {
        this.followers = followers;
    }

    public Integer getFollowing() {
        return following;
    }

    public void setFollowing(Integer following) {
        this.following = following;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public Boolean getPrivateAccount() {
        return privateAccount;
    }

    public void setPrivateAccount(Boolean privateAccount) {
        this.privateAccount = privateAccount;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean isRunning() {
        return running;
    }

    public void setRunning(Boolean running) {
        this.running = running;
    }

    public Integer getActions() {
        return actions;
    }

    public void setActions(Integer actions) {
        this.actions = actions;
    }

    public Setting getSetting() {
        return setting;
    }

    public void setSetting(Setting setting) {
        this.setting = setting;
    }

    public Integer getFollowersGained() {
        return followersGained;
    }

    public void setFollowersGained(Integer followersGained) {
        this.followersGained = followersGained;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public boolean equals(Object obj) {

        Account account = (Account) obj;

        if(this.getId().equals(account.getId())){
            return true;
        }
        return false;
    }

    public Timestamp getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(Timestamp registerDate) {
        this.registerDate = registerDate;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isPendingUpgrade() {
        return pendingUpgrade;
    }

    public void setPendingUpgrade(boolean pendingUpgrade) {
        this.pendingUpgrade = pendingUpgrade;
    }

    public void updateStats(StatsRepository statsRepository){
        Stats stats = new Stats();
        stats.setAccount(this);
        stats.setFollowers(getFollowers());
        stats.setActions(getActions());
        stats.setFollowing(getFollowing());
        stats.setPostCount(getPostCount());
        statsRepository.save(stats);

        List<Stats> followersGainedList = statsRepository.findByAccount(this);
        if (!followersGainedList.isEmpty()) {
            int followersGained = followersGainedList.get(followersGainedList.size() - 1).getFollowers() - followersGainedList.get(0).getFollowers();
            this.setFollowersGained(followersGained);
        }
    }

    public Boolean getRunning() {
        return running;
    }

    public boolean isAutomationLock() {
        return automationLock;
    }

    public void setAutomationLock(boolean automationLock) {
        this.automationLock = automationLock;
    }

    @Override
    public String toString() {
        return "@" + username;
    }
}
