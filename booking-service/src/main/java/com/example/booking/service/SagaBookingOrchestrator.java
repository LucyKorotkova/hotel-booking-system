package com.example.booking.service;

import com.example.booking.client.HotelClient;
import com.example.booking.entity.Booking;
import com.example.booking.entity.BookingStatus;
import com.example.booking.repository.BookingRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaBookingOrchestrator {

    private final BookingRepository bookingRepository;
    private final HotelClient hotelClient;

    @Transactional
    public Booking createBookingSaga(Long userId, Long roomId, boolean autoSelect,
                                     String startDateStr, String endDateStr) {
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);

        if (!startDate.isBefore(endDate)) {
            throw new IllegalArgumentException("startDate должна быть раньше endDate");
        }

        if (autoSelect) {
            List<Map<String, Object>> recommended = hotelClient.getRecommendedRooms(startDateStr, endDateStr);
            if (recommended.isEmpty()) {
                throw new IllegalStateException("Нет доступных номеров на указанные даты");
            }
            roomId = ((Number) recommended.get(0).get("id")).longValue();
            log.info("Автоподбор выбрал roomId={}", roomId);
        }

        Booking booking = Booking.builder()
                .userId(userId)
                .roomId(roomId)
                .startDate(startDate)
                .endDate(endDate)
                .status(BookingStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        booking = bookingRepository.save(booking);

        String requestId = UUID.randomUUID().toString();
        log.info("[BookingID={}] Создано бронирование PENDING, requestId={}", booking.getId(), requestId);

        try {
            boolean confirmed = executeRemoteConfirm(roomId, booking.getId(),
                    startDateStr, endDateStr, requestId);
            if (confirmed) {
                booking.setStatus(BookingStatus.CONFIRMED);
                log.info("[BookingID={}] Статус CONFIRMED", booking.getId());
            } else {
                executeCompensation(booking, requestId, "Номер занят");
            }
        } catch (Exception ex) {
            log.error("[BookingID={}] Сбой при вызове Hotel Service: {}", booking.getId(), ex.getMessage());
            executeCompensation(booking, requestId, ex.getMessage());
        }

        return bookingRepository.save(booking);
    }

    @Retry(name = "hotelServiceRetry", fallbackMethod = "confirmFallback")
    @CircuitBreaker(name = "hotelServiceCB", fallbackMethod = "confirmFallback")
    public boolean executeRemoteConfirm(Long roomId, Long bookingId,
                                        String startDate, String endDate, String requestId) {
        Map<String, Object> req = new HashMap<>();
        req.put("bookingId", bookingId);
        req.put("startDate", startDate);
        req.put("endDate", endDate);
        req.put("requestId", requestId);
        return Boolean.TRUE.equals(hotelClient.confirmAvailability(roomId, req));
    }

    public boolean confirmFallback(Long roomId, Long bookingId,
                                   String startDate, String endDate, String requestId, Exception ex) {
        log.warn("Fallback: bookingId={}, причина: {}", bookingId, ex.getMessage());
        return false;
    }

    private void executeCompensation(Booking booking, String requestId, String reason) {
        booking.setStatus(BookingStatus.CANCELLED);
        log.warn("[BookingID={}] Компенсация, причина: {}", booking.getId(), reason);
        try {
            Map<String, Object> req = new HashMap<>();
            req.put("bookingId", booking.getId());
            req.put("requestId", requestId + "-release");
            hotelClient.releaseRoom(booking.getRoomId(), req);
            log.info("[BookingID={}] Компенсация выполнена", booking.getId());
        } catch (Exception ex) {
            log.error("[BookingID={}] Ошибка компенсации: {}", booking.getId(), ex.getMessage());
        }
    }

    @Transactional
    public void cancelBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Бронирование не найдено"));

        if (!booking.getUserId().equals(userId)) {
            throw new IllegalStateException("Нельзя отменить чужое бронирование");
        }

        String requestId = UUID.randomUUID().toString();
        executeCompensation(booking, requestId, "Отмена пользователем");
        bookingRepository.save(booking);
    }
}
