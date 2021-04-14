package org.example;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.annotation.Id;
import org.example.annotation.OneToOne;
import org.example.annotation.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {

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

    public User(Integer id) {
        this.id = id;
    }
}
