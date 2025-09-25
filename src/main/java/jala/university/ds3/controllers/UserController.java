package jala.university.ds3.controllers;

import java.util.UUID;
import java.util.Optional;
import jala.university.ds3.domain.user.User;
import jala.university.ds3.repositories.UserRepository;
import jala.university.ds3.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/users")
@Tag(name = "User Management", description = "User CRUD operations")
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    @Autowired
    public UserController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping("/currentUser")
    @Operation(summary = "Get current user", description = "Returns the currently authenticated user data")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<?> currentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentLogin = authentication.getName();

            Optional<User> userOptional = userRepository.findByLogin(currentLogin);

            if (userOptional.isPresent()) {
                User safeUser = createSafeUser(userOptional.get());
                return ResponseEntity.ok(safeUser);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/id")
    @Operation(summary = "Get user by ID")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getById(@RequestParam("id") String idParam) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentLogin = authentication.getName();

            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            Optional<User> userOptional = findUserById(idParam);

            if (userOptional.isPresent()) {
                User user = userOptional.get();

                if (!user.getLogin().equals(currentLogin) && !isAdmin) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
                }

                User safeUser = createSafeUser(user);
                return ResponseEntity.ok(safeUser);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> updateUser(@PathVariable("id") String idParam,
                                        @RequestBody User updatedUser) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentLogin = authentication.getName();

            Optional<User> userOptional = findUserById(idParam);

            if (userOptional.isPresent()) {
                User user = userOptional.get();

                boolean isAdmin = authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

                if (!user.getLogin().equals(currentLogin) && !isAdmin) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
                }

                if (updatedUser.getName() != null && !updatedUser.getName().trim().isEmpty()) {
                    user.setName(updatedUser.getName().trim());
                }

                if (updatedUser.getPassword() != null && !updatedUser.getPassword().trim().isEmpty()) {
                    user.setPassword(updatedUser.getPassword());
                }

                User savedUser = userRepository.save(user);
                User safeUser = createSafeUser(savedUser);

                return ResponseEntity.ok(safeUser);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> deleteUser(@PathVariable("id") String idParam) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentLogin = authentication.getName();

            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Only administrators can delete users");
            }

            Optional<User> userOptional = findUserById(idParam);

            if (userOptional.isPresent()) {
                User user = userOptional.get();

                if (user.getLogin().equals(currentLogin)) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("Cannot delete your own account");
                }

                userRepository.delete(user);
                return ResponseEntity.ok()
                        .body("User with login '" + user.getLogin() + "' has been deleted");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping
    @Operation(summary = "List users")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getAllUsers() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Only administrators can list all users");
            }

            return ResponseEntity.ok(
                    userRepository.findAll().stream()
                            .map(this::createSafeUser)
                            .toList()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    private Optional<User> findUserById(String idParam) {
        try {
            UUID uuid = UUID.fromString(idParam);
            return userRepository.findById(uuid.toString());
        } catch (IllegalArgumentException e) {
            return userRepository.findById(idParam);
        }
    }

    private User createSafeUser(User user) {
        User safeUser = new User();
        safeUser.setId(user.getId());
        safeUser.setName(user.getName());
        safeUser.setLogin(user.getLogin());
        safeUser.setRole(user.getRole());
        return safeUser;
    }
}