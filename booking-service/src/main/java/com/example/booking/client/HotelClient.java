package com.example.booking.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "hotel-service")
public interface HotelClient {

    @PostMapping("/api/rooms/{id}/confirm-availability")
    Boolean confirmAvailability(@PathVariable("id") Long roomId, @RequestBody Map<String, Object> body);

    @PostMapping("/api/rooms/{id}/release")
    void releaseRoom(@PathVariable("id") Long roomId, @RequestBody Map<String, Object> body);

    @GetMapping("/api/rooms/recommend")
    List<Map<String, Object>> getRecommendedRooms(@RequestParam("startDate") String startDate,
                                                  @RequestParam("endDate") String endDate);
}
