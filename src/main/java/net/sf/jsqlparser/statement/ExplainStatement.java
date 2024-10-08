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

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Select;

/**
 * An {@code EXPLAIN} statement
 */
public class ExplainStatement implements Statement {
    private String keyword;
    private Select select;
    private LinkedHashMap<OptionType, Option> options;
    private Table table;

    public ExplainStatement(String keyword) {
        this.keyword = keyword;
    }

    public ExplainStatement() {
        this("EXPLAIN");
    }

    public ExplainStatement(String keyword, Table table) {
        this.keyword = keyword;
        this.table = table;
        this.select = null;
    }

    public ExplainStatement(String keyword, Select select, List<Option> optionList) {
        this.keyword = keyword;
        this.select = select;
        this.table = null;

        if (optionList != null && !optionList.isEmpty()) {
            options = new LinkedHashMap<>();
            for (Option o : optionList) {
                options.put(o.getType(), o);
            }
        }
    }

    public ExplainStatement(Select select) {
        this("EXPLAIN", select, null);
    }

    public Table getTable() {
        return table;
    }

    public ExplainStatement setTable(Table table) {
        this.table = table;
        return this;
    }

    public Select getStatement() {
        return select;
    }

    public void setStatement(Select select) {
        this.select = select;
    }

    public LinkedHashMap<OptionType, Option> getOptions() {
        return options == null ? null : new LinkedHashMap<>(options);
    }

    public void addOption(Option option) {
        if (options == null) {
            options = new LinkedHashMap<>();
        }

        options.put(option.getType(), option);
    }

    /**
     * Returns the first option that matches this optionType
     *
     * @param optionType the option type to retrieve an Option for
     * @return an option of that type, or null. In case of duplicate options, the first found option
     *         will be returned.
     */
    public Option getOption(OptionType optionType) {
        if (options == null) {
            return null;
        }
        return options.get(optionType);
    }

    public String getKeyword() {
        return keyword;
    }

    public ExplainStatement setKeyword(String keyword) {
        this.keyword = keyword;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(keyword);
        if (table != null) {
            builder.append(" ").append(table);
        } else {
            if (options != null) {
                builder.append(" ");
                builder.append(options.values().stream().map(Option::formatOption)
                        .collect(Collectors.joining(" ")));
            }

            builder.append(" ");
            if (select != null) {
                select.appendTo(builder);
            }
        }

        return builder.toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    public enum OptionType {
        ANALYZE, VERBOSE, COSTS, BUFFERS, FORMAT, PLAN, PLAN_FOR;

        public static OptionType from(String type) {
            return Enum.valueOf(OptionType.class, type.toUpperCase());
        }
    }

    public static class Option implements Serializable {

        private final OptionType type;
        private String value;

        public Option(OptionType type) {
            this.type = type;
        }

        public OptionType getType() {
            return type;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String formatOption() {
            return type.name().replace("_", " ") + (value != null
                    ? " " + value
                    : "");
        }

        public Option withValue(String value) {
            this.setValue(value);
            return this;
        }
    }
}
