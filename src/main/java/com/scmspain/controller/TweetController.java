package com.scmspain.controller;

import com.scmspain.dtos.TweetDTO;
import com.scmspain.services.TweetService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
public class TweetController {
    private TweetService tweetService;

    public TweetController(TweetService tweetService) {
        this.tweetService = tweetService;
    }

    @GetMapping("/tweet")
    public List<TweetDTO> listAllTweets() {
        return this.tweetService.listAllPublishedTweets();
    }

    @PostMapping("/tweet")
    @ResponseStatus(CREATED)
    public void publishTweet(@RequestBody TweetDTO tweetDTO) {
        this.tweetService.publishTweet(tweetDTO);
    }

    @PostMapping("/discarded")
    @ResponseStatus(OK)
    public void discardTweet(@RequestBody TweetDTO tweetDTO) {
        this.tweetService.discardTweet(Long.parseLong(tweetDTO.getTweet()));
    }

    @GetMapping("/discarded")
    public List<TweetDTO> listAllDiscardedTweets() {
        return this.tweetService.listAllDiscardedTweets();
    }

    @DeleteMapping("/deleteAllTweets")
    @ResponseStatus(OK)
    public int deleteAllTweetsTweets() {
        return this.tweetService.deleteAllTweets();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public Object invalidArgumentException(IllegalArgumentException ex) {
        return new Object() {
            public String message = ex.getMessage();
            public String exceptionClass = ex.getClass().getSimpleName();
        };
    }
}
