package jala.university.ds3.repository;

import jala.university.ds3.SqlModel.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository {
    Optional<User> findById(String userId);
    Optional<User> findByUserName(String userName);
}
