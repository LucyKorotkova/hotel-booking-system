package com.example.hotel.service;

import com.example.hotel.entity.ProcessedRequest;
import com.example.hotel.entity.Room;
import com.example.hotel.entity.RoomLock;
import com.example.hotel.repository.ProcessedRequestRepository;
import com.example.hotel.repository.RoomLockRepository;
import com.example.hotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotelService {

    private final RoomRepository roomRepository;
    private final RoomLockRepository roomLockRepository;
    private final ProcessedRequestRepository processedRequestRepository;

    public List<Room> getRecommendedRooms(LocalDate startDate, LocalDate endDate) {
        return roomRepository.findAvailableRoomsRecommended(startDate, endDate);
    }

    public List<Room> getAvailableRooms(LocalDate startDate, LocalDate endDate) {
        return roomRepository.findAvailableRooms(startDate, endDate);
    }

    @Transactional
    public boolean confirmAvailability(Long roomId, Long bookingId,
                                       LocalDate startDate, LocalDate endDate, String requestId) {
        log.info("confirmAvailability: requestId={}, bookingId={}, roomId={}", requestId, bookingId, roomId);

        if (processedRequestRepository.existsById(requestId)) {
            log.warn("Повторный запрос requestId={}. Возврат успеха (идемпотентность).", requestId);
            return true;
        }

        List<Room> available = roomRepository.findAvailableRoomsRecommended(startDate, endDate);
        boolean isFree = available.stream().anyMatch(r -> r.getId().equals(roomId));

        if (!isFree) {
            log.error("Комната ID={} занята на даты {} - {}", roomId, startDate, endDate);
            return false;
        }

        RoomLock lock = RoomLock.builder()
                .roomId(roomId)
                .bookingId(bookingId)
                .startDate(startDate)
                .endDate(endDate)
                .build();
        roomLockRepository.save(lock);

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalStateException("Комната не найдена"));
        room.setTimesBooked(room.getTimesBooked() + 1);
        roomRepository.save(room);

        processedRequestRepository.save(new ProcessedRequest(requestId, "CONFIRMED"));
        log.info("Комната roomId={} успешно забронирована для bookingId={}", roomId, bookingId);
        return true;
    }

    @Transactional
    public void releaseRoom(Long roomId, Long bookingId, String requestId) {
        log.info("releaseRoom: Компенсация roomId={}, bookingId={}, requestId={}", roomId, bookingId, requestId);

        if (processedRequestRepository.existsById(requestId)) {
            log.warn("Повторный запрос компенсации requestId={}", requestId);
            return;
        }

        roomLockRepository.findByBookingId(bookingId).ifPresent(lock -> {
            roomLockRepository.delete(lock);
            Room room = roomRepository.findById(roomId).orElse(null);
            if (room != null && room.getTimesBooked() > 0) {
                room.setTimesBooked(room.getTimesBooked() - 1);
                roomRepository.save(room);
            }
        });

        processedRequestRepository.save(new ProcessedRequest(requestId, "RELEASED"));
        log.info("Компенсация roomId={} успешно завершена", roomId);
    }
}