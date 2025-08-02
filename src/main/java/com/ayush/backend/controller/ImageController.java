package com.ayush.backend.controller;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ayush.backend.dto.UserDTO;
import com.ayush.backend.response.RemoveBGResponse;
import com.ayush.backend.service.RemoveBackgroundService;
import com.ayush.backend.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final RemoveBackgroundService removeBackgroundService;
    private final UserService userService;

    @PostMapping("/remove-background")
    public ResponseEntity<?> removeBackground(@RequestParam("file") MultipartFile file,
                                              Authentication authentication) {

        Map<String, Object> responseMap = new HashMap<>();

        try {
            System.out.println("=== [POST] /remove-background Called ===");

            if (authentication == null || authentication.getName() == null || authentication.getName().isEmpty()) {
                System.out.println("Authentication missing!");
                RemoveBGResponse response = RemoveBGResponse.builder()
                        .success(false)
                        .data("User does not have access to this resource")
                        .statusCode(HttpStatus.FORBIDDEN)
                        .build();
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            System.out.println("Authenticated user: " + authentication.getName());

            if (file == null || file.isEmpty()) {
                System.out.println("File not received or is empty");
                RemoveBGResponse response = RemoveBGResponse.builder()
                        .success(false)
                        .data("No file uploaded")
                        .statusCode(HttpStatus.BAD_REQUEST)
                        .build();
                return ResponseEntity.badRequest().body(response);
            }

            System.out.println("Received file: " + file.getOriginalFilename());

            UserDTO userDTO = userService.getUserByClerkId(authentication.getName());

            if (userDTO == null) {
                System.out.println("User not found in DB");
                RemoveBGResponse response = RemoveBGResponse.builder()
                        .success(false)
                        .data("User not found")
                        .statusCode(HttpStatus.NOT_FOUND)
                        .build();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            System.out.println("User credits: " + userDTO.getCredits());

            if (userDTO.getCredits() <= 0) {
                System.out.println("User has no credits left");
                responseMap.put("message", "No credit balance");
                responseMap.put("credits", userDTO.getCredits());

                RemoveBGResponse response = RemoveBGResponse.builder()
                        .statusCode(HttpStatus.BAD_REQUEST)
                        .success(false)
                        .data(responseMap)
                        .build();

                return ResponseEntity.badRequest().body(response);
            }

            // 🔥 Actual remove background call
            byte[] imageBytes = removeBackgroundService.removeBackground(file);
            System.out.println("Background removed successfully. Image bytes length: " + imageBytes.length);

            // 🔄 Base64 encode
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            System.out.println("Image converted to base64");

            // ⏬ Reduce 1 credit
            userDTO.setCredits(userDTO.getCredits() - 1);
            userService.saveUser(userDTO);
            System.out.println("User credits updated and saved");

            // 🎉 Send base64 image
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(base64Image);

        } catch (Exception e) {
            System.out.println("Exception occurred in /remove-background endpoint:");
            e.printStackTrace();

            RemoveBGResponse response = RemoveBGResponse.builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
                    .success(false)
                    .data("Something went wrong: " + e.getMessage()) // TEMP for debugging
                    .build();

            return ResponseEntity.internalServerError().body(response);
        }
    }
}
