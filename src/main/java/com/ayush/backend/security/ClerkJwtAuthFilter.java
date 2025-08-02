package com.ayush.backend.security;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ClerkJwtAuthFilter extends OncePerRequestFilter {

    @Value("${clerk.issuer}")
    private String clerkIssuer;

    @Autowired
    private ClerkJwksProvider jwksProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

                if (request.getRequestURI().contains("/api/webhooks")) {
                    filterChain.doFilter(request, response);
                    return;
                }

        String authHeader = request.getHeader("Authorization");
        System.out.println("Authorization Header: " + authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("Invalid or missing Authorization header");
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Authorization header missing/Invalid");
            return;
        }

        try {
            String token = authHeader.substring(7);
            System.out.println("JWT Token: " + token);

            String[] chunks = token.split("\\.");
            if (chunks.length < 2) {
                System.out.println("Malformed JWT: less than 2 chunks");
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Malformed JWT");
                return;
            }

            String headerJson = new String(Base64.getUrlDecoder().decode(chunks[0]));
            System.out.println("Decoded JWT Header: " + headerJson);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode headerNode = mapper.readTree(headerJson);
            String kid = headerNode.get("kid").asText();
            System.out.println("KID from Header: " + kid);

            PublicKey publicKey = jwksProvider.getPublicKey(kid);
            System.out.println("Public Key Retrieved");

            Claims claims = Jwts.parser()
                                .clockSkewSeconds(120) 
                                .verifyWith(publicKey)
                                .requireIssuer(clerkIssuer)
                                .build()
                                .parseSignedClaims(token)
                                .getPayload();

            String clerkUserId = claims.getSubject();
            System.out.println("Clerk User ID from Token: " + clerkUserId);

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            clerkUserId,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
                    );

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            System.out.println("Authentication set successfully");

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            System.out.println("Exception during JWT validation: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid Jwt token");
        }
    }
}
