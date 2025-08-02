package com.ayush.backend.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ayush.backend.dto.UserDTO;
import com.ayush.backend.response.RemoveBGResponse;
import com.ayush.backend.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<?> createOrUpdateUser(@RequestBody UserDTO userDTO, Authentication authentication) {
        try {
            System.out.println("🔥 Received request to create/update user:");
            System.out.println("🔐 Authenticated Clerk ID: " + authentication.getName());
            System.out.println("📦 Payload Clerk ID: " + userDTO.getClerkId());
            System.out.println("📧 Email: " + userDTO.getEmail());
            System.out.println("👤 First Name: " + userDTO.getFirstName());
            System.out.println("👤 Last Name: " + userDTO.getLastName());

            if (!authentication.getName().equals(userDTO.getClerkId())) {
                System.out.println("🚫 Clerk ID mismatch! Unauthorized access attempt.");
                RemoveBGResponse response = RemoveBGResponse.builder()
                        .success(false)
                        .data("User Unauthorised")
                        .statusCode(HttpStatus.FORBIDDEN)
                        .build();
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }

            UserDTO savedUser = userService.saveUser(userDTO);
            System.out.println("✅ User saved/updated successfully: " + savedUser);

            RemoveBGResponse response = RemoveBGResponse.builder()
                    .success(true)
                    .statusCode(HttpStatus.CREATED)
                    .data(savedUser)
                    .build();

            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (Exception e) {
            System.out.println("💥 Exception occurred in createOrUpdateUser: " + e.getMessage());
            RemoveBGResponse response = RemoveBGResponse.builder()
                    .success(false)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
                    .data(e.getMessage())
                    .build();
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/credits")
    public ResponseEntity<?> getUserCredits(Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null || authentication.getName().isEmpty()) {
                RemoveBGResponse response = RemoveBGResponse.builder()
                        .success(false)
                        .data("User does not have access to this resource")
                        .statusCode(HttpStatus.FORBIDDEN)
                        .build();
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }

            String clerkId = authentication.getName();
            UserDTO existingUser = userService.getUserByClerkId(clerkId);

            Map<String, Integer> map = new HashMap<>();
            map.put("credits", existingUser.getCredits());

            RemoveBGResponse response = RemoveBGResponse.builder()
                    .success(true)
                    .statusCode(HttpStatus.OK)
                    .data(map)
                    .build();

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            System.out.println("💥 Exception occurred in getUserCredits: " + e.getMessage());
            RemoveBGResponse response = RemoveBGResponse.builder()
                    .success(false)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
                    .data(e.getMessage())
                    .build();
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
