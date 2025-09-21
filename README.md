# API Connection with Database and Legacy Code

This is the main repository that contains the logic of how the User interacts with the login system, performing CRUD operations with the legacy code in the background.

**Repository under development**

Requires MySQL and Docker connection.

## Prerequisites

- **MySQL**: Database server for data persistence
- **Docker**: Container platform for deployment
- **Java 17+**: Required runtime
- **Maven 3.6+**: Build tool

## Features

- JWT Authentication
- User management (CRUD operations)
- Legacy system compatibility
- RESTful API design
- OpenAPI/Swagger documentation
- Role-based access control (RBAC)

## Quick Start

### 1. Database Setup

```sql
CREATE SCHEMA sd3;
USE sd3;

CREATE TABLE users (
    id VARCHAR(36) NOT NULL,
    name VARCHAR(200) NOT NULL,
    login VARCHAR(20) NOT NULL,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    PRIMARY KEY (id),
    UNIQUE INDEX id_UNIQUE (id ASC) VISIBLE,
    UNIQUE INDEX login_UNIQUE (login ASC) VISIBLE
);
```

### 2. Configuration

Update `src/main/resources/application.properties` with your database credentials:

```properties
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/sd3
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. Run the Application

```bash
./mvnw spring-boot:run
```

### 4. Access Documentation

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/v3/api-docs

## API Endpoints

### Authentication
- `POST /auth/register` - Register new user
- `POST /auth/login` - User login

### User Management
- `GET /users/currentUser` - Get current user info
- `GET /users/id?id={id}` - Get user by ID
- `GET /users` - List all users (Admin only)
- `PUT /users/{id}` - Update user
- `DELETE /users/{id}` - Delete user (Admin only)

## Testing

```bash
# Run unit tests
./mvnw test

# Run integration tests
./mvnw test -Dtest=**/*IntegrationTest
```

## Legacy System Compatibility

This API is designed to work alongside existing legacy systems. The `role` column is added with default values to maintain backward compatibility.

## Security

- JWT token-based authentication
- BCrypt password encryption
- Role-based authorization
- CORS configuration

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `JWT_SECRET` | Secret key for JWT token generation | `my-secret-key-ds3-legacy-system-2024` |
| `DB_HOST` | Database host | `127.0.0.1` |
| `DB_PORT` | Database port | `3306` |
| `DB_NAME` | Database name | `sd3` |
| `DB_USER` | Database username | `appuser` |
| `DB_PASS` | Database password | `capstonesd3teamtwo` |

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contact

DS3 Team - team@ds3.com

Project Link: [https://github.com/your-org/ds3-api](https://github.com/your-org/ds3-api)