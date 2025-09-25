package jala.university.ds3.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component("appResponseBuilder")
public class ResponseBuilder {

    public ResponseEntity<?> buildResponse(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("status", status.value());

        return new ResponseEntity<>(response, status);
    }

    public ResponseEntity<Map<String, Object>> build(String message, HttpStatus status, String path) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("status", status.value());
        if (path != null) {
            response.put("path", path);
        }

        return new ResponseEntity<>(response, status);
    }

    public ResponseEntity<Map<String, Object>> build(String message, HttpStatus status) {
        return build(message, status, null);
    }
}