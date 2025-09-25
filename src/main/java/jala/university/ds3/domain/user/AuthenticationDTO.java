package jala.university.ds3.domain.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User authentication data")
public record AuthenticationDTO(
        @NotBlank(message = "Login is required")
        @Size(min = 3, max = 20, message = "Login must be between 3 and 20 characters")
        @Schema(description = "User login", example = "admin")
        String login,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters long")
        @Schema(description = "User password", example = "123456")
        String password
) {}