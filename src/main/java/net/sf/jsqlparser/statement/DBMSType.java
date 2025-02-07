/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.expression.StringValue;

public enum DBMSType implements SourceDestinationType {
    EXA,
    ORA,
    JDBC;

    private StringValue jdbcDriverDefinition;

    public StringValue getJDBCDriverDefinition() {
        return jdbcDriverDefinition;
    }

    public void setJDBCDriverDefinition(StringValue jdbcDriverDefinition) {
        this.jdbcDriverDefinition = jdbcDriverDefinition;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();

        sql.append(super.toString());
        if (jdbcDriverDefinition != null) {
            sql.append(" DRIVER = ").append(jdbcDriverDefinition);
        }

        return sql.toString();
    }
}
