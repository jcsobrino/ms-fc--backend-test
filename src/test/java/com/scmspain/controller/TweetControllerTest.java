package com.scmspain.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scmspain.configuration.TestConfiguration;
import com.scmspain.dtos.TweetDTO;
import com.scmspain.entities.Tweet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestConfiguration.class)
public class TweetControllerTest {
    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        this.mockMvc = webAppContextSetup(this.context).build();
        mockMvc.perform(this.deleteAllTweets());
    }

    @Test
    public void shouldReturn200WhenInsertingAValidTweet() throws Exception {
        mockMvc.perform(newTweet("Prospect", "Breaking the law"))
            .andExpect(status().is(201));
    }

    @Test
    public void shouldReturn400WhenInsertingAnInvalidTweet() throws Exception {
        mockMvc.perform(newTweet("Schibsted Spain", "We are Schibsted Spain (look at our home pagehttp://www.schibsted.es/), we own Vibbo, InfoJobs, fotocasa, coches.net and milanuncios. Welcome!"))
                .andExpect(status().is(400));
    }

    @Test
    public void shouldReturn200WhenInsertingALongTweetWithAValidLink() throws Exception {
        mockMvc.perform(newTweet("Schibsted Spain", "We are Schibsted Spain (look at our home page http://www.schibsted.es/ ), we own Vibbo, InfoJobs, fotocasa, coches.net and milanuncios. Welcome!"))
                .andExpect(status().is(201));
    }

    @Test
    public void shouldReturnAllPublishedTweets() throws Exception {
        mockMvc.perform(newTweet("Yo", "How are you?"))
                .andExpect(status().is(201));

        MvcResult getResult = mockMvc.perform(get("/tweet"))
                .andExpect(status().is(200))
                .andReturn();

        String content = getResult.getResponse().getContentAsString();
        assertThat(new ObjectMapper().readValue(content, List.class).size()).isEqualTo(1);
    }

    @Test
    public void shouldSetTweetAsDiscarded() throws Exception {
        mockMvc.perform(newTweet("Yo", "How are you?"))
                .andExpect(status().is(201));

        MvcResult getResultPublished = mockMvc.perform(get("/tweet"))
                .andExpect(status().is(200))
                .andReturn();

        String contentPublished = getResultPublished.getResponse().getContentAsString();
        List<TweetDTO> listOfTweets = new ObjectMapper().readValue(contentPublished, new ObjectMapper().getTypeFactory().constructCollectionType(List.class, TweetDTO.class));
        assertThat(listOfTweets.size()).isEqualTo(1);

        // set first tweet as discarded
        mockMvc.perform(discardTweet(listOfTweets.get(0).getId()))
                .andExpect(status().is(200));

        getResultPublished = mockMvc.perform(get("/tweet"))
                .andExpect(status().is(200))
                .andReturn();

        contentPublished = getResultPublished.getResponse().getContentAsString();

        //tweet does not appear in published tweet list
        assertThat(new ObjectMapper().readValue(contentPublished, List.class).size()).isEqualTo(0);

        MvcResult getResultDiscarded = mockMvc.perform(get("/discarded"))
                .andExpect(status().is(200))
                .andReturn();

        String contentDiscarded = getResultDiscarded.getResponse().getContentAsString();
        assertThat(new ObjectMapper().readValue(contentDiscarded, List.class).size()).isEqualTo(1);

    }

    @Test
    public void shouldListPublishedTweetsByPublishedOrder() throws Exception {
        mockMvc.perform(newTweet("Yo", "Tweet 1"))
                .andExpect(status().is(201));

        mockMvc.perform(newTweet("Yo", "Tweet 2"))
                .andExpect(status().is(201));

        mockMvc.perform(newTweet("Yo", "Tweet 3"))
                .andExpect(status().is(201));

        MvcResult getResultPublished = mockMvc.perform(get("/tweet"))
                .andExpect(status().is(200))
                .andReturn();

        String contentPublished = getResultPublished.getResponse().getContentAsString();
        List<TweetDTO> listOfTweets = new ObjectMapper().readValue(contentPublished, new ObjectMapper().getTypeFactory().constructCollectionType(List.class, TweetDTO.class));
        assertThat(listOfTweets.get(0).getTweet()).isEqualTo("Tweet 3");
        assertThat(listOfTweets.get(1).getTweet()).isEqualTo("Tweet 2");
        assertThat(listOfTweets.get(2).getTweet()).isEqualTo("Tweet 1");
    }


    @Test
    public void shouldListDiscardedTweetsByDiscardedOrder() throws Exception {
        mockMvc.perform(newTweet("Yo", "Tweet 1"))
                .andExpect(status().is(201));

        mockMvc.perform(newTweet("Yo", "Tweet 2"))
                .andExpect(status().is(201));

        mockMvc.perform(newTweet("Yo", "Tweet 3"))
                .andExpect(status().is(201));

        MvcResult getResultPublished = mockMvc.perform(get("/tweet"))
                .andExpect(status().is(200))
                .andReturn();

        String contentPublished = getResultPublished.getResponse().getContentAsString();
        List<TweetDTO> listOfTweets = new ObjectMapper().readValue(contentPublished, new ObjectMapper().getTypeFactory().constructCollectionType(List.class, TweetDTO.class));
        assertThat(listOfTweets.size()).isEqualTo(3);

        mockMvc.perform(discardTweet(listOfTweets.get(1).getId()))
                .andExpect(status().is(200));

        mockMvc.perform(discardTweet(listOfTweets.get(2).getId()))
                .andExpect(status().is(200));

        mockMvc.perform(discardTweet(listOfTweets.get(0).getId()))
                .andExpect(status().is(200));

        MvcResult getResultDiscarded = mockMvc.perform(get("/discarded"))
                .andExpect(status().is(200))
                .andReturn();

        String contentDiscarded = getResultDiscarded.getResponse().getContentAsString();
        List<TweetDTO> listOfDiscardedTweets = new ObjectMapper().readValue(contentDiscarded, new ObjectMapper().getTypeFactory().constructCollectionType(List.class, TweetDTO.class));
        assertThat(listOfDiscardedTweets.size()).isEqualTo(3);
        assertThat(listOfDiscardedTweets.get(0).getId()).isEqualTo(listOfTweets.get(0).getId());
        assertThat(listOfDiscardedTweets.get(1).getId()).isEqualTo(listOfTweets.get(2).getId());
        assertThat(listOfDiscardedTweets.get(2).getId()).isEqualTo(listOfTweets.get(1).getId());


    }



    private MockHttpServletRequestBuilder newTweet(String publisher, String tweet) {
        return post("/tweet")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(format("{\"publisher\": \"%s\", \"tweet\": \"%s\"}", publisher, tweet));
    }

    private MockHttpServletRequestBuilder discardTweet(Long tweetId) {
        return post("/discarded")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(format("{\"tweet\": %d}", tweetId));
    }

    private MockHttpServletRequestBuilder deleteAllTweets() {
        return delete("/deleteAllTweets");
    }

}
