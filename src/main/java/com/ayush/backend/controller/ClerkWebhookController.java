package com.ayush.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ayush.backend.dto.UserDTO;
import com.ayush.backend.response.RemoveBGResponse;
import com.ayush.backend.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class ClerkWebhookController {

    private final UserService userService;

    @Value("${clerk.webhook.secret}")
    private String webhooksSecret;

    @PostMapping("/clerk")
    public ResponseEntity<?> handleClerkWebhook(
            @RequestHeader("svix-id") String svixId,
            @RequestHeader("svix-timestamp") String svixTimestamp,
            @RequestHeader("svix-signature") String svixSignature,
            @RequestBody String payload
    ) {
        try {
            System.out.println("🔐 Webhook called!");
            System.out.println("svix-id: " + svixId);
            System.out.println("svix-timestamp: " + svixTimestamp);
            System.out.println("svix-signature: " + svixSignature);
            System.out.println("Payload:\n" + payload);

            boolean isValid = verifyWebhookSignature(svixId, svixTimestamp, svixSignature, payload);
            if (!isValid) {
                System.out.println("❌ Invalid webhook signature!");
                RemoveBGResponse response = RemoveBGResponse.builder()
                        .statusCode(HttpStatus.UNAUTHORIZED)
                        .data("Invalid webhook signature")
                        .success(false)
                        .build();
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(payload);
            String eventType = rootNode.get("type").asText();
            JsonNode dataNode = rootNode.path("data");

            System.out.println("📩 Event Type: " + eventType);

            switch (eventType) {
                case "user.created":
                    handleUserCreated(dataNode);
                    break;

                case "user.updated":
                    handleUserUpdated(dataNode);
                    break;

                case "user.deleted":
                    handleUserDeleted(dataNode);
                    break;

                default:
                    System.out.println("⚠️ Unhandled event type: " + eventType);
            }

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            e.printStackTrace();
            RemoveBGResponse response = RemoveBGResponse.builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
                    .data("Something went wrong while processing webhook")
                    .success(false)
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private void handleUserCreated(JsonNode data) {
        try {
            UserDTO newUser = UserDTO.builder()
                .clerkId(data.path("id").asText())
                .email(data.path("email_addresses").path(0).path("email_address").asText())
                .firstName(data.path("first_name").asText())
                .lastName(data.path("last_name").asText())
                .photoUrl(data.path("image_url").asText())
                .build();

            System.out.println("✅ Creating user: " + newUser);
            userService.saveUser(newUser);
        } catch (Exception e) {
            System.out.println("❌ Error while creating user: " + e.getMessage());
        }
    }

    private void handleUserUpdated(JsonNode data) {
        try {
            String clerkId = data.path("id").asText();
            UserDTO existingUser = userService.getUserByClerkId(clerkId);

            existingUser.setEmail(data.path("email_addresses").path(0).path("email_address").asText());
            existingUser.setFirstName(data.path("first_name").asText());
            existingUser.setLastName(data.path("last_name").asText());
            existingUser.setPhotoUrl(data.path("image_url").asText());

            System.out.println("🔁 Updating user: " + existingUser);
            userService.saveUser(existingUser);
        } catch (Exception e) {
            System.out.println("❌ Error while updating user: " + e.getMessage());
        }
    }

    private void handleUserDeleted(JsonNode data) {
        try {
            String clerkId = data.path("id").asText();
            System.out.println("🗑️ Deleting user with Clerk ID: " + clerkId);
            userService.deleteUserByClerkId(clerkId);
        } catch (Exception e) {
            System.out.println("❌ Error while deleting user: " + e.getMessage());
        }
    }

    private boolean verifyWebhookSignature(String svixId, String svixTimestamp, String svixSignature, String payload) {
        System.out.println("🧪 Verifying webhook signature... (dummy true returned)");
        return true;
    }
}
