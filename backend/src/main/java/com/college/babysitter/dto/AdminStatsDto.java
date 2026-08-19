package com.college.babysitter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Admin dashboard counters: users, pending sitters and total bookings.
 */
@Data
@AllArgsConstructor
public class AdminStatsDto {

    private long totalUsers;
    private long pendingBabysitters;
    private long totalBookings;

    /**
     * Builds the stats payload.
     *
     * @param totalUsers registered account count
     * @param pendingBabysitters sitters awaiting verification
     * @param totalBookings platform booking count
     * @return the stats payload
     */
    public static AdminStatsDto of(long totalUsers, long pendingBabysitters, long totalBookings) {
        return new AdminStatsDto(totalUsers, pendingBabysitters, totalBookings);
    }
}
