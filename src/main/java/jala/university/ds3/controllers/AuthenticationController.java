package jala.university.ds3.controllers;

import jakarta.validation.Valid;
import jala.university.ds3.domain.user.AuthenticationDTO;
import jala.university.ds3.domain.user.RegisterDTO;
import jala.university.ds3.domain.user.User;
import jala.university.ds3.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository repository;

    @Autowired(required = false)
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid AuthenticationDTO data) {
        try {
            var usernamePassword = new UsernamePasswordAuthenticationToken(data.login(), data.password());
            this.authenticationManager.authenticate(usernamePassword);
            // TODO: gerar e devolver JWT/token se for o caso
            return ResponseEntity.ok().build();
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterDTO data) {

        Optional<User> existing = repository.findByLogin(data.login());
        if (existing.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Login já existe");
        }
        
        if (this.passwordEncoder == null) {
            this.passwordEncoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        }

        String encryptedPassword = this.passwordEncoder.encode(data.password());
        User newUser = new User(data.name(), data.login(), encryptedPassword, data.role());
        User saved = repository.save(newUser);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
