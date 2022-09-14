/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2022 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.jdbc;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JDBCMetaDataHelper {
  private Connection connection;

  private Map<Triple<String, String, String>, List<Column>> columnLookupCache;

  public JDBCMetaDataHelper(Connection connection) {
    setConnection(connection);
  }

  public void setConnection(Connection connection) {
    this.connection = connection;
    this.columnLookupCache = new HashMap<>();
  }

  private List<Column> getOrCreateColumnLookupCache(String schemaName, String tableName,
      String columnName) {
    return getOrCreateColumnLookupCache(new Triple<>(schemaName, tableName, columnName));
  }

  private List<Column> getOrCreateColumnLookupCache(Triple<String, String, String> triple) {
    return columnLookupCache.computeIfAbsent(triple, k -> new ArrayList<>());
  }

  private DatabaseMetaData getMetaData() throws SQLException {
    return connection.getMetaData();
  }

  public void clearColumnLookupCache() {
    columnLookupCache.clear();
  }

  public List<Column> getColumns(Column column) throws SQLException {
    Table table = column.getTable();
    String columnName = column.getColumnName();
    return getColumns(table, columnName);
  }

  public List<Column> getColumns(Table table) throws SQLException {
    return getColumns(table, null);
  }

  private List<Column> getColumns(Table table, String columnName) throws SQLException {
    String schemaName = null;
    String tableName = null;
    if (table != null) {
      schemaName = table.getSchemaName();
      tableName = table.getName();
    }

    return getColumns(schemaName, tableName, columnName);
  }

  public List<Column> getColumns(String schemaName, String tableName, String columnName)
      throws SQLException {
    List<Column> columns = getOrCreateColumnLookupCache(schemaName, tableName, columnName);
    if (!columns.isEmpty()) {
      return columns;
    }

    String schemaPattern = escapePattern(schemaName);
    String tablePattern = escapePattern(tableName);
    String columnPattern = escapePattern(columnName);

    ResultSet rs = getMetaData().getColumns(null, schemaPattern, tablePattern, columnPattern);
    while (rs.next()) {
      String dbSchemaName = rs.getString("TABLE_SCHEM");
      String dbTableName = rs.getString("TABLE_NAME");
      String dbColumnName = rs.getString("COLUMN_NAME");

      columns.add(new Column(new Table(dbSchemaName, dbTableName), dbColumnName));
    }

    return columns;
  }

  private String escapePattern(String str) throws SQLException {
    if (str == null) {
      return null;
    }

    // for details see: https://docs.oracle.com/javase/8/docs/api/java/sql/DatabaseMetaData.html#getSearchStringEscape--
    String escapeStr = getMetaData().getSearchStringEscape();
    String replaceRegex = "(_|%)";
    return str.replaceAll(replaceRegex, escapeStr + "$1");
  }
}
