package com.mintyi.parser.optim1;

import com.mintyi.parser.entity.MovieInfo;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class MovieParser extends com.mintyi.parser.naive.MovieParser {
    protected int batchSize;

    public MovieParser(JdbcTemplate j, int batchSize) {
        super(j);
        this.batchSize = batchSize;
    }
    protected String getMovieStatement(int size) {
        StringBuilder builder = new StringBuilder();
        builder.append("insert ignore into movies values ");
        for(int i = 0; i < size - 1; ++ i){
            builder.append("(?, ?, ?, ?), ");
        }
        builder.append("(?, ?, ?, ?)");
        return builder.toString();
    }
    protected Object[] prepareSqlParam(int i, int bsize) {
        ArrayList<Object> objects = new ArrayList<>(bsize);
        while(bsize > 0) {
            bsize --;
            if(i >= myMovie.size()) break;
            MovieInfo si = myMovie.get(i);
            objects.add(si.getId());
            objects.add(si.getTitle());
            objects.add(si.getYear());
            objects.add(si.getDirector());
            i ++;
            // insert genres
            for(String g: si.getGenres())
                getGenId(g);
        }
        return objects.toArray();
    }
    protected void filterMyMovie() {
        myMovie = myMovie.stream().filter(m -> {
            if(m.getId() == null) {
                m.setId(idPrefix + currId);
                currId ++;
            }
            if(m.getTitle() == null || m.getYear() == null || m.getDirector() == null) {
                writeErr(m);
                failNum ++;
                return false;
            }
            return true;
        }).collect(Collectors.toList());
    }
    @Override
    public void insert(String badFileName) {
        System.out.println("movie size: " + myMovie.size());
        initBadFile(badFileName);
        initId();
        filterMyMovie();
        int bsize = Math.min(batchSize, myMovie.size());
        int i=0;
        String sql = getMovieStatement(bsize);
        while(i < myMovie.size()) {
            // insert into movies, genres
            Object[] param = prepareSqlParam(i, bsize);
            jdbcTemplate.update(sql, param);
            // insert into genres_in_movies
            String relationSql = getRelationStatement(i, bsize);
            param = prepareRelationParam(i, bsize);
            jdbcTemplate.update(relationSql, param);

            i += bsize;
            if(myMovie.size() - i < bsize) {
                bsize = myMovie.size() - i;
                sql = getMovieStatement(bsize);
            }
        }
        System.out.println("insert into movie finish: " + myMovie.size() + " success, " + failNum + " fail.");
    }

    protected String getRelationStatement(int i, int bsize) {
        StringBuilder builder = new StringBuilder();
        builder.append("insert ignore into genres_in_movies values ");
        for(int j = 0; j < bsize; ++ j) {
            MovieInfo m = myMovie.get(i+j);
            for(int k = 0; k < m.getGenres().size(); ++ k)
                builder.append("(?, ?), ");
        }
        builder.delete(builder.length()-2, builder.length());
        return builder.toString();
    }

    protected Object[] prepareRelationParam(int i, int bsize) {
        ArrayList<Object> objects = new ArrayList<>(bsize);
        for(int j = 0; j < bsize; ++ j) {
            MovieInfo m = myMovie.get(i + j);
            for(String genre: m.getGenres()){
                objects.add(getGenId(genre));
                objects.add(m.getId());
            }
        }
        return objects.toArray();
    }
}
