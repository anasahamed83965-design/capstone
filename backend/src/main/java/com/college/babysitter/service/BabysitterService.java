package com.college.babysitter.service;

import com.college.babysitter.dto.BabysitterDto;
import com.college.babysitter.dto.BabysitterProfileRequest;
import com.college.babysitter.exception.ApiException;
import com.college.babysitter.exception.ResourceNotFoundException;
import com.college.babysitter.model.Babysitter;
import com.college.babysitter.model.User;
import com.college.babysitter.repository.BabysitterRepository;
import com.college.babysitter.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Sitter directory and profile logic. Only admin-verified sitters ever reach
 * the public directory; everything else is the sitter's private area.
 */
@Service
public class BabysitterService {

    private final BabysitterRepository babysitterRepository;
    private final UserRepository userRepository;

    /**
     * Wires the sitter collaborators (constructor injection keeps the class unit-testable).
     *
     * @param babysitterRepository sitter-profile persistence
     * @param userRepository account lookup for profile creation
     */
    public BabysitterService(BabysitterRepository babysitterRepository, UserRepository userRepository) {
        this.babysitterRepository = babysitterRepository;
        this.userRepository = userRepository;
    }

    /**
     * Lists verified sitters for the public directory, best rated first.
     *
     * @param minRating minimum average rating, or null for no floor
     * @param maxRate maximum hourly rate, or null for no ceiling
     * @return verified sitters matching the filters
     */
    @Transactional(readOnly = true)
    public List<BabysitterDto> listVerified(Double minRating, BigDecimal maxRate) {
        double floor = minRating == null ? 0.0 : minRating;
        return babysitterRepository.searchDirectory(floor, maxRate)
                .stream()
                .map(BabysitterDto::from)
                .toList();
    }

    /**
     * Returns one verified sitter's public profile. Unverified profiles are
     * reported as not found so they stay invisible to parents.
     *
     * @param id babysitter id
     * @return the public profile
     * @throws ResourceNotFoundException if missing or unverified
     */
    @Transactional(readOnly = true)
    public BabysitterDto getPublicProfile(Long id) {
        Babysitter babysitter = babysitterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Babysitter not found"));
        if (!babysitter.isVerified()) {
            throw new ResourceNotFoundException("Babysitter not found");
        }
        return BabysitterDto.from(babysitter);
    }

    /**
     * Returns the sitter entity belonging to an account. Shared helper used
     * by slot and booking flows to resolve "the current sitter".
     *
     * @param userId account id
     * @return the sitter entity
     * @throws ApiException with 400 when the account has no sitter profile
     */
    public Babysitter getMyProfile(Long userId) {
        return babysitterRepository.findByUserId(userId)
                .orElseThrow(() -> ApiException.badRequest("You do not have a babysitter profile yet"));
    }

    /**
     * Creates the sitter profile on first save, updates it afterwards. A
     * sitter goes live as soon as the profile is complete (bio plus a real
     * rate), so slots never wait on admin approval. Admins keep the
     * verified flag for trust marking: rejecting hides the profile again.
     *
     * @param userId account id
     * @param request validated bio, experience and rate
     * @return the saved profile
     */
    @Transactional
    public BabysitterDto createOrUpdateProfile(Long userId, BabysitterProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Babysitter babysitter = babysitterRepository.findByUserId(userId).orElse(null);
        if (babysitter == null) {
            babysitter = new Babysitter(user, request.getBio(), request.getExperienceYears(), request.getHourlyRate());
        } else {
            babysitter.setBio(request.getBio());
            babysitter.setExperienceYears(request.getExperienceYears());
            babysitter.setHourlyRate(request.getHourlyRate());
        }
        if (isCompleteProfile(request)) {
            babysitter.setVerified(true);
        }
        return BabysitterDto.from(babysitterRepository.save(babysitter));
    }

    /**
     * Decides whether a saved profile is complete enough to go live:
     * a real bio plus an hourly rate above zero.
     *
     * @param request the saved profile payload
     * @return true when the profile may be published immediately
     */
    private boolean isCompleteProfile(BabysitterProfileRequest request) {
        return request.getBio() != null && !request.getBio().isBlank()
                && request.getHourlyRate() != null
                && request.getHourlyRate().compareTo(BigDecimal.ZERO) > 0;
    }
}
