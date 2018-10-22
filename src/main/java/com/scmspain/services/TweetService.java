package com.scmspain.services;

import com.scmspain.converters.TweetConverter;
import com.scmspain.dtos.TweetDTO;
import com.scmspain.entities.Tweet;
import org.springframework.boot.actuate.metrics.writer.Delta;
import org.springframework.boot.actuate.metrics.writer.MetricWriter;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TweetService {
    public static final String LIST_PUBLISHED_TWEETS = "SELECT t FROM Tweet AS t WHERE discarded = false ORDER BY publishedTimestamp DESC";
    public static final String LIST_DISCARDED_TWEETS = "SELECT t FROM Tweet AS t WHERE discarded = true ORDER BY discardedTimestamp DESC";
    public static final String UPDATE_TWEET_AS_DISCARDED = "UPDATE Tweet SET discarded = true, discardedTimestamp = CURRENT_TIMESTAMP() WHERE id = :id AND discarded = false";
    public static final String LINK_PATTERN = "\\bhttps?://\\S+ ";

    private EntityManager entityManager;
    private MetricWriter metricWriter;

    public TweetService(EntityManager entityManager, MetricWriter metricWriter) {
        this.entityManager = entityManager;
        this.metricWriter = metricWriter;
    }

    /**
      Push tweet to repository
      Parameter - publisher - creator of the Tweet
      Parameter - text - Content of the Tweet
      Result - recovered Tweet
    */
    public void publishTweet(TweetDTO tweetDTO) {
        if (tweetDTO.getPublisher() == null || tweetDTO.getPublisher().isEmpty()){
            throw new IllegalArgumentException("Published must not be null or empty");
        }

        if(tweetDTO.getTweet() == null || tweetDTO.getTweet().isEmpty() || tweetDTO.getTweet().replaceAll(LINK_PATTERN,"").length() > 140) {
            throw new IllegalArgumentException("Tweet must not be null, empty or greater than 140 characters without URLs");
        }

        if(tweetDTO.getTweet().length() > 500) {
            throw new IllegalArgumentException("Tweet must not be greater than 500 characters with URLs");
        }

        this.metricWriter.increment(new Delta<Number>("published-tweets", 1));
        this.entityManager.persist(TweetConverter.toEntity(tweetDTO));
    }

    public Boolean discardTweet(Long tweetId) {
        this.metricWriter.increment(new Delta<Number>("discarded-tweets", 1));
        if(tweetId != null) {
            return this.entityManager.createQuery(UPDATE_TWEET_AS_DISCARDED)
                    .setParameter("id", tweetId)
                    .executeUpdate() != 0;
        } else {
            throw new IllegalArgumentException("Tweet ID must not be null");
        }
    }

    /**
      Recover tweet from repository
      Parameter - id - id of the Tweet to retrieve
      Result - retrieved Tweet
    */
    public Tweet getTweet(Long id) {
      return this.entityManager.find(Tweet.class, id);
    }

    public int deleteAllTweets() {
        return this.entityManager.createQuery("DELETE FROM Tweet").executeUpdate();
    }

    /**
      Recover tweet from repository
      Parameter - id - id of the Tweet to retrieve
      Result - retrieved Tweet
    */
    public List<TweetDTO> listAllPublishedTweets() {
        this.metricWriter.increment(new Delta<Number>("times-queried-published-tweets", 1));
        return this.entityManager.createQuery(LIST_PUBLISHED_TWEETS, Tweet.class)
                .getResultList()
                .stream()
                .map(t -> TweetConverter.toDTO(t))
                .collect(Collectors.toList());
    }

    public List<TweetDTO> listAllDiscardedTweets() {
        this.metricWriter.increment(new Delta<Number>("times-queried-published-tweets", 1));
        return this.entityManager.createQuery(LIST_DISCARDED_TWEETS, Tweet.class)
                .getResultList()
                .stream()
                .map(t -> TweetConverter.toDTO(t))
                .collect(Collectors.toList());
    }

}
