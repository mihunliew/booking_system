package com.n2n.booking.service;

import com.n2n.booking.dto.AuthDTOs;
import com.n2n.booking.dto.RoleDTOs;
import com.n2n.booking.entity.AdminPermission;
import com.n2n.booking.entity.User;
import com.n2n.booking.enums.Role;
import com.n2n.booking.exception.BadRequestException;
import com.n2n.booking.repository.UserRepository;
import com.n2n.booking.security.JwtTokenProvider;
import com.n2n.booking.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthDTOs.JwtResponse login(AuthDTOs.LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new BadRequestException("User not found"));

        return AuthDTOs.JwtResponse.builder()
                .token(jwt)
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .customRole(user.getAdminRole() != null ? user.getAdminRole().getName() : null)
                .permissions(mapPermissions(user))
                .build();
    }

    @Transactional
    public AuthDTOs.UserResponse signup(AuthDTOs.SignupRequest signupRequest) {
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new BadRequestException("Username is already taken!");
        }

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new BadRequestException("Email is already in use!");
        }

        User user = User.builder()
                .username(signupRequest.getUsername())
                .email(signupRequest.getEmail())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .fullName(signupRequest.getFullName())
                .phone(signupRequest.getPhone())
                .role(Role.ROLE_USER)
                .build();

        User savedUser = userRepository.save(user);

        return mapToUserResponse(savedUser);
    }

    public AuthDTOs.UserResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));
        return mapToUserResponse(user);
    }

    public AuthDTOs.UserResponse mapToUserResponse(User user) {
        return AuthDTOs.UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .customRole(user.getAdminRole() != null ? user.getAdminRole().getName() : null)
                .permissions(mapPermissions(user))
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null)
                .build();
    }

    private List<RoleDTOs.AdminPermissionDTO> mapPermissions(User user) {
        if (user.getRole() == Role.ROLE_SUPERADMIN) {
            // Superadmin has all permissions effectively, but we can just return null or empty 
            // and handle it on frontend or return a special flag.
            // Let's return empty and rely on ROLE_SUPERADMIN on frontend
            return new ArrayList<>();
        }
        if (user.getAdminRole() == null || user.getAdminRole().getPermissions() == null) {
            return new ArrayList<>();
        }
        return user.getAdminRole().getPermissions().stream().map(p -> RoleDTOs.AdminPermissionDTO.builder()
                .moduleName(p.getModuleName())
                .canCreate(p.isCanCreate())
                .canRead(p.isCanRead())
                .canUpdate(p.isCanUpdate())
                .canDelete(p.isCanDelete())
                .build()
        ).collect(Collectors.toList());
    }
}
