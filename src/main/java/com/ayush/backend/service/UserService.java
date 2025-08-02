package com.ayush.backend.service;

import com.ayush.backend.dto.UserDTO;

public interface UserService {
    UserDTO saveUser(UserDTO userDTO);
    UserDTO getUserByClerkId(String clerkId);
    void deleteUserByClerkId(String clerkId);
}
