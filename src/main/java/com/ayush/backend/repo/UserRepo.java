package com.ayush.backend.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ayush.backend.model.User;

@Repository
public interface UserRepo extends JpaRepository<User, Long>{
    Optional<User> findByClerkId(String clerkId);
    Boolean existsByClerkId(String clerkId);
}
