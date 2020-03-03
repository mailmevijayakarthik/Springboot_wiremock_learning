package service;

import constant.MovieAppConstants;
import dto.Movie;
import exceptions.MovieErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Slf4j
public class MovieRestClient {

    private WebClient webClient;

    public MovieRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public List<Movie> retriveallMovies(){
       //  http://localhost:8081/movieservice/v1/allMovies

       try {
           return webClient.get().uri(MovieAppConstants.GET_ALL_MOVIES_V1)
                   .retrieve()
                   .bodyToFlux(Movie.class)
                   .collectList()
                   .block();
       }catch (WebClientResponseException ex) {
           log.error("WebClientResponseException in retriveMoviebyID and the message is {} with Status as  {}",ex.getResponseBodyAsString(),ex.getStatusCode());
           throw new MovieErrorResponse(ex.getStatusText(),ex);
       }catch (Exception ex) {
           log.error("Exception in retriveMoviebyID and the message is {} ",ex.getMessage());
           throw new MovieErrorResponse(ex);
       }


    }

    public Movie retriveMoviebyID(Integer movieid){
        //http://localhost:8081/movieservice/v1/movie/1

        try {
            return webClient.get().uri(MovieAppConstants.RETRIVE_MOVIE_BYID_V1, movieid)
                    .retrieve()
                    .bodyToMono(Movie.class)
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("WebClientResponseException in retriveMoviebyID and the message is {} with Status as  {}",ex.getResponseBodyAsString(),ex.getStatusCode());
            throw new MovieErrorResponse(ex.getStatusText(),ex);
        }catch (Exception ex) {
            log.error("Exception in retriveMoviebyID and the message is {} ",ex.getMessage());
            throw new MovieErrorResponse(ex);
        }

    }

    public List<Movie> retriveMoviebyName(String moviename){
        //http://localhost:8081/movieservice/v1/movieName?movie_name=Avenger

        String RetriveUriByName= UriComponentsBuilder.fromUriString(MovieAppConstants.GET_MOVIE_BYNAME_V1).queryParam("movie_name",moviename)
                .buildAndExpand()
                .toUriString();

        try {
            return webClient.get().uri(RetriveUriByName)
                    .retrieve()
                    .bodyToFlux(Movie.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("WebClientResponseException in retriveMoviebyName and the message is {} with Status as  {}",ex.getResponseBodyAsString(),ex.getStatusCode());
            throw new MovieErrorResponse(ex.getStatusText(),ex);
        }catch (Exception ex) {
            log.error("Exception in retriveMoviebyName and the message is {} ",ex.getMessage());
            throw new MovieErrorResponse(ex);
        }

    }

    public List<Movie> retriveMoviebyYear(Integer Year){
        //http://localhost:8081/movieservice/v1/movieYear?year=2010
        String RetriveUriByYear= UriComponentsBuilder.fromUriString(MovieAppConstants.GER_MOVIE_BYYEAR_V1).queryParam("year",Year)
                .buildAndExpand()
                .toUriString();

        try {
            return webClient.get().uri(RetriveUriByYear)
                    .retrieve()
                    .bodyToFlux(Movie.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("WebClientResponseException in retriveMoviebyYear and the message is {} with Status as  {}",ex.getResponseBodyAsString(),ex.getStatusCode());
            throw new MovieErrorResponse(ex.getStatusText(),ex);
        }catch (Exception ex) {
            log.error("Exception in retriveMoviebyYear and the message is {} ",ex.getMessage());
            throw new MovieErrorResponse(ex);
        }

    }

    public Movie addmovie(Movie newmovie){
        //http://localhost:8081/movieservice/v1/movie
        try {
            return webClient.post().uri(MovieAppConstants.POST_ADDMOVIE_V1)
                    .syncBody(newmovie)
                    .retrieve()
                    .bodyToMono(Movie.class)
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("WebClientResponseException in retriveMoviebyID and the message is {} with Status as  {}",ex.getResponseBodyAsString(),ex.getStatusCode());
            throw new MovieErrorResponse(ex.getStatusText(),ex);
        }catch (Exception ex) {
            log.error("Exception in retriveMoviebyID and the message is {} ",ex.getMessage());
            throw new MovieErrorResponse(ex);
        }


    }

    public Movie updatemovie(Integer movieid, Movie newmovie){
        //http://localhost:8081/movieservice/v1/movie/1
        try {
            return webClient.put().uri(MovieAppConstants.RETRIVE_MOVIE_BYID_V1, movieid)
                    .syncBody(newmovie)
                    .retrieve()
                    .bodyToMono(Movie.class)
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("WebClientResponseException in retriveMoviebyID and the message is {} with Status as  {}",ex.getResponseBodyAsString(),ex.getStatusCode());
            throw new MovieErrorResponse(ex.getStatusText(),ex);
        }catch (Exception ex) {
            log.error("Exception in retriveMoviebyID and the message is {} ",ex.getMessage());
            throw new MovieErrorResponse(ex);
        }


    }


    public String Deletemovie(Integer movieid){
        //http://localhost:8081/movieservice/v1/movie/1
        try {
            return webClient.delete().uri(MovieAppConstants.RETRIVE_MOVIE_BYID_V1, movieid)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("WebClientResponseException in retriveMoviebyID and the message is {} with Status as  {}",ex.getResponseBodyAsString(),ex.getStatusCode());
            throw new MovieErrorResponse(ex.getStatusText(),ex);
        }catch (Exception ex) {
            log.error("Exception in retriveMoviebyID and the message is {} ",ex.getMessage());
            throw new MovieErrorResponse(ex);
        }


    }

    /* Delete movie using movie Name -- Note : This endpoint is not available yet but using wiremock
    * we are going to automate this considering the end point gives an void response with status code alone*/
    public String DeletemovieByName(String movieName){
        //http://localhost:8081/movieservice/v1/movieName?movie_name=Avenger



        try {

            String deleteUriByName= UriComponentsBuilder.fromUriString(MovieAppConstants.GET_MOVIE_BYNAME_V1).queryParam("movie_name",movieName)
                    .buildAndExpand()
                    .toUriString();

             webClient.delete().uri(deleteUriByName)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        }catch (WebClientResponseException ex) {
            log.error("WebClientResponseException in DeleteMovieByName and the message is {} with Status as  {}",ex.getResponseBodyAsString(),ex.getStatusCode());
            throw new MovieErrorResponse(ex.getStatusText(),ex);
        }catch (Exception ex) {
            log.error("Exception in DeleteMovieByName and the message is {} ",ex.getMessage());
            throw new MovieErrorResponse(ex);
        }

        return "Movie is Deleted Successfully";
    }
}
