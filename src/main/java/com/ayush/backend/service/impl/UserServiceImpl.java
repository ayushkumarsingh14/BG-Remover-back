package com.ayush.backend.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ayush.backend.dto.UserDTO;
import com.ayush.backend.model.User;
import com.ayush.backend.repo.UserRepo;
import com.ayush.backend.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Autowired
    private final UserRepo userRepo;
    
    @Override
    public UserDTO saveUser(UserDTO userDTO) {
        Optional<User> user = userRepo.findByClerkId(userDTO.getClerkId());
        if (user.isPresent()) {
            User existingUser = user.get();
            existingUser.setEmail(userDTO.getEmail());
            existingUser.setFirstName(userDTO.getFirstName());
            existingUser.setLastName(userDTO.getLastName());
            existingUser.setPhotoUrl(userDTO.getPhotoUrl());
            if (userDTO.getCredits() != null) {
                existingUser.setCredits(userDTO.getCredits());
            }
            existingUser = userRepo.save(existingUser);
            return mapToDTO(existingUser);
        }
        User newUser = mapToEntity(userDTO);
        userRepo.save(newUser);
        return mapToDTO(newUser);
    }
    
    private UserDTO mapToDTO(User user){
        return UserDTO.builder()
                   .clerkId(user.getClerkId())
                   .credits(user.getCredits())
                   .email(user.getEmail())
                   .firstName(user.getFirstName())
                   .lastName(user.getLastName())
                   .build();
    }

     private User mapToEntity(UserDTO userDTO){
        return User.builder()
            .clerkId(userDTO.getClerkId())
            .email(userDTO.getEmail())
            .firstName(userDTO.getFirstName())
            .lastName(userDTO.getLastName())
            .photoUrl(userDTO.getPhotoUrl())
            .build();
   
    }

    public UserDTO getUserByClerkId(String clerkId){
        User user = userRepo.findByClerkId(clerkId)
                            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return mapToDTO(user);
    }

    @Override
    public void deleteUserByClerkId(String clerkId) {
        User user = userRepo.findByClerkId(clerkId)
                            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        userRepo.delete(user);
    }
}