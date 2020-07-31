package com.mintyi.parser.optim1;

import com.mintyi.parser.entity.Cast;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;

public class CastParser extends com.mintyi.parser.naive.CastParser {
    protected int batchSize;
    public CastParser(JdbcTemplate j, int batchSize) {
        super(j);
        this.batchSize = batchSize;
    }

    @Override
    public void insert(String badFileName) {
        System.out.println("cast size: " + myCast.size());
        initBadFile(badFileName);
        initId();
        filterCast();

        int bsize = Math.min(batchSize, myCast.size());
        int i=0;
        String sql = getStatement(bsize);
        while(i < myCast.size()) {
            System.out.println("now position: " + i);
            Object[] param = prepareSqlParam(i, bsize);
            jdbcTemplate.update(sql, param);
            i += bsize;
            if(myCast.size() - i < bsize) {
                bsize = myCast.size() - i;
                sql = getStatement(bsize);
            }
        }
        System.out.println("insert into stars_in_movies finish: " + myCast.size() + " success, " + failNum + " fail.");
    }

    protected Object[] prepareSqlParam(int i, int bsize) {
        ArrayList<Object> objects = new ArrayList<>(bsize);
        while(bsize > 0) {
            bsize --;

            if(i >= myCast.size()) break;
            Cast ci = myCast.get(i++);
            String starId;
            if (starName2Id.containsKey(ci.getStarName())){
                starId = starName2Id.get(ci.getStarName());
            } else {
                try {
                    List<String> r = jdbcTemplate.queryForList("select id from stars where name = ?", new Object[]{ci.getStarName()}, String.class);
                    if(r.size() > 0)
                        starId = r.get(0);
                    else {
                        starId = (starIdPrefix + starCurrId);
                        jdbcTemplate.update("INSERT INTO stars (id, name) VALUES(?,?)", starId, ci.getStarName());
                        starCurrId ++;
                    }
                    starName2Id.put(ci.getStarName(), starId);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    writeErr(ci);
                    continue;
                }
            }
            objects.add(starId);
            objects.add(ci.getMovieId());
        }
        return objects.toArray();
    }

    protected String getStatement(int bsize) {
        StringBuilder builder = new StringBuilder();
        builder.append("insert ignore into stars_in_movies values ");
        for(int i = 0; i < bsize - 1; ++ i){
            builder.append("(?, ?), ");
        }
        builder.append("(?, ?)");
        return builder.toString();
    }
}
