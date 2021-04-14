package org.example;

import org.example.annotation.Id;
import org.example.annotation.Table;

import java.lang.reflect.Field;
import java.util.*;

public class QueryGenerator {

    private static final String INSERT_PATTERN = "insert into %s";
    private static final String DELETE_PATTERN = "delete from %s where id = %d";
    private static final String UPDATE_PATTERN = "update %s set ";
    private static final String SELECT_PATTERN = "select * from %s where %s = ";
    private static final String SELECT_PATTERN_WITHOUT_WHERE = "select * from %s";

    private Query generate(String sql) {
        return new Query(sql);
    }

    public Query generateSelectSqlQuery(Object o, boolean all, String columnName, Object value) {
        if (all){
            return generate(String.format(SELECT_PATTERN_WITHOUT_WHERE, o.getClass().getSimpleName().toLowerCase()));
        }
        Object[] arrayOfSimpleNameAndParameter = createArrayOfSimpleNameAndParameter(o, columnName, value);
        StringBuilder stringBuilder = new StringBuilder(SELECT_PATTERN);
        if (columnName.getClass().isAnnotationPresent(Id.class)){
            stringBuilder.append("%s");
        }else{
            stringBuilder.append("'%s'");
        }
        return generate(String.format(stringBuilder.toString(), arrayOfSimpleNameAndParameter));
    }

    public Query generateInsertSqlQuery(Object o) {
        String tableName = o.getClass().getDeclaredAnnotation(Table.class).name();
        Field[] declaredFields = o.getClass().getDeclaredFields();
        Map<String, Object> parameters = createParametersForInsert(declaredFields, o);
        return generate(buildSql(parameters, tableName));
    }

    public Query generateDeleteSqlQuery(Object o) {
        Object[] obj = createArrayOfSimpleNameAndId(o);
        return generate(String.format(DELETE_PATTERN, obj));
    }

    private Object[] createArrayOfSimpleNameAndId(Object o) {
        String tableName = o.getClass().getDeclaredAnnotation(Table.class).name();
        Object[] obj = new Object[2];
        obj[0] = tableName;
        Field[] declaredFields = o.getClass().getDeclaredFields();
        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            if (declaredField.isAnnotationPresent(Id.class)) {
                try {
                    obj[1] = declaredField.get(o);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return obj;
    }

    private Object[] createArrayOfSimpleNameAndParameter(Object o, String columnName, Object value) {
        String tableName = o.getClass().getDeclaredAnnotation(Table.class).name();
        Object[] obj = new Object[3];
        obj[0] = tableName;
        obj[1] = columnName;
        obj[2] = value;
        return obj;
    }

    public Query generateUpdateSqlQuery(Object o) {
        Field[] declaredFields = o.getClass().getDeclaredFields();
        StringBuilder stringBuilder = new StringBuilder(UPDATE_PATTERN);
        generateStringWithSpecifications(declaredFields, stringBuilder, o);
        Object[] objects = createArrayOfParametersForSpecifications(o, declaredFields);
        return generate(String.format(stringBuilder.toString(),objects));
    }

    private Object[] createArrayOfParametersForSpecifications(Object o, Field[] declaredFields) {
        int size = (declaredFields.length - 1) * 2 + 2;
        String tableName = o.getClass().getDeclaredAnnotation(Table.class).name();
        Object[] objects = new Object[size];
        objects[0] = tableName;
        for (int i = 1, j = 0, k = size - 1; j < declaredFields.length; j++) {
            declaredFields[j].setAccessible(true);
            if (declaredFields[j].isAnnotationPresent(Id.class)){
                try {
                    objects[k] = declaredFields[j].get(o);
                    continue;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            objects[i] = declaredFields[j].getName();
            try {
                objects[i + 1] = declaredFields[j].get(o);
                i = i + 2;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return objects;
    }

    private void generateStringWithSpecifications(Field[] declaredFields, StringBuilder stringBuilder, Object o) {
        for (int i = 0; i < declaredFields.length; i++) {
            declaredFields[i].setAccessible(true);
            if (declaredFields[i].isAnnotationPresent(Id.class)) {
                continue;
            }
            stringBuilder.append("%s = ");
            try {
                if (declaredFields[i].get(o) instanceof Number){
                    stringBuilder.append("%s");
                }else {
                    stringBuilder.append("'%s'");
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (i + 1 == declaredFields.length) {
                stringBuilder.append(" where id = %d");
            } else {
                stringBuilder.append(", ");
            }
        }
    }

    private String buildSql(Map<String, Object> o, String tableName) {
        String format = String.format(INSERT_PATTERN, tableName);
        StringBuilder stringBuilder = new StringBuilder(format);
        Object[] values = o.values().toArray(new Object[0]);
        Object[] keys = o.keySet().toArray(new Object[0]);
        stringBuilder.append("(");
        buildStringWithSpecification(o, stringBuilder);
        stringBuilder.append(" values(");
        buildStringWithSpecification(o, stringBuilder, values);
        return String.format(stringBuilder.toString(), getKeysAndValuesArray(keys, values));
    }

    private void buildStringWithSpecification(Map<String, Object> o, StringBuilder stringBuilder, Object[] array) {
        for (int i = 0; i < o.size(); i++) {
            if (array[i].getClass().getTypeName().equals("java.lang.String")) {
                stringBuilder.append("'%s'");
            } else {
                stringBuilder.append("%s");
            }
            if (i + 1 == o.size()) {
                stringBuilder.append(")");
            } else {
                stringBuilder.append(", ");
            }
        }
    }

    private void buildStringWithSpecification(Map<String, Object> o, StringBuilder stringBuilder) {
        for (int i = 0; i < o.size(); i++) {
            stringBuilder.append("%s");
            if (i + 1 == o.size()) {
                stringBuilder.append(")");
            } else {
                stringBuilder.append(", ");
            }
        }
    }

    private Object[] getKeysAndValuesArray(Object[] keys, Object[] values) {
        int size = keys.length + values.length;
        Object[] keysAndValuesArray = new Object[size];
        for (int i = 0, j = size - 1, k = values.length - 1; i < keys.length; i++, j--, k--) {
            keysAndValuesArray[i] = keys[i];
            keysAndValuesArray[j] = values[k];
        }
        return keysAndValuesArray;
    }

    private Map<String, Object> createParametersForInsert(Field[] fields, Object o) {
        Map<String, Object> map = new HashMap<>();
        for (Field field : fields) {
            if (field.getName().equalsIgnoreCase("id")) {
                continue;
            }
            try {
                field.setAccessible(true);
                map.put(field.getName(), field.get(o));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

}
