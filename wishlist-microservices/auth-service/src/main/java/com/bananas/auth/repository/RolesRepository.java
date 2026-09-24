package com.bananas.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bananas.auth.entity.Roles;

public interface RolesRepository extends JpaRepository<Roles, Long> {
}
