package com.mintyi.fablix.dao.Impl;

import com.mintyi.fablix.dao.MovieDao;
import com.mintyi.fablix.dao.mapper.MovieMapper;
import com.mintyi.fablix.domain.Genre;
import com.mintyi.fablix.domain.Movie;
import com.mintyi.fablix.domain.Rating;
import com.mintyi.fablix.domain.Star;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class MovieDaoImpl implements MovieDao {

    @Autowired
    JdbcTemplate readTemplate;
    @Autowired
    JdbcTemplate writeTemplate;
    @Autowired
    TransactionTemplate transactionTemplate;

    @Override
    public Movie queryMovieById(String id) {
        List<Movie> list = readTemplate.query(
                "select * from movies where id = ?", new Object[]{id}, new BeanPropertyRowMapper<>(Movie.class));
        if(list != null && list.size() > 0){
            return list.get(0);
        }
        return null;
    }

    @Override
    public List<Movie> getTopByRating(int num) {
        // top 20 by rating
        String sql = "select id,title, movies.year as year, director, rating from movies join " +
                "(select movieId, rating from ratings order by rating desc LIMIT ?) as r on movies.id=movieId";
        List<Movie> list = readTemplate.query(
                sql, new Object[]{num}, new BeanPropertyRowMapper<>(Movie.class));
        for (Movie movie: list) {
            List<Star> a = getMovieStars(movie.getId(), 3);
            movie.setActors(a);
            List<Genre> b = getMovieGenre(movie.getId(), 3);
            movie.setGenres(b);
        }
        return list;
    }

    @Override
    public List<Star> getMovieStars(String id) {
        String sql = "select id, name, birthYear from stars join (select c.starId, count(*) as counter from stars_in_movies as c join (select starId from stars_in_movies where movieId = ?) as b on b.starId=c.starId group by c.starId) as a on id = a.starId order by a.counter desc, name asc";
        List<Star> list = readTemplate.query(sql, new Object[]{id}, new BeanPropertyRowMapper<>(Star.class));
        return list;
    }

    @Override
    public List<Star> getMovieStars(String id, int limit) {
//        String sql = "select id, name, a.counter from stars join (select c.starId, count(*) as counter from stars_in_movies as c join (select starId from stars_in_movies where movieId = ?) as b on b.starId=c.starId group by c.starId) as a on id = a.starId order by a.counter desc, name asc limit ?" ;
        String sql = "select id, name from stars join (select c.starId, count(*) as counter from stars_in_movies as c join (select starId from stars_in_movies where movieId = ?) as b on b.starId=c.starId group by c.starId) as a on id = a.starId order by a.counter desc, name asc limit ?" ;

        List<Star> list = readTemplate.query(
                sql, new Object[]{id, limit}, new BeanPropertyRowMapper<>(Star.class));
        return list;
    }

    @Override
    public Rating getMovieRating(String id) {
        List<Rating> list = readTemplate.query("select * from ratings where movieId = ?", new Object[]{id}, new BeanPropertyRowMapper<>(Rating.class));
        if (list.size()==0) {
            Rating r = new Rating();
            r.setRating(-1);
            return r;
        }
        return list.get(0);
    }

    @Override
    public List<Genre> getMovieGenre(String id) {
        String sql = "select id, name from genres join (select genreId from genres_in_movies where movieId = ?) as a on id = a.genreId order by name";
        List<Genre> list = readTemplate.query(sql, new Object[]{id}, new BeanPropertyRowMapper<>(Genre.class));
        return list;
    }

    @Override
    public List<Genre> getMovieGenre(String id, int limit) {
        String sql = "select id, name from genres join " +
                "(select genreId from genres_in_movies where movieId = ?) as a " +
                "on id = a.genreId order by name limit ?";
        List<Genre> list = readTemplate.query(sql, new Object[]{id, limit}, new BeanPropertyRowMapper<>(Genre.class));
        return list;
    }

    @Override
    public List<Movie> getMovieByGenre(String genreId, int limit, int offset, String order) {
        String sql = "select * from movies join (select movieId from genres_in_movies where genreId = ?) as a on a.movieId = id left join ratings r on movies.id = r.movieId";

        sql += getOrderBySql(order);
        sql += " limit ? offset ?";

        int newLimit = 5 * limit;
        List<Movie> list = readTemplate.query(sql, new Object[]{genreId, newLimit, offset}, new MovieMapper());
        return fillMovies(limit, 0, list);
    }

    private void fillSingleMovie(Movie movie) {
        List<Star> a = getMovieStars(movie.getId(), 3);
        movie.setActors(a);
        List<Genre> b = getMovieGenre(movie.getId(), 3);
        movie.setGenres(b);
    }

    @Override
    public List<Movie> getMovieByInitial(String initial, int limit, int offset, String order) {
        String sql;
        List<Movie> list;
        int newLimit = 5 * limit;
        if(initial.equals("*")){
            sql = "select * from movies left join ratings on id=movieId where title not regexp '^[a-zA-Z0-9]'";
            sql += getOrderBySql(order);
            sql += " limit ? offset ?";
            list = readTemplate.query(sql, new Object[]{newLimit, offset}, new MovieMapper());

        }
        else {
            sql = "select * from movies left join ratings on id=movieId where title LIKE ?";
            sql += getOrderBySql(order);
            sql += " limit ? offset ?";
            list = readTemplate.query(sql, new Object[]{initial + "%", newLimit, offset}, new MovieMapper());
        }
        return fillMovies(limit, 0, list);
    }

    private List<Movie> fillMovies(int limit, int offset, List<Movie> list) {
        int totalNum = list.size();
        list = list.stream().skip(offset).limit(limit).collect(Collectors.toList());
        list.stream().forEach(movie -> {
            fillSingleMovie(movie);
            movie.setTotalNum(totalNum);
        });
        return list;
    }

    @Override
    public List<Genre> getAllGenre() {
        String sql = "select id, name from genres order by name;";
        List<Genre> list = readTemplate.query(sql, new BeanPropertyRowMapper<>(Genre.class));
        return list;
    }

    @Override
    public List<Movie> searchMovie(String title, String actor, String director, int year, String order, int limit, int offset) {
        ArrayList<Object> parameters = new ArrayList<>();
        String sql = "select * from ";
        String[] titles = title.split("\\s+");
        Optional<String> a = Arrays.stream(titles).filter(s -> s.length() > 0).map(s -> "+" + s + "*").reduce((s, s2) -> s + " "+s2);
        String titleMatch = "";
        if(a.isPresent()) {
            titleMatch = " match (title) against (? in boolean mode) and ";
        }
        if (actor != null && !"".equals(actor)) {
            sql += "(select * from movies join (select distinct movieId from stars_in_movies join (select id, name from stars where name like ?) as n on n.id = starId) as s on id = s.movieId where "+ titleMatch + "director like ? ";
            parameters.add('%'+actor+'%');
            if(a.isPresent()) {
                parameters.add(a.get());
            }
            parameters.add('%'+director+'%');
            if (year != 0) {
                sql += " and year=?) as m"; //String.format(" and year = "+year);
                parameters.add(year);
            } else sql += ") as m";
        } else if (year != 0) {
            sql += " (select * from movies where "+ titleMatch + " director like ? and year = ?) as m";
            if(a.isPresent()) {
                parameters.add(a.get());
            }
            parameters.add('%'+director+'%');
            parameters.add(year);
        } else {
            sql += " (select * from movies where " + titleMatch + " director like ?) as m";
            if(a.isPresent()) {
                parameters.add(a.get());
            }
            parameters.add('%'+director+'%');
        }
        sql += " left join ratings r on m.id = r.movieId ";
        sql += getOrderBySql(order);
        int newLimit = 5 * limit;
        sql += " limit ? offset ?";
        parameters.add(newLimit);
        parameters.add(offset);
        // System.out.println(sql);
        List<Movie> list = readTemplate.query(sql, parameters.toArray(),new MovieMapper());
        return fillMovies(limit, 0, list);
    }

    private String getOrderBySql(String order) {
        String[] strings = order.split("~");
        String sql = "";
        if(strings.length < 2)
            return " order by title asc, rating asc ";
        switch (strings[0]) {
            case "titleDesc":
                sql += " order by title desc, ";
                break;
            case "ratingAsc":
                sql += " order by rating asc, ";
                break;
            case "ratingDesc":
                sql += " order by rating desc, ";
                break;
            case "titleAsc":
            default:
                sql += " order by title asc, ";
        }
        switch (strings[1]) {
            case "titleDesc":
                sql += "title desc";
                break;
            case "ratingAsc":
                sql += "rating asc";
                break;
            case "ratingDesc":
                sql += "rating desc";
                break;
            case "titleAsc":
            default:
                sql += "title asc";
        }
        return sql;
    }

    @Override
    public Map<String, Object> insertMovie(String movieTitle, int movieYear, String movieDirector, String starName, String genreName){
        HashMap<String, Object> res = new HashMap<>();
        return transactionTemplate.execute(t->{
            try {
                SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(writeTemplate.getDataSource())
                        .withProcedureName("add_movie");

                SqlParameterSource in = new MapSqlParameterSource().addValue("movietitle", movieTitle).addValue("movieyear", movieYear)
                        .addValue("moviedirector", movieDirector).addValue("starname", starName).addValue("genrename", genreName);
                Map<String, Object> out = simpleJdbcCall.execute(in);

                int errCode = (Integer) out.get("errcode");
                System.out.println("errcode: "+ errCode);
                res.putAll(out);
                if (errCode < 0)
                    t.setRollbackOnly();
                return res;
            } catch (Exception e) {
                e.printStackTrace();
                t.setRollbackOnly();
                res.put("errcode", -2);
                return res;
            }
        });
    }

    @Override
    public List<Movie> getTitleSuggestion(String title) {
        String sql = "select id, title from movies where match (title) against (? in boolean mode) limit 10";
        String[] titles = title.split("\\s+");
        Optional<String> a = Arrays.stream(titles).filter(s -> s.length() > 0).map(s -> "+" + s + "*").reduce((s, s2) -> s + " "+s2);
        if(!a.isPresent())
            return null;
        System.out.println("query titles: "+a.get());
        List<Movie> res = readTemplate.query(sql, new Object[]{a.get()}, new BeanPropertyRowMapper<>(Movie.class));
        return res;
    }
}
