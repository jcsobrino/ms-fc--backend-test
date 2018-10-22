package com.scmspain.dtos;

import java.sql.Timestamp;

public class TweetDTO {
    private Long id;
    private String publisher;
    private String tweet;

    public TweetDTO(){

    }

    public TweetDTO(String publisher, String tweet) {
        this.publisher = publisher;
        this.tweet = tweet;
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
}
