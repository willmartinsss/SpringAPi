package jala.university.ds3.repositories;

import jala.university.ds3.domain.user.User;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, String>{
    
//    Optional<User> findByUsername(String name);
    Optional<User> findById(UUID id);
    Optional<User> findByLogin(String login);
    Optional<User> findByLoginIgnoreCase(String login);
    
}