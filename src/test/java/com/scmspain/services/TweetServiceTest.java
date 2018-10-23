package com.scmspain.services;

import com.scmspain.dtos.TweetDTO;
import com.scmspain.entities.Tweet;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.actuate.metrics.writer.MetricWriter;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class TweetServiceTest {
    public static final long TWEET_ID = 892037429898L;
    private EntityManager entityManager;
    private MetricWriter metricWriter;
    private TweetService tweetService;
    private Query mockedQuery;
    private TypedQuery mockedTypedQuery;

    @Before
    public void setUp() throws Exception {
        this.entityManager = mock(EntityManager.class);
        this.metricWriter = mock(MetricWriter.class);
        this.mockedQuery = mock(Query.class);
        this.mockedTypedQuery = mock(TypedQuery.class);
        this.tweetService = spy(new TweetService(entityManager, metricWriter));
    }

    @Test
    public void shouldInsertANewTweet() throws Exception {
        tweetService.publishTweet(new TweetDTO("Guybrush Threepwood", "I am Guybrush Threepwood, mighty pirate."));

        verify(entityManager).persist(any(Tweet.class));
        verify(tweetService).metricIncrement(TweetService.METRIC_PUBLISHED_TWEETS);
    }

    @Test
    public void shouldReturnATweet() throws Exception {
        tweetService.getTweet(TWEET_ID);

        verify(entityManager).find(Tweet.class, TWEET_ID);
    }

    @Test
    public void shouldDiscardATweet() throws Exception {
        when(entityManager.createQuery(TweetService.UPDATE_TWEET_AS_DISCARDED)).thenReturn(mockedQuery);
        when(mockedQuery.setParameter("id", TWEET_ID)).thenReturn(mockedQuery);

        tweetService.discardTweet(TWEET_ID);

        verify(tweetService).metricIncrement(TweetService.METRIC_DISCARDED_TWEETS);
        verify(entityManager).createQuery(TweetService.UPDATE_TWEET_AS_DISCARDED);
        verify(mockedQuery).setParameter("id", TWEET_ID);
        verify(mockedQuery).executeUpdate();
    }

    @Test
    public void shouldReturnListAllPublishedTweets() throws Exception {
        when(entityManager.createQuery(TweetService.LIST_PUBLISHED_TWEETS, Tweet.class)).thenReturn(mockedTypedQuery);
        when(mockedQuery.getResultList()).thenReturn(Lists.emptyList());

        tweetService.listAllPublishedTweets();
        verify(tweetService).metricIncrement(TweetService.METRIC_TIMES_QUERIED_PUBLISHED_TWEETS);
        verify(entityManager).createQuery(TweetService.LIST_PUBLISHED_TWEETS, Tweet.class);
    }

    @Test
    public void shouldReturnListAllDiscardedTweets() throws Exception {
        when(entityManager.createQuery(TweetService.LIST_DISCARDED_TWEETS, Tweet.class)).thenReturn(mockedTypedQuery);
        when(mockedQuery.getResultList()).thenReturn(Lists.emptyList());

        tweetService.listAllDiscardedTweets();
        verify(tweetService).metricIncrement(TweetService.METRIC_TIMES_QUERIED_DISCARDED_TWEETS);
        verify(entityManager).createQuery(TweetService.LIST_DISCARDED_TWEETS, Tweet.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowAnExceptionWhenTweetIdToDiscardIsNull() throws Exception {
        tweetService.discardTweet(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowAnExceptionWhenTweetLengthIsInvalid() throws Exception {
        tweetService.publishTweet(new TweetDTO("Pirate", "LeChuck? He's the guy that went to the Governor's for dinner and never wanted to leave. He fell for her in a big way, but she told him to drop dead. So he did. Then things really got ugly."));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowAnExceptionWhenPublishedLengthIsInvalid() throws Exception {
        tweetService.publishTweet(new TweetDTO("", "I am Guybrush Threepwood, mighty pirate."));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowAnExceptionWhenTweetLengthIsGreaterThan500Characters() throws Exception {
        StringBuffer validLink = new StringBuffer("http://");
        while(validLink.length() < TweetService.TWEET_MAX_LENGTH_WITH_LINKS){
            validLink.append("x");
        }
        validLink.append(" ");
        tweetService.publishTweet(new TweetDTO("Guybrush Threepwood", validLink.toString()));
    }
}
