package jala.university.ds3.service;

import jala.university.ds3.domain.user.User;
import jala.university.ds3.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    public String authenticate(String login, String password) throws AuthenticationException {
        // Autenticação via Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(login, password)
        );

        // Busca usuário no banco
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Gera JWT usando o TokenService
        return tokenService.generateToken(user);
    }
}