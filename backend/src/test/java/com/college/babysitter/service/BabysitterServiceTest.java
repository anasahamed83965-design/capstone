package com.college.babysitter.service;

import com.college.babysitter.dto.BabysitterDto;
import com.college.babysitter.dto.BabysitterProfileRequest;
import com.college.babysitter.exception.ApiException;
import com.college.babysitter.exception.ResourceNotFoundException;
import com.college.babysitter.model.Babysitter;
import com.college.babysitter.model.Role;
import com.college.babysitter.model.User;
import com.college.babysitter.repository.BabysitterRepository;
import com.college.babysitter.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BabysitterServiceTest {

    @Mock
    private BabysitterRepository babysitterRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BabysitterService babysitterService;

    private User sitterUser;
    private Babysitter babysitter;

    @BeforeEach
    void setUp() {
        sitterUser = new User("Ana Torres", "ana@example.com", "hashed", "555-0200", Role.BABYSITTER);
        sitterUser.setId(2L);
        babysitter = new Babysitter(sitterUser, "Friendly and reliable", 3, new BigDecimal("20.00"));
        babysitter.setId(5L);
        babysitter.setVerified(true);
    }

    @Test
    void listVerified_returnsDirectoryDtos() {
        when(babysitterRepository.searchDirectory(0.0, null)).thenReturn(List.of(babysitter));

        List<BabysitterDto> result = babysitterService.listVerified(null, null);

        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).getId());
        assertEquals("Ana Torres", result.get(0).getFullName());
    }

    @Test
    void getPublicProfile_verified_returnsDto() {
        when(babysitterRepository.findById(5L)).thenReturn(Optional.of(babysitter));

        BabysitterDto dto = babysitterService.getPublicProfile(5L);

        assertEquals(new BigDecimal("20.00"), dto.getHourlyRate());
        assertTrue(dto.isVerified());
    }

    @Test
    void getPublicProfile_unverified_throwsNotFound() {
        babysitter.setVerified(false);
        when(babysitterRepository.findById(5L)).thenReturn(Optional.of(babysitter));

        assertThrows(ResourceNotFoundException.class, () -> babysitterService.getPublicProfile(5L));
    }

    @Test
    void getPublicProfile_missing_throwsNotFound() {
        when(babysitterRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> babysitterService.getPublicProfile(9L));
    }

    @Test
    void getMyProfile_missing_throws() {
        when(babysitterRepository.findByUserId(2L)).thenReturn(Optional.empty());

        assertThrows(ApiException.class, () -> babysitterService.getMyProfile(2L));
    }

    @Test
    void createOrUpdateProfile_createsWhenMissing() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(sitterUser));
        when(babysitterRepository.findByUserId(2L)).thenReturn(Optional.empty());
        when(babysitterRepository.save(any(Babysitter.class))).thenAnswer(inv -> {
            Babysitter b = inv.getArgument(0);
            b.setId(5L);
            return b;
        });

        BabysitterDto dto = babysitterService.createOrUpdateProfile(2L, request("New bio", 1, "15.00"));

        assertEquals("New bio", dto.getBio());
        assertTrue(dto.isVerified(), "a complete profile should go live without admin wait");
        verify(babysitterRepository).save(any(Babysitter.class));
    }

    @Test
    void createOrUpdateProfile_updatesExisting() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(sitterUser));
        when(babysitterRepository.findByUserId(2L)).thenReturn(Optional.of(babysitter));
        when(babysitterRepository.save(any(Babysitter.class))).thenAnswer(inv -> inv.getArgument(0));

        BabysitterDto dto = babysitterService.createOrUpdateProfile(2L, request("Updated bio", 4, "25.00"));

        assertEquals("Updated bio", dto.getBio());
        assertEquals(4, dto.getExperienceYears());
        assertEquals(new BigDecimal("25.00"), dto.getHourlyRate());
        assertTrue(dto.isVerified());
    }

    @Test
    void createOrUpdateProfile_incompleteProfile_staysUnverified() {
        Babysitter draft = new Babysitter(sitterUser, "", 0, BigDecimal.ZERO);
        draft.setId(6L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(sitterUser));
        when(babysitterRepository.findByUserId(2L)).thenReturn(Optional.of(draft));
        when(babysitterRepository.save(any(Babysitter.class))).thenAnswer(inv -> inv.getArgument(0));

        BabysitterDto dto = babysitterService.createOrUpdateProfile(2L, request("", 0, "0.00"));

        assertFalse(dto.isVerified(), "empty profiles must stay out of the public directory");
    }

    private BabysitterProfileRequest request(String bio, int years, String rate) {
        BabysitterProfileRequest req = new BabysitterProfileRequest();
        req.setBio(bio);
        req.setExperienceYears(years);
        req.setHourlyRate(new BigDecimal(rate));
        return req;
    }
}
