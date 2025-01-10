package com.example.gym.repository;

import com.example.gym.model.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    // Additional query methods can be added here if needed
	Optional<User> findByUsername(String username);
	Optional<User> findByEmail(String email);
}
