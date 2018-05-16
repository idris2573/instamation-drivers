package com.instamation.drivers.model;

import com.instamation.drivers.config.SpeedSettings;
import com.instamation.drivers.repository.PostRepository;

import javax.persistence.*;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Entity
@Table(name = "settings")
public class Setting {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settings_id")
    @Id
    private Long id;

    @OneToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(name = "actions_per_day")
    private Integer actionsPerDay = 0;

    @Column(name = "unfollow_after_no_days")
    private Integer unfollowAfterNoDays = 0;

    @Column(name = "likes_per_page")
    private Integer likesPerPage = 0;

    @Column(name = "posts_per_day")
    private Integer postsPerDay = 0;

    @Column(name = "follow")
    private boolean follow = false;

    @Column(name = "unfollow")
    private boolean unfollow = false;

    @Column(name = "likes")
    private boolean likes = false;

    @Column(name = "autopost")
    private boolean autopost = false;

    @Column(name = "comment")
    private boolean comment = false;

    @Column(name = "startTime")
    private String startTime;

    @Column(name = "endTime")
    private String endTime;

    @Column(name = "post_actions")
    private Integer postActions = 0;

    @Column(name = "hashtag_category")
    private String hashtagCategory;

    @Column(name = "hashtag_amount")
    private Integer hashtagAmount = 0;

    @Column(name = "action_speed")
    private String actionSpeed = "Normal";

    @Column(name = "media_type")
    private String mediaType = "All";

    @Column(name = "min_likes_filter")
    private Integer minLikesFilter;

    @Column(name = "max_likes_filter")
    private Integer maxLikesFilter;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Integer getActionsPerDay() {
        return actionsPerDay;
    }

    public void setActionsPerDay(Integer actionsPerDay) {
        this.actionsPerDay = actionsPerDay;
    }

    public Integer getUnfollowAfterNoDays() {
        return unfollowAfterNoDays;
    }

    public void setUnfollowAfterNoDays(Integer unfollowAfterNoDays) {
        this.unfollowAfterNoDays = unfollowAfterNoDays;
    }

    public boolean isFollow() {
        return follow;
    }

    public void setFollow(boolean follow) {
        this.follow = follow;
    }

    public boolean isUnfollow() {
        return unfollow;
    }

    public void setUnfollow(boolean unfollow) {
        this.unfollow = unfollow;
    }

    public boolean isLikes() {
        return likes;
    }

    public void setLikes(boolean likes) {
        this.likes = likes;
    }

    public boolean isComment() {
        return comment;
    }

    public void setComment(boolean comment) {
        this.comment = comment;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Integer getLikesPerPage() {
        return likesPerPage;
    }

    public void setLikesPerPage(Integer likesPerPage) {
        this.likesPerPage = likesPerPage;
    }

    public Integer getPostActions() {
        return postActions;
    }

    public void setPostActions(Integer postActions) {
        this.postActions = postActions;
    }

    public String getHashtagCategory() {
        return hashtagCategory;
    }

    public void setHashtagCategory(String hashtagCategory) {
        this.hashtagCategory = hashtagCategory;
    }

    public Integer getHashtagAmount() {
        return hashtagAmount;
    }

    public void setHashtagAmount(Integer hashtagAmount) {
        this.hashtagAmount = hashtagAmount;
    }

    public Integer actionSleepTime() throws Exception{
        DateFormat format = new SimpleDateFormat("HH:mm");

        Time startTime = new Time(format.parse(getStartTime()).getTime());
        Time endTime = new Time(format.parse(getEndTime()).getTime());
        if(startTime.after(endTime)){
            // Add 1 day to end time if before start time
            endTime = new Time(endTime.getTime() + 86400000L);
        }

        long workingTime = endTime.getTime() - startTime.getTime();

        workingTime = workingTime / 1000; // seconds

        long actionTime = workingTime / getActionsPerDay(); // seconds
        int actionTimeInt = Math.toIntExact(actionTime);

        int buffer = new Random().nextInt(actionTimeInt) + 1;

        return (actionTimeInt + buffer) * 1000;
    }

    public Time workingPeriod() throws Exception{
        DateFormat format = new SimpleDateFormat("HH:mm");
        Time startTime = new Time(format.parse(getStartTime()).getTime());
        Time endTime = new Time(format.parse(getEndTime()).getTime());

        if(startTime.after(endTime)){
            // Add 1 day to end time if before start time
            endTime = new Time(endTime.getTime() + 86400000L);
        }
        long workingtime = endTime.getTime() - startTime.getTime();

        return new Time(workingtime);
    }

    public boolean isWorkingTime() throws Exception{
        DateFormat format = new SimpleDateFormat("HH:mm");

        Time startTime = new Time(format.parse(getStartTime()).getTime());
        Time endTime = new Time(format.parse(getEndTime()).getTime());

        if(startTime.after(endTime)){
            // Add 1 day to end time if before start time
            endTime = new Time(endTime.getTime() + 86400000L);
        }

        Time currentTime = currentTimeIgnoreDate();

        if(currentTime.after(startTime) && currentTime.before(endTime)){
            return true;
        }

        return false;
    }

    public boolean isAutopost() {
        return autopost;
    }

    public void setAutopost(boolean autopost) {
        this.autopost = autopost;
    }

    public Integer getPostsPerDay() {
        return postsPerDay;
    }

    public void setPostsPerDay(Integer postsPerDay) {
        this.postsPerDay = postsPerDay;
    }

    public boolean isPostTime(PostRepository postRepository) throws Exception{

        if(postsPerDay == 0){
            return false;
        }

        DateFormat format = new SimpleDateFormat("HH:mm");

        long postActionTime = workingPeriod().getTime() / postsPerDay;
        long minute = 60000L;

        Time startTime = new Time(format.parse(getStartTime()).getTime());

        List<Time> postStartPeriods = new ArrayList<>();
        List<Time> postEndPeriods = new ArrayList<>();

        for(int i = 1; i <= postsPerDay; i++){
            postStartPeriods.add(new Time(startTime.getTime() + (postActionTime*i)));
            postEndPeriods.add(new Time(startTime.getTime() + (postActionTime*i) + (minute*10)));
        }

        Time currentTime = currentTimeIgnoreDate();

        for(int i = 0; i < postsPerDay; i++){
            if(currentTime.after(postStartPeriods.get(i)) && currentTime.before(postEndPeriods.get(i))){

                // check if there was a post with 10 minutes ago
                if(postRepository.findByAccountAndPostDateAfter(account, new Time(System.currentTimeMillis() - (minute*10))).isEmpty()){
                    return true;
                }
            }
        }

        return false;
    }

    public Map<Time, Time> getPostTimes() throws Exception{

        if(postsPerDay == 0){
            return null;
        }

        DateFormat format = new SimpleDateFormat("HH:mm");

        long postActionTime = workingPeriod().getTime() / postsPerDay;
        long minute = 60000L;

        Time startTime = new Time(format.parse(getStartTime()).getTime());

        Map<Time, Time> postPeriods = new TreeMap<>();

        for(int i = 1; i <= postsPerDay; i++){
            postPeriods.put(
                    new Time(startTime.getTime() + (postActionTime*i)),
                    new Time(startTime.getTime() + (postActionTime*i) + (minute*10)));
        }

        return postPeriods;
    }

    private Time currentTimeIgnoreDate() throws Exception{
        Time currentTime = new Time(System.currentTimeMillis());
        String cTime = currentTime.toString();
        DateFormat format = new SimpleDateFormat("HH:mm:ss");
        return new Time(format.parse(cTime).getTime());
    }

    public String getActionSpeed() {
        return actionSpeed;
    }

    public void setActionSpeed(String actionSpeed) {
        this.actionSpeed = actionSpeed;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public Integer getMinLikesFilter() {
        return minLikesFilter;
    }

    public void setMinLikesFilter(Integer minLikesFilter) {
        this.minLikesFilter = minLikesFilter;
    }

    public Integer getMaxLikesFilter() {
        return maxLikesFilter;
    }

    public void setMaxLikesFilter(Integer maxLikesFilter) {
        this.maxLikesFilter = maxLikesFilter;
    }

    public void updateSettingsSpeed(){
        // Update settings speed
        SpeedSettings speedSettings;
        if(actionSpeed.equals(SpeedSettings.SLOW.toString().toLowerCase())){
            speedSettings = SpeedSettings.SLOW;
        } else if(actionSpeed.equals(SpeedSettings.NORMAL.toString().toLowerCase())){
            speedSettings = SpeedSettings.NORMAL;
        } else {
            speedSettings = SpeedSettings.FAST;
        }
        setActionsPerDay(speedSettings.getActionsPerDay());
        setUnfollowAfterNoDays(speedSettings.getUnfollowAfterNoDays());
        setLikesPerPage(speedSettings.getLikesPerPage());

        long hour = 3600000L;
        Time currentTime = new Time(System.currentTimeMillis());
        String currentTimeString = currentTime.toString().substring(0,5);
        Time endTime = new Time(System.currentTimeMillis() + (hour * speedSettings.getHoursWorking()));
        String endTimeString = endTime.toString().substring(0,5);

        setStartTime(currentTimeString);
        setEndTime(endTimeString);
    }
}
