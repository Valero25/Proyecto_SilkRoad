package edu.dosw.parcial.core.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.dosw.parcial.core.models.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
