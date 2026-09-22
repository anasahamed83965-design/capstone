package com.college.babysitter.controller;

import com.college.babysitter.dto.AdminStatsDto;
import com.college.babysitter.dto.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.college.babysitter.dto.BabysitterDto;
import com.college.babysitter.dto.BookingDto;
import com.college.babysitter.dto.UserDto;
import com.college.babysitter.security.AppUserPrincipal;
import com.college.babysitter.security.SecurityUtils;
import com.college.babysitter.service.AdminService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Platform-operator endpoints. The class-level {@code @PreAuthorize} keeps
 * every method admin-only, so verification and oversight stay in one place.
 */
@Tag(name = "Admin", description = "Verification, users, bookings and stats (ADMIN only)")
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Lists sitter profiles waiting for verification.
     *
     * @return unverified sitters
     */
    @GetMapping("/pending-babysitters")
    public ApiResponse<List<BabysitterDto>> pendingBabysitters() {
        return ApiResponse.ok(adminService.pendingBabysitters());
    }

    /**
     * Approves (or rejects) a sitter. Approval is what makes the profile
     * appear in the public parent directory.
     *
     * @param id sitter id
     * @param request approval flag
     * @return the updated profile
     */
    @PostMapping("/babysitters/{id}/verify")
    public ApiResponse<BabysitterDto> verify(@PathVariable Long id,
                                             @RequestBody AdminVerifyRequest request) {
        return ApiResponse.ok(adminService.verify(id, request.isApproved()),
                request.isApproved() ? "Babysitter verified" : "Babysitter rejected");
    }

    /**
     * Lists every booking on the platform, newest first.
     *
     * @return all bookings
     */
    @GetMapping("/bookings")
    public ApiResponse<List<BookingDto>> bookings() {
        return ApiResponse.ok(adminService.allBookings());
    }

    /**
     * Returns dashboard counters: users, pending sitters, total bookings.
     *
     * @return platform stats
     */
    @GetMapping("/stats")
    public ApiResponse<AdminStatsDto> stats() {
        return ApiResponse.ok(AdminStatsDto.of(
                adminService.userCount(),
                adminService.pendingBabysitters().size(),
                adminService.allBookings().size()));
    }

    /**
     * Lists every registered account, newest first. The frontend splits
     * them into parent and sitter columns.
     *
     * @return all users
     */
    @GetMapping("/users")
    public ApiResponse<List<UserDto>> users() {
        return ApiResponse.ok(adminService.allUsers());
    }

    /**
     * Deletes an account. Self-deletion, admin deletion and accounts with
     * bookings are refused with a clear message.
     *
     * @param authentication the admin's JWT authentication
     * @param id id of the account to delete
     * @return empty success envelope
     */
    @DeleteMapping("/users/{id}")
    public ApiResponse<Void> deleteUser(Authentication authentication, @PathVariable Long id) {
        AppUserPrincipal admin = SecurityUtils.currentUser(authentication);
        adminService.deleteUser(admin.getId(), id);
        return ApiResponse.ok(null, "User deleted");
    }

    /**
     * Body for the verify endpoint: {@code approved=true} publishes the
     * profile, {@code approved=false} keeps it hidden.
     */
    public static class AdminVerifyRequest {
        private boolean approved;

        /**
         * Returns the approval flag.
         *
         * @return true to approve, false to reject
         */
        public boolean isApproved() {
            return approved;
        }

        /**
         * Sets the approval flag.
         *
         * @param approved true to approve, false to reject
         */
        public void setApproved(boolean approved) {
            this.approved = approved;
        }
    }
}
