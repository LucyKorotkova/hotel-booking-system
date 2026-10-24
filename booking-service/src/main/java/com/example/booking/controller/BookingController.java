package com.example.booking.controller;

import com.example.booking.entity.Booking;
import com.example.booking.entity.User;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.UserRepository;
import com.example.booking.service.SagaBookingOrchestrator;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class BookingController {

    private final SagaBookingOrchestrator sagaOrchestrator;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @PostMapping("/booking")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Booking createBooking(@RequestBody BookingRequest req, Authentication auth) {
        Long userId = resolveUserId(auth);
        return sagaOrchestrator.createBookingSaga(
                userId, req.getRoomId(), req.isAutoSelect(),
                req.getStartDate(), req.getEndDate());
    }

    @GetMapping("/bookings")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public List<Booking> getMyBookings(Authentication auth) {
        Long userId = resolveUserId(auth);
        return bookingRepository.findByUserId(userId);
    }

    @GetMapping("/booking/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Booking getBookingById(@PathVariable Long id, Authentication auth) {
        Long userId = resolveUserId(auth);
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Бронирование не найдено"));
        if (!booking.getUserId().equals(userId) &&
                !auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new IllegalStateException("Нет доступа к чужому бронированию");
        }
        return booking;
    }

    @DeleteMapping("/booking/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id, Authentication auth) {
        Long userId = resolveUserId(auth);
        sagaOrchestrator.cancelBooking(id, userId);
        return ResponseEntity.noContent().build();
    }

    private Long resolveUserId(Authentication auth) {
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Пользователь не найден"));
        return user.getId();
    }

    @Data
    public static class BookingRequest {
        private Long roomId;
        private boolean autoSelect;
        private String startDate;
        private String endDate;
    }
}
