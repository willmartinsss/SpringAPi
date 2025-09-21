package jala.university.ds3.controllers;

import jala.university.ds3.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;


@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

    @Autowired
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/currentUser")
    public ResponseEntity<?> currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentLogin = authentication.getName();

        return userRepository.findByLogin(currentLogin)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/id")
    public ResponseEntity<?> getById(@RequestParam("id") String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentLogin = authentication.getName();

        return userRepository.findById(id).map(user -> {
            if (!user.getLogin().equals(currentLogin)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authorized");
            }
            return ResponseEntity.ok(user);
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found in data!"));
    }

    // START: Code for User Deletion
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found!");
        }
    }
    // END: Code for User Deletion
}