package com.urlshortener.service;

import com.urlshortener.dto.request.UpdateUserRequest;
import com.urlshortener.dto.response.UserResponse;
import com.urlshortener.exception.BadRequestException;
import com.urlshortener.exception.ResourceNotFoundException;
import com.urlshortener.model.User;
import com.urlshortener.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    public UserResponse getCurrentUser(String email) {
        User user = findByEmail(email);
        return UserResponse.fromUser(user);
    }

    public UserResponse updateUser(String email, UpdateUserRequest request) {
        User user = findByEmail(email);

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }

        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            if (request.getCurrentPassword() == null ||
                    !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new BadRequestException("Current password is incorrect");
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);

        log.info("User updated: {}", email);
        return UserResponse.fromUser(updatedUser);
    }

    public UserResponse uploadProfilePicture(String email, MultipartFile file) {
        User user = findByEmail(email);

        String filePath = fileStorageService.storeFile(file, "profiles");
        user.setProfilePictureUrl(filePath);
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);

        log.info("Profile picture uploaded for user: {}", email);
        return UserResponse.fromUser(updatedUser);
    }

    public void deleteUser(String email) {
        User user = findByEmail(email);
        user.setEnabled(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("User account disabled: {}", email);
    }
}
