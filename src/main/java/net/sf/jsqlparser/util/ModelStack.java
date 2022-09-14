package net.sf.jsqlparser.util;

import net.sf.jsqlparser.Model;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.values.ValuesStatement;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Objects;

public class ModelStack extends ArrayDeque<Model> {
  public <T extends Model> T findMatch(T obj) {
    Objects.requireNonNull(obj);

    Iterator<Model> it = this.descendingIterator();
    while (it.hasNext()) {
      Model model = it.next();
      T t = findMatch(obj, model);
      if (t != null) {
        return t;
      }
    }

    return null;
  }

  private <T extends Model> T findMatch(T obj, Model model) {
    if (model instanceof Statement) {
      return findMatch(obj, (Statement) model);
    } else if (model instanceof SelectBody) {
      return findMatch(obj, (SelectBody) model);
    }

    throw getUnsupportedException(model);
  }

  private <T extends Model> T findMatch(T obj, Statement statement) {
    if (statement instanceof Select) {
      throw getUnsupportedException(statement);
    } else if (statement instanceof Update) {
      throw getUnsupportedException(statement);
    } else if (statement instanceof ValuesStatement) {
      throw getUnsupportedException(statement);
    } else if (statement instanceof CreateTable) {
      throw getUnsupportedException(statement);
    } else if (statement instanceof Delete) {
      throw getUnsupportedException(statement);
    } else if (statement instanceof Merge) {
      throw getUnsupportedException(statement);
    }

    throw getUnsupportedException(statement);
  }

  private <T extends Model> T findMatch(T obj, SelectBody selectBody) {
    if (selectBody instanceof ValuesStatement) {
      throw getUnsupportedException(selectBody);
    } else if (selectBody instanceof PlainSelect) {
      throw getUnsupportedException(selectBody);
    } else if (selectBody instanceof SetOperationList) {
      throw getUnsupportedException(selectBody);
    } else if (selectBody instanceof WithItem) {
      throw getUnsupportedException(selectBody);
    }

    throw getUnsupportedException(selectBody);
  }

  private static <T> UnsupportedOperationException getUnsupportedException(T type) {
    return new UnsupportedOperationException(
        String.format("%s for %s is not supported", ModelStack.class.getSimpleName(),
            type.getClass().getSimpleName()));
  }
}
