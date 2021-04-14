package org.example;

import lombok.SneakyThrows;
import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ObjectMapper {

    private DataSource dataSource;
    private final QueryGenerator queryGenerator = new QueryGenerator();

    public ObjectMapper(BasicDataSource basicDataSource) {
        this.dataSource = basicDataSource;
    }

    public void save(Object o) {
        Query query = queryGenerator.generateInsertSqlQuery(o);
        execute(query.getSql());
    }

    public void update(Object o) {
        Query query = queryGenerator.generateUpdateSqlQuery(o);
        execute(query.getSql());
    }

    public void delete(Object o){
        Query query = queryGenerator.generateDeleteSqlQuery(o);
        execute(query.getSql());
    }

    public <T> List<T> findAll(T o, ResultSetMapper<T> mapper) {
        List<T> execute = null;
        Query query = queryGenerator.generateSelectSqlQuery(o, true, null, null);
        try {
            execute = (List<T>) execute(query, mapper);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return execute;
    }

    public <T> List<T> findByParameter(T o, ResultSetMapper<T> mapper, String column, Object value) {
        Query query = queryGenerator.generateSelectSqlQuery(o, false, column, value);
        List<T> execute = null;
        try {
            execute = (List<T>) execute(query, mapper);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return execute;
    }

    private List<Object> execute(Query query, ResultSetMapper<?> mapper) throws SQLException {
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query.getSql());
        ArrayList<Object> objects = new ArrayList<>();
        while (resultSet.next()) {
            objects.add(mapper.toObject(resultSet));
        }
        return objects;
    }

    @SneakyThrows
    private void execute(String sql) {
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        statement.execute(sql);
        connection.close();
    }
}
