package com.bookings.controller;

import com.bookings.models.Booking;
import com.bookings.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<Booking> getBooking(@PathVariable Long id) {
        return this.bookingService.getBooking(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Booking> create(@Valid @RequestBody Booking booking) {
        return new ResponseEntity<>(this.bookingService.createBooking(booking), HttpStatus.CREATED);
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<Booking> update(@Valid @RequestBody Booking booking, @PathVariable Long id) {
        booking.setId(id);
        Booking updatedBooking = this.bookingService.updateBooking(booking);
        return ResponseEntity.ok(updatedBooking);
    }

    @DeleteMapping(path = "/{id}/cancel")
    public void cancel(@PathVariable Long id) {
        this.bookingService.cancelBooking(id);
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        this.bookingService.deleteBooking(id);
    }
}
