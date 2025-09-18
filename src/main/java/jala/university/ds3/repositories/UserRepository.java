package jala.university.ds3.repositories;

import jala.university.ds3.domain.user.User;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String>{
    
//    Optional<User> findByUsername(String name);
    Optional<User> findById(String id);
    Optional<User> findByLogin(String login);
    Optional<User> findByLoginIgnoreCase(String login);
    
}