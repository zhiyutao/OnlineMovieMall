package com.mintyi.fablix.dao.mapper;

import com.mintyi.fablix.domain.Movie;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MovieMapper implements RowMapper<Movie> {
    @Override
    public Movie mapRow(ResultSet resultSet, int i) throws SQLException {
        Movie movie = new Movie();
        movie.setTitle(resultSet.getString("title"));
        movie.setId(resultSet.getString("id"));
        movie.setYear(resultSet.getInt("year"));
        movie.setDirector(resultSet.getString("director"));
        Float rating = (Float) resultSet.getObject("rating");
        if(rating == null)
            movie.setRating(-1f);
        else
            movie.setRating(rating);
        return movie;
    }
}
