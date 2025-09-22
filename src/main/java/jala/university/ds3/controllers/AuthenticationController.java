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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
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
    public ResponseEntity<?> register(@RequestBody @Valid RegisterDTO data, org.springframework.validation.BindingResult result) {
        if (result.hasErrors()) {
            var errors = result.getFieldErrors().stream()
                    .map(fieldError -> {
                        return fieldError.getDefaultMessage();
                    })
                    .toList();
            return ResponseEntity.badRequest().body(errors);
        }
        
        String resolvedLogin = data.login() == null ? "" : data.login().trim().toLowerCase();
        if (resolvedLogin == null || resolvedLogin.isEmpty() || resolvedLogin.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invald login!");
        }
        
//        Optional<User> existing = repository.findByLoginIgnoreCase(resolvedLogin);
//        if (existing.isPresent()) {
//            return ResponseEntity.status(HttpStatus.CONFLICT).body("Login already exists! (CS)");
//        }
//        if (this.passwordEncoder == null) {
//            this.passwordEncoder = new BCryptPasswordEncoder();
//        }
        
        Optional<User> existing = repository.findByLogin(data.login());
        if (existing.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Login already exists!");
        }

        if (this.passwordEncoder == null) {
            this.passwordEncoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        }

        String encryptedPassword = this.passwordEncoder.encode(data.password());
        User newUser = new User(data.name(), data.login(), encryptedPassword, data.role());
        User saved = repository.save(newUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("login", saved.getLogin(),"message", saved.getName()));
    }
}
