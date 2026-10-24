package com.example.hotel.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "processed_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessedRequest {
    @Id
    private String requestId;

    @Column(nullable = false)
    private String status;
}