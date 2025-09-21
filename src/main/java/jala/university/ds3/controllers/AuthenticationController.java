package jala.university.ds3.controllers;

import jakarta.validation.Valid;
import jala.university.ds3.domain.user.AuthenticationDTO;
import jala.university.ds3.domain.user.RegisterDTO;
import jala.university.ds3.domain.user.User;
import jala.university.ds3.repositories.UserRepository;
import jala.university.ds3.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenService tokenService;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates user and returns JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<?> login(@RequestBody @Valid AuthenticationDTO data) {
        try {
            var usernamePassword = new UsernamePasswordAuthenticationToken(data.login(), data.password());
            var auth = this.authenticationManager.authenticate(usernamePassword);

            var user = (User) auth.getPrincipal();
            var token = tokenService.generateToken(user);

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "type", "Bearer",
                    "user", Map.of(
                            "login", user.getLogin(),
                            "name", user.getName(),
                            "role", user.getRole().name()
                    )
            ));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        }
    }

    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Creates a new user in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Invalid data",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "409", description = "Login already exists",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<?> register(@RequestBody @Valid RegisterDTO data,
                                      BindingResult result) {
        if (result.hasErrors()) {
            var errors = result.getFieldErrors().stream()
                    .map(fieldError -> fieldError.getDefaultMessage())
                    .toList();
            return ResponseEntity.badRequest().body(Map.of("errors", errors));
        }

        String resolvedLogin = data.login() == null ? "" : data.login().trim();
        if (resolvedLogin.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Login is required"));
        }

        Optional<User> existing = repository.findByLogin(resolvedLogin);
        if (existing.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Login already exists"));
        }

        String encryptedPassword = this.passwordEncoder.encode(data.password());
        User newUser = new User(data.name(), resolvedLogin, encryptedPassword, data.role());
        User saved = repository.save(newUser);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "message", "User created successfully",
                        "user", Map.of(
                                "login", saved.getLogin(),
                                "name", saved.getName(),
                                "role", saved.getRole().name()
                        )
                ));
    }
}