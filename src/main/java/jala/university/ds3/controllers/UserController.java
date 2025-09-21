package jala.university.ds3.controllers;

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

@RestController
@RequestMapping("/users")
@Tag(name = "User", description = "API para gerenciar usuários")
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    @Autowired
    public UserController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping("/currentUser")
    @Operation(summary = "Obter usuário logado", description = "Retorna os dados do usuário atualmente autenticado")
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
@Operation(summary = "Obter usuário por ID", description = "Retorna o usuário com base no ID fornecido, apenas se for o mesmo usuário logado")
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

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar usuário", description = "Atualiza os dados do usuário com base no ID fornecido")
    public ResponseEntity<User> updateUser(
            @PathVariable String id,
            @RequestBody User updatedUser) {
        User user = userService.updateUser(id, updatedUser);
        return ResponseEntity.ok(user);
    }


}
