package com.example.hotel.repository;

import com.example.hotel.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    @Query("""
            SELECT r FROM Room r
            WHERE r.available = true
            AND r.id NOT IN (
                SELECT rl.roomId FROM RoomLock rl
                WHERE rl.startDate < :endDate AND rl.endDate > :startDate
            )
            ORDER BY r.timesBooked ASC, r.id ASC
            """)
    List<Room> findAvailableRoomsRecommended(@Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    @Query("""
            SELECT r FROM Room r
            WHERE r.available = true
            AND r.id NOT IN (
                SELECT rl.roomId FROM RoomLock rl
                WHERE rl.startDate < :endDate AND rl.endDate > :startDate
            )
            """)
    List<Room> findAvailableRooms(@Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate);
}