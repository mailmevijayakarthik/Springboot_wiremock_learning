package com.wiremock_Boot.wiremock_Boot.servicetest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.wiremock_Boot.wiremock_Boot.dto.Movie;
import com.wiremock_Boot.wiremock_Boot.service.MovieRestClient;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties= {"movieapp.baseUrl=http://localhost:8089"})
public class MovieRestClientTest_wiremockrule_Junit4 {
    @Autowired
    MovieRestClient movieRestClient;

    Options options = wireMockConfig().
            port(8089)
            .notifier(new ConsoleNotifier(true))
            .extensions(new ResponseTemplateTransformer(true));

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options);

    @Test
    public void getAllMovies() {

        //given
        stubFor(get(WireMock.anyUrl())
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(HttpStatus.OK.value())
                        .withBodyFile("allmovies.json")));
        //when
        List<Movie> movieList = movieRestClient.retriveallMovies();
        System.out.println("movieList : " + movieList);

        //then
        assertTrue(!movieList.isEmpty());
    }

    @Test
    public void retrieveMovieById_withResponseTemplating() {

        //given
        stubFor(get(urlPathMatching("/movieservice/v1/movie/[0-9]+"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("single_movie_ResponseTemplate.json")));


        //given
        Integer movieId = 200;

        //when
        Movie movie = movieRestClient.retriveMoviebyID(movieId);
        //then
        assertEquals("Avatar 2", movie.getName());
        assertEquals(movieId.intValue(), movie.getMovie_id().intValue());
    }

}
