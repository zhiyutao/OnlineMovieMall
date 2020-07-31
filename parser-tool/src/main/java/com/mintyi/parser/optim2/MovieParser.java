package com.mintyi.parser.optim2;

import com.mintyi.parser.entity.MovieInfo;
import org.springframework.jdbc.core.JdbcTemplate;

public class MovieParser extends com.mintyi.parser.optim1.MovieParser{
    public MovieParser(JdbcTemplate j, int batchSize) {
        super(j, batchSize);
    }

    @Override
    protected String getMovieStatement(int size) {
        StringBuilder builder = new StringBuilder();
        builder.append("insert ignore into tmp_movies values ");
        for(int i = 0; i < size - 1; ++ i){
            builder.append("(?, ?, ?, ?), ");
        }
        builder.append("(?, ?, ?, ?)");
        return builder.toString();
    }

    @Override
    protected String getRelationStatement(int i, int bsize) {
        StringBuilder builder = new StringBuilder();
        builder.append("insert ignore into tmp_genres_in_movies values ");
        for(int j = 0; j < bsize; ++ j) {
            MovieInfo m = myMovie.get(i+j);
            for(int k = 0; k < m.getGenres().size(); ++ k)
                builder.append("(?, ?), ");
        }
        builder.delete(builder.length()-2, builder.length());
        return builder.toString();
    }

    @Override
    public void insert(String badFileName) {
        System.out.println("movie size: " + myMovie.size());
        initBadFile(badFileName);
        initId();
        filterMyMovie();
        createTmpTable();
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
        copyTmpTable();
        System.out.println("insert into movie finish: " + myMovie.size() + " success, " + failNum + " fail.");
    }
    private void copyTmpTable() {
        jdbcTemplate.execute("insert ignore into movies select * from tmp_movies");
        jdbcTemplate.execute("insert ignore into genres_in_movies select * from tmp_genres_in_movies");
        jdbcTemplate.execute("drop temporary table if exists tmp_movies, tmp_genres_in_movies");
    }

    private void createTmpTable() {
        jdbcTemplate.execute("create temporary table tmp_movies engine memory as select * from movies where 1=2;");
        jdbcTemplate.execute("create temporary table tmp_genres_in_movies engine memory as select * from genres_in_movies where 1=2;");
    }
}
