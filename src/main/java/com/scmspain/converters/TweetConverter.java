package com.scmspain.converters;

import com.scmspain.dtos.TweetDTO;
import com.scmspain.entities.Tweet;

/**
 * Converter between DTO and Entity Tweet
 */
public class TweetConverter {

    private TweetConverter(){
    }

    public static TweetDTO toDTO(final Tweet tweet){
        final TweetDTO tweetDTO = new TweetDTO();

        tweetDTO.setId(tweet.getId());
        tweetDTO.setPublisher(tweet.getPublisher());
        tweetDTO.setTweet(tweet.getTweet());
        tweetDTO.setPre2015MigrationStatus(tweet.getPre2015MigrationStatus());

        return tweetDTO;
    }

    public static Tweet toEntity(final TweetDTO tweetDTO){
        final Tweet tweet = new Tweet();

        tweet.setId(tweetDTO.getId());
        tweet.setPublisher(tweetDTO.getPublisher());
        tweet.setTweet(tweetDTO.getTweet());

        return tweet;
    }
}
