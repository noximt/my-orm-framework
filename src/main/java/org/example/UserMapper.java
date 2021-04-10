package org.example;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserMapper implements ResultSetMapper<User>{
    @Override
    public User toObject(ResultSet resultSet) {
        try {
            User user = new User();
            user.setId(resultSet.getInt(1));
            user.setLogin(resultSet.getString(3));
            user.setName(resultSet.getString(2));
            user.setPassword(resultSet.getString(4));
            user.setAge(resultSet.getInt(5));
            return user;
        } catch (SQLException e){

        }
        return null;
    }
}
