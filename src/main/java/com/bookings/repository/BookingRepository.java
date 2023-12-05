package com.bookings.repository;

import com.bookings.models.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("""
            select case when count(b)> 0 then true else false end from Booking b where
                 b.checkInDate >= :checkinDate and
                 b.checkOutDate >= :checkoutDate and
                 b.property.id = :propertyId and
                 b.canceled = :canceled""")
    boolean isBooked(@Param("checkinDate") LocalDate checkinDate, @Param("checkoutDate") LocalDate checkoutDate, @Param("propertyId") Long propertyId, @Param("canceled") boolean canceled);
}
