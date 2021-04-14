package org.example;

import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;
import org.junit.Test;

import java.util.List;


public class AppTest {
    @Test
    public void shouldAnswerWithTrue() {
        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setUrl("");
        basicDataSource.setUsername("");
        basicDataSource.setPassword("");
        basicDataSource.setDriverClassName("");


        ObjectMapper objectMapper = new ObjectMapper(basicDataSource);
//        objectMapper.save(new User("Yauheni","noximt", "root", 24));
//        objectMapper.update(new User(13, "Yauheni", "noximt", "rooot", 24))
//        objectMapper.delete(new User(13));
        List<User> byParameter = objectMapper.findByParameter(new User(), new UserMapper(), "id", 15);

    }
}
