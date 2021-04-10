package org.example;

import java.sql.ResultSet;

public interface ResultSetMapper<T> {
    T toObject(ResultSet resultSet);
}
