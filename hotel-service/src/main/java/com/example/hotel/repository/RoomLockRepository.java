package com.example.hotel.repository;

import com.example.hotel.entity.RoomLock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomLockRepository extends JpaRepository<RoomLock, Long> {
    Optional<RoomLock> findByBookingId(Long bookingId);
}
