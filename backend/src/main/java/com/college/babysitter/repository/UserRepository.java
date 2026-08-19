package com.college.babysitter.repository;

import com.college.babysitter.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Account persistence. Email is the login identity, hence unique lookups.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds an account by login email.
     *
     * @param email login email
     * @return the account, if present
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks whether an email is already registered (signup guard).
     *
     * @param email login email
     * @return true when taken
     */
    boolean existsByEmail(String email);
}
