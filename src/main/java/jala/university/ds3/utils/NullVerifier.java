package jala.university.ds3.utils;

import jala.university.ds3.Exceptions.GeneralExceptions;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;


@Component
public class NullVerifier {

    //private final UserRepository userRepository;  

    public NullVerifier() {
    }

    //TODO ALTERAR PARA INT OU LONG
    public void userVerify(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new GeneralExceptions("User with ID: " + userId + " was not found!", HttpStatus.NOT_FOUND);
        }
    }
    
    
}
