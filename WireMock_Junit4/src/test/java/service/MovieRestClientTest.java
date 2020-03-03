package service;

import dto.Movie;
import exceptions.MovieErrorResponse;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MovieRestClientTest {

    MovieRestClient movieRestClient;
    WebClient webClient;
    /*
    * WebClient.create() ==> gets the baseUrl inbuilt
    * */

    @Before
    void setup(){

        String baseUrl="http://localhost:8081";
        webClient=WebClient.create(baseUrl);
        movieRestClient=new MovieRestClient(webClient);

    }

    @Test
    void retriveallMovies(){
        List<Movie> allmovies= movieRestClient.retriveallMovies();
        System.out.println(allmovies);
        assertTrue(allmovies.size()>0);

    }

    @Test
    void retriveMovieByID(){
        Integer movieID=1;
        Movie myMovie= movieRestClient.retriveMoviebyID(movieID);

        assertEquals("Avatar 2",myMovie.getName());

    }

    /*
    * Invalid scenario for 404 validation
    *
    * Assertions.assertThrows for Junit 5
    *
    *A lambda based expection
    * */

    @Test(expected = MovieErrorResponse.class)
    void retriveMovieByID_notfound() {
        // Given 
        Integer movieID=100;
        // When
        movieRestClient.retriveMoviebyID(movieID);

    }

    @Test
    void retriveMoviebyName() {
        // Given
        String movieName="Avenger";

        // When
        List<Movie> Searchmovies= movieRestClient.retriveMoviebyName(movieName);
        //Then
        for(Movie mov : Searchmovies){
            assertTrue(mov.getName().contains(movieName));
        }
    }

    @Test(expected = MovieErrorResponse.class)
    void retriveMoviebyName_notfound() {
        // Given
        String movieName="Avenger1";

        // When
        movieRestClient.retriveMoviebyName(movieName);

    }

    @Test
    void retriveMoviebyYear() {
        // Given
        Integer movieYear=2012;

        // When
        List<Movie> Searchmovies= movieRestClient.retriveMoviebyYear(movieYear.intValue());
        //Then
        for(Movie mov : Searchmovies){
            assertTrue(mov.getYear().toString().contains(Integer.toString(movieYear)));
        }
    }

    @Test(expected = MovieErrorResponse.class)
    void retriveMoviebyYear_notfound() {
        // Given
        Integer movieYear=2020;

        // When
        movieRestClient.retriveMoviebyYear(movieYear.intValue());

    }

    @Test
    void addmovie() {
        // Given
        Movie newmovie = new Movie("Rajini",null,"Dharbar", LocalDate.of(2019,06,30),2019);

        // When
        Movie addedmovie = movieRestClient.addmovie(newmovie);

        //Then
        assertTrue(addedmovie.getMovie_id()!=null);
    }

    @Test(expected = MovieErrorResponse.class)
    void addmovie_notfound() {
        // Given
        Movie newmovie = new Movie("Rajini",null,null, LocalDate.of(2019,06,30),2019);

        // When
        // When
        movieRestClient.addmovie(newmovie);

    }

    @Test
    void updatemovie() {
        // Given
        Integer movieID=1;
        Movie neededupdate = new Movie("Rajini",movieID.longValue(),"Avatar 2", LocalDate.of(2019,06,30),2019);

        // When
        Movie UpdatedMovie = movieRestClient.updatemovie(movieID,neededupdate);

        //Then
        assertTrue(UpdatedMovie.getName().contains("Avatar 2"));
    }

    @Test(expected = MovieErrorResponse.class)
    void updatemovie_notfound() {
        // Given
        Integer movieID=100;
        Movie neededupdate = new Movie("Rajini",movieID.longValue(),"Avatar 2", LocalDate.of(2019,06,30),2019);


        // When
        movieRestClient.updatemovie(movieID,neededupdate);

    }

    @Test
    void deletemovie() {
        // Given
        Movie newmovie = new Movie("Rajini",null,"Dharbar", LocalDate.of(2019,06,30),2019);
        Movie addedmovie = movieRestClient.addmovie(newmovie);
        Integer movieID = addedmovie.getMovie_id().intValue();

        // When
        String Deleteresponsemessage = movieRestClient.Deletemovie(movieID);

        //Then
        assertTrue(Deleteresponsemessage.contentEquals("Movie Deleted Successfully"));
    }

    @Test(expected = MovieErrorResponse.class)
    void deletemovie_notfound() {
        // Given
        Integer movieID = 1000;

        // When
        movieRestClient.Deletemovie(movieID);


    }
}
