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

@RestController
@RequestMapping("/users")
@Tag(name = "User", description = "User management API")
@SecurityRequirement(name = "bearerAuth")
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
    public ResponseEntity<?> currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentLogin = authentication.getName();

        Optional<User> userOptional = userRepository.findByLogin(currentLogin);

        if (userOptional.isPresent()) {
            User safeUser = createSafeUser(userOptional.get());
            return ResponseEntity.ok(safeUser);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    @GetMapping("/id")
    @Operation(summary = "Get user by ID",
            description = "Returns the user based on provided ID (accepts UUID or String)")
    public ResponseEntity<?> getById(@RequestParam("id") String idParam) {
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
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Updates user data (name and password)")
    public ResponseEntity<?> updateUser(@PathVariable("id") String idParam,
                                        @RequestBody User updatedUser) {
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

            // Update allowed fields only
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
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Removes a user from the system (administrators only)")
    public ResponseEntity<?> deleteUser(@PathVariable("id") String idParam) {
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
    }

    @GetMapping
    @Operation(summary = "List users", description = "Lists all users (administrators only)")
    public ResponseEntity<?> getAllUsers() {
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
    }

    /**
     * Utility method to find user by flexible ID
     * Accepts UUID or direct String
     */
    private Optional<User> findUserById(String idParam) {
        // First try as UUID
        try {
            UUID uuid = UUID.fromString(idParam);
            return userRepository.findById(uuid.toString());
        } catch (IllegalArgumentException e) {
            // If not valid UUID, try as direct String
            return userRepository.findById(idParam);
        }
    }

    /**
     * Creates a safe copy of user without sensitive information
     */
    private User createSafeUser(User user) {
        User safeUser = new User();
        safeUser.setId(user.getId());
        safeUser.setName(user.getName());
        safeUser.setLogin(user.getLogin());
        safeUser.setRole(user.getRole());
        // Password is intentionally not set (remains null)
        return safeUser;
    }
}