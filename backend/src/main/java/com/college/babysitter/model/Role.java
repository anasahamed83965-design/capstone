package com.college.babysitter.model;

/**
 * Account roles. Parents book, sitters offer care, admins verify sitters
 * and oversee the platform. Stored as strings and mapped to ROLE_* authorities.
 */
public enum Role {
    PARENT,
    BABYSITTER,
    ADMIN
}
