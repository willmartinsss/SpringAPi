package jala.university.ds3.Client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class ApiTestClient {

    private static final String BASE_URL = "http://localhost:8080";
    private static final String AUTH_LOGIN_URL = BASE_URL + "/auth/login";
    private static final String AUTH_REGISTER_URL = BASE_URL + "/auth/register";
    private static final String USERS_URL = BASE_URL + "/users";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String adminToken;

    // Statistics
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger failureCount = new AtomicInteger(0);

    public static void main(String[] args) {
        ApiTestClient client = new ApiTestClient();

        int numberOfUsers = args.length > 0 ? Integer.parseInt(args[0]) : 10;

        System.out.println("=".repeat(60));
        System.out.println("API TEST CLIENT - USER CRUD OPERATIONS");
        System.out.println("Testing with " + numberOfUsers + " users");
        System.out.println("=".repeat(60));

        client.runTests(numberOfUsers);
    }

    public void runTests(int numberOfUsers) {
        try {
            // Step 1: Create admin user and get token
            createAdminAndGetToken();

            // Step 2: Test user operations
            testUserCrudOperations(numberOfUsers);

            // Step 3: Print results
            printResults();

        } catch (Exception e) {
            System.err.println("Error during test execution: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createAdminAndGetToken() {
        System.out.println("\n1. Setting up admin user...");

        try {
            // Try to create admin user
            Map<String, Object> adminData = new HashMap<>();
            adminData.put("name", "Test Admin");
            adminData.put("login", "testadmin");
            adminData.put("password", "admin123");
            adminData.put("role", "ADMIN");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(adminData, headers);

            try {
                restTemplate.postForEntity(AUTH_REGISTER_URL, request, String.class);
                System.out.println("   ✓ Admin user created successfully");
            } catch (HttpClientErrorException.Conflict e) {
                System.out.println("   ⚠ Admin user already exists");
            }

            // Login and get token
            Map<String, String> loginData = new HashMap<>();
            loginData.put("login", "testadmin");
            loginData.put("password", "admin123");

            HttpEntity<Map<String, String>> loginRequest = new HttpEntity<>(loginData, headers);
            ResponseEntity<String> loginResponse = restTemplate.postForEntity(AUTH_LOGIN_URL, loginRequest, String.class);

            JsonNode loginResult = objectMapper.readTree(loginResponse.getBody());
            adminToken = loginResult.get("token").asText();

            System.out.println("   ✓ Admin token obtained successfully");

        } catch (Exception e) {
            throw new RuntimeException("Failed to setup admin user: " + e.getMessage(), e);
        }
    }

    private void testUserCrudOperations(int numberOfUsers) {
        System.out.println("\n2. Testing CRUD operations for " + numberOfUsers + " users...");

        String[] createdUserIds = new String[numberOfUsers];
        Random random = new Random();

        // CREATE operations
        System.out.println("\n   Creating users...");
        for (int i = 0; i < numberOfUsers; i++) {
            String userId = createTestUser("testuser" + i, "User " + i, "password" + i);
            createdUserIds[i] = userId;
        }

        // READ operations
        System.out.println("\n   Reading users...");
        for (String userId : createdUserIds) {
            if (userId != null) {
                readUser(userId);
            }
        }

        // UPDATE operations
        System.out.println("\n   Updating users...");
        for (String userId : createdUserIds) {
            if (userId != null) {
                updateUser(userId, "Updated Name " + random.nextInt(1000));
            }
        }

        // DELETE operations
        System.out.println("\n   Deleting users...");
        for (String userId : createdUserIds) {
            if (userId != null) {
                deleteUser(userId);
            }
        }
    }

    private String createTestUser(String login, String name, String password) {
        try {
            Map<String, Object> userData = new HashMap<>();
            userData.put("name", name);
            userData.put("login", login);
            userData.put("password", password);
            userData.put("role", "USER");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(userData, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(AUTH_REGISTER_URL, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                successCount.incrementAndGet();
                System.out.println("     ✓ Created user: " + login);
                return login; // In this case, we'll use login as ID for simplicity
            }

        } catch (HttpClientErrorException e) {
            failureCount.incrementAndGet();
            System.out.println("     ✗ Failed to create user " + login + ": " + e.getStatusCode());
        } catch (Exception e) {
            failureCount.incrementAndGet();
            System.out.println("     ✗ Failed to create user " + login + ": " + e.getMessage());
        }

        return null;
    }

    private void readUser(String login) {
        try {
            // First login as the user to get their token
            Map<String, String> loginData = new HashMap<>();
            loginData.put("login", login);
            loginData.put("password", "password" + login.replace("testuser", ""));

            HttpHeaders loginHeaders = new HttpHeaders();
            loginHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> loginRequest = new HttpEntity<>(loginData, loginHeaders);

            ResponseEntity<String> loginResponse = restTemplate.postForEntity(AUTH_LOGIN_URL, loginRequest, String.class);
            JsonNode loginResult = objectMapper.readTree(loginResponse.getBody());
            String userToken = loginResult.get("token").asText();

            // Now get current user info
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(userToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    USERS_URL + "/currentUser",
                    HttpMethod.GET,
                    request,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                successCount.incrementAndGet();
                System.out.println("     ✓ Read user: " + login);
            }

        } catch (Exception e) {
            failureCount.incrementAndGet();
            System.out.println("     ✗ Failed to read user " + login + ": " + e.getMessage());
        }
    }

    private void updateUser(String userId, String newName) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> updateData = new HashMap<>();
            updateData.put("name", newName);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(updateData, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    USERS_URL + "/" + userId,
                    HttpMethod.PUT,
                    request,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                successCount.incrementAndGet();
                System.out.println("     ✓ Updated user: " + userId);
            }

        } catch (Exception e) {
            failureCount.incrementAndGet();
            System.out.println("     ✗ Failed to update user " + userId + ": " + e.getMessage());
        }
    }

    private void deleteUser(String userId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    USERS_URL + "/" + userId,
                    HttpMethod.DELETE,
                    request,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                successCount.incrementAndGet();
                System.out.println("     ✓ Deleted user: " + userId);
            }

        } catch (Exception e) {
            failureCount.incrementAndGet();
            System.out.println("     ✗ Failed to delete user " + userId + ": " + e.getMessage());
        }
    }

    private void printResults() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST RESULTS SUMMARY");
        System.out.println("=".repeat(60));
        System.out.println("✓ Successful operations: " + successCount.get());
        System.out.println("✗ Failed operations: " + failureCount.get());
        System.out.println("Total operations: " + (successCount.get() + failureCount.get()));

        double successRate = (double) successCount.get() / (successCount.get() + failureCount.get()) * 100;
        System.out.println("Success rate: " + String.format("%.2f", successRate) + "%");

        if (successRate >= 90) {
            System.out.println("🎉 EXCELLENT! API is working very well.");
        } else if (successRate >= 75) {
            System.out.println("👍 GOOD! API is working with minor issues.");
        } else if (successRate >= 50) {
            System.out.println("⚠️  WARNING! API has significant issues.");
        } else {
            System.out.println("❌ CRITICAL! API has major problems.");
        }

        System.out.println("=".repeat(60));
    }
}