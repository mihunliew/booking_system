package com.n2n.booking.service;

import com.n2n.booking.dto.AdminDashboardDTO;
import com.n2n.booking.dto.AuthDTOs;
import com.n2n.booking.dto.BookingDTOs;
import com.n2n.booking.entity.Booking;
import com.n2n.booking.entity.User;
import com.n2n.booking.enums.BookingStatus;
import com.n2n.booking.enums.Role;
import com.n2n.booking.exception.ResourceNotFoundException;
import com.n2n.booking.repository.AdminRoleRepository;
import com.n2n.booking.repository.BookingRepository;
import com.n2n.booking.repository.ProductRepository;
import com.n2n.booking.repository.UserRepository;
import com.n2n.booking.exception.BadRequestException;
import com.n2n.booking.entity.AdminRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;
    private final AuthService authService;
    private final AdminRoleRepository adminRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminDashboardDTO getDashboardData() {
        Long totalUsers = userRepository.count();
        Long totalProducts = productRepository.count();
        Long totalBookings = bookingRepository.count();
        Long pendingBookings = bookingRepository.countByStatus(BookingStatus.PENDING);
        BigDecimal totalRevenue = bookingRepository.calculateTotalRevenue();
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        List<Booking> recentBookings = bookingRepository.findAllByOrderByCreatedAtDesc();
        List<BookingDTOs.BookingResponse> recentDTOs = recentBookings.stream()
                .limit(5)
                .map(bookingService::mapToDTO)
                .collect(Collectors.toList());

        return AdminDashboardDTO.builder()
                .totalUsers(totalUsers)
                .totalProducts(totalProducts)
                .totalBookings(totalBookings)
                .pendingBookings(pendingBookings)
                .totalRevenue(totalRevenue)
                .recentBookings(recentDTOs)
                .build();
    }

    public List<AuthDTOs.UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(authService::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AuthDTOs.UserResponse createUser(AuthDTOs.AdminCreateUserRequest request, Long currentUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        if (request.getRole() == Role.ROLE_SUPERADMIN && currentUser.getRole() != Role.ROLE_SUPERADMIN) {
            throw new BadRequestException("Only SUPERADMIN can create a SUPERADMIN");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already in use");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(request.getRole())
                .build();

        if (request.getRole() == Role.ROLE_ADMIN && request.getAdminRoleId() != null) {
            AdminRole adminRole = adminRoleRepository.findById(request.getAdminRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Admin role not found"));
            user.setAdminRole(adminRole);
        }

        User savedUser = userRepository.save(user);
        return authService.mapToUserResponse(savedUser);
    }

    @Transactional
    public AuthDTOs.UserResponse updateUserRole(Long userId, Role role, Long adminRoleId, Long currentUserId) {
        if (userId.equals(currentUserId)) {
            throw new BadRequestException("You cannot modify your own role");
        }

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        if (targetUser.getRole() == Role.ROLE_SUPERADMIN && currentUser.getRole() != Role.ROLE_SUPERADMIN) {
            throw new BadRequestException("Only SUPERADMIN can modify a SUPERADMIN's role");
        }

        if (role == Role.ROLE_SUPERADMIN && currentUser.getRole() != Role.ROLE_SUPERADMIN) {
            throw new BadRequestException("Only SUPERADMIN can assign the SUPERADMIN role");
        }

        targetUser.setRole(role);

        if (role == Role.ROLE_ADMIN && adminRoleId != null) {
            AdminRole adminRole = adminRoleRepository.findById(adminRoleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Admin role not found"));
            targetUser.setAdminRole(adminRole);
        } else {
            targetUser.setAdminRole(null);
        }

        User updated = userRepository.save(targetUser);
        return authService.mapToUserResponse(updated);
    }

    @Transactional
    public void deleteUser(Long userId, Long currentUserId) {
        if (userId.equals(currentUserId)) {
            throw new BadRequestException("You cannot delete yourself");
        }
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }
        userRepository.deleteById(userId);
    }

    public List<BookingDTOs.BookingResponse> getAllBookings() {
        return bookingRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(bookingService::mapToDTO)
                .collect(Collectors.toList());
    }

    public BookingDTOs.BookingResponse getBookingById(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
        return bookingService.mapToDTO(booking);
    }

    @Transactional
    public BookingDTOs.BookingResponse updateBookingStatus(Long bookingId, BookingDTOs.UpdateStatusRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (request.getStatus() != null) {
            booking.setStatus(request.getStatus());
        }
        if (request.getPaymentStatus() != null) {
            booking.setPaymentStatus(request.getPaymentStatus());
        }

        Booking updated = bookingRepository.save(booking);
        return bookingService.mapToDTO(updated);
    }
}
