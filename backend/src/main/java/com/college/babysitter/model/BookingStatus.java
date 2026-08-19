package com.college.babysitter.model;

/**
 * Booking lifecycle: PENDING (awaiting sitter) -&gt; CONFIRMED (sitter accepted)
 * -&gt; COMPLETED or CANCELLED. Only legal transitions are allowed.
 */
public enum BookingStatus {
    PENDING,
    CONFIRMED,
    COMPLETED,
    CANCELLED
}
