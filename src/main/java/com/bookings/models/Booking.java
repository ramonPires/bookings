package com.bookings.models;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "bookings")
public class Booking implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Future(message = "CheckIn date need to be in the future")
    @Column(name = "checkin_date", columnDefinition = "DATE", nullable = false)
    private LocalDate checkInDate;

    @Future(message = "CheckOut date need to be in the future")
    @Column(name = "checkout_date", columnDefinition = "DATE", nullable = false)
    private LocalDate checkOutDate;

    @NotNull(message = "Property is required")
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Embedded
    @JsonUnwrapped(enabled = false)
    @Valid
    private Guest guest;

    @Column(columnDefinition = "boolean default false")
    private Boolean canceled = Boolean.FALSE;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public Guest getGuest() {
        return guest;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
    }

    public Boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(Boolean canceled) {
        this.canceled = canceled;
    }

    @JsonGetter("propertyId")
    public Long getPropertyId() {
        return property.getId();
    }

    @JsonSetter("propertyId")
    public void setPropertyId(Long propertyId) {
        if (property == null) {
            this.property = new Property(propertyId);
        } else {
            property.setId(propertyId);
        }
    }

    @JsonIgnore
    public boolean hasValidBookingDates() {
        return this.checkInDate.isBefore(this.checkOutDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Booking booking = (Booking) o;
        return Objects.equals(id, booking.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Booking{" + "id=" + id + ", checkinDate=" + checkInDate + ", checkoutDate=" + checkOutDate + ", property=" + property + ", guest=" + guest + ", canceled=" + canceled + '}';
    }
}
