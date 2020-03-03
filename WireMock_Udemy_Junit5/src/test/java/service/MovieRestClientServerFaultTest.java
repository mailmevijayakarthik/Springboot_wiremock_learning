package service;

import com.github.jenspiegsa.wiremockextension.ConfigureWireMock;
import com.github.jenspiegsa.wiremockextension.InjectServer;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.http.Fault;
import dto.Movie;
import exceptions.MovieErrorResponse;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(WireMockExtension.class)
public class MovieRestClientServerFaultTest {

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

    TcpClient tcpClient = TcpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000)
            .doOnConnected(connection -> {
                connection.addHandlerLast(new ReadTimeoutHandler(5))
                        .addHandlerLast(new WriteTimeoutHandler(5));
            });

    @BeforeEach
    void setup(){
        int port = wireMockServer.port();
        String baseUrl=String.format("http://localhost:%s",port);
        System.out.println(baseUrl);
      //   webClient=WebClient.create(baseUrl);
        /*
        * Need to update teh webClient initialization as below which takes tcpClient object
        * */
        webClient=WebClient.builder().clientConnector(new ReactorClientHttpConnector((HttpClient.from(tcpClient))))
                .baseUrl(baseUrl).build();
        movieRestClient=new MovieRestClient(webClient);

    }

    @Test
    void retriveallMovies_ServerError500(){

        //given
        stubFor(get(anyUrl())
                .willReturn(serverError()));

        assertThrows(MovieErrorResponse.class,()->movieRestClient.retriveallMovies());

    }

    @Test
    void retriveallMovies_ServiceUnavailable503(){

        //given
        stubFor(get(anyUrl())
                .willReturn(serverError()
                .withStatus(HttpStatus.SERVICE_UNAVAILABLE.value())
                .withBody("Service Unavailable")));

       MovieErrorResponse error = assertThrows(MovieErrorResponse.class,()->movieRestClient.retriveallMovies());
       assertEquals("Service Unavailable",error.getMessage());
    }

    @Test
    void retriveallMovies_FaultEmptyResponse(){

        //given
        stubFor(get(anyUrl())
                .willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));

        MovieErrorResponse error = assertThrows(MovieErrorResponse.class,()->movieRestClient.retriveallMovies());
        String Errormessage = "reactor.core.Exceptions$ReactiveException: reactor.netty.http.client.PrematureCloseException: Connection prematurely closed BEFORE response";
        assertEquals(Errormessage,error.getMessage());

    }

    @Test
    void retriveallMovies_FaultRandomResponse(){

        //given
        stubFor(get(anyUrl())
                .willReturn(aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE)));

        MovieErrorResponse error = assertThrows(MovieErrorResponse.class,()->movieRestClient.retriveallMovies());
        String Errormessage = "reactor.core.Exceptions$ReactiveException: reactor.netty.http.client.PrematureCloseException: Connection prematurely closed BEFORE response";
        assertEquals(Errormessage,error.getMessage());

    }

    @Test
    void retriveallMovies_fixedDelay(){

        //given
        stubFor(get(anyUrl())
                .willReturn(ok().withFixedDelay(10000)));


        assertThrows(MovieErrorResponse.class,()->movieRestClient.retriveallMovies());


    }

    @Test
    void retriveallMovies_UniformRandomDelay(){

        //given
        stubFor(get(anyUrl())
                .willReturn(ok().withUniformRandomDelay(6000,10000)));


        assertThrows(MovieErrorResponse.class,()->movieRestClient.retriveallMovies());


    }
}
