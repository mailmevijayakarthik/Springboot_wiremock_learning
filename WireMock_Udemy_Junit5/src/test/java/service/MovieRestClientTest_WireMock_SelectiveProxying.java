package service;

import com.github.jenspiegsa.wiremockextension.ConfigureWireMock;
import com.github.jenspiegsa.wiremockextension.InjectServer;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import constant.MovieAppConstants;
import dto.Movie;
import exceptions.MovieErrorResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(WireMockExtension.class)
public class MovieRestClientTest_WireMock_SelectiveProxying {

    MovieRestClient movieRestClient;


    WebClient webClient;
    /*
    * WebClient.create() ==> gets the baseUrl inbuilt
    * */

    @InjectServer
    WireMockServer wireMockServer;

    /*
    * ResponseTemplateTransformer --> to handle dynamic Response
    *
    * */
    @ConfigureWireMock
    Options option = wireMockConfig()
            .port(8088)
            .notifier(new ConsoleNotifier(true))
            .extensions(new ResponseTemplateTransformer(true));

    @BeforeEach
    void setup(){
        int port = wireMockServer.port();
        String baseUrl=String.format("http://localhost:%s",port);
        System.out.println(baseUrl);
        webClient=WebClient.create(baseUrl);
        movieRestClient=new MovieRestClient(webClient);

        stubFor(any(anyUrl()).willReturn(aResponse().proxiedFrom("http://localhost:8081")));

    }


    @Test
    void deletemovie_ByMovieName_SelectiveProxying() {
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
