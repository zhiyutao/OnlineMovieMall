package com.mintyi.fablix.service.Impl;

import com.mintyi.fablix.dao.MovieDao;
import com.mintyi.fablix.domain.Genre;
import com.mintyi.fablix.domain.Movie;
import com.mintyi.fablix.domain.Rating;
import com.mintyi.fablix.domain.Star;
import com.mintyi.fablix.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MovieServiceImpl implements MovieService {
    @Autowired
    private MovieDao movieDao;

    @Override
    public Movie queryMovieById(String id) {
        return movieDao.queryMovieById(id);
    }

    @Override
    public List<Movie> getTopByRating(int num) {
        return movieDao.getTopByRating(num);
    }

    @Override
    public List<Star> getMovieStars(String id) {return movieDao.getMovieStars(id);}

    @Override
    public Rating getMovieRating(String id){return movieDao.getMovieRating(id);}

    @Override
    public List<Genre> getMovieGenre(String id){return movieDao.getMovieGenre(id);}

    @Override
    public List<Movie> getMovieByGenre(String genreId, int limit, int offset, String order) {
        return movieDao.getMovieByGenre(genreId, limit, offset, order);
    }

    @Override
    public List<Genre> getAllGenre() {
        return movieDao.getAllGenre();
    }

    @Override
    public List<Movie> getMovieByInitial(String initial, int limit, int offset, String order) {
        return movieDao.getMovieByInitial(initial, limit, offset, order);
    }

    @Override
    public List<Movie> searchMovie(String title, String actor, String director, int year, String order, int limit, int offset){return movieDao.searchMovie(title,actor,director,year,order,limit,offset);}

    @Override
    public Map<String, Object> insertMovie(String movieTitle, int movieYear, String movieDirector, String starName, String genreName){return movieDao.insertMovie(movieTitle, movieYear, movieDirector, starName, genreName);}

    @Override
    public List<Movie> getTitleSuggestion(String title){return movieDao.getTitleSuggestion(title);}
}
