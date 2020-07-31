package com.mintyi.fablix.dao;

import com.mintyi.fablix.domain.Column;
import com.mintyi.fablix.domain.Table;

import java.util.List;

public interface MetadataDao {
    List<Table> getAllTables();
    List<Column> getAllColumns(String tableName);
}
