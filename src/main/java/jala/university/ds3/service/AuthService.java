package jala.university.ds3.service;

import org.springframework.stereotype.Service;
import org.springframework.security.core.AuthenticationException;

@Service
public class AuthService {

    public String authenticate(String username, String password) {
        if ("admin".equals(username) && "password".equals(password)) {
            return "jwt-token-example";
        }
        throw new AuthenticationException("Invalid credentials") {};
    }
}
