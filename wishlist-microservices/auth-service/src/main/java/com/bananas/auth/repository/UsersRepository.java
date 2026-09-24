package com.bananas.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bananas.auth.entity.Users;

public interface UsersRepository extends JpaRepository<Users, Long> {
    Optional<Users> findFirstByEmail(String email);
    Optional<Users> findFirstByUsername(String username);
}
