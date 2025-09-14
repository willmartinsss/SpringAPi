package jala.university.ds3.SqlModel;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String userId;
//    private String name;
//    private String login;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(nullable = false, length = 20)
    private String login;
    
    @Column(nullable = false, length = 100)
    private String password;
}
