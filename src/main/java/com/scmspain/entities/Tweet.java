package com.scmspain.entities;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
public class Tweet {
    @Id
    @GeneratedValue
    private Long id;
    @Column(nullable = false)
    private String publisher;
    @Column(nullable = false, length = 500)
    private String tweet;
    @Column (nullable=true)
    private Long pre2015MigrationStatus = 0L;
    @Column(nullable=false)
    private Boolean discarded = false;
    @Column
    @CreationTimestamp
    private Timestamp publishedTimestamp;
    @Column
    private Timestamp discardedTimestamp;

    public Tweet() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getTweet() {
        return tweet;
    }

    public void setTweet(String tweet) {
        this.tweet = tweet;
    }

    public Long getPre2015MigrationStatus() {
        return pre2015MigrationStatus;
    }

    public void setPre2015MigrationStatus(Long pre2015MigrationStatus) {
        this.pre2015MigrationStatus = pre2015MigrationStatus;
    }

    public Boolean getDiscarded() {
        return discarded;
    }

    public void setDiscarded(Boolean discarded) {
        this.discarded = discarded;
    }

    public Timestamp getPublishedTimestamp() {
        return publishedTimestamp;
    }

    public void setPublishedTimestamp(Timestamp publishedTimestamp) {
        this.publishedTimestamp = publishedTimestamp;
    }

    public Timestamp getDiscardedTimestamp() {
        return discardedTimestamp;
    }

    public void setDiscardedTimestamp(Timestamp discardedTimestamp) {
        this.discardedTimestamp = discardedTimestamp;
    }
}
