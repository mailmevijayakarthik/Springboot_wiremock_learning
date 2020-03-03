package constant;

import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.function.Function;

public class MovieAppConstants {
    public static String  POST_ADDMOVIE_V1 = "/movieservice/v1/movie";
    public static  String GER_MOVIE_BYYEAR_V1 = "/movieservice/v1/movieYear";
    public static String GET_MOVIE_BYNAME_V1 ="/movieservice/v1/movieName" ;
    public static String RETRIVE_MOVIE_BYID_V1 ="/movieservice/v1/movie/{id}";
    public static String GET_ALL_MOVIES_V1 = "/movieservice/v1/allMovies" ;
}
