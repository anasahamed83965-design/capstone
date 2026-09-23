package com.college.babysitter.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;

/**
 * Sitter profile linked one-to-one to an account. Eager user fetch is
 * deliberate: every sitter read also needs the account name and phone.
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "babysitters")
public class Babysitter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(nullable = false)
    private int experienceYears;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @Column(nullable = false)
    private boolean verified;

    @Column(nullable = false)
    private double avgRating;

    /**
     * Creates a sitter profile. New profiles start unverified with a zero
     * rating until the admin approves them and parents review them.
     *
     * @param user owning account
     * @param bio public biography
     * @param experienceYears years of experience
     * @param hourlyRate hourly rate used for booking totals
     */
    public Babysitter(User user, String bio, int experienceYears, BigDecimal hourlyRate) {
        this.user = user;
        this.bio = bio;
        this.experienceYears = experienceYears;
        this.hourlyRate = hourlyRate;
    }
}
