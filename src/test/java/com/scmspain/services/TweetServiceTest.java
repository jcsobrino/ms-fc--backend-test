package com.scmspain.services;

import com.scmspain.dtos.TweetDTO;
import com.scmspain.entities.Tweet;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.actuate.metrics.writer.MetricWriter;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class TweetServiceTest {
    private EntityManager entityManager;
    private MetricWriter metricWriter;
    private TweetService tweetService;

    @Before
    public void setUp() throws Exception {
        this.entityManager = mock(EntityManager.class);
        this.metricWriter = mock(MetricWriter.class);

        this.tweetService = new TweetService(entityManager, metricWriter);
    }

    @Test
    public void shouldInsertANewTweet() throws Exception {
        tweetService.publishTweet(new TweetDTO("Guybrush Threepwood", "I am Guybrush Threepwood, mighty pirate."));

        verify(entityManager).persist(any(Tweet.class));
    }

    @Test
    public void shouldDiscardATweet() throws Exception {
        Query mockedQuery = mock(Query.class);
        when(entityManager.createQuery(TweetService.UPDATE_TWEET_AS_DISCARDED)).thenReturn(mockedQuery);
        when(mockedQuery.setParameter("id", 1L)).thenReturn(mockedQuery);
        tweetService.discardTweet(1L);

        verify(entityManager).createQuery(TweetService.UPDATE_TWEET_AS_DISCARDED);
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
        while(validLink.length() < 500){
            validLink.append("x");
        }
        validLink.append(" ");
        tweetService.publishTweet(new TweetDTO("Guybrush Threepwood", validLink.toString()));
    }
}
