package com.universityprinting.printing_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.universityprinting.printing_backend.dto.CreateUserRequest;
import com.universityprinting.printing_backend.dto.UserResponse;
import com.universityprinting.printing_backend.exception.DuplicateEmailException;
import com.universityprinting.printing_backend.model.Role;
import com.universityprinting.printing_backend.model.User;
import com.universityprinting.printing_backend.repository.UserRepository;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    @Test
    void createUser_Success() {
        CreateUserRequest request = new CreateUserRequest("Alice Smith", "alice@university.edu", "+1234567890");

        when(userRepository.existsByEmail("alice@university.edu")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("mock-id-123");
            return user;
        });

        UserResponse response = userService.createUser(request);

        assertNotNull(response);
        assertEquals("mock-id-123", response.id());
        assertEquals("Alice Smith", response.name());
        assertEquals("alice@university.edu", response.email());
        assertEquals("+1234567890", response.phone());
        assertEquals(Role.STUDENT, response.role());
        assertNotNull(response.createdAt());
        assertNotNull(response.updatedAt());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_DuplicateEmail_ThrowsDuplicateEmailException() {
        CreateUserRequest request = new CreateUserRequest("Alice Smith", "alice@university.edu", "+1234567890");

        when(userRepository.existsByEmail("alice@university.edu")).thenReturn(true);

        DuplicateEmailException exception = assertThrows(
            DuplicateEmailException.class,
            () -> userService.createUser(request)
        );

        assertEquals("User with email 'alice@university.edu' already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
}
