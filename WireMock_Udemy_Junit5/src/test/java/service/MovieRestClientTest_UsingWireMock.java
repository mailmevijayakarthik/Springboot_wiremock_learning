package service;

import com.github.jenspiegsa.wiremockextension.ConfigureWireMock;
import com.github.jenspiegsa.wiremockextension.InjectServer;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import constant.MovieAppConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import dto.Movie;
import exceptions.MovieErrorResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import com.github.tomakehurst.wiremock.core.Options;


import java.time.LocalDate;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(WireMockExtension.class)
public class MovieRestClientTest_UsingWireMock {

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

    }

    /*
    *
    * Using anyUrl
    * //given
        stubFor(get(anyUrl())
                .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("allmovies.json")));

    *
    * */
    @Test
    void retriveallMovies(){

        //given
        stubFor(get(urlPathEqualTo("/movieservice/v1/allMovies"))
                .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("allmovies.json")));


        List<Movie> allmovies= movieRestClient.retriveallMovies();
        System.out.println(allmovies);
        assertTrue(allmovies.size()>0);

    }

    @Test
    void retriveMovieByID(){
        //given
        stubFor(get(urlPathMatching("/movieservice/v1/movie/[0-9]+"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("single_movie.json")));
        Integer movieID=1;
        Movie myMovie= movieRestClient.retriveMoviebyID(movieID);
        assertEquals("Avatar 2",myMovie.getName());
        assertEquals(movieID,myMovie.getMovie_id().intValue());
    }

    /*
    * Using HandleBars {{request.path.[3]}}
    * */
    @Test
    void retriveMovieByID_ResponseTemplate(){
        //given
        stubFor(get(urlPathMatching("/movieservice/v1/movie/[0-9]+"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("single_movie_ResponseTemplate.json")));
        Integer movieID=8;
        Movie myMovie= movieRestClient.retriveMoviebyID(movieID);
        assertEquals("Avatar 2",myMovie.getName());
        assertEquals(movieID,myMovie.getMovie_id().intValue());
    }

    /*
    * Invalid scenario for 404 validation
    *
    * Assertions.assertThrows for Junit 5
    *
    *A lambda based expection
    * */

    @Test
    void retriveMovieByID_notfound() {
        // Given 

        stubFor(get(urlPathMatching("/movieservice/v1/movie/[0-9]+"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.NOT_FOUND.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("404_movies.json")));
        Integer movieID=200;
        // When
        Assertions.assertThrows(MovieErrorResponse.class,()->movieRestClient.retriveMoviebyID(movieID));
    }

    /*
    * We can use UrlEqualTo ==> need to build url with "?" and some hardcoded values in it
    * or
    * UrlPathEqualTo ==> using inbuilt query parameter
    * */
    @Test
    void retriveMoviebyName() {
        // Given
        String movieName="Avenger";
        stubFor(get(urlPathEqualTo(MovieAppConstants.GET_MOVIE_BYNAME_V1))
                .withQueryParam("movie_name",equalTo(movieName))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("search_avengers.json")));
        // When
        List<Movie> Searchmovies= movieRestClient.retriveMoviebyName(movieName);
        //Then
        for(Movie mov : Searchmovies){
            assertTrue(mov.getName().contains(movieName));
        }
    }

    @Test
    void retriveMoviebyName_ResponseTemplate() {
        // Given
        String movieName="SuperMan";
        stubFor(get(urlPathEqualTo(MovieAppConstants.GET_MOVIE_BYNAME_V1))
                .withQueryParam("movie_name",equalTo(movieName))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("search_avengers_ResponseTemplate.json")));
        // When
        List<Movie> Searchmovies= movieRestClient.retriveMoviebyName(movieName);
        //Then
        for(Movie mov : Searchmovies){
            assertTrue(mov.getName().contains(movieName));
        }
    }
    @Test
    void retriveMoviebyName_notfound() {
        // Given
        String movieName="Avenger1";
        stubFor(get(urlPathEqualTo(MovieAppConstants.GET_MOVIE_BYNAME_V1))
                .withQueryParam("movie_name",equalTo(movieName))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.NOT_FOUND.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("search_avengers.json")));
        // When
        Assertions.assertThrows(MovieErrorResponse.class,()->movieRestClient.retriveMoviebyName(movieName));

    }

    @Test
    void retriveMoviebyYear() {
        // Given
        Integer movieYear=2012;
        stubFor(get(urlPathEqualTo(MovieAppConstants.GER_MOVIE_BYYEAR_V1))
                .withQueryParam("year",equalTo(movieYear.toString()))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("searchmovieby_year_ResponseTemplate.json")));


        // When
        List<Movie> Searchmovies= movieRestClient.retriveMoviebyYear(movieYear.intValue());
        System.out.println(Searchmovies);
        //Then
        for(Movie mov : Searchmovies){
            assertTrue(mov.getYear().toString().contains(Integer.toString(movieYear)));
        }
    }

    @Test
    void retriveMoviebyYear_notfound() {
        // Given
        Integer movieYear=2020;

        stubFor(get(urlPathEqualTo(MovieAppConstants.GER_MOVIE_BYYEAR_V1))
                .withQueryParam("year",equalTo(movieYear.toString()))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.NOT_FOUND.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("404_for_Year.json")));


        // When
        Assertions.assertThrows(MovieErrorResponse.class,()->movieRestClient.retriveMoviebyYear(movieYear.intValue()));

    }

    @Test
    void addmovie() {
        // Given
        Movie newmovie = new Movie("Vijay",null,"Sarkar", LocalDate.of(2019,06,30),2019);

        stubFor(post(urlPathEqualTo(MovieAppConstants.POST_ADDMOVIE_V1))
                .withRequestBody(matchingJsonPath(("$.name"),equalTo("Sarkar")))
                .withRequestBody(matchingJsonPath(("$.cast"),containing("Vijay")))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("add_movie.json")));

        // When
        Movie addedmovie = movieRestClient.addmovie(newmovie);

        //Then
        assertTrue(addedmovie.getMovie_id()!=null);
    }


    @Test
    void addmovie_ResponseTemplate() {
        // Given
        Movie newmovie = new Movie("Vijay",null,"Sarkar", LocalDate.of(2019,06,30),2019);

        stubFor(post(urlPathEqualTo(MovieAppConstants.POST_ADDMOVIE_V1))
                .withRequestBody(matchingJsonPath(("$.name"),equalTo("Sarkar")))
                .withRequestBody(matchingJsonPath(("$.cast"),containing("Vijay")))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("add_movie_ResponseTemplate.json")));

        // When
        Movie addedmovie = movieRestClient.addmovie(newmovie);

        //Then
        assertTrue(addedmovie.getMovie_id()!=null);
    }

    @Test
    void addmovie_notfound() {
        // Given
        Movie newmovie = new Movie("Vijay",null,null, LocalDate.of(2019,06,30),2019);

        stubFor(post(urlPathEqualTo(MovieAppConstants.POST_ADDMOVIE_V1))
                .withRequestBody(matchingJsonPath(("$.cast"),containing("Vijay")))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("400_movies.json")));
        // When
        Assertions.assertThrows(MovieErrorResponse.class,()->movieRestClient.addmovie(newmovie));

    }

    @Test
    void updatemovie() {
        // Given
        Integer movieID=1;
        String cast = "Karthik";
        Movie neededupdate = new Movie("Karthik",movieID.longValue(),"Avatar 2", LocalDate.of(2019,06,30),2019);

        stubFor(put(urlPathMatching("/movieservice/v1/movie/[0-9]+"))
                .withRequestBody(matchingJsonPath(("$.cast")))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("update_movie.json")));

        // When
        Movie UpdatedMovie = movieRestClient.updatemovie(movieID,neededupdate);
        System.out.println(UpdatedMovie);
        System.out.println("Get Cast :"+ UpdatedMovie.getCast());
        //Then
        assertTrue(UpdatedMovie.getCast().contains(cast));
    }

    @Test
    void updatemovie_notfound() {
        // Given
        Integer movieID=100;
        Movie neededupdate = new Movie("Karthik",movieID.longValue(),"Avatar 2", LocalDate.of(2019,06,30),2019);

        stubFor(put(urlPathMatching("/movieservice/v1/movie/[0-9]+"))
                .withRequestBody(matchingJsonPath(("$.cast")))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.NOT_FOUND.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        ));


        // When
        Assertions.assertThrows(MovieErrorResponse.class,()->movieRestClient.updatemovie(movieID,neededupdate));

    }

    @Test
    void deletemovie() {
        // Given
        Movie newmovie = new Movie("Vijay",null,"Sarkar", LocalDate.of(2019,06,30),2019);

        stubFor(post(urlPathEqualTo(MovieAppConstants.POST_ADDMOVIE_V1))
                .withRequestBody(matchingJsonPath(("$.name"),equalTo("Sarkar")))
                .withRequestBody(matchingJsonPath(("$.cast"),containing("Vijay")))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("add_movie_ResponseTemplate.json")));
        Movie addedmovie = movieRestClient.addmovie(newmovie);

        stubFor(delete(urlPathMatching("/movieservice/v1/movie/[0-9]+"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("Movie Deleted Successfully")));



        Integer movieID = addedmovie.getMovie_id().intValue();

        // When
        String Deleteresponsemessage = movieRestClient.Deletemovie(movieID);

        //Then
        assertTrue(Deleteresponsemessage.contentEquals("Movie Deleted Successfully"));
    }

    @Test
    void deletemovie_notfound() {
        // Given
        Integer movieID = 1000;
        stubFor(delete(urlPathMatching("/movieservice/v1/movie/[0-9]+"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.NOT_FOUND.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));


        // When
        Assertions.assertThrows(MovieErrorResponse.class,()->movieRestClient.Deletemovie(movieID));

    }

    /* Delete movie using movie Name -- Note : This endpoint is not available yet but using wiremock
     * we are going to automate this considering the end point gives an void response with status code alone*/

    @Test
    void deletemovie_ByMovieName() {
        // Given
        Movie newmovie = new Movie("Vijay",null,"Sarkar", LocalDate.of(2019,06,30),2019);

        stubFor(post(urlPathEqualTo(MovieAppConstants.POST_ADDMOVIE_V1))
                .withRequestBody(matchingJsonPath(("$.name"),equalTo("Sarkar")))
                .withRequestBody(matchingJsonPath(("$.cast"),containing("Vijay")))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("add_movie_ResponseTemplate.json")));
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
