package com.mintyi.fablix.dao.Impl;

import com.mintyi.fablix.dao.MetadataDao;
import com.mintyi.fablix.domain.Column;
import com.mintyi.fablix.domain.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MetadataDaoImpl implements MetadataDao {
    @Autowired
    JdbcTemplate readTemplate;
    @Autowired
    JdbcTemplate writeTemplate;
    @Override
    public List<Column> getAllColumns(String tableName) {
        List<Column> res = null;
        ResultSet resultSet = null;
        try {
            DatabaseMetaData metaData = readTemplate.getDataSource().getConnection().getMetaData();
            res = new ArrayList<>();
            resultSet = metaData.getColumns(null, null, tableName, "%");
            while(resultSet.next()) {
                Column column = new Column();
                column.setName(resultSet.getString("COLUMN_NAME"));
                column.setTypeName(resultSet.getString("TYPE_NAME"));
                res.add(column);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            if(resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        return res;
    }
    @Override
    public List<Table> getAllTables() {
        List<Table> res = new ArrayList<>();
        ResultSet resultSet = null;
        try {
            DatabaseMetaData metaData = readTemplate.getDataSource().getConnection().getMetaData();
            resultSet = metaData.getTables(null, null, "%", new String[]{"TABLE"});
            while(resultSet.next()) {
                Table tbl = new Table();
                tbl.setName(resultSet.getString("TABLE_NAME"));
                res.add(tbl);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            if(resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
//        res.forEach(table -> {
//            System.out.println(table.getName());
//        });
        return res;
    }
}
