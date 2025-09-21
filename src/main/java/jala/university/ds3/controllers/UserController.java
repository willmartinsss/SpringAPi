package jala.university.ds3.controllers;
import java.util.UUID;

import jala.university.ds3.domain.user.User;
import jala.university.ds3.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

//    @GetMapping("/currentUser")
//    public ResponseEntity<?> currentUser(@AutheticationPrincipal UserDetails principal) {
//        if (principal == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found in data!");
//        }
//        String currentLogin = principal.getUser();
    
//        return userRepository.findByLogin(currentLogin)
//                .map(user -> ResponseEntity.ok(user))
//                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found in data!"));
//    } 

    @GetMapping("/id")
    public ResponseEntity<?> getById(@RequestParam("id") String id) {
        //@PathVariable String id
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentLogin = authentication.getName();

        return userRepository.findById(id).map(user -> {
            if (!user.getLogin().equals(currentLogin)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authorized");
            }
            return ResponseEntity.ok(user);
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found in data!"));
    }

     // 🔹 Novo endpoint para atualização/Add validation to ensure that only the user themselves (or an administrator) can update their data./ pode atualizar nome e senha 
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable("id") UUID id,
                                        @RequestBody User updatedUser) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentLogin = authentication.getName();

        return userRepository.findById(id).map(user -> {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            if (!user.getLogin().equals(currentLogin) && !isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not authorized to update this account");
            }

            // Updte
             user.setName(updatedUser.getName());   // atualiza o nome

             if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
             user.setPassword(updatedUser.getPassword()); //só atualiza senha se não for nula ou vazia
             }

            userRepository.save(user);
            return ResponseEntity.ok(user);

        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found in data!"));
    }
}