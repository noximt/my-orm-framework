package org.example;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.annotation.Id;
import org.example.annotation.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {

    public static void main(String[] args) {
        User user = new User();
        String name = user.getClass().getDeclaredAnnotation(Table.class).name();
        System.out.println(name);
    }

    @Id
    private Integer id;
    private String name;
    private String login;
    private String password;
    private int age;

    public User(String name, String login, String password, int age) {
        this.age = age;
        this.name = name;
        this.login = login;
        this.password = password;
    }
}
