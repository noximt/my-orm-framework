package org.example;

public class Query{
    private String sql;

    public Query(String sql) {
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }

    @Override
    public String toString() {
        return "Query{" +
                "sql='" + sql + '\'' +
                '}';
    }

}
