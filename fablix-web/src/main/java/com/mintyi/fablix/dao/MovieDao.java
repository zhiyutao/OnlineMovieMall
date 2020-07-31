package com.mintyi.fablix.dao;

import com.mintyi.fablix.domain.Genre;
import com.mintyi.fablix.domain.Movie;
import com.mintyi.fablix.domain.Rating;
import com.mintyi.fablix.domain.Star;

import java.util.List;
import java.util.Map;

public interface MovieDao {
    public Movie queryMovieById(String id);
    public List<Movie> getTopByRating(int num);
    public List<Star> getMovieStars(String id);
    public List<Star> getMovieStars(String id, int limit);
    public Rating getMovieRating(String id);
    public List<Genre> getMovieGenre(String id);
    public List<Genre> getMovieGenre(String id, int limit);
    public List<Movie> getMovieByGenre(String genreId, int limit, int offset, String order);
    public List<Movie> getMovieByInitial(String initial, int limit, int offset, String order);
    public List<Genre> getAllGenre();
    public List<Movie> searchMovie(String title, String actor, String director, int year, String order, int limit, int offset);
    public Map<String, Object> insertMovie(String movieTitle, int movieYear, String movieDirector, String starName, String genreName);
    public List<Movie> getTitleSuggestion(String title);
}
