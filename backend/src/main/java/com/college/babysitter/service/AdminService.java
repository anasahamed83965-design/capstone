package com.college.babysitter.service;

import com.college.babysitter.dto.BabysitterDto;
import com.college.babysitter.dto.BookingDto;
import com.college.babysitter.dto.UserDto;
import com.college.babysitter.exception.ApiException;
import com.college.babysitter.exception.ResourceNotFoundException;
import com.college.babysitter.model.Babysitter;
import com.college.babysitter.model.Booking;
import com.college.babysitter.model.Role;
import com.college.babysitter.model.User;
import com.college.babysitter.repository.AvailabilitySlotRepository;
import com.college.babysitter.repository.BabysitterRepository;
import com.college.babysitter.repository.BookingRepository;
import com.college.babysitter.repository.NotificationRepository;
import com.college.babysitter.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Platform-operator logic: sitter verification, booking oversight and
 * dashboard counters. All callers are admin-only via the controller.
 */
@Service
public class AdminService {

    private final BabysitterRepository babysitterRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final AvailabilitySlotRepository slotRepository;

    /**
     * Wires the admin collaborators (constructor injection keeps the class unit-testable).
     *
     * @param babysitterRepository sitter-profile persistence
     * @param bookingRepository booking oversight queries
     * @param userRepository user management
     * @param notificationRepository inbox cleanup on user deletion
     * @param slotRepository slot cleanup on sitter deletion
     */
    public AdminService(BabysitterRepository babysitterRepository,
                        BookingRepository bookingRepository,
                        UserRepository userRepository,
                        NotificationRepository notificationRepository,
                        AvailabilitySlotRepository slotRepository) {
        this.babysitterRepository = babysitterRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.slotRepository = slotRepository;
    }

    /**
     * Lists sitter profiles waiting for verification.
     *
     * @return unverified sitters
     */
    @Transactional(readOnly = true)
    public List<BabysitterDto> pendingBabysitters() {
        return babysitterRepository.findAll().stream()
                .filter(b -> !b.isVerified())
                .map(BabysitterDto::from)
                .toList();
    }

    /**
     * Approves or rejects a sitter. Approval publishes the profile to the
     * public parent directory; rejection keeps it hidden.
     *
     * @param babysitterId sitter id
     * @param approved true to approve, false to reject
     * @return the updated profile
     */
    @Transactional
    public BabysitterDto verify(Long babysitterId, boolean approved) {
        Babysitter babysitter = babysitterRepository.findById(babysitterId)
                .orElseThrow(() -> new ResourceNotFoundException("Babysitter not found"));
        babysitter.setVerified(approved);
        return BabysitterDto.from(babysitterRepository.save(babysitter));
    }

    /**
     * Lists every booking on the platform, newest first.
     *
     * @return all bookings
     */
    @Transactional(readOnly = true)
    public List<BookingDto> allBookings() {
        return bookingRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(BookingDto::from)
                .toList();
    }

    /**
     * Counts all registered accounts for the dashboard.
     *
     * @return total user count
     */
    public long userCount() {
        return userRepository.count();
    }

    /**
     * Lists every sitter profile, verified or not.
     *
     * @return all sitters
     */
    @Transactional(readOnly = true)
    public List<BabysitterDto> allBabysitters() {
        return babysitterRepository.findAll().stream()
                .map(BabysitterDto::from)
                .toList();
    }

    /**
     * Lists every registered account, newest first, for the admin user lists.
     *
     * @return all users
     */
    @Transactional(readOnly = true)
    public List<UserDto> allUsers() {
        return userRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(UserDto::from)
                .toList();
    }

    /**
     * Deletes an account and its dependent rows (sitter profile, slots,
     * inbox). Self-deletion and admin deletion are blocked, and accounts
     * with bookings are protected because history must survive.
     *
     * @param actorId id of the admin performing the deletion
     * @param userId id of the account to delete
     * @throws ResourceNotFoundException when the account does not exist
     * @throws ApiException with 400/403 for protected accounts
     */
    @Transactional
    public void deleteUser(Long actorId, Long userId) {
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (target.getId().equals(actorId)) {
            throw ApiException.badRequest("You cannot delete your own account");
        }
        if (target.getRole() == Role.ADMIN) {
            throw ApiException.forbidden("Admin accounts cannot be deleted");
        }
        if (!bookingRepository.findByParentIdOrderByCreatedAtDesc(userId).isEmpty()) {
            throw ApiException.badRequest("This account has bookings and cannot be deleted");
        }

        babysitterRepository.findByUserId(userId).ifPresent(profile -> {
            if (!bookingRepository.findByBabysitterIdOrderByCreatedAtDesc(profile.getId()).isEmpty()) {
                throw ApiException.badRequest("This sitter has bookings and cannot be deleted");
            }
            slotRepository.deleteAll(
                    slotRepository.findByBabysitterIdOrderByStartTimeAsc(profile.getId()));
            babysitterRepository.delete(profile);
        });

        notificationRepository.deleteAll(
                notificationRepository.findByUserIdOrderByCreatedAtDesc(userId));
        userRepository.delete(target);
    }
}
