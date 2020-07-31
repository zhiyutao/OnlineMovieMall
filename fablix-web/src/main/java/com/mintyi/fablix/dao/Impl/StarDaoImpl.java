package com.mintyi.fablix.dao.Impl;

import com.mintyi.fablix.dao.StarDao;
import com.mintyi.fablix.domain.Movie;
import com.mintyi.fablix.domain.Star;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;

@Repository
public class StarDaoImpl implements StarDao {
    @Autowired
    JdbcTemplate readTemplate;
    @Autowired
    JdbcTemplate writeTemplate;
    @Autowired
    TransactionTemplate transactionTemplate;


    @Override
    public Star queryStarById(String id) {
        List<Star> list = readTemplate.query("select * from stars where id = ?", new Object[]{id}, new BeanPropertyRowMapper<>(Star.class));
        if(list != null && list.size() > 0){
            return list.get(0);
        }
        return null;
    }

    @Override
    public List<Movie> getStarInMovie(String id) {
        String sql = "select id, title, year, director from movies join (select movieId from stars_in_movies where starId = ?) as a on id = a.movieId order by year desc, title asc;";
        List<Movie> list = readTemplate.query(sql, new Object[]{id}, new BeanPropertyRowMapper<>(Movie.class));
        return list;
    }

    @Override
    public String getMaxId(){
        String sql = "select max(id) from stars";
        String id = (String)readTemplate.queryForObject(sql,String.class);
        return id;
    }

    @Override
    public String insertStar(String name, Integer year){

        return transactionTemplate.execute(t->{
            try {
                SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(writeTemplate.getDataSource())
                        .withProcedureName("insert_star");

                SqlParameterSource in = new MapSqlParameterSource().addValue("insertname", name).addValue("insertyear", year);
                Map<String, Object> out = simpleJdbcCall.execute(in);

                String insertId = (String) out.get("insertid");
                System.out.println("insertID: "+insertId);
                return insertId;
            } catch (Exception e) {
                t.setRollbackOnly();
                return null;
            }
        });
    }
}
