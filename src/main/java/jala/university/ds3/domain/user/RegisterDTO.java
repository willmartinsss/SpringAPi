package jala.university.ds3.domain.user;

public record RegisterDTO(String name, String login, String password, UserRole role) {
}
