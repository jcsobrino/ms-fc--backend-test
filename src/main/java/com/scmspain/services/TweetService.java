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

import static org.springframework.util.StringUtils.isEmpty;

@Service
@Transactional
public class TweetService {
    public static final String LIST_PUBLISHED_TWEETS = "SELECT t FROM Tweet AS t WHERE discarded = false AND pre2015MigrationStatus <> 99 ORDER BY publishedTimestamp DESC";
    public static final String LIST_DISCARDED_TWEETS = "SELECT t FROM Tweet AS t WHERE discarded = true AND pre2015MigrationStatus <> 99 ORDER BY discardedTimestamp DESC";
    public static final String UPDATE_TWEET_AS_DISCARDED = "UPDATE Tweet SET discarded = true, discardedTimestamp = CURRENT_TIMESTAMP() WHERE id = :id AND discarded = false";
    public static final String LINK_PATTERN = "\\bhttps?://\\S+ ";
    public static final int TWEET_MAX_LENGTH = 140;
    public static final int TWEET_MAX_LENGTH_WITH_LINKS = 500;
    public static final String METRIC_DISCARDED_TWEETS = "discarded-tweets";
    public static final String METRIC_PUBLISHED_TWEETS = "published-tweets";
    public static final String METRIC_TIMES_QUERIED_PUBLISHED_TWEETS = "times-queried-published-tweets";
    public static final String METRIC_TIMES_QUERIED_DISCARDED_TWEETS = "times-queried-discarded-tweets";

    private EntityManager entityManager;
    private MetricWriter metricWriter;

    public TweetService(EntityManager entityManager, MetricWriter metricWriter) {
        this.entityManager = entityManager;
        this.metricWriter = metricWriter;
    }

    /**
     * Push tweet to repository
     * @param tweetDTO published and text of the new tweet
     * @throws IllegalArgumentException if published is null or empty
     * @throws IllegalArgumentException if tweet is null, empty, length is greater than 140 characters without links or greater than 500 with links. A link is a string with the pattern '\bhttps?://\S+ '
     */
    public void publishTweet(final TweetDTO tweetDTO) {

        if (isEmpty(tweetDTO.getPublisher())){
            throw new IllegalArgumentException("Published must not be null or empty");
        }

        if(isEmpty(tweetDTO.getTweet()) || tweetDTO.getTweet().replaceAll(LINK_PATTERN,"").length() > TWEET_MAX_LENGTH) {
            throw new IllegalArgumentException(String.format("Tweet must not be null, empty or greater than %d characters without URLs", TWEET_MAX_LENGTH));
        }

        if(tweetDTO.getTweet().length() > TWEET_MAX_LENGTH_WITH_LINKS) {
            throw new IllegalArgumentException(String.format("Tweet must not be greater than %d characters with URLs", TWEET_MAX_LENGTH_WITH_LINKS));
        }

        metricIncrement(METRIC_PUBLISHED_TWEETS);
        this.entityManager.persist(TweetConverter.toEntity(tweetDTO));
    }

    /**
     * Set a published tweet as discarded
     * @param tweetId the tweet id to discard
     * @return true if a tweet was discarded. Otherwise, false
     * @throws IllegalArgumentException if tweetId is null
     */
    public Boolean discardTweet(final Long tweetId) {

        metricIncrement(METRIC_DISCARDED_TWEETS);

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

    /**
     * List all published tweets but not the discarded ones and ordered by published timestamp
     * @return list of published tweets
     */
    public List<TweetDTO> listAllPublishedTweets() {

        metricIncrement(METRIC_TIMES_QUERIED_PUBLISHED_TWEETS);

        return this.entityManager.createQuery(LIST_PUBLISHED_TWEETS, Tweet.class)
                .getResultList()
                .stream()
                .map(t -> TweetConverter.toDTO(t))
                .collect(Collectors.toList());
    }

    /**
     * List all discarded tweets ordered by discarded timestamp
     * @return list of discarded tweets
     */
    public List<TweetDTO> listAllDiscardedTweets() {

        metricIncrement(METRIC_TIMES_QUERIED_DISCARDED_TWEETS);

        return this.entityManager.createQuery(LIST_DISCARDED_TWEETS, Tweet.class)
                .getResultList()
                .stream()
                .map(t -> TweetConverter.toDTO(t))
                .collect(Collectors.toList());
    }

    protected void metricIncrement(final String name){
        this.metricWriter.increment(new Delta<Number>(name, 1));
    }

}
