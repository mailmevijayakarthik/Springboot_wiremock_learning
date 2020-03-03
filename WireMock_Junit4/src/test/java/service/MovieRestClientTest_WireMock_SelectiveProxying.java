package service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import constant.MovieAppConstants;
import dto.Movie;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertTrue;

public class MovieRestClientTest_WireMock_SelectiveProxying {

    MovieRestClient movieRestClient;
    WebClient webClient;
    /*
    * WebClient.create() ==> gets the baseUrl inbuilt
    * */


    /*
    * ResponseTemplateTransformer --> to handle dynamic Response
    *
    * */

    Options option = wireMockConfig()
            .port(8088)
            .notifier(new ConsoleNotifier(true))
            .extensions(new ResponseTemplateTransformer(true));
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(option);

    @Before
    public void setup(){
        int port = wireMockRule.port();
        String baseUrl=String.format("http://localhost:%s",port);
        System.out.println(baseUrl);
        webClient=WebClient.create(baseUrl);
        movieRestClient=new MovieRestClient(webClient);

        stubFor(any(anyUrl()).willReturn(aResponse().proxiedFrom("http://localhost:8081")));

    }


    @Test
    public void deletemovie_ByMovieName_SelectiveProxying() {
        // Given
        Movie newmovie = new Movie("Vijay",null,"Sarkar", LocalDate.of(2019,06,30),2019);
        Movie addedmovie = movieRestClient.addmovie(newmovie);

        stubFor(delete(urlEqualTo(MovieAppConstants.GET_MOVIE_BYNAME_V1+"?movie_name="+addedmovie.getName()))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        ));

        // When
        String Deleteresponsemessage = movieRestClient.DeletemovieByName(addedmovie.getName());

        //Then
        assertTrue(Deleteresponsemessage.contentEquals("Movie is Deleted Successfully"));

        verify(exactly(1),postRequestedFor((urlPathEqualTo(MovieAppConstants.POST_ADDMOVIE_V1)))
                .withRequestBody(matchingJsonPath(("$.name"),equalTo("Sarkar")))
                .withRequestBody(matchingJsonPath(("$.cast"),containing("Vijay"))));

       verify(exactly(1),deleteRequestedFor((urlEqualTo(MovieAppConstants.GET_MOVIE_BYNAME_V1+"?movie_name="+addedmovie.getName()))));
    }

}
