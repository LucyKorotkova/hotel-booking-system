package com.example.hotel.repository;

import com.example.hotel.entity.ProcessedRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedRequestRepository extends JpaRepository<ProcessedRequest, String> {
}