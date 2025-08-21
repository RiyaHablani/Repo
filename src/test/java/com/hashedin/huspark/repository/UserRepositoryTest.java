package com.hashedin.huspark.repository;

import com.hashedin.huspark.entity.Role;
import com.hashedin.huspark.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setName("John Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPassword("password123");
        testUser.setRole(Role.MEMBER);
    }

    @Test
    void testFindByEmail_WhenUserExists_ShouldReturnUser() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        Optional<User> found = userRepository.findByEmail("john.doe@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("john.doe@example.com");
        assertThat(found.get().getName()).isEqualTo("John Doe");
    }

    @Test
    void testFindByEmail_WhenUserDoesNotExist_ShouldReturnEmpty() {
        // When
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void testExistsByEmail_WhenUserExists_ShouldReturnTrue() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        boolean exists = userRepository.existsByEmail("john.doe@example.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void testExistsByEmail_WhenUserDoesNotExist_ShouldReturnFalse() {
        // When
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void testSaveUser_ShouldPersistUser() {
        // When
        User savedUser = userRepository.save(testUser);

        // Then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(savedUser.getName()).isEqualTo("John Doe");
    }
}
