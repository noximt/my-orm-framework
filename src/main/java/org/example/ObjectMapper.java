package org.example;

import lombok.SneakyThrows;
import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class ObjectMapper {
    private String url;
    private String username;
    private String password;
    private String driverClassName;

    private DataSource dataSource;
    private QueryGenerator queryGenerator = new QueryGenerator();

    public ObjectMapper(String url, String username, String password, String driverClassName) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.driverClassName = driverClassName;
        init();
    }

    private void init() {
        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setUrl(this.url);
        basicDataSource.setUsername(this.username);
        basicDataSource.setPassword(this.password);
        basicDataSource.setDriverClassName(this.driverClassName);
        this.dataSource = basicDataSource;
    }

    public void save(Object o) {
        Queue<Query> generate = queryGenerator.generate(o, Method.INSERT);
        execute(generate.remove().getSql());
    }

    public void update(Object o) {
        Queue<Query> generate = queryGenerator.generate(o, Method.UPDATE);
        execute(generate.remove().getSql());
    }

    public <T> List<T> findAll(T o, ResultSetMapper<T> mapper) {
        Queue<Query> generate = queryGenerator.generate(o, Method.SELECT_WITHOUT_WHERE);
        for (Query query : generate) {
            try {
                return (List<T>) execute(query, mapper);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return null;
    }

    public <T> T findById(T o, ResultSetMapper<T> mapper) {
        Queue<Query> generate = queryGenerator.generate(o, Method.SELECT);
        try {
            return (T) execute(generate, mapper);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
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

    private List<Object> execute(Queue<Query> queue, ResultSetMapper<?> mapper) throws SQLException {
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        ArrayList<Object> objects = new ArrayList<>();
        for (Query query : queue) {
            ResultSet resultSet = statement.executeQuery(query.getSql());
            while (resultSet.next()) {
                objects.add(mapper.toObject(resultSet));
            }
        }
        statement.executeBatch();
        connection.close();
        return objects;
    }

    private void mapDeclaredFields(Object o, StringBuilder stringBuilder, Field declaredField1, List<Object> objectList) {
        String name = declaredField1.getName();
        declaredField1.setAccessible(true);
        if (name.equalsIgnoreCase("id")) {
            stringBuilder.append("%s");
            objectList.add("default");
        } else {
            stringBuilder.append("'%s'");
            try {
                objectList.add(declaredField1.get(o));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @SneakyThrows
    private void execute(String sql) {
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        statement.execute(sql);
        connection.close();
    }
}
