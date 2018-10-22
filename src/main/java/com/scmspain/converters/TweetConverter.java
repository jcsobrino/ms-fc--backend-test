package com.scmspain.converters;

import com.scmspain.dtos.TweetDTO;
import com.scmspain.entities.Tweet;

public class TweetConverter {

    private TweetConverter(){
    }

    public static TweetDTO toDTO(Tweet tweet){
        TweetDTO tweetDTO = new TweetDTO();
        tweetDTO.setId(tweet.getId());
        tweetDTO.setPublisher(tweet.getPublisher());
        tweetDTO.setTweet(tweet.getTweet());

        return tweetDTO;
    }

    public static Tweet toEntity(TweetDTO tweetDTO){
        Tweet tweet = new Tweet();
        tweet.setId(tweetDTO.getId());
        tweet.setPublisher(tweetDTO.getPublisher());
        tweet.setTweet(tweetDTO.getTweet());

        return tweet;
    }
}
