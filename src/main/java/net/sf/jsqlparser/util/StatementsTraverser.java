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

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.conditional.XorExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.*;
import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.statement.alter.*;
import net.sf.jsqlparser.statement.alter.sequence.AlterSequence;
import net.sf.jsqlparser.statement.analyze.Analyze;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.schema.CreateSchema;
import net.sf.jsqlparser.statement.create.sequence.CreateSequence;
import net.sf.jsqlparser.statement.create.synonym.CreateSynonym;
import net.sf.jsqlparser.statement.create.table.*;
import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.grant.Grant;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.insert.InsertConflictAction;
import net.sf.jsqlparser.statement.insert.InsertConflictTarget;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.merge.MergeInsert;
import net.sf.jsqlparser.statement.merge.MergeUpdate;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.show.ShowTablesStatement;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;
import net.sf.jsqlparser.statement.upsert.Upsert;
import net.sf.jsqlparser.statement.values.ValuesStatement;

import java.util.*;

/**
 * Traversal of a statement - visit all parser objects.
 */
@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ExcessiveMethodLength"})
public class StatementsTraverser
    implements ExpressionVisitor, ItemsListVisitor, StatementVisitor, FromItemVisitor, PivotVisitor,
    GroupByVisitor, OrderByVisitor, SelectItemVisitor, SelectVisitor {

  public StatementsTraverser() {
  }

  /**
   * Traversal a statement - visit all parser objects.
   *
   * @param statement The statement to be traversed.
   */
  public void traverse(Statement statement) {
    statement.accept(this);
  }

  /**
   * Traversal all statement - visit all parser objects.
   *
   * @param statements The statements to be traversed.
   */
  public void traverse(Statements statements) {
    statements.accept(this);
  }

  @Override
  public void visit(Statements stmts) {
    for (Statement stmt : stmts.getStatements()) {
      stmt.accept(this);
    }
  }

  @Override
  public void visit(Select select) {
    List<WithItem> withItemsList = select.getWithItemsList();
    if (withItemsList != null) {
      for (WithItem withItem : withItemsList) {
        withItem.accept(this);
      }
    }

    SelectBody selectBody = select.getSelectBody();
    if (selectBody != null) {
      selectBody.accept(this);
    }
  }

  @Override
  public void visit(WithItem withItem) {
    List<SelectItem> selectItemsList = withItem.getWithItemList();
    if (selectItemsList != null) {
      for (SelectItem selectItem : selectItemsList) {
        selectItem.accept(this);
      }
    }

    ItemsList itemsList = withItem.getItemsList();
    if (itemsList != null) {
      itemsList.accept(this);
    }

    SubSelect subSelect = withItem.getSubSelect();
    if (subSelect != null) {
      visit(subSelect);
    }
  }

  @Override
  public void visit(PlainSelect plainSelect) {
    OracleHint oracleHint = plainSelect.getOracleHint();
    if (oracleHint != null) {
      oracleHint.accept(this);
    }

    Distinct distinct = plainSelect.getDistinct();
    if (distinct != null) {
      visit(distinct);
    }

    Top top = plainSelect.getTop();
    if (top != null) {
      visit(top);
    }

    List<SelectItem> selectItems = plainSelect.getSelectItems();
    if (selectItems != null) {
      for (SelectItem item : selectItems) {
        item.accept(this);
      }
    }

    List<Table> intoTables = plainSelect.getIntoTables();
    if (intoTables != null) {
      for (Table table : intoTables) {
        table.accept(this);
      }
    }

    FromItem fromItem = plainSelect.getFromItem();
    if (fromItem != null) {
      fromItem.accept(this);
    }

    List<Join> joins = plainSelect.getJoins();
    if (joins != null) {
      for (Join join : joins) {
        join.getRightItem().accept(this);
      }
    }

    Expression where = plainSelect.getWhere();
    if (where != null) {
      where.accept(this);
    }

    OracleHierarchicalExpression oracleHierarchical = plainSelect.getOracleHierarchical();
    if (oracleHierarchical != null) {
      oracleHierarchical.accept(this);
    }

    GroupByElement groupBy = plainSelect.getGroupBy();
    if (groupBy != null) {
      groupBy.accept(this);
    }

    Expression having = plainSelect.getHaving();
    if (having != null) {
      having.accept(this);
    }

    List<WindowDefinition> windowDefinitions = plainSelect.getWindowDefinitions();
    if (windowDefinitions != null) {
      for (WindowDefinition windowDefinition : windowDefinitions) {
        visit(windowDefinition);
      }
    }

    Limit limit = plainSelect.getLimit();
    if (limit != null) {
      visit(limit);
    }

    Offset offset = plainSelect.getOffset();
    if (offset != null) {
      visit(offset);
    }

    Fetch fetch = plainSelect.getFetch();
    if (fetch != null) {
      visit(fetch);
    }

    WithIsolation withIsolation = plainSelect.getWithIsolation();
    if (withIsolation != null) {
      visit(withIsolation);
    }

    Table forUpdateTable = plainSelect.getForUpdateTable();
    if (forUpdateTable != null) {
      forUpdateTable.accept(this);
    }

    Wait wait = plainSelect.getWait();
    if (wait != null) {
      visit(wait);
    }

    OptimizeFor optimizeFor = plainSelect.getOptimizeFor();
    if (optimizeFor != null) {
      visit(optimizeFor);
    }
  }

  public void visit(Distinct distinct) {
    List<SelectItem> onSelectItems = distinct.getOnSelectItems();
    if (onSelectItems != null) {
      for (SelectItem selectItem : onSelectItems) {
        selectItem.accept(this);
      }
    }
  }

  public void visit(Top top) {
    Expression expr = top.getExpression();
    if (expr != null) {
      expr.accept(this);
    }
  }

  public void visit(WindowDefinition windowDefinition) {
    ExpressionList exprList = windowDefinition.getPartitionExpressionList();
    if (exprList != null) {
      exprList.accept(this);
    }

    List<OrderByElement> orderByElements = windowDefinition.getOrderByElements();
    if (orderByElements != null) {
      for (OrderByElement orderByElement : orderByElements) {
        orderByElement.accept(this);
      }
    }

    WindowElement windowElement = windowDefinition.getWindowElement();
    if (windowElement != null) {
      visit(windowElement);
    }
  }

  public void visit(WindowElement windowElement) {
    WindowOffset offset = windowElement.getOffset();
    if (offset != null) {
      visit(offset);
    }

    WindowRange range = windowElement.getRange();
    if (range != null) {
      visit(range);
    }
  }

  public void visit(WindowOffset windowOffset) {
    Expression expression = windowOffset.getExpression();
    if (expression != null) {
      expression.accept(this);
    }
  }

  public void visit(WindowRange windowRange) {
    WindowOffset start = windowRange.getStart();
    visit(start);

    WindowOffset end = windowRange.getEnd();
    visit(end);
  }

  public void visit(Limit limit) {
    Expression offset = limit.getOffset();
    if (offset != null) {
      offset.accept(this);
    }

    Expression rowCount = limit.getRowCount();
    if (rowCount != null) {
      rowCount.accept(this);
    }
  }

  public void visit(Offset offset) {
    Expression offsetExpr = offset.getOffset();
    if (offsetExpr != null) {
      offsetExpr.accept(this);
    }
  }

  public void visit(Fetch fetch) {
    // nothing to do
  }

  public void visit(WithIsolation withIsolation) {
    // nothing to do
  }

  public void visit(Wait wait) {
    // nothing to do
  }

  public void visit(OptimizeFor optimizeFor) {
    // nothing to do
  }

  @Override
  public void visit(Table table) {
    Alias alias = table.getAlias();
    if (alias != null) {
      visit(alias);
    }

    Pivot pivot = table.getPivot();
    if (pivot != null) {
      pivot.accept(this);
    }

    UnPivot unPivot = table.getUnPivot();
    if (unPivot != null) {
      unPivot.accept(this);
    }

    MySQLIndexHint mySQLIndexHint = table.getIndexHint();
    if (mySQLIndexHint != null) {
      visit(mySQLIndexHint);
    }

    SQLServerHints sqlServerHints = table.getSqlServerHints();
    if (sqlServerHints != null) {
      visit(sqlServerHints);
    }
  }

  public void visit(Alias alias) {
    List<Alias.AliasColumn> aliasColumns = alias.getAliasColumns();
    if (aliasColumns != null) {
      for (Alias.AliasColumn aliasColumn : aliasColumns) {
        visit(aliasColumn);
      }
    }
  }

  public void visit(Alias.AliasColumn aliasColumn) {
    // nothing to do
  }

  public void visit(MySQLIndexHint mySQLIndexHint) {
    // nothing to do
  }

  public void visit(SQLServerHints sqlServerHints) {
    // nothing to do
  }

  @Override
  public void visit(SubSelect subSelect) {
    if (subSelect.getWithItemsList() != null) {
      for (WithItem withItem : subSelect.getWithItemsList()) {
        withItem.accept(this);
      }
    }

    subSelect.getSelectBody().accept(this);

    Alias alias = subSelect.getAlias();
    if (alias != null) {
      visit(alias);
    }

    Pivot pivot = subSelect.getPivot();
    if (pivot != null) {
      pivot.accept(this);
    }

    UnPivot unPivot = subSelect.getUnPivot();
    if (unPivot != null) {
      unPivot.accept(this);
    }
  }

  @Override
  public void visit(Between between) {
    between.getLeftExpression().accept(this);
    between.getBetweenExpressionStart().accept(this);
    between.getBetweenExpressionEnd().accept(this);
  }

  @Override
  public void visit(Column column) {
    Table table = column.getTable();
    if (table != null) {
      table.accept(this);
    }
  }

  @Override
  public void visit(OverlapsCondition overlapsCondition) {
    overlapsCondition.getLeft().accept(this);
    overlapsCondition.getRight().accept(this);
  }

  public void visitBinaryExpression(BinaryExpression binaryExpression) {
    binaryExpression.getLeftExpression().accept(this);
    binaryExpression.getRightExpression().accept(this);
  }

  @Override
  public void visit(Addition addition) {
    visitBinaryExpression(addition);
  }

  @Override
  public void visit(AndExpression andExpression) {
    visitBinaryExpression(andExpression);
  }

  @Override
  public void visit(Division division) {
    visitBinaryExpression(division);
  }

  @Override
  public void visit(IntegerDivision division) {
    visitBinaryExpression(division);
  }

  @Override
  public void visit(DoubleValue doubleValue) {
    // nothing to do
  }

  @Override
  public void visit(EqualsTo equalsTo) {
    visitBinaryExpression(equalsTo);
  }

  @Override
  public void visit(Function function) {
    ExpressionList expressionList = function.getParameters();
    if (expressionList != null) {
      expressionList.accept(this);
    }

    List<OrderByElement> orderByElements = function.getOrderByElements();
    if (orderByElements != null) {
      for (OrderByElement orderByElement : orderByElements) {
        orderByElement.accept(this);
      }
    }

    NamedExpressionList namedExpressionList = function.getNamedParameters();
    if (namedExpressionList != null) {
      namedExpressionList.accept(this);
    }

    Expression attribute = function.getAttribute();
    if (attribute != null) {
      attribute.accept(this);
    }

    KeepExpression keepExpression = function.getKeep();
    if (keepExpression != null) {
      keepExpression.accept(this);
    }
  }

  @Override
  public void visit(GreaterThan greaterThan) {
    visitBinaryExpression(greaterThan);
  }

  @Override
  public void visit(GreaterThanEquals greaterThanEquals) {
    visitBinaryExpression(greaterThanEquals);
  }

  @Override
  public void visit(InExpression inExpression) {
    Expression leftExpression = inExpression.getLeftExpression();
    if (leftExpression != null) {
      leftExpression.accept(this);
    }

    Expression rightExpression = inExpression.getRightExpression();
    if (rightExpression != null) {
      rightExpression.accept(this);
    }

    ItemsList rightItemsList = inExpression.getRightItemsList();
    if (rightItemsList != null) {
      rightItemsList.accept(this);
    }
  }

  @Override
  public void visit(FullTextSearch fullTextSearch) {
    List<Column> matchColumns = fullTextSearch.getMatchColumns();
    if (matchColumns != null) {
      for (Column column : matchColumns) {
        column.accept(this);
      }
    }

    Expression againstValue = fullTextSearch.getAgainstValue();
    if (againstValue != null) {
      againstValue.accept(this);
    }
  }

  @Override
  public void visit(SignedExpression signedExpression) {
    signedExpression.getExpression().accept(this);
  }

  @Override
  public void visit(IsNullExpression isNullExpression) {
    Expression leftExpression = isNullExpression.getLeftExpression();
    if (leftExpression != null) {
      leftExpression.accept(this);
    }
  }

  @Override
  public void visit(IsBooleanExpression isBooleanExpression) {
    Expression leftExpression = isBooleanExpression.getLeftExpression();
    if (leftExpression != null) {
      leftExpression.accept(this);
    }
  }

  @Override
  public void visit(JdbcParameter jdbcParameter) {
    // nothing to do
  }

  @Override
  public void visit(LikeExpression likeExpression) {
    visitBinaryExpression(likeExpression);
  }

  @Override
  public void visit(ExistsExpression existsExpression) {
    existsExpression.getRightExpression().accept(this);
  }

  @Override
  public void visit(LongValue longValue) {
    // nothing to do
  }

  @Override
  public void visit(MinorThan minorThan) {
    visitBinaryExpression(minorThan);
  }

  @Override
  public void visit(MinorThanEquals minorThanEquals) {
    visitBinaryExpression(minorThanEquals);
  }

  @Override
  public void visit(Multiplication multiplication) {
    visitBinaryExpression(multiplication);
  }

  @Override
  public void visit(NotEqualsTo notEqualsTo) {
    visitBinaryExpression(notEqualsTo);
  }

  @Override
  public void visit(NullValue nullValue) {
    // nothing to do
  }

  @Override
  public void visit(OrExpression orExpression) {
    visitBinaryExpression(orExpression);
  }

  @Override
  public void visit(XorExpression xorExpression) {
    visitBinaryExpression(xorExpression);
  }

  @Override
  public void visit(Parenthesis parenthesis) {
    parenthesis.getExpression().accept(this);
  }

  @Override
  public void visit(StringValue stringValue) {
    // nothing to do
  }

  @Override
  public void visit(Subtraction subtraction) {
    visitBinaryExpression(subtraction);
  }

  @Override
  public void visit(NotExpression notExpr) {
    notExpr.getExpression().accept(this);
  }

  @Override
  public void visit(BitwiseRightShift expr) {
    visitBinaryExpression(expr);
  }

  @Override
  public void visit(BitwiseLeftShift expr) {
    visitBinaryExpression(expr);
  }

  @Override
  public void visit(ExpressionList expressionList) {
    for (Expression expression : expressionList.getExpressions()) {
      expression.accept(this);
    }
  }

  @Override
  public void visit(NamedExpressionList namedExpressionList) {
    for (Expression expression : namedExpressionList.getExpressions()) {
      expression.accept(this);
    }
  }

  @Override
  public void visit(DateValue dateValue) {
    // nothing to do
  }

  @Override
  public void visit(TimestampValue timestampValue) {
    // nothing to do
  }

  @Override
  public void visit(TimeValue timeValue) {
    // nothing to dos
  }

  @Override
  public void visit(CaseExpression caseExpression) {
    Expression switchExpression = caseExpression.getSwitchExpression();
    if (switchExpression != null) {
      switchExpression.accept(this);
    }

    List<WhenClause> whenClauses = caseExpression.getWhenClauses();
    if (whenClauses != null) {
      for (WhenClause when : whenClauses) {
        when.accept(this);
      }
    }

    Expression elseExpr = caseExpression.getElseExpression();
    if (elseExpr != null) {
      elseExpr.accept(this);
    }
  }

  @Override
  public void visit(WhenClause whenClause) {
    Expression whenExpr = whenClause.getWhenExpression();
    if (whenExpr != null) {
      whenExpr.accept(this);
    }

    Expression thenExpr = whenClause.getThenExpression();
    if (thenExpr != null) {
      thenExpr.accept(this);
    }
  }

  @Override
  public void visit(AnyComparisonExpression anyComparisonExpression) {
    AnyType anyType = anyComparisonExpression.getAnyType();
    if (anyType != null) {
      visit(anyType);
    }

    SubSelect subSelect = anyComparisonExpression.getSubSelect();
    if (subSelect != null) {
      visit(subSelect);
    }

    ItemsList itemsList = anyComparisonExpression.getItemsList();
    if (itemsList != null) {
      itemsList.accept(this);
    }
  }

  public void visit(AnyType anyType) {
    // nothing to do
  }

  @Override
  public void visit(SubJoin subjoin) {
    FromItem left = subjoin.getLeft();
    if (left != null) {
      left.accept(this);
    }

    List<Join> joinList = subjoin.getJoinList();
    for (Join join : joinList) {
      visit(join);
    }

    Alias alias = subjoin.getAlias();
    if (alias != null) {
      visit(alias);
    }

    Pivot pivot = subjoin.getPivot();
    if (pivot != null) {
      pivot.accept(this);
    }

    UnPivot unPivot = subjoin.getUnPivot();
    if (unPivot != null) {
      unPivot.accept(this);
    }
  }

  public void visit(Join join) {
    FromItem right = join.getRightItem();
    if (right != null) {
      right.accept(this);
    }

    KSQLJoinWindow ksqlJoinWindow = join.getJoinWindow();
    if (ksqlJoinWindow != null) {
      visit(ksqlJoinWindow);
    }

    Collection<Expression> onExpressions = join.getOnExpressions();
    if (onExpressions != null) {
      for (Expression onExpression : onExpressions) {
        onExpression.accept(this);
      }
    }

    List<Column> usingColumns = join.getUsingColumns();
    if (usingColumns != null) {
      for (Column usingColumn : usingColumns) {
        usingColumn.accept(this);
      }
    }
  }

  public void visit(KSQLJoinWindow ksqlJoinWindow) {
    // nothing to do
  }

  @Override
  public void visit(Concat concat) {
    visitBinaryExpression(concat);
  }

  @Override
  public void visit(Matches matches) {
    visitBinaryExpression(matches);
  }

  @Override
  public void visit(BitwiseAnd bitwiseAnd) {
    visitBinaryExpression(bitwiseAnd);
  }

  @Override
  public void visit(BitwiseOr bitwiseOr) {
    visitBinaryExpression(bitwiseOr);
  }

  @Override
  public void visit(BitwiseXor bitwiseXor) {
    visitBinaryExpression(bitwiseXor);
  }

  @Override
  public void visit(CastExpression cast) {
    Expression leftExpression = cast.getLeftExpression();
    if (leftExpression != null) {
      leftExpression.accept(this);
    }

    RowConstructor rowConstructor = cast.getRowConstructor();
    if (rowConstructor != null) {
      rowConstructor.accept(this);
    }

    ColDataType colDataType = cast.getType();
    if (colDataType != null) {
      visit(colDataType);
    }
  }

  public void visit(ColDataType colDataType) {
    // nothing to do
  }

  @Override
  public void visit(TryCastExpression cast) {
    Expression leftExpression = cast.getLeftExpression();
    if (leftExpression != null) {
      leftExpression.accept(this);
    }

    RowConstructor rowConstructor = cast.getRowConstructor();
    if (rowConstructor != null) {
      rowConstructor.accept(this);
    }

    ColDataType colDataType = cast.getType();
    if (colDataType != null) {
      visit(colDataType);
    }
  }

  @Override
  public void visit(Modulo modulo) {
    visitBinaryExpression(modulo);
  }

  @Override
  public void visit(AnalyticExpression analytic) {
    Expression expr = analytic.getExpression();
    if (expr != null) {
      expr.accept(this);
    }

    Expression offset = analytic.getOffset();
    if (offset != null) {
      offset.accept(this);
    }

    Expression defaultValue = analytic.getDefaultValue();
    if (defaultValue != null) {
      defaultValue.accept(this);
    }

    List<OrderByElement> orderByElements = analytic.getFuncOrderBy();
    if (orderByElements != null) {
      for (OrderByElement orderByElement : orderByElements) {
        orderByElement.accept(this);
      }
    }

    KeepExpression keep = analytic.getKeep();
    if (keep != null) {
      keep.accept(this);
    }

    Expression filterExpr = analytic.getFilterExpression();
    if (filterExpr != null) {
      filterExpr.accept(this);
    }

    WindowDefinition windowDefinition = analytic.getWindowDefinition();
    if (windowDefinition != null) {
      visit(windowDefinition);
    }
  }

  @Override
  public void visit(SetOperationList list) {
    List<SelectBody> selects = list.getSelects();
    for (SelectBody select : selects) {
      select.accept(this);
    }

    List<OrderByElement> orderByElements = list.getOrderByElements();
    if (orderByElements != null) {
      for (OrderByElement orderByElement : orderByElements) {
        orderByElement.accept(this);
      }
    }

    Limit limit = list.getLimit();
    if (limit != null) {
      visit(limit);
    }

    Offset offset = list.getOffset();
    if (offset != null) {
      visit(offset);
    }

    Fetch fetch = list.getFetch();
    if (fetch != null) {
      visit(fetch);
    }

    WithIsolation withIsolation = list.getWithIsolation();
    if (withIsolation != null) {
      visit(withIsolation);
    }
  }

  @Override
  public void visit(ExtractExpression eexpr) {
    eexpr.getExpression().accept(this);
  }

  @Override
  public void visit(LateralSubSelect lateralSubSelect) {
    SubSelect subSelect = lateralSubSelect.getSubSelect();
    if (subSelect != null) {
      visit(subSelect);
    }

    Alias alias = lateralSubSelect.getAlias();
    if (alias != null) {
      visit(alias);
    }

    Pivot pivot = lateralSubSelect.getPivot();
    if (pivot != null) {
      pivot.accept(this);
    }

    UnPivot unPivot = lateralSubSelect.getUnPivot();
    if (unPivot != null) {
      unPivot.accept(this);
    }
  }

  @Override
  public void visit(MultiExpressionList multiExprList) {
    List<ExpressionList> exprLists = multiExprList.getExpressionLists();
    if (exprLists != null) {
      for (ExpressionList exprList : exprLists) {
        exprList.accept(this);
      }
    }
  }

  @Override
  public void visit(ValuesList valuesList) {
    MultiExpressionList multiExpressionList = valuesList.getMultiExpressionList();
    if (multiExpressionList != null) {
      multiExpressionList.accept(this);
    }

    Alias alias = valuesList.getAlias();
    if (alias != null) {
      visit(alias);
    }

    Pivot pivot = valuesList.getPivot();
    if (pivot != null) {
      pivot.accept(this);
    }

    UnPivot unPivot = valuesList.getUnPivot();
    if (unPivot != null) {
      unPivot.accept(this);
    }
  }

  @Override
  public void visit(IntervalExpression iexpr) {
    Expression expr = iexpr.getExpression();
    if (expr != null) {
      expr.accept(this);
    }
  }

  @Override
  public void visit(JdbcNamedParameter jdbcNamedParameter) {
    // nothing to do
  }

  @Override
  public void visit(OracleHierarchicalExpression oexpr) {
    if (!oexpr.isConnectFirst()) {
      Expression start = oexpr.getStartExpression();
      if (start != null) {
        start.accept(this);
      }
    }

    Expression connect = oexpr.getConnectExpression();
    if (connect != null) {
      connect.accept(this);
    }

    if (oexpr.isConnectFirst()) {
      Expression start = oexpr.getStartExpression();
      if (start != null) {
        start.accept(this);
      }
    }
  }

  @Override
  public void visit(RegExpMatchOperator rexpr) {
    visitBinaryExpression(rexpr);
  }

  @Override
  public void visit(RegExpMySQLOperator rexpr) {
    visitBinaryExpression(rexpr);
  }

  @Override
  public void visit(JsonExpression jsonExpr) {
    Expression expression = jsonExpr.getExpression();
    if (expression != null) {
      expression.accept(this);
    }
  }

  @Override
  public void visit(JsonOperator jsonExpr) {
    visitBinaryExpression(jsonExpr);
  }

  @Override
  public void visit(AllColumns allColumns) {
    // nothing to do
  }

  @Override
  public void visit(AllTableColumns allTableColumns) {
    Table table = allTableColumns.getTable();
    if (table != null) {
      table.accept(this);
    }
  }

  @Override
  public void visit(AllValue allValue) {
    // nothing to do
  }

  @Override
  public void visit(IsDistinctExpression isDistinctExpression) {
    visitBinaryExpression(isDistinctExpression);
  }

  @Override
  public void visit(SelectExpressionItem item) {
    Expression expr = item.getExpression();
    if (expr != null) {
      expr.accept(this);
    }

    Alias alias = item.getAlias();
    if (alias != null) {
      visit(alias);
    }
  }

  @Override
  public void visit(UserVariable var) {
    // nothing to do
  }

  @Override
  public void visit(NumericBind bind) {
    // nothing to do
  }

  @Override
  public void visit(KeepExpression aexpr) {
    List<OrderByElement> orderByElements = aexpr.getOrderByElements();
    if (orderByElements != null) {
      for (OrderByElement orderByElement : orderByElements) {
        orderByElement.accept(this);
      }
    }
  }

  @Override
  public void visit(MySQLGroupConcat groupConcat) {
    ExpressionList exprList = groupConcat.getExpressionList();
    if (exprList != null) {
      exprList.accept(this);
    }

    List<OrderByElement> orderByElements = groupConcat.getOrderByElements();
    if (orderByElements != null) {
      for (OrderByElement orderByElement : orderByElements) {
        orderByElement.accept(this);
      }
    }
  }

  @Override
  public void visit(ValueListExpression valueList) {
    valueList.getExpressionList().accept(this);
  }

  @Override
  public void visit(Delete delete) {
    List<WithItem> withItemsList = delete.getWithItemsList();
    if (withItemsList != null) {
      for (WithItem withItem : withItemsList) {
        withItem.accept(this);
      }
    }

    List<Table> tables = delete.getTables();
    if (tables != null) {
      for (Table table : tables) {
        table.accept(this);
      }
    }

    OutputClause outputClause = delete.getOutputClause();
    if (outputClause != null) {
      visit(outputClause);
    }

    Table table = delete.getTable();
    if (table != null) {
      table.accept(this);
    }

    List<Table> usingTables = delete.getUsingList();
    if (usingTables != null) {
      for (Table usingTable : usingTables) {
        usingTable.accept(this);
      }
    }

    List<Join> joins = delete.getJoins();
    if (joins != null) {
      for (Join join : joins) {
        visit(join);
      }
    }

    Expression where = delete.getWhere();
    if (where != null) {
      where.accept(this);
    }

    List<OrderByElement> orderByElements = delete.getOrderByElements();
    if (orderByElements != null) {
      for (OrderByElement orderByElement : orderByElements) {
        orderByElement.accept(this);
      }
    }

    Limit limit = delete.getLimit();
    if (limit != null) {
      visit(limit);
    }

    List<SelectItem> returningExpressionList = delete.getReturningExpressionList();
    if (returningExpressionList != null) {
      for (SelectItem returningExpression : returningExpressionList) {
        returningExpression.accept(this);
      }
    }
  }

  public void visit(OutputClause outputClause) {
    List<SelectItem> selectItems = outputClause.getSelectItemList();
    if (selectItems != null) {
      for (SelectItem selectItem : selectItems) {
        selectItem.accept(this);
      }
    }

    UserVariable tableVariable = outputClause.getTableVariable();
    if (tableVariable != null) {
      tableVariable.accept(this);
    }

    Table outputTable = outputClause.getOutputTable();
    if (outputTable != null) {
      outputTable.accept(this);
    }
  }

  @Override
  public void visit(Update update) {
    List<WithItem> withItemsList = update.getWithItemsList();
    if (withItemsList != null) {
      for (WithItem withItem : withItemsList) {
        withItem.accept(this);
      }
    }

    Table table = update.getTable();
    if (table != null) {
      table.accept(this);
    }

    List<Join> startJoins = update.getStartJoins();
    if (startJoins != null) {
      for (Join join : startJoins) {
        visit(join);
      }
    }

    List<UpdateSet> updateSets = update.getUpdateSets();
    if (updateSets != null) {
      for (UpdateSet updateSet : updateSets) {
        visit(updateSet);
      }
    }

    OutputClause outputClause = update.getOutputClause();
    if (outputClause != null) {
      visit(outputClause);
    }

    FromItem fromItem = update.getFromItem();
    if (fromItem != null) {
      fromItem.accept(this);
    }

    List<Join> joins = update.getJoins();
    if (joins != null) {
      for (Join join : joins) {
        visit(join);
      }
    }

    Expression where = update.getWhere();
    if (where != null) {
      where.accept(this);
    }

    List<OrderByElement> orderByElements = update.getOrderByElements();
    if (orderByElements != null) {
      for (OrderByElement orderByElement : orderByElements) {
        orderByElement.accept(this);
      }
    }

    Limit limit = update.getLimit();
    if (limit != null) {
      visit(limit);
    }

    List<SelectItem> returningExpressionList = update.getReturningExpressionList();
    if (returningExpressionList != null) {
      for (SelectItem returningExpression : returningExpressionList) {
        returningExpression.accept(this);
      }
    }
  }

  public void visit(UpdateSet updateSet) {
    List<Column> columns = updateSet.getColumns();
    if (columns != null) {
      for (Column column : columns) {
        column.accept(this);
      }
    }

    List<Expression> expressions = updateSet.getExpressions();
    if (expressions != null) {
      for (Expression expression : expressions) {
        expression.accept(this);
      }
    }
  }

  @Override
  public void visit(Insert insert) {
    List<WithItem> withItemsList = insert.getWithItemsList();
    if (withItemsList != null) {
      for (WithItem withItem : withItemsList) {
        withItem.accept(this);
      }
    }

    Table table = insert.getTable();
    if (table != null) {
      table.accept(this);
    }

    List<Column> columns = insert.getColumns();
    if (columns != null) {
      for (Column column : columns) {
        column.accept(this);
      }
    }

    OutputClause outputClause = insert.getOutputClause();
    if (outputClause != null) {
      visit(outputClause);
    }

    Select select = insert.getSelect();
    if (select != null) {
      select.accept(this);
    }

    List<Column> setColumns = insert.getSetColumns();
    List<Expression> setExpressions = insert.getSetExpressionList();
    if (setColumns != null && setExpressions != null) {
      Iterator<Column> setColumnIt = setColumns.iterator();
      Iterator<Expression> setExpressionIt = setExpressions.iterator();
      while (setColumnIt.hasNext() && setExpressionIt.hasNext()) {
        Column setColumn = setColumnIt.next();
        Expression setExpr = setExpressionIt.next();

        setColumn.accept(this);
        setExpr.accept(this);
      }
    }

    List<Column> duplicateUpdateColumns = insert.getDuplicateUpdateColumns();
    List<Expression> duplicateUpdateExpressions = insert.getDuplicateUpdateExpressionList();
    if (duplicateUpdateColumns != null && duplicateUpdateExpressions != null) {
      Iterator<Column> duplicateUpdateColumnsIt = duplicateUpdateColumns.iterator();
      Iterator<Expression> duplicateUpdateExpressionIt = duplicateUpdateExpressions.iterator();
      while (duplicateUpdateColumnsIt.hasNext() && duplicateUpdateExpressionIt.hasNext()) {
        Column duplicateUpdateColumn = duplicateUpdateColumnsIt.next();
        Expression duplicateUpdateExpr = duplicateUpdateExpressionIt.next();

        duplicateUpdateColumn.accept(this);
        duplicateUpdateExpr.accept(this);
      }
    }

    InsertConflictTarget conflictTarget = insert.getConflictTarget();
    if (conflictTarget != null) {
      visit(conflictTarget);
    }

    InsertConflictAction conflictAction = insert.getConflictAction();
    if (conflictAction != null) {
      visit(conflictAction);
    }

    List<SelectItem> returningExpressionList = insert.getReturningExpressionList();
    if (returningExpressionList != null) {
      for (SelectItem returningExpression : returningExpressionList) {
        returningExpression.accept(this);
      }
    }
  }

  public void visit(InsertConflictTarget conflictTarget) {
    Expression indexExpression = conflictTarget.getIndexExpression();
    if (indexExpression != null) {
      indexExpression.accept(this);
    }

    Expression where = conflictTarget.getWhereExpression();
    if (where != null) {
      where.accept(this);
    }
  }

  public void visit(InsertConflictAction conflictAction) {
    List<UpdateSet> updateSets = conflictAction.getUpdateSets();
    if (updateSets != null) {
      for (UpdateSet updateSet : updateSets) {
        visit(updateSet);
      }
    }

    Expression where = conflictAction.getWhereExpression();
    if (where != null) {
      where.accept(this);
    }
  }

  @Override
  public void visit(Replace replace) {
    Table table = replace.getTable();
    if (table != null) {
      table.accept(this);
    }

    List<Column> columns = replace.getColumns();
    List<Expression> expressions = replace.getExpressions();
    if (columns != null) {
      Iterator<Column> columnIt = columns.iterator();
      Iterator<Expression> exprIt = expressions != null ? expressions.iterator() : null;

      while (columnIt.hasNext() && (exprIt == null || exprIt.hasNext())) {
        Column col = columnIt.next();
        Expression expr = exprIt != null ? exprIt.next() : null;

        col.accept(this);
        if (expr != null) {
          expr.accept(this);
        }
      }
    }

    ItemsList itemsList = replace.getItemsList();
    if (itemsList != null) {
      itemsList.accept(this);
    }
  }

  public void visit(Analyze analyze) {
    Table table = analyze.getTable();
    if (table != null) {
      table.accept(this);
    }
  }

  @Override
  public void visit(Drop drop) {
    Table table = drop.getName();
    if (table != null) {
      table.accept(this);
    }
  }

  @Override
  public void visit(Truncate truncate) {
    Table table = truncate.getTable();
    if (table != null) {
      table.accept(this);
    }
  }

  @Override
  public void visit(CreateIndex createIndex) {
    Index index = createIndex.getIndex();
    if (index != null) {
      visit(index);
    }

    Table table = createIndex.getTable();
    if (table != null) {
      table.accept(this);
    }
  }

  public void visit(Index index) {
    List<Index.ColumnParams> columnParamsList = index.getColumns();
    if (columnParamsList != null) {
      for (Index.ColumnParams columnParams : columnParamsList) {
        visit(columnParams);
      }
    }
  }

  public void visit(Index.ColumnParams columnParams) {
    // nothing to do
  }

  @Override
  public void visit(CreateSchema createSchema) {
    List<Statement> statements = createSchema.getStatements();
    if (statements != null) {
      for (Statement statement : statements) {
        statement.accept(this);
      }
    }
  }

  @Override
  public void visit(CreateTable createTable) {
    Table table = createTable.getTable();
    if (table != null) {
      table.accept(this);
    }

    // TODO handle columns

    List<ColumnDefinition> columnDefinitions = createTable.getColumnDefinitions();
    if (columnDefinitions != null) {
      for (ColumnDefinition columnDefinition : columnDefinitions) {
        visit(columnDefinition);
      }
    }

    List<Index> indexes = createTable.getIndexes();
    if (indexes != null) {
      for (Index index : indexes) {
        visit(index);
      }
    }

    RowMovement rowMovement = createTable.getRowMovement();
    if (rowMovement != null) {
      visit(rowMovement);
    }

    Select select = createTable.getSelect();
    if (select != null) {
      select.accept(this);
    }

    Table likeTable = createTable.getLikeTable();
    if (likeTable != null) {
      likeTable.accept(this);
    }
  }

  public void visit(ColumnDefinition columnDefinition) {
    ColDataType colDataType = columnDefinition.getColDataType();
    if (colDataType != null) {
      visit(colDataType);
    }
  }

  public void visit(RowMovement rowMovement) {
    RowMovementMode rowMovementMode = rowMovement.getMode();
    if (rowMovementMode != null) {
      visit(rowMovementMode);
    }
  }

  public void visit(RowMovementMode rowMovementMode) {
    // nothing to do
  }

  @Override
  public void visit(CreateView createView) {
    Table view = createView.getView();
    if (view != null) {
      view.accept(this);
    }

    // TODO handle columns
    //    createView.getColumnNames();

    Select select = createView.getSelect();
    if (select != null) {
      select.accept(this);
    }
  }

  @Override
  public void visit(Alter alter) {
    Table table = alter.getTable();
    if (table != null) {
      table.accept(this);
    }

    List<AlterExpression> expressions = alter.getAlterExpressions();
    if (expressions != null) {
      for (AlterExpression alterExpression : expressions) {
        visit(alterExpression);
      }
    }
  }

  public void visit(AlterExpression alterExpression) {
    // TODO handle columns
    //    alterExpression.getColumnName();
    //    alterExpression.getColumnOldName();
    //    alterExpression.getFkSourceColumns();
    //    alterExpression.getFkColumns();
    //    alterExpression.getPkColumns();
    //    alterExpression.getUkColumns();

    List<AlterExpression.ColumnDataType> columnDataTypeList = alterExpression.getColDataTypeList();
    if (columnDataTypeList != null) {
      for (AlterExpression.ColumnDataType columnDataType : columnDataTypeList) {
        visit(columnDataType);
      }
    }

    List<AlterExpression.ColumnDropNotNull> columnDropNotNullList =
        alterExpression.getColumnDropNotNullList();
    if (columnDropNotNullList != null) {
      for (AlterExpression.ColumnDropNotNull columnDropNotNull : columnDropNotNullList) {
        visit(columnDropNotNull);
      }
    }

    for (ReferentialAction.Type type : ReferentialAction.Type.values()) {
      ReferentialAction referentialAction = alterExpression.getReferentialAction(type);
      if (referentialAction != null) {
        visit(referentialAction);
      }
    }

    Index index = alterExpression.getIndex();
    if (index != null) {
      visit(index);
    }

    List<ConstraintState> constraints = alterExpression.getConstraints();
    if (constraints != null) {
      for (ConstraintState constraint : constraints) {
        visit(constraint);
      }
    }
  }

  public void visit(AlterExpression.ColumnDropNotNull columnDropNotNull) {
    // TODO handle column name
    //    columnDropNotNull.getColumnName();
  }

  public void visit(ReferentialAction referentialAction) {
    // nothing to do
  }

  public void visit(ConstraintState constraintState) {
    // nothing to do
  }

  @Override
  public void visit(Execute execute) {
    ExpressionList expressionList = execute.getExprList();
    if (expressionList != null) {
      expressionList.accept(this);
    }
  }

  @Override
  public void visit(SetStatement set) {
    List<Expression> expressions = set.getExpressions();
    if (expressions != null) {
      for (Expression expression : expressions) {
        expression.accept(this);
      }
    }
  }

  @Override
  public void visit(ResetStatement reset) {
    // nothing to do
  }

  @Override
  public void visit(ShowColumnsStatement showColumnsStatement) {
    // TODO handle table name
    //    showColumnsStatement.getTableName();
  }

  @Override
  public void visit(RowConstructor rowConstructor) {
    List<ColumnDefinition> columnDefinitions = rowConstructor.getColumnDefinitions();
    if (columnDefinitions != null) {
      for (ColumnDefinition columnDefinition : columnDefinitions) {
        visit(columnDefinition);
      }
    }

    ExpressionList expressionList = rowConstructor.getExprList();
    if (expressionList != null) {
      expressionList.accept(this);
    }
  }

  @Override
  public void visit(RowGetExpression rowGetExpression) {
    // TODO handle column name
    //    rowGetExpression.getColumnName();

    Expression expression = rowGetExpression.getExpression();
    if (expression != null) {
      expression.accept(this);
    }
  }

  @Override
  public void visit(HexValue hexValue) {
    // nothing to do
  }

  @Override
  public void visit(Merge merge) {
    OracleHint oracleHint = merge.getOracleHint();
    if (oracleHint != null) {
      oracleHint.accept(this);
    }

    List<WithItem> withItemsList = merge.getWithItemsList();
    if (withItemsList != null) {
      for (WithItem withItem : withItemsList) {
        withItem.accept(this);
      }
    }

    Table table = merge.getTable();
    if (table != null) {
      table.accept(this);
    }

    Table usingTable = merge.getUsingTable();
    if (usingTable != null) {
      usingTable.accept(this);
    }

    SubSelect select = merge.getUsingSelect();
    if (select != null) {
      visit(select);
    }

    Alias alias = merge.getUsingAlias();
    if (alias != null) {
      visit(alias);
    }

    Expression onCondition = merge.getOnCondition();
    if (onCondition != null) {
      onCondition.accept(this);
    }

    MergeInsert mergeInsert = merge.getMergeInsert();
    if (merge.isInsertFirst() && mergeInsert != null) {
      visit(mergeInsert);
    }

    MergeUpdate mergeUpdate = merge.getMergeUpdate();
    if (mergeUpdate != null) {
      visit(mergeUpdate);
    }

    if (!merge.isInsertFirst() && mergeInsert != null) {
      visit(mergeInsert);
    }
  }

  public void visit(MergeInsert mergeInsert) {
    List<Column> columns = mergeInsert.getColumns();
    if (columns != null) {
      for (Column column : columns) {
        column.accept(this);
      }
    }

    List<Expression> expressions = mergeInsert.getValues();
    if (expressions != null) {
      for (Expression expression : expressions) {
        expression.accept(this);
      }
    }

    Expression where = mergeInsert.getWhereCondition();
    if (where != null) {
      where.accept(this);
    }
  }

  public void visit(MergeUpdate mergeUpdate) {
    List<Column> columns = mergeUpdate.getColumns();
    if (columns != null) {
      for (Column column : columns) {
        column.accept(this);
      }
    }

    List<Expression> expressions = mergeUpdate.getValues();
    if (expressions != null) {
      for (Expression expression : expressions) {
        expression.accept(this);
      }
    }

    Expression where = mergeUpdate.getWhereCondition();
    if (where != null) {
      where.accept(this);
    }

    Expression deleteWhere = mergeUpdate.getDeleteWhereCondition();
    if (deleteWhere != null) {
      deleteWhere.accept(this);
    }
  }

  @Override
  public void visit(OracleHint hint) {
    // nothing to do
  }

  @Override
  public void visit(TableFunction tableFunction) {
    Pivot pivot = tableFunction.getPivot();
    if (pivot != null) {
      pivot.accept(this);
    }

    UnPivot unPivot = tableFunction.getUnPivot();
    if (unPivot != null) {
      unPivot.accept(this);
    }
  }

  @Override
  public void visit(AlterView alterView) {
    Table view = alterView.getView();
    if (view != null) {
      view.accept(this);
    }

    // TODO handle columns
    //    alterView.getColumnNames();

    SelectBody selectBody = alterView.getSelectBody();
    if (selectBody != null) {
      selectBody.accept(this);
    }
  }

  @Override
  public void visit(TimeKeyExpression timeKeyExpression) {
    // nothing to do
  }

  @Override
  public void visit(DateTimeLiteralExpression dateTimeLiteralExpression) {
    // nothing to do
  }

  @Override
  public void visit(Commit commit) {
    // nothing to do
  }

  @Override
  public void visit(Upsert upsert) {
    Table table = upsert.getTable();
    if (table != null) {
      table.accept(this);
    }

    List<Column> columns = upsert.getColumns();
    if (columns != null) {
      for (Column column : columns) {
        column.accept(this);
      }
    }

    ItemsList itemsList = upsert.getItemsList();
    if (itemsList != null) {
      itemsList.accept(this);
    }

    Select select = upsert.getSelect();
    if (select != null) {
      select.accept(this);
    }

    List<Column> duplicateUpdateColumns = upsert.getDuplicateUpdateColumns();
    List<Expression> duplicateUpdateExpressions = upsert.getDuplicateUpdateExpressionList();
    if (duplicateUpdateColumns != null && duplicateUpdateExpressions != null) {
      Iterator<Column> duplicateUpdateColIt = duplicateUpdateColumns.iterator();
      Iterator<Expression> duplicateUpdateExprIt = duplicateUpdateExpressions.iterator();
      while (duplicateUpdateColIt.hasNext() && duplicateUpdateExprIt.hasNext()) {
        Column col = duplicateUpdateColIt.next();
        Expression expr = duplicateUpdateExprIt.next();

        col.accept(this);
        expr.accept(this);
      }
    }
  }

  @Override
  public void visit(UseStatement use) {
    // nothing to do
  }

  @Override
  public void visit(ParenthesisFromItem parenthesis) {
    FromItem fromItem = parenthesis.getFromItem();
    if (fromItem != null) {
      fromItem.accept(this);
    }

    Alias alias = parenthesis.getAlias();
    if (alias != null) {
      visit(alias);
    }

    Pivot pivot = parenthesis.getPivot();
    if (pivot != null) {
      pivot.accept(this);
    }

    UnPivot unPivot = parenthesis.getUnPivot();
    if (unPivot != null) {
      unPivot.accept(this);
    }
  }

  @Override
  public void visit(Block block) {
    Statements statements = block.getStatements();
    if (statements != null) {
      statements.accept(this);
    }
  }

  @Override
  public void visit(Comment comment) {
    Table table = comment.getTable();
    if (table != null) {
      table.accept(this);
    }

    Table view = comment.getView();
    if (view != null) {
      view.accept(this);
    }

    Column column = comment.getColumn();
    if (column != null) {
      column.accept(this);
    }
  }

  @Override
  public void visit(ValuesStatement values) {
    ItemsList expressions = values.getExpressions();
    if (expressions != null) {
      expressions.accept(this);
    }
  }

  @Override
  public void visit(DescribeStatement describe) {
    Table table = describe.getTable();
    if (table != null) {
      table.accept(this);
    }
  }

  @Override
  public void visit(ExplainStatement explain) {
    Map<ExplainStatement.OptionType, ExplainStatement.Option> options = explain.getOptions();
    if (options != null) {
      for (ExplainStatement.Option option : options.values()) {
        visit(option);
      }
    }

    Statement statement = explain.getStatement();
    if (statement != null) {
      statement.accept(this);
    }
  }

  public void visit(ExplainStatement.Option option) {
    // nothing to do
  }

  @Override
  public void visit(NextValExpression nextVal) {
    // nothing to do
  }

  @Override
  public void visit(CollateExpression col) {
    Expression leftExpr = col.getLeftExpression();
    if (leftExpr != null) {
      leftExpr.accept(this);
    }
  }

  @Override
  public void visit(ShowStatement showStatement) {
    // nothing to do
  }

  @Override
  public void visit(SimilarToExpression similarToExpression) {
    visitBinaryExpression(similarToExpression);
  }

  @Override
  public void visit(DeclareStatement declareStatement) {
    UserVariable userVariable = declareStatement.getUserVariable();
    if (userVariable != null) {
      userVariable.accept(this);
    }

    List<ColumnDefinition> columnDefinitions = declareStatement.getColumnDefinitions();
    if (columnDefinitions != null) {
      for (ColumnDefinition columnDefinition : columnDefinitions) {
        visit(columnDefinition);
      }
    }

    List<DeclareStatement.TypeDefExpr> typeDefExprList = declareStatement.getTypeDefExprList();
    if (typeDefExprList != null) {
      for (DeclareStatement.TypeDefExpr expr : typeDefExprList) {
        visit(expr);
      }
    }
  }

  public void visit(DeclareStatement.TypeDefExpr typeDefExpr) {
    UserVariable userVariable = typeDefExpr.getUserVariable();
    if (userVariable != null) {
      userVariable.accept(this);
    }

    ColDataType colDataType = typeDefExpr.getColDataType();
    if (colDataType != null) {
      visit(colDataType);
    }

    Expression expr = typeDefExpr.getDefaultExpression();
    if (expr != null) {
      expr.accept(this);
    }
  }

  @Override
  public void visit(Grant grant) {
    // nothing to do
  }

  @Override
  public void visit(ArrayExpression array) {
    Expression objExpr = array.getObjExpression();
    if (objExpr != null) {
      objExpr.accept(this);
    }

    Expression indexExpr = array.getIndexExpression();
    if (indexExpr != null) {
      indexExpr.accept(this);
    }

    Expression startIndexExpr = array.getStartIndexExpression();
    if (startIndexExpr != null) {
      startIndexExpr.accept(this);
    }

    Expression stopIndexExpr = array.getStopIndexExpression();
    if (stopIndexExpr != null) {
      stopIndexExpr.accept(this);
    }
  }

  @Override
  public void visit(ArrayConstructor array) {
    List<Expression> expressions = array.getExpressions();
    if (expressions != null) {
      for (Expression expression : expressions) {
        expression.accept(this);
      }
    }
  }

  @Override
  public void visit(CreateSequence createSequence) {
    Sequence sequence = createSequence.getSequence();
    if (sequence != null) {
      visit(sequence);
    }
  }

  @Override
  public void visit(AlterSequence alterSequence) {
    Sequence sequence = alterSequence.getSequence();
    if (sequence != null) {
      visit(sequence);
    }
  }

  public void visit(Sequence sequence) {
    List<Sequence.Parameter> parameters = sequence.getParameters();
    if (parameters != null) {
      for (Sequence.Parameter parameter : parameters) {
        visit(parameter);
      }
    }

    Database database = sequence.getDatabase();
    if (database != null) {
      visit(database);
    }
  }

  public void visit(Sequence.Parameter parameter) {
    // nothing to do
  }

  public void visit(Database database) {
    Server server = database.getServer();
    if (server != null) {
      visit(server);
    }
  }

  public void visit(Server server) {
    // nothing to do
  }

  @Override
  public void visit(CreateFunctionalStatement createFunctionalStatement) {
    // nothing to do
  }

  @Override
  public void visit(ShowTablesStatement showTables) {
    Expression likeExpression = showTables.getLikeExpression();
    if (likeExpression != null) {
      likeExpression.accept(this);
    }

    Expression where = showTables.getWhereCondition();
    if (where != null) {
      where.accept(this);
    }
  }

  @Override
  public void visit(VariableAssignment var) {
    UserVariable userVariable = var.getVariable();
    if (userVariable != null) {
      userVariable.accept(this);
    }

    Expression expression = var.getExpression();
    if (expression != null) {
      expression.accept(this);
    }
  }

  @Override
  public void visit(XMLSerializeExpr xmlSerializeExpr) {
    Expression expression = xmlSerializeExpr.getExpression();
    if (expression != null) {
      expression.accept(this);
    }

    List<OrderByElement> orderByElements = xmlSerializeExpr.getOrderByElements();
    if (orderByElements != null) {
      for (OrderByElement orderByElement : orderByElements) {
        orderByElement.accept(this);
      }
    }

    ColDataType colDataType = xmlSerializeExpr.getDataType();
    if (colDataType != null) {
      visit(colDataType);
    }
  }

  @Override
  public void visit(CreateSynonym createSynonym) {
    Synonym synonym = createSynonym.getSynonym();
    if (synonym != null) {
      visit(synonym);
    }
  }

  public void visit(Synonym synonym) {
    Database database = synonym.getDatabase();
    if (database != null) {
      visit(database);
    }
  }

  @Override
  public void visit(TimezoneExpression timezoneExpression) {
    Expression leftExpr = timezoneExpression.getLeftExpression();
    if (leftExpr != null) {
      leftExpr.accept(this);
    }

    List<Expression> timezoneExprList = timezoneExpression.getTimezoneExpressions();
    if (timezoneExprList != null) {
      for (Expression timezoneExpr : timezoneExprList) {
        timezoneExpr.accept(this);
      }
    }
  }

  @Override
  public void visit(SavepointStatement savepointStatement) {
    // nothing to do
  }

  @Override
  public void visit(RollbackStatement rollbackStatement) {
    // nothing to do
  }

  @Override
  public void visit(AlterSession alterSession) {
    // nothing to do
  }

  @Override
  public void visit(JsonAggregateFunction jsonAggregateFunction) {
    Expression expression = jsonAggregateFunction.getExpression();
    if (expression != null) {
      expression.accept(this);
    }

    List<OrderByElement> expressionOrderByElements =
        jsonAggregateFunction.getExpressionOrderByElements();
    if (expressionOrderByElements != null) {
      for (OrderByElement orderByElement : expressionOrderByElements) {
        orderByElement.accept(this);
      }
    }

    Expression filterExpression = jsonAggregateFunction.getFilterExpression();
    if (filterExpression != null) {
      filterExpression.accept(this);
    }

    ExpressionList partitionExpressionList = jsonAggregateFunction.getPartitionExpressionList();
    if (partitionExpressionList != null) {
      partitionExpressionList.accept(this);
    }

    List<OrderByElement> orderByElements = jsonAggregateFunction.getOrderByElements();
    if (orderByElements != null) {
      for (OrderByElement orderByElement : orderByElements) {
        orderByElement.accept(this);
      }
    }

    WindowElement windowElement = jsonAggregateFunction.getWindowElement();
    if (windowElement != null) {
      visit(windowElement);
    }
  }

  @Override
  public void visit(JsonFunction jsonFunction) {
    List<JsonKeyValuePair> jsonKeyValuePairs = jsonFunction.getKeyValuePairs();
    if (jsonKeyValuePairs != null) {
      for (JsonKeyValuePair jsonKeyValuePair : jsonKeyValuePairs) {
        visit(jsonKeyValuePair);
      }
    }

    List<JsonFunctionExpression> expressions = jsonFunction.getExpressions();
    for (JsonFunctionExpression expr : expressions) {
      visit(expr);
    }
  }

  public void visit(JsonKeyValuePair jsonKeyValuePair) {
    // nothing to do
  }

  public void visit(JsonFunctionExpression jsonFunctionExpression) {
    Expression expression = jsonFunctionExpression.getExpression();
    if (expression != null) {
      expression.accept(this);
    }
  }

  @Override
  public void visit(ConnectByRootOperator connectByRootOperator) {
    Column column = connectByRootOperator.getColumn();
    if (column != null) {
      column.accept(this);
    }
  }

  public void visit(IfElseStatement ifElseStatement) {
    Expression condition = ifElseStatement.getCondition();
    if (condition != null) {
      condition.accept(this);
    }

    Statement ifStatement = ifElseStatement.getIfStatement();
    if (ifStatement != null) {
      ifStatement.accept(this);
    }

    Statement elseStatement = ifElseStatement.getElseStatement();
    if (elseStatement != null) {
      elseStatement.accept(this);
    }
  }

  public void visit(OracleNamedFunctionParameter oracleNamedFunctionParameter) {
    Expression expression = oracleNamedFunctionParameter.getExpression();
    if (expression != null) {
      expression.accept(this);
    }
  }

  @Override
  public void visit(RenameTableStatement renameTableStatement) {
    Set<Map.Entry<Table, Table>> tableNames = renameTableStatement.getTableNames();
    if (tableNames != null) {
      for (Map.Entry<Table, Table> tableName : tableNames) {
        tableName.getKey().accept(this);
        tableName.getValue().accept(this);
      }
    }
  }

  @Override
  public void visit(PurgeStatement purgeStatement) {
    if (purgeStatement.getPurgeObjectType() == PurgeObjectType.TABLE) {
      ((Table) purgeStatement.getObject()).accept(this);
    }
  }

  @Override
  public void visit(AlterSystemStatement alterSystemStatement) {
    // nothing to do
  }

  @Override
  public void visit(UnsupportedStatement unsupportedStatement) {
    // nothing to do
  }

  @Override
  public void visit(GeometryDistance geometryDistance) {
    visitBinaryExpression(geometryDistance);
  }

  @Override
  public void visit(GroupByElement groupBy) {
    ExpressionList expressionList = groupBy.getGroupByExpressionList();
    if (expressionList != null) {
      expressionList.accept(this);
    }

    List groupingSets = groupBy.getGroupingSets();
    if (groupingSets != null) {
      for (Object groupingSet : groupingSets) {
        if (groupingSet instanceof Expression) {
          ((Expression) groupingSet).accept(this);
        } else if (groupingSet instanceof ExpressionList) {
          ((ExpressionList) groupingSet).accept(this);
        } else {
          throw getUnsupportedException(groupingSet);
        }
      }
    }
  }

  @Override
  public void visit(OrderByElement orderBy) {
    Expression expression = orderBy.getExpression();
    if (expression != null) {
      expression.accept(this);
    }
  }

  @Override
  public void visit(Pivot pivot) {
    List<FunctionItem> functionItems = pivot.getFunctionItems();
    if (functionItems != null) {
      for (FunctionItem functionItem : functionItems) {
        visit(functionItem);
      }
    }

    List<Column> forColumns = pivot.getForColumns();
    if (forColumns != null) {
      for (Column forColumn : forColumns) {
        forColumn.accept(this);
      }
    }

    List<SelectExpressionItem> singleInItems = pivot.getSingleInItems();
    if (singleInItems != null) {
      for (SelectExpressionItem singleInItem : singleInItems) {
        singleInItem.accept(this);
      }
    }

    List<ExpressionListItem> multiInItems = pivot.getMultiInItems();
    if (multiInItems != null) {
      for (ExpressionListItem multiInItem : multiInItems) {
        visit(multiInItem);
      }
    }

    Alias alias = pivot.getAlias();
    if (alias != null) {
      visit(alias);
    }
  }

  public void visit(FunctionItem functionItem) {
    Function function = functionItem.getFunction();
    if (function != null) {
      function.accept(this);
    }

    Alias alias = functionItem.getAlias();
    if (alias != null) {
      visit(alias);
    }
  }

  public void visit(ExpressionListItem expressionListItem) {
    ExpressionList expressionList = expressionListItem.getExpressionList();
    if (expressionList != null) {
      expressionList.accept(this);
    }

    Alias alias = expressionListItem.getAlias();
    if (alias != null) {
      visit(alias);
    }
  }

  @Override
  public void visit(PivotXml pivot) {
    visit((Pivot) pivot);

    SelectBody selectBody = pivot.getInSelect();
    if (selectBody != null) {
      selectBody.accept(this);
    }
  }

  @Override
  public void visit(UnPivot unpivot) {
    List<Column> unpivotClauseColumns = unpivot.getUnPivotClause();
    if (unpivotClauseColumns != null) {
      for (Column column : unpivotClauseColumns) {
        column.accept(this);
      }
    }

    List<Column> unpivotForClauseColumns = unpivot.getUnPivotForClause();
    if (unpivotForClauseColumns != null) {
      for (Column column : unpivotForClauseColumns) {
        column.accept(this);
      }
    }

    List<SelectExpressionItem> unpivotInClauseItems = unpivot.getUnPivotInClause();
    if (unpivotInClauseItems != null) {
      for (SelectExpressionItem unpivotInClauseItem : unpivotInClauseItems) {
        unpivotInClauseItem.accept(this);
      }
    }

    Alias alias = unpivot.getAlias();
    if (alias != null) {
      visit(alias);
    }
  }

  protected static <T> UnsupportedOperationException getUnsupportedException(Class<?> c, T type) {
    return new UnsupportedOperationException(
        String.format("%s for %s is not supported", c.getSimpleName(),
            type.getClass().getSimpleName()));
  }

  private static <T> UnsupportedOperationException getUnsupportedException(T type) {
    return getUnsupportedException(StatementsTraverser.class, type);
  }
}
