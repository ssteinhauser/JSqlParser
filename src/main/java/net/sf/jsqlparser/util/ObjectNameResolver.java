/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util;

import net.sf.jsqlparser.Model;
import net.sf.jsqlparser.expression.AllValue;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.values.ValuesStatement;
import net.sf.jsqlparser.util.jdbc.JDBCMetaDataHelper;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resolution of all object names in a given statement.
 */
public class ObjectNameResolver extends StatementsTraverser {

  private JDBCMetaDataHelper jdbcMetaDataHelper;

  private String openSchema;

  private final ModelStack modelStack;

  private Map<Table, List<Column>> temporaryTables;

  public ObjectNameResolver(Connection connection) {
    this(connection, null);
  }

  public ObjectNameResolver(Connection connection, String openSchema) {
    this(connection, openSchema, null);
  }

  public ObjectNameResolver(Connection connection, String openSchema,
      Map<Table, List<Column>> temporaryTables) {
    this.jdbcMetaDataHelper = new JDBCMetaDataHelper(connection);
    this.openSchema = openSchema;
    this.temporaryTables = new HashMap<>();
    if (temporaryTables != null) {
      putAllTemporaryTables(temporaryTables);
    }

    this.modelStack = new ModelStack();
  }

  /**
   * Resolve all object names to fully qualified objects
   *
   * @param statement The statement to be resolved.
   */
  public void resolve(Statement statement) {
    statement.accept(this);
  }

  /**
   * Resolve all object names to fully qualified objects
   *
   * @param statements The statements to be resolved.
   */
  public void resolve(Statements statements) {
    statements.accept(this);
  }

  public void setConnection(Connection connection) {
    jdbcMetaDataHelper.setConnection(connection);
  }

  public void setOpenSchema(String openSchema) {
    this.openSchema = openSchema;
  }

  public String getOpenSchema() {
    return openSchema;
  }

  public void setTemporaryTables(Map<Table, List<Column>> temporaryTables) {
    this.temporaryTables = temporaryTables;
  }

  public List<Column> putTemporaryTable(Table table, List<Column> columns) {
    return temporaryTables.put(table, columns);
  }

  public void putAllTemporaryTables(Map<Table, List<Column>> temporaryTables) {
    this.temporaryTables.putAll(temporaryTables);
  }

  public List<Column> removeTemporaryTable(Table table) {
    return temporaryTables.remove(table);
  }

  public boolean removeTemporaryTable(Table table, List<Column> columns) {
    return temporaryTables.remove(table, columns);
  }

  public void clearTemporaryTables() {
    temporaryTables.clear();
  }

  private boolean needsToBeResolved(Model model) {
    if (model instanceof Table) {
      return needsToBeResolved((Table) model);
    } else if (model instanceof Column) {
      return needsToBeResolved((Column) model);
    } else if (model instanceof AllColumns) {
      return needsToBeResolved((AllColumns) model);
    } else if (model instanceof AllTableColumns) {
      return needsToBeResolved((AllTableColumns) model);
    } else if (model instanceof AllValue) {
      return needsToBeResolved((AllValue) model);
    }

    throw getUnsupportedException(model);
  }

  private boolean needsToBeResolved(Table table) {
    String schemaName = table.getSchemaName();
    return isNullOrEmpty(schemaName);
  }

  private boolean needsToBeResolved(Column column) {
    Table table = column.getTable();
    return needsToBeResolved(table);
  }

  private boolean needsToBeResolved(AllColumns allColumns) {
    throw getUnsupportedException(allColumns);
  }

  private boolean needsToBeResolved(AllTableColumns allTableColumns) {
    throw getUnsupportedException(allTableColumns);
  }

  private boolean needsToBeResolved(AllValue allValue) {
    throw getUnsupportedException(allValue);
  }

  private void resolve(Model model) {
    if (model instanceof Table) {
      resolve((Table) model);
    } else if (model instanceof Column) {
      resolve((Column) model);
    } else if (model instanceof AllColumns) {
      resolve((AllColumns) model);
    } else if (model instanceof AllTableColumns) {
      resolve((AllTableColumns) model);
    } else if (model instanceof AllValue) {
      resolve((AllValue) model);
    }

    throw getUnsupportedException(model);
  }

  private void resolve(Table table) {
    // if schema name is still null or empty
    if (isNullOrEmpty(table.getSchemaName())) {
      table.setSchemaName(openSchema);
    }
  }

  private void resolve(Column column) {
    Table table = column.getTable();
    if (table != null) {
      resolve(table);
    }

    // TODO implement case that table name is alias
    // TODO implement case that no table is set
    // TODO implement case that table name is set and no alias, but no schema is set
    throw getUnsupportedException(column);
  }

  private void resolve(AllColumns allColumns) {
    throw getUnsupportedException(allColumns);
  }

  private void resolve(AllTableColumns allTableColumns) {
    throw getUnsupportedException(allTableColumns);
  }

  private void resolve(AllValue allValue) {
    throw getUnsupportedException(allValue);
  }

  private void resolveIfNeeded(Model model) {
    if (needsToBeResolved(model)) {
      resolve(model);
    }
  }

  private boolean isNullOrEmpty(String str) {
    return str == null || str.isEmpty();
  }

  @Override
  public void visit(WithItem withItem) {
    modelStack.push(withItem);
    super.visit(withItem);
    modelStack.pop();
  }

  @Override
  public void visit(PlainSelect plainSelect) {
    modelStack.push(plainSelect);
    super.visit(plainSelect);
    modelStack.pop();
  }

  @Override
  public void visit(ValuesList valuesList) {
    modelStack.push(valuesList);
    super.visit(valuesList);
    modelStack.pop();
  }

  @Override
  public void visit(ValuesStatement valuesStatement) {
    modelStack.push(valuesStatement);
    super.visit(valuesStatement);
    modelStack.pop();
  }

  @Override
  public void visit(AllColumns allColumns) {
    super.visit(allColumns);
    resolveIfNeeded(allColumns);
  }

  @Override
  public void visit(AllTableColumns allTableColumns) {
    super.visit(allTableColumns);
    resolveIfNeeded(allTableColumns);
  }

  @Override
  public void visit(AllValue allValue) {
    super.visit(allValue);
    resolveIfNeeded(allValue);
  }

  @Override
  public void visit(Table table) {
    super.visit(table);
    resolveIfNeeded(table);
  }

  @Override
  public void visit(Column column) {
    super.visit(column);
    resolveIfNeeded(column);
  }

  private static <T> UnsupportedOperationException getUnsupportedException(T type) {
    return getUnsupportedException(ObjectNameResolver.class, type);
  }
}
