package service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import exceptions.MovieErrorResponse;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;


public class MovieRestClientServerFaultTest {

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

    TcpClient tcpClient = TcpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000)
            .doOnConnected(connection -> {
                connection.addHandlerLast(new ReadTimeoutHandler(5))
                        .addHandlerLast(new WriteTimeoutHandler(5));
            });

    @Before
    public void setup(){
        int port = wireMockRule.port();
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

    @Test(expected=MovieErrorResponse.class)
    public void retriveallMovies_ServerError500(){

        //given
        stubFor(get(anyUrl())
                .willReturn(serverError()));

        movieRestClient.retriveallMovies();


    }

    @Test(expected=MovieErrorResponse.class)
    public void retriveallMovies_ServiceUnavailable503(){

        //given
        stubFor(get(anyUrl())
                .willReturn(serverError()
                .withStatus(HttpStatus.SERVICE_UNAVAILABLE.value())
                .withBody("Service Unavailable")));

        movieRestClient.retriveallMovies();

    }

    @Test(expected=MovieErrorResponse.class)
    public void retriveallMovies_FaultEmptyResponse(){

        //given
        stubFor(get(anyUrl())
                .willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));
        movieRestClient.retriveallMovies();

    }

    @Test(expected=MovieErrorResponse.class)
    public void retriveallMovies_FaultRandomResponse(){

        //given
        stubFor(get(anyUrl())
                .willReturn(aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE)));

        movieRestClient.retriveallMovies();


    }

    @Test(expected=MovieErrorResponse.class)
    public void retriveallMovies_fixedDelay(){

        //given
        stubFor(get(anyUrl())
                .willReturn(ok().withFixedDelay(10000)));
        movieRestClient.retriveallMovies();

    }

    @Test(expected=MovieErrorResponse.class)
    public void retriveallMovies_UniformRandomDelay(){

        //given
        stubFor(get(anyUrl())
                .willReturn(ok().withUniformRandomDelay(6000,10000)));
        movieRestClient.retriveallMovies();

    }
}
