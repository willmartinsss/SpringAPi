package jala.university.ds3.domain.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "New user registration data")
public record RegisterDTO(
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
        @Schema(description = "User full name", example = "John Silva")
        String name,

        @NotBlank(message = "Login is required")
        @Size(min = 3, max = 20, message = "Login must be between 3 and 20 characters")
        @Schema(description = "Unique user login", example = "johnsilva")
        String login,

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
        @Schema(description = "User password", example = "mypassword123")
        String password,

        @NotNull(message = "Role is required")
        @Schema(description = "User role in the system", example = "USER")
        UserRole role
) {}