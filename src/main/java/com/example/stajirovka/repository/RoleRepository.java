package com.example.stajirovka.repository;

import com.example.stajirovka.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    @Query(value = "select t from Role t where t.code=?1")
    Role findByCode(String code);
}
