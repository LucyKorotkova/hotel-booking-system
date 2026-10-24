package com.example.booking;

import com.example.booking.client.HotelClient;
import com.example.booking.entity.Booking;
import com.example.booking.entity.BookingStatus;
import com.example.booking.repository.BookingRepository;
import com.example.booking.service.SagaBookingOrchestrator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SagaBookingTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private HotelClient hotelClient;

    @InjectMocks
    private SagaBookingOrchestrator orchestrator;

    @Test
    @DisplayName("Успешное двухшаговое бронирование (PENDING -> CONFIRMED)")
    void testSuccessfulBookingSaga() {
        when(hotelClient.confirmAvailability(any(), any())).thenReturn(true);
        when(bookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Booking booking = orchestrator.createBookingSaga(1L, 101L, false, "2026-05-01", "2026-05-10");

        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        verify(hotelClient, times(1)).confirmAvailability(any(), any());
    }

    @Test
    @DisplayName("Отмена бронирования и компенсация при сбое (PENDING -> CANCELLED)")
    void testFailedBookingSagaWithCompensation() {
        when(hotelClient.confirmAvailability(any(), any())).thenReturn(false);
        when(bookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Booking booking = orchestrator.createBookingSaga(1L, 101L, false, "2026-05-01", "2026-05-10");

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        verify(hotelClient, times(1)).releaseRoom(any(), any());
    }
}