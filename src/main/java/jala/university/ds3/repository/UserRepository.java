package jala.university.ds3.repository;

import jala.university.ds3.SqlModel.User;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String>{
    Optional<User> findById(String userId);
    
    Optional<User> findByUserName(String userName);
    
    Optional<User> findByLogin(String login);
}
