package com.mintyi.parser.optim1;

import com.mintyi.parser.entity.Star;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;

public class StarParser extends com.mintyi.parser.naive.StarParser {
    protected int batchSize;
    protected String idPrefix;
    protected int currId;

    public StarParser(JdbcTemplate j, int batchSize) {
        super(j);
        this.batchSize = batchSize;
    }

    protected String getStatement(int size) {
        StringBuilder builder = new StringBuilder();
        builder.append("insert into stars values ");
        for(int i = 0; i < size - 1; ++ i){
            builder.append("(?, ?, ?), ");
        }
        builder.append("(?, ?, ?)");
        return builder.toString();
    }
    protected Object[] prepareSqlParam(int i, int bsize) {
        ArrayList<Object> objects = new ArrayList<>(bsize);
        while(bsize > 0) {
            bsize --;
            if(i >= myStar.size()) break;
            Star si = myStar.get(i);
            si.setId(idPrefix + currId);
            currId ++;
            objects.add(si.getId());
            objects.add(si.getName());
            objects.add(si.getBirthYear());
            i ++;
        }
        return objects.toArray();
    }
    protected void initId() {
        String s = jdbcTemplate.queryForObject("select max(id) from stars", String.class);
        idPrefix = s.substring(0, 2);
        currId = Integer.parseInt(s.substring(2)) + 1;
        System.out.println("maxId: " + s);
    }
    @Override
    public void insert(String badFileName) {
        System.out.println("star size: " + myStar.size());
        initBadFile(badFileName);
        initId();
        int bsize = Math.min(batchSize, myStar.size());
        int i=0;
        String sql = getStatement(bsize);
        while(i < myStar.size()) {
            Object[] param = prepareSqlParam(i, bsize);
            jdbcTemplate.update(sql, param);

            i += bsize;
            if(myStar.size() - i < bsize) {
                bsize = myStar.size() - i;
                sql = getStatement(bsize);
            }
        }
        System.out.println("insert into star finish: " + myStar.size() + " success, " + failNum + " fail.");
    }
}
