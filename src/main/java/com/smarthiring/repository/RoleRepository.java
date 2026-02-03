package com.smarthiring.repository;

import com.smarthiring.entity.Role;
import com.smarthiring.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {


     // Find role by name
    Optional<Role> findByName(RoleName name);

    /**
     * Check if role exists by name
     */
    boolean existsByName(RoleName name);
}