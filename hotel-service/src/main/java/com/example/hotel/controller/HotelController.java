package com.example.hotel.controller;

import com.example.hotel.entity.Hotel;
import com.example.hotel.entity.Room;
import com.example.hotel.repository.HotelRepository;
import com.example.hotel.repository.RoomRepository;
import com.example.hotel.service.HotelService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;

    @PostMapping("/hotels")
    @PreAuthorize("hasRole('ADMIN')")
    public Hotel createHotel(@RequestBody Hotel hotel) {
        return hotelRepository.save(hotel);
    }

    @GetMapping("/hotels")
    public List<Hotel> getHotels() {
        return hotelRepository.findAll();
    }

    @PostMapping("/rooms")
    @PreAuthorize("hasRole('ADMIN')")
    public Room createRoom(@RequestBody Room room) {
        return roomRepository.save(room);
    }

    @GetMapping("/rooms")
    public List<Room> getAvailableRooms(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return hotelService.getAvailableRooms(startDate, endDate);
    }

    @GetMapping("/rooms/recommend")
    public List<Room> getRecommended(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return hotelService.getRecommendedRooms(startDate, endDate);
    }

    @PostMapping("/rooms/{id}/confirm-availability")
    public ResponseEntity<Boolean> confirmAvailability(
            @PathVariable("id") Long roomId,
            @RequestBody AvailabilityRequest req) {
        boolean success = hotelService.confirmAvailability(
                roomId, req.getBookingId(), req.getStartDate(), req.getEndDate(), req.getRequestId());
        return success ? ResponseEntity.ok(true) : ResponseEntity.badRequest().body(false);
    }

    @PostMapping("/rooms/{id}/release")
    public ResponseEntity<Void> releaseRoom(
            @PathVariable("id") Long roomId,
            @RequestBody ReleaseRequest req) {
        hotelService.releaseRoom(roomId, req.getBookingId(), req.getRequestId());
        return ResponseEntity.ok().build();
    }

    @Data
    public static class AvailabilityRequest {
        private Long bookingId;
        private LocalDate startDate;
        private LocalDate endDate;
        private String requestId;
    }

    @Data
    public static class ReleaseRequest {
        private Long bookingId;
        private String requestId;
    }
}