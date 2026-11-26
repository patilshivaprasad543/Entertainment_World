package com.example.demo.repository;

import com.example.demo.model.UserAccount;
import com.example.demo.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByEmailAndRole(String email, UserRole role);

    boolean existsByEmailAndRole(String email, UserRole role);

    List<UserAccount> findByRoleInOrderByIdDesc(Collection<UserRole> roles);
}
