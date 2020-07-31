package com.mintyi.parser.optim2;

import org.springframework.jdbc.core.JdbcTemplate;

public class StarParser extends com.mintyi.parser.optim1.StarParser{
    public StarParser(JdbcTemplate j, int batchSize) {
        super(j, batchSize);
    }
    @Override
    protected String getStatement(int size) {
        StringBuilder builder = new StringBuilder();
        builder.append("insert into tmp_stars values ");
        for(int i = 0; i < size - 1; ++ i){
            builder.append("(?, ?, ?), ");
        }
        builder.append("(?, ?, ?)");
        return builder.toString();
    }
    @Override
    public void insert(String badFileName) {
        System.out.println("star size: " + myStar.size());
        initBadFile(badFileName);
        initId();
        createTmpTable();
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
        copyTmpTable();
        System.out.println("insert into star finish: " + myStar.size() + " success, " + failNum + " fail.");
    }

    private void copyTmpTable() {
        jdbcTemplate.execute("insert ignore into stars select * from tmp_stars");
        jdbcTemplate.execute("drop temporary table if exists tmp_stars");
    }

    private void createTmpTable() {
        jdbcTemplate.execute("create temporary table tmp_stars engine memory as select * from stars where 1=2;");
    }
}
